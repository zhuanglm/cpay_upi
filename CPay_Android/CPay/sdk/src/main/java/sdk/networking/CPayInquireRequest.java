package sdk.networking;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import sdk.CPaySDK;

/**
 * Created by alexandrudiaconu on 7/22/17.
 */

public class CPayInquireRequest extends Request<JSONObject>
{
    private Response.Listener<JSONObject> mListener;
    private Map<String, String> mParams;

    public CPayInquireRequest(int method, String url, Map<String, String> params,
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
        String auth = "Bearer " + CPaySDK.getInstance().mToken;
        headers.put("Authorization", auth);
        return headers;
    }
}