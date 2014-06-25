
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

    public static final int PARSE_TYPE_TAX_COLLECTION = 2;

    public static final int PARSE_TYPE_VALUE_BUSINESS_LAW = 3;

    public static final int PARSE_TYPE_ESTATE_GIFT_TAX = 4;

    public static final int PARSE_TYPE_BUSINESS_ENTITY_ACCOUNTING = 5;

    public static final int PARSE_TYPE_SECURITY_EXCHANGE = 6;

    public static final String SEP_1 = "rrr";

    public static final String SEP_2 = "bbb";

    public static final int BEHAVIOR_INSERT = 1;

    public static final int BEHAVIOR_UPDATE = 2;

    private int mParseBehaviou = BEHAVIOR_INSERT;

    private final ArrayList<LawAttrs> mData = new ArrayList<LawAttrs>();

    private Context mContext;

    private int mParseType;

    public GovLawParser(Context c, int type, int behaviour) {
        mContext = c;
        mParseType = type;
        mParseBehaviou = behaviour;
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
            case PARSE_TYPE_TAX_COLLECTION:
                rtn = "http://law.moj.gov.tw/LawClass/LawAll.aspx?PCode=G0340001";
                break;
            case PARSE_TYPE_VALUE_BUSINESS_LAW:
                rtn = "http://law.moj.gov.tw/LawClass/LawAll.aspx?PCode=G0340080";
                break;
            case PARSE_TYPE_ESTATE_GIFT_TAX:
                rtn = "http://law.moj.gov.tw/LawClass/LawAll.aspx?PCode=G0340072";
                break;
            case PARSE_TYPE_BUSINESS_ENTITY_ACCOUNTING:
                rtn = "http://law.moj.gov.tw/LawClass/LawAll.aspx?PCode=J0080009";
                break;
            case PARSE_TYPE_SECURITY_EXCHANGE:
                rtn = "http://law.moj.gov.tw/LawClass/LawAll.aspx?PCode=G0400001";
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
            if (mParseBehaviou == BEHAVIOR_INSERT) {
                AccountantApplication.getDatabaseHelper(mContext).createLawTable(mData, mParseType);
                Log.d(TAG, "createLawTable DONE, type: " + mParseType);
            } else if (mParseBehaviou == BEHAVIOR_UPDATE) {
                AccountantApplication.getDatabaseHelper(mContext).updateLawTable(mData, mParseType);
                Log.d(TAG, "updateLawTable DONE, type: " + mParseType);
            }
        } else {
            Log.w(TAG, "data set is empty, type: " + mParseType);
        }
    }

    public static final int getTypeTextResource(int type) {
        if (type == GovLawParser.PARSE_TYPE_COMPANY) {
            return R.string.company_law;
        } else if (type == GovLawParser.PARSE_TYPE_LAND) {
            return R.string.land_law;
        } else if (type == GovLawParser.PARSE_TYPE_TAX_COLLECTION) {
            return R.string.tax_collection_law;
        } else if (type == GovLawParser.PARSE_TYPE_VALUE_BUSINESS_LAW) {
            return R.string.value_business_law;
        } else if (type == GovLawParser.PARSE_TYPE_ESTATE_GIFT_TAX) {
            return R.string.estate_gift_tax;
        } else if (type == GovLawParser.PARSE_TYPE_BUSINESS_ENTITY_ACCOUNTING) {
            return R.string.business_entity_accounting;
        } else if (type == GovLawParser.PARSE_TYPE_SECURITY_EXCHANGE) {
            return R.string.security_exchange_accounting;
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
            return GovLawParser.PARSE_TYPE_TAX_COLLECTION;
        } else if (txt.equals(context.getString(R.string.value_business_law))) {
            return GovLawParser.PARSE_TYPE_VALUE_BUSINESS_LAW;
        } else if (txt.equals(context.getString(R.string.estate_gift_tax))) {
            return GovLawParser.PARSE_TYPE_ESTATE_GIFT_TAX;
        } else if (txt.equals(context.getString(R.string.business_entity_accounting))) {
            return GovLawParser.PARSE_TYPE_BUSINESS_ENTITY_ACCOUNTING;
        } else if (txt.equals(context.getString(R.string.security_exchange_accounting))) {
            return GovLawParser.PARSE_TYPE_SECURITY_EXCHANGE;
        } else {
            return GovLawParser.PARSE_TYPE_COMPANY;
        }
    }

    public static final String getTypeText(Context context, int type) {
        if (type == GovLawParser.PARSE_TYPE_COMPANY) {
            return context.getString(R.string.company_law);
        } else if (type == GovLawParser.PARSE_TYPE_LAND) {
            return context.getString(R.string.land_law);
        } else if (type == GovLawParser.PARSE_TYPE_TAX_COLLECTION) {
            return context.getString(R.string.tax_collection_law);
        } else if (type == GovLawParser.PARSE_TYPE_VALUE_BUSINESS_LAW) {
            return context.getString(R.string.value_business_law);
        } else if (type == GovLawParser.PARSE_TYPE_ESTATE_GIFT_TAX) {
            return context.getString(R.string.estate_gift_tax);
        } else if (type == GovLawParser.PARSE_TYPE_BUSINESS_ENTITY_ACCOUNTING) {
            return context.getString(R.string.business_entity_accounting);
        } else if (type == GovLawParser.PARSE_TYPE_SECURITY_EXCHANGE) {
            return context.getString(R.string.security_exchange_accounting);
        } else {
            return "";
        }
    }
}
