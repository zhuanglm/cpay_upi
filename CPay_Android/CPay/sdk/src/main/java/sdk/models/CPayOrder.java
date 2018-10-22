package sdk.models;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

import sdk.networking.CPayEnv;

/**
 * Created by alexandrudiaconu on 7/22/17.
 */

public class CPayOrder
{
    public String mReferenceId;
    private String mAmount;
    private String mCurrency;
    private String mVendor;
    private String mSubject;
    private String mBody;
    private String mIpnUrl;
    private String mCallbackUrl;
    private boolean mAllowDuplicate;

    public String getmVendor() {
        return mVendor;
    }
    public void setmVendor(String mVendor){
        this.mVendor = mVendor;
    }

    public void setmCurrency(String currency){
        this.mCurrency = currency;
    }

    public String getmCurrency(){
        return this.mCurrency;
}

    public CPayOrder(){

    }

    public CPayOrder(String referenceId, String subject, String body, String amount, String currency, String vendor, String ipnUrl, String callbackUrl,
                     boolean allowDuplicate)
    {
        mReferenceId = referenceId;
        mAmount = amount;
        mCurrency = TextUtils.isEmpty(currency) || (!currency.equals(CPayEnv.USD) && !currency.equals(CPayEnv.CNY))? CPayEnv.USD: currency;
        mVendor = vendor;
        mSubject = subject;
        mBody = body;
        mIpnUrl = ipnUrl;
        mCallbackUrl = callbackUrl;
        mAllowDuplicate = allowDuplicate;
    }

    public boolean isValid()
    {
        if (mReferenceId == null ||
                mAmount == null ||
                mCurrency == null ||
                mVendor == null ||
                mIpnUrl == null ||
                mCallbackUrl == null)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    public Map<String, String> toPayload()
    {
        Map<String, String> returned = new HashMap<>();
        returned.put("reference", mReferenceId);
        returned.put("subject", mSubject);
        returned.put("body", mBody);
        returned.put("amount", mAmount);
        returned.put("currency", mCurrency);
        returned.put("ipn_url", mIpnUrl);
        returned.put("callback_url", mCallbackUrl);
        returned.put("allow_duplicates", mAllowDuplicate ? "yes" : "no");
        returned.put("vendor", mVendor);
        return returned;
    }
}
