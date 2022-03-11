package upisdk.models;

import java.io.Serializable;

public class CPayUPIInquireResult implements Serializable
{
    public String mId;
    public String mType;
    public int mAmount;
    public int mCaptureAmount;
    public int mRefundAmount;
    public long mTime;
    public String mCancelTime;
    public long mCaptureTime;
    public String mReference;
    public String mStatus;
    public String mCurrency;
    public String mNote;
    public String mCountry;
}
