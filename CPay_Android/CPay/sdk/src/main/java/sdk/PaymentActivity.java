package sdk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

public class PaymentActivity extends Activity implements IWXAPIEventHandler {

    private static final String TAG = "WXPayEntryActivity";

    private IWXAPI api;

    private String orderID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String wxAppId = CPaySDK.getWXAppId();
        api = WXAPIFactory.createWXAPI(this, wxAppId);
        api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq req) {
        Log.e(TAG, req.toString());
    }

    @Override
    public void onResp(BaseResp resp) {
        Log.d(TAG, "onPayFinish, errCode = " + resp.errCode);
        if(resp.errCode == 0){
            // success
            checkPaymentResult();
            return;
        }else if(resp.errCode == -1){
            Toast.makeText(this, "Error code:" + resp.errCode + " sign error", Toast.LENGTH_LONG).show();
        }else if(resp.errCode == -2){
            Toast.makeText(this, "Error code:" + resp.errCode + " user cancel", Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Error code:" + resp.errCode + " other error", Toast.LENGTH_LONG).show();
        }
        finish();
    }


    private void checkPaymentResult(){
        orderID = getIntent().getStringExtra("_wxapi_payresp_extdata");
        CPaySDK.getInstance().onWXPaySuccess(orderID);
        finish();
    }
}