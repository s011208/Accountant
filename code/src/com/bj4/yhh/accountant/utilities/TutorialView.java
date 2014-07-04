
package com.bj4.yhh.accountant.utilities;

import com.bj4.yhh.accountant.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Handler;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class TutorialView extends RelativeLayout {
    public interface Callback {
        public void onDismiss();

        public void onDrawBackgroundDone();

        public void onFailed();
    }

    private Callback mCallback;

    private Context mContext;

    private TextView mShowText;

    private Handler mHandler = new Handler();

    public static final int POSITION_TOP = 0;

    public static final int POSITION_CENTER = 1;

    public static final int POSITION_BOTTOM = 2;

    public TutorialView(Context context) {
        this(context, null);
    }

    public TutorialView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TutorialView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        setVisibility(View.GONE);
        if (mCallback != null) {
            mCallback.onDismiss();
        }
        return true;
    }

    public void setCallback(Callback cb) {
        mCallback = cb;
    }

    public void setText(String text, int position) {
        if (mShowText == null) {
            mShowText = new TextView(mContext);
            mShowText.setBackgroundColor(0x66000000);
            mShowText.setTextColor(Color.WHITE);
            mShowText.setGravity(Gravity.CENTER);
            int padding = (int)mContext.getResources().getDimension(R.dimen.tutorial_text_padding);
            mShowText.setPadding(padding, padding, padding, padding);
            mShowText.setTextSize(mContext.getResources().getDimension(R.dimen.tutorial_text_size));
        }
        mShowText.setText(text);
        RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        switch (position) {
            case POSITION_TOP:
                rl.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                rl.addRule(RelativeLayout.CENTER_HORIZONTAL);
                break;
            case POSITION_CENTER:
                rl.addRule(RelativeLayout.CENTER_IN_PARENT);
                break;
            case POSITION_BOTTOM:
                rl.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                rl.addRule(RelativeLayout.CENTER_HORIZONTAL);
                break;
        }
        addView(mShowText, rl);
    }

    public void setMainBackground(final View backgroundView, final View targetView) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (backgroundView != null) {
                    try {
                        Bitmap bitmap = Bitmap.createBitmap(backgroundView.getMeasuredWidth(),
                                backgroundView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
                        Canvas c = new Canvas(bitmap);
                        backgroundView.draw(c);
                        c.setBitmap(null);
                        bitmap = fastblur(mContext, bitmap, 16);
                        c = new Canvas(bitmap);
                        Paint transparentPaint = new Paint();
                        transparentPaint.setColor(getResources().getColor(
                                android.R.color.transparent));
                        transparentPaint.setXfermode(new PorterDuffXfermode(
                                android.graphics.PorterDuff.Mode.CLEAR));
                        Rect targetViewRect = new Rect();
                        if (targetView != null) {
                            targetView.getGlobalVisibleRect(targetViewRect);
                            c.drawRect(targetViewRect, transparentPaint);
                            Paint rectPaint = new Paint();
                            rectPaint.setColor(Color.MAGENTA);
                            rectPaint.setAntiAlias(true);
                            rectPaint.setStyle(Style.STROKE);
                            rectPaint.setStrokeWidth(3);
                            c.drawRect(targetViewRect, rectPaint);
                        } else {
                            backgroundView.getGlobalVisibleRect(targetViewRect);
                        }
                        @SuppressWarnings("deprecation")
                        final Drawable drawable = new BitmapDrawable(bitmap);
                        mHandler.post(new Runnable() {
                            @SuppressWarnings("deprecation")
                            @Override
                            public void run() {
                                if (mCallback != null) {
                                    try {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                            setBackground(drawable);
                                        } else {
                                            setBackgroundDrawable(drawable);
                                        }
                                        mCallback.onDrawBackgroundDone();
                                    } catch (Exception e) {
                                        if (mCallback != null) {
                                            mCallback.onFailed();
                                        }
                                    }
                                }
                            }
                        });
                    } catch (Exception e) {
                        // failed to show, clear mess
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (mCallback != null) {
                                    mCallback.onFailed();
                                }
                            }
                        });
                    }
                }
            }
        }).start();
    }

    public static Bitmap fastblur(Context context, Bitmap sentBitmap, int radius) {

        if (VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

            final RenderScript rs = RenderScript.create(context);
            final Allocation input = Allocation.createFromBitmap(rs, sentBitmap,
                    Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
            final Allocation output = Allocation.createTyped(rs, input.getType());
            final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
            script.setRadius(radius /* e.g. 3.f */);
            script.setInput(input);
            script.forEach(output);
            output.copyTo(bitmap);
            return bitmap;
        }

        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        bitmap.setPixels(pix, 0, w, 0, 0, w, h);
        return (bitmap);
    }

}
