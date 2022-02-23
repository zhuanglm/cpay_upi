package sdk.networking;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.Volley;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import sdk.CPayEntryType;
import sdk.CPayLaunchType;
import sdk.CPaySDK;
import sdk.models.CPayInquireResult;
import sdk.models.CPayOrder;
import sdk.models.CPayOrderResult;
import sdk.models.WXPayorder;


/**
 * Created by alexandrudiaconu on 7/20/17.
 */

public class APIManager {
    private static final String TAG = "APIManager";
    private static APIManager sInstance;
    private final RequestQueue mGlobalRequestQueue;

    public static APIManager getInstance(Context context) {
        if (sInstance == null)
            sInstance = new APIManager(context);
        return sInstance;
    }

    private APIManager(Context context) {
        mGlobalRequestQueue = Volley.newRequestQueue(context);
        VolleyLog.DEBUG = true;
    }

    public void requestOrder(final Activity activity, final CPayOrder order) {

        if (!order.getVendor().equals("wechatpay")
                && !order.getVendor().equals("alipay")
                && !order.getVendor().equals("alipay_hk")
                && !order.getVendor().equals("gcash")
                && !order.getVendor().equals("kakaopay")
                && !order.getVendor().equals("dana")
                && !order.getVendor().equals("upop")
                && order.getLaunchType() != CPayLaunchType.URL) {
            Log.e(TAG, "Error unknown vendor: " + order.getVendor());
            CPaySDK.initInstance().onOrderRequestError();
            return;
        }

        String entryPoint = CPayEnv.getEntryPoint(order.getCurrency(), order.getVendor(),
                CPayEntryType.ORDER, order.isAccelerateCNPay());
        if (entryPoint == null) {
            Log.e(TAG, "requestOrder: baseURL error, please check currency and vendor");
            CPaySDK.initInstance().onOrderRequestError();
            return;
        }
        Log.e(TAG, entryPoint);

        CPayOrderRequest request = new CPayOrderRequest(Request.Method.POST, entryPoint, order,
                response -> {
                    if (response.optBoolean("failed") ||
                            response.optString("result").equals("fail")) {
                        // {"failed":true,"message":"Invalid request."}
                        String errorMessage = response.optString("message");
                        Log.e(TAG, "Error when requestOrder reason: " + errorMessage);
                        CPaySDK.initInstance().onOrderRequestError();
                        return;
                    }

                    if(order.getLaunchType() == CPayLaunchType.URL) {
                        //common starting URL mode
                        if(order.getLaunchType() == CPayLaunchType.URL) {
                            CPayOrderResult result = new CPayOrderResult();

                            result.mOrder = order;
                            result.mStatus = response.optString("status");
                            result.mOrderId = response.optString("transaction_id");
                            result.mRedirectUrl = response.optString("url");
                            result.mGateway = response.optString("gateway");
                            try {
                                Intent intent = Intent.parseUri(result.mRedirectUrl, Intent.URI_INTENT_SCHEME);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                activity.startActivity(intent);
                                CPaySDK.initInstance().setupOnResumeCheck(result);
                            } catch (URISyntaxException e) {
                                e.printStackTrace();
                            }
                        }
                        Log.i(TAG, "Response : " + response);
                    } else {
                        switch (order.getVendor()) {
                            case "wechatpay": {
                                WXPayorder result = new WXPayorder();
                                result.appid = response.optString("appid");
                                result.partnerid = response.optString("partnerid");
                                result.mPackage = response.optString("package");
                                result.noncestr = response.optString("noncestr");
                                result.timestamp = response.optString("timestamp");
                                result.prepayid = response.optString("prepayid");
                                result.sign = response.optString("sign");
                                result.extData = response.optString("order_id");
                                CPaySDK.initInstance().gotWX(activity, result, order);
                                break;
                            }
                            case "alipay": {
                                CPayOrderResult result = new CPayOrderResult();
                                result.mRedirectUrl = response.optString("redirect_url");
                                result.mOrderId = response.optString("order_id");
                                result.mSignedString = response.optString("signed_string");
                                result.mOrderSpec = response.optString("orderSpec");
                                result.mCurrency = order.getCurrency();
                                result.mOrder = order;
                                CPaySDK.initInstance().gotAlipay(activity, result);
                                break;
                            }
                            case "alipay_hk":
                            case "gcash":
                            case "kakaopay":
                            case "dana": {
                                CPayOrderResult result = new CPayOrderResult();
                                result.mRedirectUrl = response.optString("redirect_url");
                                result.mOrderId = response.optString("order_id");
                                result.mSignedString = response.optString("signed_string");
                                result.mOrderSpec = response.optString("orderSpec");
                                result.mCurrency = order.getCurrency();
                                result.mOrder = order;
                                if (TextUtils.isEmpty(result.mRedirectUrl)||result.mRedirectUrl.equals("null")){
                                    Log.e(TAG, "redirect_url: is null");
                                    CPaySDK.initInstance().onOrderRequestError();
                                    return;
                                }
                                try {
                                    Log.e(TAG, result.mRedirectUrl);
                                    Intent intent = Intent.parseUri(result.mRedirectUrl, Intent.URI_INTENT_SCHEME);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    activity.startActivity(intent);
                                    CPaySDK.initInstance().setupOnResumeCheck(result);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                break;
                            }
                            case "upop": {
                                CPayOrderResult result = new CPayOrderResult();
                                result.mOrderId = response.optString("order_id");
                                result.mSignedString = response.optString("tn");
                                result.mCurrency = order.getCurrency();
                                result.mOrder = order;
                                CPaySDK.initInstance().gotUnionPay(activity, result);
                                CPaySDK.initInstance().setupOnResumeCheck(result);
                                break;
                            }

                            default: {

                            }
                        }
                    }
                },
                error -> {
                    error.printStackTrace();
                    CPaySDK.initInstance().onOrderRequestError();
                }
        );
        request.setRetryPolicy(new DefaultRetryPolicy(
                20 * 1000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT

        ));
        try {
            Log.i(TAG, "Request header: " + request.getHeaders());
            Log.i(TAG, "Request body: " + request.getParams());
        } catch (AuthFailureError authFailureError) {
            authFailureError.printStackTrace();
        }
        mGlobalRequestQueue.add(request);

    }

    public void inquireOrder(final CPayOrderResult orderResult) {
        String entryPoint = CPayEnv.getEntryPoint(orderResult.mCurrency, orderResult.mOrder.getVendor(),
                CPayEntryType.INQUIRE, orderResult.mOrder.isAccelerateCNPay());
        if (entryPoint == null) {
            Log.e(TAG, "requestOrder: baseURL error, please check currency and vendor");
            CPaySDK.initInstance().onInquiredOrderError();
            return;
        }

        Map<String, String> payload = new HashMap<>();
        payload.put("transaction_id", orderResult.mOrderId);
        payload.put("inquire_method", "real");
        CPayInquireRequest request = new CPayInquireRequest(Request.Method.POST, entryPoint, payload,
                response -> {
                    CPayInquireResult inquireResult = new CPayInquireResult();
                    inquireResult.mId = response.optString("id");
                    inquireResult.mType = response.optString("type");
                    inquireResult.mAmount = response.optString("amount");
                    inquireResult.mTime = response.optString("time");
                    inquireResult.mReference = response.optString("reference");
                    inquireResult.mStatus = response.optString("status");
                    inquireResult.mCurrency = response.optString("currency");
                    inquireResult.mNote = response.optString("note");
                    CPaySDK.initInstance().inquiredOrder(inquireResult);
                },
                error -> {
                    error.printStackTrace();
                    CPaySDK.initInstance().onInquiredOrderError();
                }
        );
        request.setRetryPolicy(new DefaultRetryPolicy(
                20 * 1000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT

        ));
        mGlobalRequestQueue.add(request);
    }

    public void inquireOrderByRef(final String referenceId, final String currency, final String vendor,
                                  boolean isCNAcceleration) {
        String entryPoint = CPayEnv.getEntryPoint(currency, vendor, CPayEntryType.INQUIRE, isCNAcceleration);
        if (entryPoint == null) {
            Log.e(TAG, "inquireOrderByRef: baseURL error, please check currency and vendor");
            CPaySDK.initInstance().onInquiredOrderError();
            return;
        }

        Map<String, String> payload = new HashMap<>();
        payload.put("reference", referenceId);
        payload.put("inquire_method", "real");
        CPayInquireRequest request = new CPayInquireRequest(Request.Method.POST, entryPoint, payload,
                response -> {
                    CPayInquireResult inquireResult = new CPayInquireResult();
                    inquireResult.mId = response.optString("id");
                    inquireResult.mType = response.optString("type");
                    inquireResult.mAmount = response.optString("amount");
                    inquireResult.mTime = response.optString("time");
                    inquireResult.mReference = response.optString("reference");
                    inquireResult.mStatus = response.optString("status");
                    inquireResult.mCurrency = response.optString("currency");
                    inquireResult.mNote = response.optString("note");
                    CPaySDK.initInstance().inquiredOrder(inquireResult);
                },
                error -> {
                    error.printStackTrace();
                    CPaySDK.initInstance().onInquiredOrderError();
                }
        );
        request.setRetryPolicy(new DefaultRetryPolicy(
                20 * 1000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT

        ));
        mGlobalRequestQueue.add(request);
    }


}
