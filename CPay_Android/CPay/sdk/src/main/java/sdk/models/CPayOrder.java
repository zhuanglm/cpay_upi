package sdk.models;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import sdk.CPayLaunchType;
import sdk.networking.CPayEnv;

/**
 * Created by alexandrudiaconu on 7/22/17.
 */

@SuppressWarnings("unused")
public class CPayOrder {
    public CPayLaunchType mLaunchType = CPayLaunchType.OTHERS;

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

    //add for new kcp
    private String mCallbackFailUrl;
    private String mCancelUrl;
    private Locale mCountry;
    private String mNote;
    private String mSource;
    private Boolean mIsAutoCapture;
    private Consumer mConsumer;
    private Goods mGoods;

    public String getVendor() {
        return mVendor;
    }

    public CPayLaunchType getLaunchType() {
        return mLaunchType;
    }

    public String getCurrency() {
        return this.mCurrency;
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
        private boolean allowDuplicate;
        private HashMap<String, String> ext;

        private String callbackFailUrl;
        private String cancelUrl;
        private Locale country;
        private String note;
        private String source;
        private Boolean isAutoCapture;
        private Goods goods;
        private Consumer consumer;

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

        public Builder setConsumer(String first_name, String last_name, String phone,
                                   String email, String reference) {
            consumer = new Consumer(first_name,last_name,phone,email,reference);
            return this;
        }

        public Builder setGoods(String name, int taxable_amount, int tax_exempt_amount,
                                int total_tax_amount) {
            goods = new Goods(name,taxable_amount,tax_exempt_amount,total_tax_amount);
            return this;
        }

        public Builder setLaunchType(CPayLaunchType type) {
            launchType = type;
            return this;
        }

        public CPayOrder build() {
            CPayOrder order = new CPayOrder();
            order.mLaunchType = this.launchType;
            order.mReferenceId = this.referenceId;
            order.mAmount = this.amount;
            order.mCurrency = this.currency;
            order.mVendor = this.vendor;
            order.mSubject = this.subject;
            order.mBody = this.body;
            order.mIpnUrl = this.ipnUrl;
            order.mCallbackUrl = this.callbackUrl;
            order.mAllowDuplicate = this.allowDuplicate;
            order.mExt = this.ext;

            order.mCallbackFailUrl = this.callbackFailUrl;
            order.mCancelUrl = this.cancelUrl;
            order.mCountry = this.country;
            order.mNote = this.note;
            order.mSource = this.source;
            order.mIsAutoCapture = this.isAutoCapture;

            order.mGoods = this.goods;
            order.mConsumer = this.consumer;

            return order;
        }
    }

    public Map<String, String> toPayload() {
        Map<String, String> returned = new HashMap<>();
        returned.put("reference", mReferenceId);

        if(!mVendor.equals("card")) {
            returned.put("body", mBody);
            returned.put("subject", mSubject);
            returned.put("allow_duplicates", mAllowDuplicate ? "yes" : "no");
        }

        if (mVendor.equals("alipay") || mVendor.equals("wechatpay") || mVendor.equals("upop")
                || mVendor.equals("cc") || mVendor.equals("card")) {
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
        returned.put("vendor", mVendor);

        if(mExt != null && !mExt.isEmpty()){
            returned.put("ext", new JSONObject(mExt).toString());
        }

        // new properties for KCP
        if(mVendor.equals("card")){
            returned.put("source", mSource);
            returned.put("auto_capture", mIsAutoCapture ? "true" : "false");
            returned.put("country", mCountry.getCountry());
            returned.put("note", mNote);
            returned.put("callback_fail", mCallbackFailUrl);
            returned.put("cancel_url", mCancelUrl);

            returned.put("consumer[first_name]", mConsumer.getFirstName());
            returned.put("consumer[last_name]", mConsumer.getLastName());
            returned.put("consumer[phone]", mConsumer.getPhone());
            returned.put("consumer[email]", mConsumer.getEmail());
            returned.put("consumer[reference]", mConsumer.getReference());

            @SuppressLint("DefaultLocale")
            String goods = String.format("{\"data\":[{\"name\":\"%s\",\"taxable_amount\":%d,\"tax_exempt_amount\":%d,\"total_tax_amount\":%d}]}",
                    mGoods.getName(),mGoods.getTaxable_amount(),mGoods.getTax_exempt_amount(),mGoods.getTotal_tax_amount());
            returned.put("goods", goods);
        }

        return returned;
    }


}
