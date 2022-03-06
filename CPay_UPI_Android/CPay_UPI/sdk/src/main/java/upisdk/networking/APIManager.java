package upisdk.networking;

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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import upisdk.CPayEntryType;
import upisdk.CPayLaunchType;
import upisdk.CPayUPISDK;
import upisdk.models.CPayUPIInquireResult;
import upisdk.models.CPayUPIOrder;
import upisdk.models.CPayUPIOrderResult;
import upisdk.models.CitconApiResponse;
import upisdk.models.RespondCharge;
import upisdk.models.RespondChargePayment;
import upisdk.models.WXPayorder;


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

    public void requestOrder(final Activity activity, final CPayUPIOrder order) {

        if (!order.getVendor().equals("wechatpay")
                && !order.getVendor().equals("alipay")
                && !order.getVendor().equals("alipay_hk")
                && !order.getVendor().equals("gcash")
                && !order.getVendor().equals("kakaopay")
                && !order.getVendor().equals("dana")
                && !order.getVendor().equals("upop")
                && order.getLaunchType() != CPayLaunchType.URL) {
            Log.e(TAG, "Error unknown vendor: " + order.getVendor());
            CPayUPISDK.getInstance().onOrderRequestError();
            return;
        }

        String entryPoint = CPayEnv.getEntryPoint(order.getCurrency(), order.getVendor(),
                CPayEntryType.ORDER, order.isAccelerateCNPay());
        if (entryPoint == null) {
            Log.e(TAG, "requestOrder: baseURL error, please check currency and vendor");
            CPayUPISDK.getInstance().onOrderRequestError();
            return;
        }
        Log.e(TAG, entryPoint);

        CPayUPIOrderRequest request;
        try {
            request = new CPayUPIOrderRequest(Request.Method.POST, entryPoint, order,
                    response -> {
                        CitconApiResponse<RespondCharge> apiResponse;
                        Gson gson = new GsonBuilder().create();
                        Type chargeType = new TypeToken<CitconApiResponse<RespondCharge>>() {
                        }.getType();
                        apiResponse = gson.fromJson(response.toString(), chargeType);

                        if (!apiResponse.isSuccessful()) {
                            String errorMessage = response.optString("message");
                            Log.e(TAG, "Error when requestOrder reason: " + errorMessage);
                            CPayUPISDK.getInstance().onOrderRequestError();
                            return;
                        }

                        RespondCharge respondCharge = apiResponse.getData();
                        RespondChargePayment respondPament = respondCharge.getPayment();

                        if (order.getLaunchType() == CPayLaunchType.URL) {
                            //common starting URL mode
                            if (order.getLaunchType() == CPayLaunchType.URL) {
                                Log.i(TAG, "Response : " + response);
                            }

                        } else {
                            JSONObject resContent = null;
                            try {
                                resContent = new JSONObject(respondPament.getClient().get(0).getContent());
                            } catch (JSONException e) {
                                e.printStackTrace();
                                CPayUPISDK.getInstance().onOrderRequestError();
                            }

                            if (resContent != null) {
                                switch (respondPament.getMethod()) {
                                    case "wechatpay": {
                                        WXPayorder result = new WXPayorder();
                                        result.appid = resContent.optString("appid");
                                        result.partnerid = resContent.optString("mch_id");
                                        result.mPackage = resContent.optString("package");
                                        result.noncestr = resContent.optString("nonce_str");
                                        result.timestamp = resContent.optString("timestamp");
                                        result.prepayid = resContent.optString("prepay_id");
                                        result.sign = resContent.optString("sign");
                                        result.extData = respondCharge.getId();
                                        CPayUPISDK.getInstance().gotWX(activity, result, order);
                                        break;
                                    }
                                    case "alipay": {
                                        CPayUPIOrderResult result = new CPayUPIOrderResult();
                                        //result.mRedirectUrl = res.optString("redirect_url");
                                        result.mOrderId = resContent.optString("order_id");
                                        result.mSignedString = response.optString("signed_string");
                                        result.mOrderSpec = response.optString("orderSpec");
                                        result.mCurrency = order.getCurrency();
                                        result.mOrder = order;
                                        CPayUPISDK.getInstance().gotAlipay(activity, result);
                                        break;
                                    }
                                    case "alipay_hk":
                                    case "gcash":
                                    case "kakaopay":
                                    case "dana": {
                                        CPayUPIOrderResult result = new CPayUPIOrderResult();
                                        result.mRedirectUrl = response.optString("redirect_url");
                                        result.mOrderId = response.optString("order_id");
                                        result.mSignedString = response.optString("signed_string");
                                        result.mOrderSpec = response.optString("orderSpec");
                                        result.mCurrency = order.getCurrency();
                                        result.mOrder = order;
                                        if (TextUtils.isEmpty(result.mRedirectUrl) || result.mRedirectUrl.equals("null")) {
                                            Log.e(TAG, "redirect_url: is null");
                                            CPayUPISDK.getInstance().onOrderRequestError();
                                            return;
                                        }
                                        try {
                                            Log.e(TAG, result.mRedirectUrl);
                                            Intent intent = Intent.parseUri(result.mRedirectUrl, Intent.URI_INTENT_SCHEME);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            activity.startActivity(intent);
                                            CPayUPISDK.getInstance().setupOnResumeCheck(result);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        break;
                                    }
                                    case "upop": {
                                        CPayUPIOrderResult result = new CPayUPIOrderResult();
                                        result.mOrderId = respondCharge.getId();
                                        result.mSignedString = resContent.optString("tn");
                                        result.mCurrency = order.getCurrency();
                                        result.mOrder = order;
                                        CPayUPISDK.getInstance().gotUnionPay(activity, result);
                                        CPayUPISDK.getInstance().setupOnResumeCheck(result);
                                        break;
                                    }

                                    default: {

                                    }
                                }
                            }
                        }
                    },
                    error -> {
                        error.printStackTrace();
                        Log.e(TAG, "Request error: " + error.getMessage());
                        CPayUPISDK.getInstance().onOrderRequestError();
                    }
            );

            request.setRetryPolicy(new DefaultRetryPolicy(
                    20 * 1000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT

            ));
            try {
                Log.i(TAG, "Request header: " + request.getHeaders());
                Log.i(TAG, "Request body: " + new String(request.getBody()));
            } catch (AuthFailureError authFailureError) {
                authFailureError.printStackTrace();
            }
            mGlobalRequestQueue.add(request);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void inquireOrder(final CPayUPIOrderResult orderResult) {
        String entryPoint = CPayEnv.getEntryPoint(orderResult.mCurrency, orderResult.mOrder.getVendor(),
                CPayEntryType.INQUIRE, orderResult.mOrder.isAccelerateCNPay());
        if (entryPoint == null) {
            Log.e(TAG, "requestOrder: baseURL error, please check currency and vendor");
            CPayUPISDK.getInstance().onInquiredOrderError();
            return;
        }

        Map<String, String> payload = new HashMap<>();
        payload.put("transaction_id", orderResult.mOrderId);
        payload.put("inquire_method", "real");
        CPayUPIInquireRequest request = new CPayUPIInquireRequest(Request.Method.POST, entryPoint, payload,
                response -> {
                    CPayUPIInquireResult inquireResult = new CPayUPIInquireResult();
                    inquireResult.mId = response.optString("id");
                    inquireResult.mType = response.optString("type");
                    inquireResult.mAmount = response.optString("amount");
                    inquireResult.mTime = response.optString("time");
                    inquireResult.mReference = response.optString("reference");
                    inquireResult.mStatus = response.optString("status");
                    inquireResult.mCurrency = response.optString("currency");
                    inquireResult.mNote = response.optString("note");
                    CPayUPISDK.getInstance().inquiredOrder(inquireResult);
                },
                error -> {
                    error.printStackTrace();
                    CPayUPISDK.getInstance().onInquiredOrderError();
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
            CPayUPISDK.getInstance().onInquiredOrderError();
            return;
        }

        Map<String, String> payload = new HashMap<>();
        payload.put("reference", referenceId);
        payload.put("inquire_method", "real");
        CPayUPIInquireRequest request = new CPayUPIInquireRequest(Request.Method.POST, entryPoint, payload,
                response -> {
                    CPayUPIInquireResult inquireResult = new CPayUPIInquireResult();
                    inquireResult.mId = response.optString("id");
                    inquireResult.mType = response.optString("type");
                    inquireResult.mAmount = response.optString("amount");
                    inquireResult.mTime = response.optString("time");
                    inquireResult.mReference = response.optString("reference");
                    inquireResult.mStatus = response.optString("status");
                    inquireResult.mCurrency = response.optString("currency");
                    inquireResult.mNote = response.optString("note");
                    CPayUPISDK.getInstance().inquiredOrder(inquireResult);
                },
                error -> {
                    error.printStackTrace();
                    CPayUPISDK.getInstance().onInquiredOrderError();
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
