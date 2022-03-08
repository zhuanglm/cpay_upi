package upisdk.networking;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import upisdk.CPayUPISDK;
import upisdk.models.CPayUPIOrder;


public class CPayUPIOrderRequest extends JsonObjectRequest {
    private final Response.Listener<JSONObject> mListener;

    public CPayUPIOrderRequest(int method, String url, CPayUPIOrder order,
                               Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener) throws JSONException {
        super(method, url, order.toPayload(), responseListener,errorListener);
        this.mListener = responseListener;
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
        headers.put("Content-Type","application/json");
        String auth = "Bearer " + CPayUPISDK.getInstance().mToken;
        headers.put("Authorization", auth);

        return headers;
    }

}