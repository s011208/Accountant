
package com.bj4.yhh.accountant;

public class LawAttrs {
    public String mChapter;

    public String mSection;

    public String mLine;

    public String mMu;

    public String mContent;

    public LawAttrs(String chapter, String sec, String mu, String line, String content) {
        mChapter = chapter;
        mSection = sec;
        mMu = mu;
        mLine = line;
        mContent = content;
    }

    public String toString() {
        return "mChapter: " + mChapter + ", mSection: " + mSection + ", mMu: " + mMu + ", mLine: "
                + mLine + ", mContent: " + mContent;
    }
}
