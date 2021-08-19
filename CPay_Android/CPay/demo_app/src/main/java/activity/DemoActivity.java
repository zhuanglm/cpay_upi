package activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import citcon.cpay.R;
import sdk.CPayMode;
import sdk.CPaySDK;
import sdk.interfaces.OrderResponse;
import sdk.models.CPayInquireResult;
import sdk.models.CPayOrder;
import sdk.models.CPayOrderResult;

public class DemoActivity extends AppCompatActivity {
    private final static String TAG = "DemoActivity";
    private EditText mReferenceIdEditText;
    private EditText mSubjectEditText;
    private EditText mBodyEditText;
    private EditText mAmountEditText;
    private EditText mCurrencyEditText;
    private Spinner mVendorSpinner;
    private Spinner mModeSpinner;
    private Spinner mTokenSpinner;
    private EditText mIpnEditText;
    private EditText mCallbackEditText;
    private Switch mSwitch;
    private TextView mResultTextView;
    private ScrollView mScrollView;
    // After Pay success query transaction result
    private BroadcastReceiver mInquireReceiver;

    private String REF_ID;
    private String CALLBACK_URL;
    private String IPN_URL;

    //    boolean testUSD = true;
//    private static final String AMS_TOKEN = "XYIL2W9BCQSTNN1CXUQ6WEH9JQYZ3VLM";
//    private static final String UNIONPAY_TOKEN = "52A92BB2E055434DBAC0CC4585C242B2";
//    private static final String USD_TOKEN = "9FBBA96E77D747659901CCBF787CDCF1";
//    private static final String CNY_TOKEN = "CNYAPPF6A0FE479A891BF45706A690AE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        CPaySDK.getInstance(DemoActivity.this, null);

        String CURRENCY = "USD";

//        if (testUSD) {

        REF_ID = "202108170633";
//        AUTH_TOKEN = UNIONPAY_TOKEN;
        IPN_URL = "https://merchant.com/ipn.php";
        CALLBACK_URL = "https://dev.citcon-inc.com";
        /*} else {
            REF_ID = "CNY-mobile-test";
            AUTH_TOKEN = "CNYAPPF6A0FE479A891BF45706A690AE";
            CALLBACK_URL = "http://52.87.248.227/ipn.php";
            CURRENCY = "CNY";
        }*/


        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);

        mReferenceIdEditText = (EditText) findViewById(R.id.reference_id_editText);
        mSubjectEditText = (EditText) findViewById(R.id.subject_editText);
        mBodyEditText = (EditText) findViewById(R.id.body_editText);
        mAmountEditText = (EditText) findViewById(R.id.amount_editText);
        mCurrencyEditText = (EditText) findViewById(R.id.currency_editText);
        mVendorSpinner = (Spinner) findViewById(R.id.vendor_spinner);
        mModeSpinner = (Spinner) findViewById(R.id.mode_spinner);
        mTokenSpinner = (Spinner) findViewById(R.id.token_spinner);
        mIpnEditText = (EditText) findViewById(R.id.ipn_editText);
        mCallbackEditText = (EditText) findViewById(R.id.callback_editText);
        mSwitch = (Switch) findViewById(R.id.duplicate_switch);
        mResultTextView = (TextView) findViewById(R.id.result_textView);
        mScrollView = (ScrollView) findViewById(R.id.scrollView);

        mReferenceIdEditText.setText(REF_ID); //Citcon Referance ID
        mSubjectEditText.setText("Test"); // order subject
        mBodyEditText.setText("Test data"); // order body
        mAmountEditText.setText("1"); // amount
        mCurrencyEditText.setText(CURRENCY); // currency USD
        mVendorSpinner.setSelection(0); // payment vendor wechatpay or alipay
        mIpnEditText.setText(IPN_URL); //citcon payment callback url
        mCallbackEditText.setText(CALLBACK_URL); // custom callback url to customization processing

        Button requestButton = (Button) findViewById(R.id.request_button);
        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CPaySDK.setMode(mModeSpinner.getSelectedItem().toString());
                CPaySDK.setToken(mTokenSpinner.getSelectedItem().toString());

                CPayOrder order = new CPayOrder(mReferenceIdEditText.getText().toString(),
                        mSubjectEditText.getText().toString(),
                        mBodyEditText.getText().toString(),
                        mAmountEditText.getText().toString(),
                        mCurrencyEditText.getText().toString(),
                        mVendorSpinner.getSelectedItem().toString(),
                        mIpnEditText.getText().toString(),
                        mCallbackEditText.getText().toString(),
                        mSwitch.isChecked());

                CPaySDK.getInstance().requestOrder(order, new OrderResponse<CPayOrderResult>() {
                    @Override
                    public void gotOrderResult(final CPayOrderResult orderResult) {
                        if (orderResult == null) {
                            Log.e(TAG, "requestOrder failed: orderResult null");
                            Toast.makeText(getApplicationContext(), "Error: Get requestOrder null failed", Toast.LENGTH_LONG).show();
                            return;
                        }

                        if (!orderResult.mStatus.equals("0")) {
                            Log.e(TAG, "requestOrder failed, status: " + orderResult.mStatus + " message: " + orderResult.mMessage);
                            Toast.makeText(getApplicationContext(), "Error: Get requestOrder failed, status: " + orderResult.mStatus + " message: " + orderResult.mMessage, Toast.LENGTH_LONG).show();
                            return;
                        }

                        Log.d(TAG, "requestOrder success");
                    }
                });
            }
        });

        /**
         *
         * <p>Get payment success broadcasting CPayInquireResult
         *
         */
        mInquireReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                CPayInquireResult response = (CPayInquireResult) intent.getSerializableExtra("inquire_result");
                String emerging = "";
                emerging += "CHECK RESULT:\n\n";
                if(response == null){
                    emerging += "NULL response";
                }else{
                    if (response.mId != null) {
                        emerging += "ORDER ID: " + response.mId + "\n";
                    }
                    if (response.mType != null) {
                        emerging += "TYPE: " + response.mType + "\n";
                    }
                    if (response.mAmount != null) {
                        emerging += "AMOUNT: " + response.mAmount + "\n";
                    }
                    if (response.mTime != null) {
                        emerging += "TIME: " + response.mTime + "\n";
                    }
                    if (response.mReference != null) {
                        emerging += "REFERENCE: " + response.mReference + "\n";
                    }
                    if (response.mStatus != null) {
                        emerging += "STATUS: " + response.mStatus + "\n";
                    }
                    if (response.mCurrency != null) {
                        emerging += "CURRENCY: " + response.mCurrency + "\n";
                    }
                    if (response.mNote != null) {
                        emerging += "NOTE: " + response.mNote + "\n";
                    }
                }

                mResultTextView.setText(emerging);

                mScrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });
            }
        };
    }

    /**
     * <p>Init CPaySDK with AUTH_TOKEN, WXAPP_ID. Register BroadcastReceiver of payment success
     * AUTH_TOKEN author token apply from Citcon.
     * WXAPP_ID wechat appid from Wechat
     * CPayMode environment String  PROD, UAT, DEV
     */


    @Override
    public void onResume() {
        super.onResume();

        CPaySDK.getInstance().onResume();
//        if (testUSD) {
//            CPaySDK.setMode(CPayMode.DEV);
//        } else {
//            CPaySDK.setMode(CPayMode.PROD);
//        }


        registerInquireReceiver();
    }

    /**
     * <p>onPause to unregister BroadcastReceiver.
     */

    @Override
    public void onPause() {
        super.onPause();

        unregisterInquireReceiver();
    }

    /**
     * Register BroadcastReceiver.
     */
    private void registerInquireReceiver() {
        if (mInquireReceiver != null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("CPAY_INQUIRE_ORDER");
            CPaySDK.getInstance().registerReceiver(mInquireReceiver, filter);
        }
    }

    /**
     * <p>unregister BroadcastReceiver.
     */
    private void unregisterInquireReceiver() {
        if (mInquireReceiver != null)
            CPaySDK.getInstance().unregisterReceiver(mInquireReceiver);
    }
}
