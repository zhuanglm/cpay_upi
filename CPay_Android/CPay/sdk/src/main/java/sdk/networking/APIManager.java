package sdk.networking;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

import sdk.CPayEntryType;
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
    public Context context;

    public static APIManager getInstance(Context context) {
        if (sInstance == null)
            sInstance = new APIManager(context);
        return sInstance;
    }

    private APIManager(Context context) {
        this.context = context;
        mGlobalRequestQueue = Volley.newRequestQueue(context);
    }

    public void requestOrder(final CPayOrder order) {

        if (!order.getmVendor().equals("wechatpay")
                && !order.getmVendor().equals("alipay")
                && !order.getmVendor().equals("alipay_hk")
                && !order.getmVendor().equals("gcash")
                && !order.getmVendor().equals("kakaopay")
                && !order.getmVendor().equals("dana")
                && !order.getmVendor().equals("upop")) {
            Log.e(TAG, "Error unknown vendor: " + order.getmVendor());
            CPaySDK.getInstance().onOrderRequestError();
            return;
        }

        String entryPoint = CPayEnv.getEntryPoint(order.getmCurrency(), order.getmVendor(), CPayEntryType.ORDER);
        if (entryPoint == null) {
            Log.e(TAG, "requestOrder: baseURL error, please check currency and vendor");
            CPaySDK.getInstance().onOrderRequestError();
            return;
        }
        Log.e(TAG, entryPoint);
        final Context context = this.context;
        CPayOrderRequest request = new CPayOrderRequest(Request.Method.POST, entryPoint, order.toPayload(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (response.optBoolean("failed")) {
                            // {"failed":true,"message":"Invalid request."}
                            String errorMessage = response.optString("message");
                            Log.e(TAG, "Error when requestOrder reason: " + errorMessage);
                            CPaySDK.getInstance().onOrderRequestError();
                            return;
                        }

                        switch (order.getmVendor()) {
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
                                CPaySDK.getInstance().gotWX(result, order);
                                break;
                            }
                            case "alipay": {
                                CPayOrderResult result = new CPayOrderResult();
                                result.mRedirectUrl = response.optString("redirect_url");
                                result.mOrderId = response.optString("order_id");
                                result.mSignedString = response.optString("signed_string");
                                result.mOrderSpec = response.optString("orderSpec");
                                result.mCurrency = order.getmCurrency();
                                result.mOrder = order;
                                CPaySDK.getInstance().gotAlipay(result);
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
                                result.mCurrency = order.getmCurrency();
                                result.mOrder = order;
                                if (TextUtils.isEmpty(result.mRedirectUrl)||result.mRedirectUrl.equals("null")){
                                    Log.e(TAG, "redirect_url: is null");
                                    CPaySDK.getInstance().onOrderRequestError();
                                    return;
                                }
                                try {
                                    Log.e(TAG, result.mRedirectUrl);
                                    Intent intent = Intent.parseUri(result.mRedirectUrl, Intent.URI_INTENT_SCHEME);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    context.startActivity(intent);
                                    CPaySDK.getInstance().setupOnResumeCheck(result);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                break;
                            }
                            case "upop": {
                                CPayOrderResult result = new CPayOrderResult();
                                result.mOrderId = response.optString("order_id");
                                result.mSignedString = response.optString("tn");
                                result.mCurrency = order.getmCurrency();
                                result.mOrder = order;
                                CPaySDK.getInstance().gotUnionPay(result);
                                CPaySDK.getInstance().setupOnResumeCheck(result);
                                break;
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        CPaySDK.getInstance().onOrderRequestError();
                    }
                }
        );
        request.setRetryPolicy(new DefaultRetryPolicy(
                20 * 1000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT

        ));
        mGlobalRequestQueue.add(request);

    }

    public void inquireOrder(final CPayOrderResult orderResult) {
        String entryPoint = CPayEnv.getEntryPoint(orderResult.mCurrency, orderResult.mOrder.getmVendor(), CPayEntryType.INQUIRE);
        if (entryPoint == null) {
            Log.e(TAG, "requestOrder: baseURL error, please check currency and vendor");
            CPaySDK.getInstance().onInquiredOrderError();
            return;
        }

        Map<String, String> payload = new HashMap<>();
        payload.put("transaction_id", orderResult.mOrderId);
        payload.put("inquire_method", "real");
        CPayInquireRequest request = new CPayInquireRequest(Request.Method.POST, entryPoint, payload,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        CPayInquireResult inquireResult = new CPayInquireResult();
                        inquireResult.mId = response.optString("id");
                        inquireResult.mType = response.optString("type");
                        inquireResult.mAmount = response.optString("amount");
                        inquireResult.mTime = response.optString("time");
                        inquireResult.mReference = response.optString("reference");
                        inquireResult.mStatus = response.optString("status");
                        inquireResult.mCurrency = response.optString("currency");
                        inquireResult.mNote = response.optString("note");
                        CPaySDK.getInstance().inquiredOrder(inquireResult);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        CPaySDK.getInstance().onInquiredOrderError();
                    }
                }
        );
        request.setRetryPolicy(new DefaultRetryPolicy(
                20 * 1000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT

        ));
        mGlobalRequestQueue.add(request);
    }

    public void inquireOrderByRef(final String referenceId, final String currency, final String vendor) {
        String entryPoint = CPayEnv.getEntryPoint(currency, vendor, CPayEntryType.INQUIRE);
        if (entryPoint == null) {
            Log.e(TAG, "inquireOrderByRef: baseURL error, please check currency and vendor");
            CPaySDK.getInstance().onInquiredOrderError();
            return;
        }

        Map<String, String> payload = new HashMap<>();
        payload.put("reference", referenceId);
        payload.put("inquire_method", "real");
        CPayInquireRequest request = new CPayInquireRequest(Request.Method.POST, entryPoint, payload,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        CPayInquireResult inquireResult = new CPayInquireResult();
                        inquireResult.mId = response.optString("id");
                        inquireResult.mType = response.optString("type");
                        inquireResult.mAmount = response.optString("amount");
                        inquireResult.mTime = response.optString("time");
                        inquireResult.mReference = response.optString("reference");
                        inquireResult.mStatus = response.optString("status");
                        inquireResult.mCurrency = response.optString("currency");
                        inquireResult.mNote = response.optString("note");
                        CPaySDK.getInstance().inquiredOrder(inquireResult);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        CPaySDK.getInstance().onInquiredOrderError();
                    }
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
