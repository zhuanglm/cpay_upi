package sdk.networking;

import android.content.Context;
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
        String url = Environment.URL_PAY;
        int method = Request.Method.POST;
        CPayOrderRequest request = new CPayOrderRequest(method, url, order.toPayload(),
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        if(order.getmVendor().equals("wechatpay")){
                            try
                            {
                                WXPayorder result = new WXPayorder();
                                result.appid = response.optString("appid");
                                result.partnerid = response.optString("partnerid");
                                result.mPackage = response.optString("package");
                                result.noncestr = response.optString("noncestr");
                                result.timestamp = response.optString("timestamp");
                                result.prepayid = response.optString("prepayid");
                                result.sign = response.optString("sign");
                                result.extData = response.optString("order_id");
                                CPaySDK.getInstance().gotWX(result);
                            }
                            catch(Exception ex)
                            {
                                ex.printStackTrace();
                                CPaySDK.getInstance().gotWX(null);
                            }

                        }else if(order.getmVendor().equals("alipay")){
                            try
                            {
                                CPayOrderResult result = new CPayOrderResult();
                                if(response.has("redirect_url"))
                                {
                                    result.mRedirectUrl = response.getString("redirect_url");
                                }
                                if(response.has("order_id"))
                                {
                                    result.mOrderId = response.getString("order_id");
                                }
                                if(response.has("signed_string"))
                                {
                                    result.mSignedString = response.getString("signed_string");
                                }
                                if(response.has("orderSpec"))
                                {
                                    result.mOrderSpec = response.getString("orderSpec");
                                }
                                result.mOrder = order;
                                CPaySDK.getInstance().gotOrder(result);
                            }
                            catch(Exception ex)
                            {
                                ex.printStackTrace();
                                CPaySDK.getInstance().gotOrder(null);
                            }
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
        mGlobalRequestQueue.add(request);
    }

    public void inquireOrder(final CPayOrderResult orderResult)
    {
        String url;
        if(orderResult.mOrder.getmVendor().equals("wechatpay")){
            url = Environment.URL_INQUIRE_WX;
        }else {
            url = Environment.URL_INQUIRE;
        }

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
                        try
                        {
                            CPayInquireResult inquireResult = new CPayInquireResult();
                            inquireResult.mId = response.getString("id");
                            inquireResult.mType = response.getString("type");
                            inquireResult.mAmount = response.getString("amount");
                            inquireResult.mTime = response.getString("time");
                            inquireResult.mReference = response.getString("reference");
                            inquireResult.mStatus = response.getString("status");
                            inquireResult.mCurrency = response.getString("currency");
                            inquireResult.mNote = response.getString("note");
                            CPaySDK.getInstance().inquiredOrder(inquireResult);
                        }
                        catch(Exception ex)
                        {
                            ex.printStackTrace();
                            CPaySDK.getInstance().inquiredOrder(null);
                        }
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
        mGlobalRequestQueue.add(request);
    }
}
