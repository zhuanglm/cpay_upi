package sdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.alipay.sdk.app.PayTask;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.util.HashMap;
import java.util.Map;

import sdk.interfaces.InquireResponse;
import sdk.interfaces.OrderResponse;
import sdk.models.CPayInquireResult;
import sdk.models.CPayOrder;
import sdk.models.CPayOrderResult;
import sdk.models.WXPayorder;
import sdk.networking.APIManager;
import sdk.networking.CPayEnv;

/**
 * Created by alexandrudiaconu on 7/20/17.
 */

public class CPaySDK {
    private static CPaySDK sInstance;
    private final IWXAPI api;
    private APIManager mApiManager;
    private OrderResponse<CPayOrderResult> mOrderListener;
    private InquireResponse<CPayInquireResult> mInquireListener;
    public String mToken;
    private Activity mActivity;
    private CPayOrderResult mOrderResult;
    public String mWXAppId;
    private CPayMode env = CPayMode.PROD;
    private boolean allowQuery = false;

    private CPaySDK(Context context) {
        mApiManager = APIManager.getInstance(context);
        api = WXAPIFactory.createWXAPI(context, mWXAppId);
    }


    public static void setWXAppId(String wxAppId) {
        sInstance.mWXAppId = wxAppId;
    }

    public static String getWXAppId() {
        if(sInstance ==  null){
            return null;
        }
        return sInstance.mWXAppId;
    }

    public static void setMode(CPayMode env) {
        sInstance.env = env;
    }

    public static CPayMode getMode() {
        return sInstance == null ? CPayMode.PROD : sInstance.env;
    }


    public static synchronized CPaySDK getInstance(Activity activity, String token) {
        if (sInstance == null)
            sInstance = new CPaySDK(activity);

        sInstance.mActivity = activity;
        sInstance.mToken = token;
        return sInstance;
    }


    public static synchronized CPaySDK getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException(CPaySDK.class.getSimpleName() +
                    " is not initialized, call getInstance(...) first in the main Activity class");
        }
        return sInstance;
    }

    public void requestOrder(CPayOrder order, final OrderResponse<CPayOrderResult> listener) {
        mOrderListener = listener;
        mApiManager.requestOrder(order);
    }

    public void inquireOrder(CPayOrderResult orderResult, final InquireResponse<CPayInquireResult> listener) {
        mInquireListener = listener;
        mApiManager.inquireOrder(orderResult);
    }


    @SuppressWarnings("deprecation")
    public void gotOrder(CPayOrderResult orderResult) {
        if (orderResult != null) {
            mOrderResult = orderResult;
            payByAlipay();
        } else {
            mOrderListener.gotOrderResult(null);
        }
    }

    public void inquiredOrder(CPayInquireResult inquireResult) {
        mInquireListener.gotInquireResult(inquireResult);
    }

    public void onResume() {
        // check pay
        if (mOrderListener != null && mOrderResult != null && mOrderResult.mRedirectUrl != null && allowQuery) {
            inquireOrderInternally();
            mOrderListener.gotOrderResult(mOrderResult);
            mOrderListener = null;
            allowQuery = false;
        }
    }

    private String getAlipayOrderInfo() {
        if (mOrderResult.mCurrency.equals("CNY")) {
            return mOrderResult.mOrderSpec + "&sign=" + mOrderResult.mSignedString;
        } else {
            return mOrderResult.mOrderSpec + "&sign=\"" + mOrderResult.mSignedString + "\"&sign_type=\"RSA\"";
        }
    }

    private void alipayCNY(String orderInfo) {
        PayTask alipay = new PayTask(mActivity);
        Map<String, String> result = alipay.payV2(orderInfo, true);

        Message msg = new Message();
        msg.obj = result;
        mHandler.sendMessage(msg);
    }

    private void alipayUSD(String orderInfo) {
        PayTask alipay = new PayTask(mActivity);
        String result = alipay.pay(orderInfo, true);

        Message msg = new Message();
        msg.obj = result;
        mHandler.sendMessage(msg);
    }

    private void payByAlipay() {
        final String orderInfo = getAlipayOrderInfo();

        Runnable payRunnable = new Runnable() {
            @Override
            public void run() {
                if (mOrderResult.mCurrency.equals("CNY")) {
                    alipayCNY(orderInfo);
                } else {
                    alipayUSD(orderInfo);
                }
            }
        };
        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }

    private void handleAlipayCNYResult(Message msg) {
        try {
            HashMap<String, String> result = (HashMap<String, String>) msg.obj;
            if (mOrderResult != null) {
                mOrderResult.mStatus = result.get("resultStatus");
                mOrderResult.mMessage = result.get("memo");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        inquireOrderInternally();
        if (mOrderListener != null) {
            mOrderListener.gotOrderResult(mOrderResult);
        }
    }

    private void handleAlipayUSDResult(Message msg) {
        try {
            String result = (String) msg.obj;
            String[] kvp = result.split(";");
            for (String kv : kvp) {
                String[] entry = kv.split("=");
                String key = entry[0];
                if (key.equals("resultStatus")) {
                    String value = entry[1].replace("{", "").replace("}", "");
                    if (mOrderResult != null) {
                        mOrderResult.mStatus = value;
                    }
                } else if (key.equals("memo")) {
                    String value = entry[1].replace("{", "").replace("}", "");
                    if (mOrderResult != null) {
                        mOrderResult.mMessage = value;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        inquireOrderInternally();
        if(mOrderListener != null){
            mOrderListener.gotOrderResult(mOrderResult);
        }
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            allowQuery = true;
            if (msg.obj instanceof String) {
                handleAlipayUSDResult(msg);
            } else if (msg.obj instanceof HashMap) {
                handleAlipayCNYResult(msg);
            }
            return true;
        }
    });


    private void inquireOrderInternally() {

        CPaySDK.getInstance().inquireOrder(mOrderResult, new InquireResponse<CPayInquireResult>() {
            @Override
            public void gotInquireResult(CPayInquireResult response) {
                if (response != null) {
                    String emerging = "";
                    emerging += "CHECK RESULT:\n\n";
                    if (response.mId != null) {
                        emerging += "ORDER ID: " + response.mId + "\n";
                    }
                    if (response.mType != null) {
                        emerging += "TYPE: " + response.mType + "\n";
                    }
                    if (response.mAmount != null) {
                        emerging += "AMOUNT: " + response.mAmount + "\n";
                    }
                    if (response.mTime != null) {
                        emerging += "TIME: " + response.mTime + "\n";
                    }
                    if (response.mReference != null) {
                        emerging += "REFERENCE: " + response.mReference + "\n";
                    }
                    if (response.mStatus != null) {
                        emerging += "STATUS: " + response.mStatus + "\n";
                    }
                    if (response.mCurrency != null) {
                        emerging += "CURRENCY: " + response.mCurrency + "\n";
                    }
                    if (response.mNote != null) {
                        emerging += "NOTE: " + response.mNote + "\n";
                    }

                    Log.e("CPay", "inquiredOrder: " + emerging);
                }

                Intent intent = new Intent();
                intent.setAction("CPAY_INQUIRE_ORDER");
                intent.putExtra("inquire_result", response);
                mActivity.sendBroadcast(intent);
            }
        });
    }

    public void gotWX(WXPayorder result, String mCurrency) {
        if (result != null) {
            mOrderResult = new CPayOrderResult();
            mOrderResult.mCurrency = mCurrency;
            mOrderResult.mOrder = new CPayOrder();
            mOrderResult.mOrderId = result.extData;
            mOrderResult.mOrder.setmVendor("wechatpay");
            mOrderResult.mOrder.setmCurrency(mCurrency);

            PayReq req = new PayReq();
            req.appId = result.appid;
            req.partnerId = result.partnerid;
            req.prepayId = result.prepayid;
            req.nonceStr = result.noncestr;
            req.timeStamp = result.timestamp;
            req.packageValue = result.mPackage;
            req.sign = result.sign;
            req.extData = result.extData;

            Log.d("jim", "check args " + req.checkArgs());
            Log.d("jim", "send return :" + api.sendReq(req));
        } else {
            mOrderListener.gotOrderResult(null);
        }
    }

    public void onWXPaySuccess(String orderId) {
        if (orderId.equals(mOrderResult.mOrderId)) {
            inquireOrderInternally();
            if(mOrderListener != null){
                mOrderResult.mStatus = "success";
                mOrderListener.gotOrderResult(mOrderResult);
            }
        }
    }

    public void onWXPayFailed(String orderId, int respCode, String errMsg) {
        if (orderId.equals(mOrderResult.mOrderId)) {
            inquireOrderInternally();
            if(mOrderListener != null){
                mOrderResult.mStatus = Integer.toString(respCode);
                mOrderResult.mMessage = errMsg;
                mOrderListener.gotOrderResult(mOrderResult);
            }
        }
    }


    public static String getBaseURL(String currency) {
        CPayMode env = CPaySDK.getMode();
        if (env.equals(CPayMode.UAT)) {
            return currency.equals(CPayEnv.CNY) ? CPayEnv.URL_RMB_UAT : CPayEnv.URL_USD_UAT;
        } else if (env.equals(CPayMode.DEV)) {
            return currency.equals(CPayEnv.CNY) ? CPayEnv.URL_RMB_DEV : CPayEnv.URL_USD_DEV;
        } else {
            // prod
            return currency.equals(CPayEnv.CNY) ? CPayEnv.URL_RMB_PROD : CPayEnv.URL_USD_PROD;
        }
    }
}
