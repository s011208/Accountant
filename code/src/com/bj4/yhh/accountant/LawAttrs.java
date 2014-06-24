
package com.bj4.yhh.accountant;

public class LawAttrs {
    public String mPart;

    public String mChapter;

    public String mSection;

    public String mLine;

    public String mSubSection;

    public String mContent;

    public LawAttrs(String part, String chapter, String sec, String subsection, String line,
            String content) {
        mPart = part;
        mChapter = chapter;
        mSection = sec;
        mSubSection = subsection;
        mLine = line;
        mContent = content;
    }

    public String toString() {
        return "mPart: " + mPart + ", mChapter: " + mChapter + ", mSection: " + mSection
                + ", mSubSection: " + mSubSection + ", mLine: " + mLine + ", mContent: " + mContent;
    }
}
