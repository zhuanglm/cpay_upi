package sdk.networking;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.zip.GZIPInputStream;

public class Utils {
    static Response<JSONObject> parseResponse(NetworkResponse response) {
        String output = ""; // note: better to use StringBuilder
        try {
            final GZIPInputStream gStream = new GZIPInputStream(new ByteArrayInputStream(response.data));
            final InputStreamReader reader = new InputStreamReader(gStream);
            final BufferedReader in = new BufferedReader(reader);
            String read;
            while ((read = in.readLine()) != null) {
                output += read;
            }
            reader.close();
            in.close();
            gStream.close();
        } catch (IOException e) {
            try {
                output = new String(response.data,
                        HttpHeaderParser.parseCharset(response.headers));
            } catch (UnsupportedEncodingException unsupportedEncodingException) {
                return Response.error(new ParseError(e));
            }
        }

        try {
            return Response.success(new JSONObject(output), HttpHeaderParser.parseCacheHeaders(response));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }
    }

}
