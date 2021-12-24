package sdk.networking;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import sdk.CPaySDK;
import sdk.models.CPayOrder;

/**
 * Created by alexandrudiaconu on 7/22/17.
 */

public class CPayOrderRequest extends Request<JSONObject> {
    private final Response.Listener<JSONObject> mListener;
    private final Map<String, String> mParams;
    private final String mOrderType;

    public CPayOrderRequest(int method, String url, CPayOrder order/*Map<String, String> params*/,
                            Response.Listener<JSONObject> reponseListener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mListener = reponseListener;
        this.mParams = order.toPayload();
        this.mOrderType = order.getVendor();
    }

    @Override
    protected Map<String, String> getParams() throws com.android.volley.AuthFailureError {
        return mParams;
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        return Utils.parseResponse(response);
    }

    @Override
    protected void deliverResponse(JSONObject response) {
        mListener.onResponse(response);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<>();
        String auth = "Bearer " + CPaySDK.initInstance().mToken;
        headers.put("Authorization", auth);

        if(mOrderType.equals("card")) {
            //this is specific for connector
            headers.put("Content-Type", "application/x-www-form-urlencoded");
        } else {
            headers.put("Accept-Encoding", "identity");
        }
        return headers;
    }
}