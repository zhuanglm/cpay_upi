package sdk.networking;

import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import sdk.CPaySDK;
import sdk.models.CPayInquireResult;
import sdk.models.CPayOrder;
import sdk.models.CPayOrderResult;
import sdk.models.WXPayorder;

/**
 * Created by alexandrudiaconu on 7/20/17.
 */

public class APIManager
{
    private static APIManager sInstance;
    private RequestQueue mGlobalRequestQueue;
    public static APIManager getInstance(Context context)
    {
        if(sInstance == null)
            sInstance = new APIManager(context);
        return sInstance;
    }

    private APIManager(Context context)
    {
        mGlobalRequestQueue = Volley.newRequestQueue(context);
    }

    public void requestOrder(final CPayOrder order)
    {
        String url = CPaySDK.getBaseURL(order.getmCurrency()) + "payment/pay_app";
        int method = Request.Method.POST;
        CPayOrderRequest request = new CPayOrderRequest(method, url, order.toPayload(),
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        if(order.getmVendor().equals("wechatpay")){
                            WXPayorder result = new WXPayorder();
                            result.appid = response.optString("appid");
                            result.partnerid = response.optString("partnerid");
                            result.mPackage = response.optString("package");
                            result.noncestr = response.optString("noncestr");
                            result.timestamp = response.optString("timestamp");
                            result.prepayid = response.optString("prepayid");
                            result.sign = response.optString("sign");
                            result.extData = response.optString("order_id");
                            CPaySDK.setWXAppId(result.appid);
                            CPaySDK.getInstance().gotWX(result, order.getmCurrency());

                        }else if(order.getmVendor().equals("alipay")){
                            CPayOrderResult result = new CPayOrderResult();
                            result.mRedirectUrl = response.optString("redirect_url");
                            result.mOrderId = response.optString("order_id");
                            result.mSignedString = response.optString("signed_string");
                            result.mOrderSpec = response.optString("orderSpec");
                            result.mCurrency = order.getmCurrency();
                            result.mOrder = order;
                            CPaySDK.getInstance().gotOrder(result);
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        error.printStackTrace();
                        CPaySDK.getInstance().gotOrder(null);
                    }
                }
        );
//        request.setRetryPolicy(new DefaultRetryPolicy(
//                20 * 1000,
//                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
//
//        ));
        mGlobalRequestQueue.add(request);
    }

    public void inquireOrder(final CPayOrderResult orderResult)
    {
        String url = CPaySDK.getBaseURL(orderResult.mCurrency) + "payment/inquire";

        int method = Request.Method.POST;
        Map<String, String> payload = new HashMap<>();
        payload.put("transaction_id", orderResult.mOrderId);
        payload.put("inquire_method", "real");
        CPayInquireRequest request = new CPayInquireRequest(method, url, payload,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
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
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        error.printStackTrace();
                        CPaySDK.getInstance().inquiredOrder(null);
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
