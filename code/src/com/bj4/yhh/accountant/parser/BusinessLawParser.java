
package com.bj4.yhh.accountant.parser;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.bj4.yhh.accountant.AccountantApplication;
import com.bj4.yhh.accountant.LawAttrs;

import android.content.Context;
import android.util.Log;

public class BusinessLawParser implements Runnable {
    private static final String TAG = "QQQQ";

    private static final String CHAPTER_UNIT = "³¹";

    private static final String SECTION_UNIT = "¸`";

    private static final String MU_UNIT = "¥Ø";

    private static final String IGNORE_UNIT = "§R°£";

    private final ArrayList<LawAttrs> mData = new ArrayList<LawAttrs>();

    private Context mContext;

    public BusinessLawParser(Context c) {
        mContext = c;
    }

    @Override
    public void run() {
        String url = "http://law.moj.gov.tw/LawClass/LawAll.aspx?PCode=J0080001";
        try {
            Document doc = Jsoup.connect(url).get();
            int currentChapter = 0;
            int currentSection = 0;
            int currentMu = 0;
            String line = "";
            Elements td = doc.select("tr").select("td");
            for (Element data : td) {
                String content = "";
                if ("3".equals(data.attr("colspan"))) {
                    String txt = data.text();
                    if (txt.contains(IGNORE_UNIT)) {
                        continue;
                    } else if (txt.contains(SECTION_UNIT)) {
                        ++currentSection;
                        currentMu = 0;
                    } else if (txt.contains(CHAPTER_UNIT)) {
                        ++currentChapter;
                        currentSection = 0;
                        currentMu = 0;
                    } else if (txt.contains(MU_UNIT)) {
                        ++currentMu;
                    }
                } else {
                    if (currentChapter == 0)
                        continue;
                    String tempLine = data.select("a[href]").text();
                    if (tempLine != null && "".equals(tempLine) == false) {
                        line = tempLine;
                    }
                    content = data.select("pre").text();
                    if ("".equals(line) == false && content != null && "".equals(content) == false) {
                        mData.add(new LawAttrs(String.valueOf(currentChapter), String
                                .valueOf(currentSection), String.valueOf(currentMu), line, content));
                    }
                }
            }
        } catch (IOException e) {
            Log.w(TAG, "failed", e);
        } finally {
            if (mData.isEmpty() == false) {
                AccountantApplication.getDatabaseHelper(mContext).refreshBusinessLaw(mData);
                Log.d(TAG, "refreshBusinessLaw DONE");
            } else {
                Log.w(TAG, "Business data set is empty");
            }
        }
    }
}
