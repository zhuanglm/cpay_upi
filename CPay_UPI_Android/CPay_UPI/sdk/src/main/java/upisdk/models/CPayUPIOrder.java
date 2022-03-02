package upisdk.models;

import android.text.TextUtils;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import upisdk.CPayLaunchType;
import upisdk.networking.CPayEnv;

/**
 * Created by alexandrudiaconu on 7/22/17.
 */

@SuppressWarnings("unused")
public class CPayUPIOrder {
    public CPayLaunchType mLaunchType = CPayLaunchType.OTHERS;

    public String mReferenceId;
    private String mAmount;
    private String mCurrency;
    private String mVendor;
    private String mSubject;
    private String mBody;
    private String mIpnUrl;
    private String mCallbackUrl;
    private String mMobileCallback;
    private boolean mAllowDuplicate;
    private HashMap<String, String> mExt;

    //add for new kcp
    private String mCallbackFailUrl;
    private String mCancelUrl;
    private Locale mCountry;
    private String mNote;
    private String mSource;
    private boolean mIsAutoCapture;

    //add for CN pay acceleration
    private boolean mIsAccelerateCNPay;

    public String getVendor() {
        return mVendor;
    }

    public CPayLaunchType getLaunchType() {
        return mLaunchType;
    }

    public String getCurrency() {
        return this.mCurrency;
    }

    public boolean isAccelerateCNPay() {
        return mIsAccelerateCNPay;
    }

    public CPayUPIOrder() {

    }

    public CPayUPIOrder(String referenceId, String subject, String body, String amount, String currency, String vendor, String ipnUrl, String callbackUrl,
                        boolean allowDuplicate) {

        this(referenceId, subject, body, amount, currency, vendor, ipnUrl, callbackUrl, allowDuplicate, null);
    }

    public CPayUPIOrder(String referenceId, String subject, String body, String amount, String currency, String vendor, String ipnUrl, String callbackUrl,
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

    static public class Builder {
        private CPayLaunchType launchType;
        private String referenceId;
        private String amount;
        private String currency;
        private String vendor;
        private String subject;
        private String body;
        private String ipnUrl;
        private String callbackUrl;
        private String mobileCallback;
        private boolean allowDuplicate;
        private HashMap<String, String> ext;

        private String callbackFailUrl;
        private String cancelUrl;
        private Locale country;
        private String note;
        private String source;
        private boolean isAutoCapture;

        private boolean isAccelarateCNPay;

        public Builder enableCNPayAcceleration(boolean flag) {
            this.isAccelarateCNPay = flag;
            return this;
        }

        public Builder setReferenceId(String referenceId) {
            this.referenceId = referenceId;
            return this;
        }

        public Builder setAmount(String amount) {
            this.amount = amount;
            return this;
        }

        public Builder setCurrency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder setVendor(String vendor) {
            this.vendor = vendor;
            return this;
        }

        public Builder setSubject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder setBody(String body) {
            this.body = body;
            return this;
        }

        public Builder setIpnUrl(String ipnUrl) {
            this.ipnUrl = ipnUrl;
            return this;
        }

        public Builder setCallbackUrl(String callbackUrl) {
            this.callbackUrl = callbackUrl;
            return this;
        }

        public Builder setMobileCallback(String callbackUrl) {
            this.mobileCallback = callbackUrl;
            return this;
        }

        public Builder setAllowDuplicate(boolean allowDuplicate) {
            this.allowDuplicate = allowDuplicate;
            return this;
        }

        public Builder setExt(HashMap<String, String> ext) {
            this.ext = ext;
            return this;
        }

        public Builder setCallbackFailUrl(String url) {
            this.callbackFailUrl = url;
            return this;
        }

        public Builder setCallbackCancelUrl(String url) {
            this.cancelUrl = url;
            return this;
        }

        public Builder setCountry(Locale country) {
            this.country = country;
            return this;
        }

        public Builder setNote(String note) {
            this.note = note;
            return this;
        }

        public Builder setSource(String source) {
            this.source = source;
            return this;
        }

        public Builder setAutoCapture(Boolean autoCapture) {
            isAutoCapture = autoCapture;
            return this;
        }

        public Builder setLaunchType(CPayLaunchType type) {
            launchType = type;
            return this;
        }

        public CPayUPIOrder build() {
            CPayUPIOrder order = new CPayUPIOrder();
            order.mLaunchType = this.launchType;
            order.mReferenceId = this.referenceId;
            order.mAmount = this.amount;
            order.mCurrency = this.currency;
            order.mVendor = this.vendor;
            order.mSubject = this.subject;
            order.mBody = this.body;
            order.mIpnUrl = this.ipnUrl;
            order.mCallbackUrl = this.callbackUrl;
            order.mMobileCallback = this.mobileCallback;
            order.mAllowDuplicate = this.allowDuplicate;
            order.mExt = this.ext;

            order.mCallbackFailUrl = this.callbackFailUrl;
            order.mCancelUrl = this.cancelUrl;
            order.mCountry = this.country;
            order.mNote = this.note;
            order.mSource = this.source;
            order.mIsAutoCapture = this.isAutoCapture;

            order.mIsAccelerateCNPay = this.isAccelarateCNPay;

            return order;
        }
    }

    public JSONObject toPayload() throws JSONException {
        final JSONObject jsonBody;
        RequestTransaction transaction = new RequestTransaction(mReferenceId, Integer.parseInt(mAmount), mCurrency,
                mCountry.getCountry(), mIsAutoCapture);
        RequestPayment payment = new RequestPayment(mVendor, "non-authenticated",
                false, "", 60000, new ArrayList<String>(){{add("mobile_native");}});
        RequestURLs urls = new RequestURLs(mIpnUrl, mMobileCallback, mCallbackUrl, mCancelUrl, mCallbackFailUrl);

        jsonBody = new JSONObject();
        jsonBody.put("transaction", new JSONObject(new Gson().toJson(transaction)));
        jsonBody.put("payment", new JSONObject(new Gson().toJson(payment)));
        jsonBody.put("urls", new JSONObject(new Gson().toJson(urls)));

        return jsonBody;
    }

}
