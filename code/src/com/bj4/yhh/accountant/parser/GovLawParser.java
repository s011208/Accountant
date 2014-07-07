
package com.bj4.yhh.accountant.parser;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import com.bj4.yhh.accountant.AccountantApplication;
import com.bj4.yhh.accountant.LawAttrs;
import com.bj4.yhh.accountant.LawPara;
import com.bj4.yhh.accountant.R;
import com.bj4.yhh.accountant.database.DatabaseHelper;

import android.content.Context;
import android.util.Log;

public class GovLawParser implements Runnable {
    public interface ResultCallback {
        public void parseDone(int result, Exception e);

        public void loadingProgress(int type, int progress);
    }

    private ResultCallback mCallback;

    private static final String TAG = "GovLawParser";

    private static final boolean DEBUG = false;

    public static final int RESULT_OK = 0;

    public static final int RESULT_FAIL = 1;

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

    public static final int PARSE_TYPE_INCOME_TAX = 7;

    public static final String SEP_1 = "rrr";

    public static final String SEP_2 = "bbb";

    public static final int BEHAVIOR_INSERT = 1;

    public static final int BEHAVIOR_UPDATE = 2;

    private int mParseBehaviou = BEHAVIOR_INSERT;

    private final ArrayList<LawAttrs> mData = new ArrayList<LawAttrs>();

    private final ArrayList<LawPara> mParaData = new ArrayList<LawPara>();

    private Context mContext;

    private int mLawType;

    private String mUpdateTime;

    public GovLawParser(Context c, int type, int behaviour, ResultCallback cb) {
        mContext = c;
        mLawType = type;
        mParseBehaviou = behaviour;
        mCallback = cb;
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
            case PARSE_TYPE_INCOME_TAX:
                rtn = "http://law.moj.gov.tw/LawClass/LawAll.aspx?PCode=G0340003";
                break;
        }
        return rtn;
    }

    @Override
    public void run() {
        int result = RESULT_OK;
        Exception failException = null;
        String url = getParseUrl(mLawType);
        try {
            // parse law
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
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (mCallback != null) {
                        mCallback.loadingProgress(mLawType, 65);
                    }
                }
            }).start();

            // parse update time
            String updateTimeUrl = url.replace("LawAll.aspx", "LawContent.aspx");
            doc = Jsoup.connect(updateTimeUrl).get();
            Elements date = doc.select("span[id]");
            for (Element e : date) {
                if ("LawBasicData1_lblModDate".equals(e.attr("id"))) {
                    mUpdateTime = e.text();
                }
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (mCallback != null) {
                        mCallback.loadingProgress(mLawType, 80);
                    }
                }
            }).start();
            // parse paragraph
            parseParagraph(url.replace("LawAll.aspx", "LawAllPara.aspx"));
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (mCallback != null) {
                        mCallback.loadingProgress(mLawType, 90);
                    }
                }
            }).start();
        } catch (Exception e) {
            result = RESULT_FAIL;
            failException = e;
            Log.w(TAG, "failed mLawType: " + mLawType, e);
        } finally {
            refreshTable();
            if (mCallback != null) {
                mCallback.loadingProgress(mLawType, 100);
                mCallback.parseDone(result, failException);
            }
        }
    }

    private void parseParagraph(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();
        Elements td = doc.select("pre");
        for (int i = 0; i < td.size(); i++) {
            String data = null;
            String firstData = null;
            try {
                firstData = ((TextNode)doc.select("pre").select("a").get(i).childNodes().get(0))
                        .getWholeText();
                data = firstData + td.get(i).ownText();
            } catch (Exception e) {
                data = td.get(i).text();
            }
            // double check
            if ("".equals(firstData) || firstData == null) {
                data = td.get(i).text();
            }
            mParaData.add(new LawPara(mLawType, data));
        }
    }

    private static String preProcessDataContent(String content) {
        content = content.replaceAll("¡C\n|¡C\r", SEP_1).replaceAll("¡G\n|¡G\r", SEP_2);
        content = content.replace("\n", "").replace("\r", "").replaceAll(" ", "");
        content = content.replace(SEP_1, "¡C\n").replace(SEP_2, "¡G\n");
        return content;
    }

    private void refreshTable() {
        DatabaseHelper helper = AccountantApplication.getDatabaseHelper(mContext);
        if (mData.isEmpty() == false) {
            // if (mParseBehaviou == BEHAVIOR_INSERT) {
            helper.updateLawTable(mData, mLawType);
            helper.insertLawParagraph(mParaData, mLawType);
            Log.d(TAG, "createLawTable DONE, type: " + mLawType);
        } else {
            Log.w(TAG, "data set is empty, type: " + mLawType);
        }
        helper.insertLawUpdateTime(mLawType, mUpdateTime);
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
        } else if (type == GovLawParser.PARSE_TYPE_INCOME_TAX) {
            return R.string.income_tax_law;
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
        } else if (txt.equals(context.getString(R.string.income_tax_law))) {
            return GovLawParser.PARSE_TYPE_INCOME_TAX;
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
        } else if (type == GovLawParser.PARSE_TYPE_INCOME_TAX) {
            return context.getString(R.string.income_tax_law);
        } else {
            return "";
        }
    }
}
