package upisdk;

import static upisdk.CPayMode.DEV;
import static upisdk.CPayMode.PROD;
import static upisdk.CPayMode.UAT;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.alipay.sdk.app.PayTask;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.unionpay.UPPayAssistEx;

import java.util.HashMap;
import java.util.Map;

import upisdk.interfaces.InquireResponse;
import upisdk.interfaces.OrderResponse;
import upisdk.models.CPayUPIInquireResult;
import upisdk.models.CPayUPIOrder;
import upisdk.models.CPayUPIOrderResult;
import upisdk.models.ErrorMessage;
import upisdk.models.WXPayorder;
import upisdk.networking.APIManager;

public class CPayUPISDK {
    private static final String TAG = "CPaySDK";
    private static CPayUPISDK sInstance;
    private final APIManager mApiManager;
    private OrderResponse<CPayUPIOrderResult> mOrderListener;
    private InquireResponse<CPayUPIInquireResult> mInquireListener;
    public String mToken;
    private CPayUPIOrderResult mOrderResult;
    public String mWXAppId;
    private CPayMode env = PROD;
    private boolean allowQuery = false;

    public final static MutableLiveData<CPayUPIInquireResult> mInquireResult = new MutableLiveData<>();

    private CPayUPISDK(Context context) {
        mApiManager = APIManager.getInstance(context);
    }


    public static void setWXAppId(String wxAppId) {
        sInstance.mWXAppId = wxAppId;
    }

    public static String getWXAppId() {
        if (sInstance == null) {
            return null;
        }
        return sInstance.mWXAppId;
    }

    private static void setMode(CPayMode env) {
        sInstance.env = env;
    }

    public static void setMode(String envString) {
        switch (envString){
            case "DEV":
                setMode(CPayMode.DEV);
                break;
            case "QA":
                setMode(CPayMode.QA);
                break;
            case "UAT":
                setMode(CPayMode.UAT);
                break;
            default:
                setMode(CPayMode.PROD);
                break;
        }
    }


    public static CPayMode getMode() {
        return sInstance == null ? PROD : sInstance.env;
    }


    public static synchronized void initInstance(Activity activity, String token) {
        if (sInstance == null)
            sInstance = new CPayUPISDK(activity);

        if(token != null){
            sInstance.mToken = token;
        }
    }

    public static void setToken(String token){
        if (sInstance != null){
            sInstance.mToken = token;
        }
    }

    public static synchronized CPayUPISDK getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException(CPayUPISDK.class.getSimpleName() +
                    " is not initialized, call getInstance(...) first in the main Activity class");
        }
        return sInstance;
    }

    public void requestOrder(Activity activity, CPayUPIOrder order, final OrderResponse<CPayUPIOrderResult> listener) {
        mOrderListener = listener;
        mApiManager.requestOrder(activity,order);
    }

    public void inquireOrder(CPayUPIOrderResult orderResult, final InquireResponse<CPayUPIInquireResult> listener) {
        mInquireListener = listener;
        mApiManager.inquireOrder(orderResult);
    }

    @SuppressWarnings("unused")
    public void inquireOrderByRef(String referenceId, String currency, String vendor, boolean isCNAcceleration,
                                  final InquireResponse<CPayUPIInquireResult> listener) {
        mInquireListener = listener;
        mApiManager.inquireOrderByRef(referenceId, currency, vendor, isCNAcceleration);
    }

    public void inquiredOrder(CPayUPIInquireResult inquireResult) {
        mInquireListener.gotInquireResult(inquireResult);
    }

    @SuppressWarnings("unchecked")
    private final Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            allowQuery = true;
            if (msg.obj instanceof String) {
                // USD
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
                                mOrderResult.mStatus = "9000".equals(mOrderResult.mStatus) ? "0" : mOrderResult.mStatus; // Unified the success status
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
            } else if (msg.obj instanceof HashMap) {
                // RMB
                try {
                    HashMap<String, String> result = (HashMap<String, String>) msg.obj;
                    if (mOrderResult != null) {
                        mOrderResult.mStatus = result.get("resultStatus");
                        mOrderResult.mStatus = "9000".equals(mOrderResult.mStatus) ? "0" : mOrderResult.mStatus; // Unified the success status
                        mOrderResult.mMessage = result.get("memo");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                // other
                return false;
            }

            allowQuery = false;
            inquireOrderInternally();

            if (mOrderListener != null) {
                mOrderListener.gotOrderResult(mOrderResult);
            }

            return true;
        }
    });


    private void inquireOrderInternally() {

        inquireOrder(mOrderResult, response -> {
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

            mInquireResult.setValue(response);
        });
    }

    public void gotAlipay(Activity activity, CPayUPIOrderResult orderResult) {
        mOrderResult = orderResult;

        final String orderInfo;
        if (mOrderResult.mCurrency.equals("CNY")) {
            orderInfo = mOrderResult.mOrderSpec + "&sign=" + mOrderResult.mSignedString;
        } else {
            orderInfo = mOrderResult.mOrderSpec + "&sign=\"" + mOrderResult.mSignedString + "\"&sign_type=\"RSA\"";
        }

        Runnable payRunnable = () -> {
            PayTask alipay = new PayTask(activity);

            if (mOrderResult.mCurrency.equals("CNY")) {
                Map<String, String> result = alipay.payV2(orderInfo, true);
                Message msg = new Message();
                msg.obj = result;
                mHandler.sendMessage(msg);
            } else {
                Log.e(TAG, orderInfo);
                String result = alipay.pay(orderInfo, true);
                Message msg = new Message();
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        };
        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }

    public void gotUnionPay(Activity activity, CPayUPIOrderResult result) {
        String tn = result.mSignedString;
        UPPayAssistEx.startPay(activity, null, null, tn,  (env == DEV || env == UAT) ? "01": "00");
    }

    public void gotWX(Activity activity, WXPayorder result, CPayUPIOrder order) {

        mOrderResult = new CPayUPIOrderResult();
        mOrderResult.mCurrency = order.getCurrency();
        mOrderResult.mOrder = order;
        mOrderResult.mOrderId = result.extData;
        mOrderResult.mRedirectUrl = "";

        PayReq req = new PayReq();
        req.appId = result.appid;
        req.partnerId = result.partnerid;
        req.prepayId = result.prepayid;
        req.nonceStr = result.noncestr;
        req.timeStamp = result.timestamp;
        req.packageValue = result.mPackage;
        req.sign = result.sign;
        req.extData = result.extData;

        boolean argsCheck = req.checkArgs();

        setWXAppId(result.appid);
        IWXAPI api = WXAPIFactory.createWXAPI(activity, result.appid);

        boolean callWxRet = api.sendReq(req);
        Log.d("jim", "check args " + argsCheck);
        Log.d("jim", "send return :" + callWxRet);

        if (callWxRet) {
            // success invoke wechat app
            allowQuery = true;
        } else {
            // failed
            mOrderResult.mStatus = "-2";
            mOrderResult.mMessage = "WeChat is not installed on the device";
            mOrderListener.gotOrderResult(mOrderResult);
        }

    }

    public void onWXPaySuccess(String orderId) {
        if (!orderId.equals(mOrderResult.mOrderId)) {
            return;
        }

        allowQuery = false;
        inquireOrderInternally();

        if (mOrderListener == null) {
            return;
        }

        // client success callback
        mOrderResult.mStatus = "0"; // Unified the success status
        mOrderResult.mMessage = "Success";
        mOrderListener.gotOrderResult(mOrderResult);
    }

    public void onWXPayFailed(String orderId, int respCode, String errMsg) {
        if (orderId.equals(mOrderResult.mOrderId)) {
            allowQuery = false;

            inquireOrderInternally();
            if (mOrderListener != null) {
                mOrderResult.mStatus = Integer.toString(respCode);
                mOrderResult.mMessage = errMsg;
                mOrderListener.gotOrderResult(mOrderResult);
            }
        }
    }




    public void setupOnResumeCheck(CPayUPIOrderResult result) {
        // alipay_hk or UnionPay 成功唤起了客户端， 准备在OnResume的地方检查支付结果
        allowQuery = true;
        mOrderResult = result;
    }

    public void onResume() {
        // check pay
        if (mOrderListener != null && mOrderResult != null && allowQuery) {
            inquireOrderInternally();
            mOrderListener.gotOrderResult(mOrderResult);
            mOrderListener = null;
            allowQuery = false;
        }
    }


    public void onOrderRequestError() {
        mOrderListener.gotOrderResult(null);
    }

    public void onOrderRequestError(CPayUPIOrder order, String err) {
        ErrorMessage msg = new ErrorMessage("-1",err,null);
        onOrderRequestError(order, msg);
    }

    public void onOrderRequestError(CPayUPIOrder order, ErrorMessage errMsg) {
        CPayUPIOrderResult result = new CPayUPIOrderResult();
        result.mOrder = order;
        result.mStatus = errMsg.getCode();
        result.mMessage = String.format("%s (%s)", errMsg.getMessage(), errMsg.getDebug());
        mOrderListener.gotOrderResult(result);
    }

    public void onInquiredOrderError() {
        mInquireListener.gotInquireResult(null);
    }

}
