
package com.bj4.yhh.accountant;

public class LawPara {
    public int mLawType;

    public String mTitle;

    public LawPara(int type, String t) {
        mLawType = type;
        mTitle = t;
    }

    public String toString() {
        return "lawType: " + mLawType + ", title: " + mTitle;
    }

    public static final String generateReadableString(LawPara para) {
        return para.mTitle;
    }
}
