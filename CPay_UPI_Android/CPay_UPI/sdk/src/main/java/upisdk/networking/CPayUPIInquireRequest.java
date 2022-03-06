package upisdk.networking;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import upisdk.CPayUPISDK;

/**
 * Created by alexandrudiaconu on 7/22/17.
 */

public class CPayUPIInquireRequest extends Request<JSONObject>
{
    private Response.Listener<JSONObject> mListener;
    private Map<String, String> mParams;

    public CPayUPIInquireRequest(int method, String url, Map<String, String> params,
                                 Response.Listener<JSONObject> reponseListener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mListener = reponseListener;
        this.mParams = params;
    }

    protected Map<String, String> getParams()
            throws AuthFailureError
    {
        return mParams;
    };

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        return Utils.parseResponse(response);
    }

    @Override
    protected void deliverResponse(JSONObject response)
    {
        mListener.onResponse(response);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError
    {
        Map<String, String> headers = new HashMap<>();
        String auth = "Bearer " + CPayUPISDK.getInstance().mToken;
        headers.put("Authorization", auth);
        return headers;
    }
}