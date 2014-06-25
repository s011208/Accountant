
package com.bj4.yhh.accountant;

public class LawAttrs {
    public String mPart;

    public String mChapter;

    public String mSection;

    public String mLine;

    public String mSubSection;

    public String mContent;

    public int mWrongTime = 0;

    public static final int HAS_ANSWERED = 0;

    public static final int HAS_NOT_ANSWERED = 1;

    public int mHasAnswered = HAS_NOT_ANSWERED;

    public LawAttrs(String part, String chapter, String sec, String subsection, String line,
            String content) {
        mPart = part;
        mChapter = chapter;
        mSection = sec;
        mSubSection = subsection;
        mLine = line;
        mContent = content;
    }

    public LawAttrs(String part, String chapter, String sec, String subsection, String line,
            String content, int wrongTime, int answered) {
        mPart = part;
        mChapter = chapter;
        mSection = sec;
        mSubSection = subsection;
        mLine = line;
        mContent = content;
        mWrongTime = wrongTime;
        mHasAnswered = answered;
    }

    public String toString() {
        return "mPart: " + mPart + ", mChapter: " + mChapter + ", mSection: " + mSection
                + ", mSubSection: " + mSubSection + ", mLine: " + mLine + ", mContent: " + mContent
                + ", mWrongTime: " + mWrongTime + ", mHasAnswered: " + mHasAnswered;
    }
}
