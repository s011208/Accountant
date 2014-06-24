
package com.bj4.yhh.accountant.parser;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import com.bj4.yhh.accountant.AccountantApplication;
import com.bj4.yhh.accountant.LawAttrs;
import com.bj4.yhh.accountant.R;

import android.content.Context;
import android.util.Log;

public class GovLawParser implements Runnable {
    private static final String TAG = "QQQQ";

    private static final boolean DEBUG = false;

    private static final String PART_UNIT = "½s";

    private static final String CHAPTER_UNIT = "³¹";

    private static final String SECTION_UNIT = "¸`";

    private static final String SUBSECTION_UNIT = "¥Ø";

    private static final String IGNORE_UNIT = "§R°£";

    public static final int PARSE_TYPE_COMPANY = 0;

    public static final int PARSE_TYPE_LAND = 1;

    public static final int PARSE_TYPE_TEXT_COLLECTION = 2;

    public static final String SEP_1 = "rrr";

    public static final String SEP_2 = "bbb";

    private final ArrayList<LawAttrs> mData = new ArrayList<LawAttrs>();

    private Context mContext;

    private int mParseType;

    public GovLawParser(Context c, int type) {
        mContext = c;
        mParseType = type;
    }

    private static final String getParseUrl(int type) {
        String rtn = null;
        switch (type) {
            case PARSE_TYPE_COMPANY:
                rtn = "http://law.moj.gov.tw/LawClass/LawAll.aspx?PCode=J0080001";
                break;
            case PARSE_TYPE_LAND:
                rtn = "http://law.moj.gov.tw/LawClass/LawAll.aspx?PCode=G0340096";
                break;
            case PARSE_TYPE_TEXT_COLLECTION:
                rtn = "http://law.moj.gov.tw/LawClass/LawAll.aspx?PCode=G0340001";
                break;
        }
        return rtn;
    }

    @Override
    public void run() {
        String url = getParseUrl(mParseType);
        try {
            Document doc = Jsoup.connect(url).get();
            int currentChapter = 0;
            int currentPart = 0;
            int currentSection = 0;
            int currentSubsection = 0;
            String line = "";
            Elements td = doc.select("tr").select("td");
            for (Element data : td) {
                String content = "";
                if ("3".equals(data.attr("colspan"))) {
                    String txt = data.text();
                    if (txt.contains(IGNORE_UNIT)) {
                        continue;
                    } else if (txt.contains(PART_UNIT)) {
                        ++currentPart;
                        currentSection = 0;
                        currentSubsection = 0;
                        currentChapter = 0;
                    } else if (txt.contains(SECTION_UNIT)) {
                        ++currentSection;
                        currentSubsection = 0;
                    } else if (txt.contains(CHAPTER_UNIT)) {
                        ++currentChapter;
                        currentSection = 0;
                        currentSubsection = 0;
                    } else if (txt.contains(SUBSECTION_UNIT)) {
                        ++currentSubsection;
                    }
                    if (DEBUG)
                        Log.d(TAG, txt);
                } else {
                    if (currentChapter == 0)
                        continue;
                    String tempLine = data.select("a[href]").text();
                    if (tempLine != null && "".equals(tempLine) == false) {
                        line = tempLine;
                    }
                    content = preProcessDataContent(data.select("pre").text());
                    if ("".equals(line) == false && content != null && "".equals(content) == false) {
                        mData.add(new LawAttrs(String.valueOf(currentPart), String
                                .valueOf(currentChapter), String.valueOf(currentSection), String
                                .valueOf(currentSubsection), line, content));
                    }
                }
            }
        } catch (IOException e) {
            Log.w(TAG, "failed", e);
        } finally {
            refreshTable();
        }
    }

    private static String preProcessDataContent(String content) {
        content = content.replaceAll("¡C\n|¡C\r", SEP_1).replaceAll("¡G\n|¡G\r", SEP_2);
        content = content.replace("\n", "").replace("\r", "").replaceAll(" ", "");
        content = content.replace(SEP_1, "¡C\n").replace(SEP_2, "¡G\n");
        return content;
    }

    private void refreshTable() {
        if (mData.isEmpty() == false) {
            AccountantApplication.getDatabaseHelper(mContext).refreshLaw(mData, mParseType);
            Log.d(TAG, "refreshLaw DONE, type: " + mParseType);
        } else {
            Log.w(TAG, "data set is empty, type: " + mParseType);
        }
    }

    public static final int getTypeTextResource(int type) {
        if (type == GovLawParser.PARSE_TYPE_COMPANY) {
            return R.string.company_law;
        } else if (type == GovLawParser.PARSE_TYPE_LAND) {
            return R.string.land_law;
        } else if (type == GovLawParser.PARSE_TYPE_TEXT_COLLECTION) {
            return R.string.tax_collection_law;
        } else {
            return 0;
        }
    }

    public static final int getTextType(Context context, String txt) {
        if (txt.equals(context.getString(R.string.company_law))) {
            return GovLawParser.PARSE_TYPE_COMPANY;
        } else if (txt.equals(context.getString(R.string.land_law))) {
            return GovLawParser.PARSE_TYPE_LAND;
        } else if (txt.equals(context.getString(R.string.tax_collection_law))) {
            return GovLawParser.PARSE_TYPE_TEXT_COLLECTION;
        } else {
            return GovLawParser.PARSE_TYPE_COMPANY;
        }
    }

    public static final String getTypeText(Context context, int type) {
        if (type == GovLawParser.PARSE_TYPE_COMPANY) {
            return context.getString(R.string.company_law);
        } else if (type == GovLawParser.PARSE_TYPE_LAND) {
            return context.getString(R.string.land_law);
        } else if (type == GovLawParser.PARSE_TYPE_TEXT_COLLECTION) {
            return context.getString(R.string.tax_collection_law);
        } else {
            return "";
        }
    }
}
