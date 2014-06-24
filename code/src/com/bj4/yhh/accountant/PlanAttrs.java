
package com.bj4.yhh.accountant;

public class PlanAttrs {
    public int mPlanType;

    public int mReadingOrder;

    public int mTotalProgress;

    public int mCurrentProgress;

    public PlanAttrs(int plan, int readingOrder, int tProgress, int cProgress) {
        mPlanType = plan;
        mReadingOrder = readingOrder;
        mTotalProgress = tProgress;
        mCurrentProgress = cProgress;
    }
}
