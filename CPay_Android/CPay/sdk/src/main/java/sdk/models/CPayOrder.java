package sdk.models;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import sdk.networking.CPayEnv;

/**
 * Created by alexandrudiaconu on 7/22/17.
 */

public class CPayOrder {
    public String mReferenceId;
    private String mAmount;
    private String mCurrency;
    private String mVendor;
    private String mSubject;
    private String mBody;
    private String mIpnUrl;
    private String mCallbackUrl;
    private boolean mAllowDuplicate;
    private HashMap<String, String> mExt;

    public String getmVendor() {
        return mVendor;
    }

    public void setmVendor(String mVendor) {
        this.mVendor = mVendor;
    }

    public void setmCurrency(String currency) {
        this.mCurrency = currency;
    }

    public String getmCurrency() {
        return this.mCurrency;
    }

    public HashMap<String, String> getmExt() {
        return mExt;
    }


    public CPayOrder() {

    }

    public CPayOrder(String referenceId, String subject, String body, String amount, String currency, String vendor, String ipnUrl, String callbackUrl,
                     boolean allowDuplicate) {

        this(referenceId, subject, body, amount, currency, vendor, ipnUrl, callbackUrl, allowDuplicate, null);
    }

    public CPayOrder(String referenceId, String subject, String body, String amount, String currency, String vendor, String ipnUrl, String callbackUrl,
                     boolean allowDuplicate, HashMap<String, String> ext) {
        mReferenceId = referenceId;
        mAmount = amount;
        mCurrency = TextUtils.isEmpty(currency) /*|| (!currency.equals(CPayEnv.USD) && !currency.equals(CPayEnv.CNY))*/ ? CPayEnv.USD : currency;
        mVendor = vendor;
        mSubject = subject;
        mBody = body;
        mIpnUrl = ipnUrl;
        mCallbackUrl = callbackUrl;
        mAllowDuplicate = allowDuplicate;
        mExt = ext;
    }

    public boolean isValid() {
        if (mReferenceId == null ||
                mAmount == null ||
                mCurrency == null ||
                mVendor == null ||
                mIpnUrl == null ||
                mCallbackUrl == null) {
            return false;
        } else {
            return true;
        }
    }

    public Map<String, String> toPayload() {
        Map<String, String> returned = new HashMap<>();
        returned.put("reference", mReferenceId);
        returned.put("subject", mSubject);
        returned.put("body", mBody);

        if (mVendor.equals("alipay") || mVendor.equals("wechatpay") || mVendor.equals("upop") || mVendor.equals("cc")) {
            //Please only use amount when the payment method is alipay, wechatpay, cc, or upop.
            //Please only use trans_amount when the payment method is jkopay, alipay_hk, kakaopay, gcash, dana, truemoney, bkash, or easypaisa.
            returned.put("amount", mAmount);
            returned.put("currency", mCurrency);
        } else {
            returned.put("trans_amount", mAmount);
            returned.put("trans_currency", mCurrency);
        }

        returned.put("ipn_url", mIpnUrl);
        returned.put("callback_url", mCallbackUrl);
        returned.put("allow_duplicates", mAllowDuplicate ? "yes" : "no");
        returned.put("vendor", mVendor);

        if(mExt != null && !mExt.isEmpty()){
            returned.put("ext", new JSONObject(mExt).toString());
        }

        return returned;
    }


}
