package activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import citcon.cpay.R;
import sdk.CPaySDK;
import sdk.interfaces.OrderResponse;
import sdk.models.CPayInquireResult;
import sdk.models.CPayOrder;
import sdk.models.CPayOrderResult;

public class DemoActivity extends AppCompatActivity {
    private EditText mReferenceIdEditText, mSubjectEditText, mBodyEditText, mAmountEditText,
            mCurrencyEditText, mVendorEditText, mIpnEditText, mCallbackEditText;
    private Switch mSwitch;
    private TextView mResultTextView;
    private ScrollView mScrollView;

    // After Pay success query transaction result

    private BroadcastReceiver mInquireReceiver;
    private static String ENV = sdk.Env.DEV;

    private String REF_ID;
    private String AUTH_TOKEN;
    private String CALLBACK_URL;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        boolean testUSD = true;
        String CURRENCY;

        if (testUSD) {
            REF_ID = "pay-mobile-test";
            AUTH_TOKEN = "9FBBA96E77D747659901CCBF787CDCF1";
            CALLBACK_URL = "https://uat.citconpay.com/payment/notify_wechatpay.php";
            CURRENCY = "USD";
        } else {
            REF_ID = "CNY-mobile-test";
            AUTH_TOKEN = "CNYAPPF6A0FE479A891BF45706A690AE";
            CALLBACK_URL = "http://52.87.248.227/ipn.php";
            CURRENCY = "CNY";
        }


        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);

        mReferenceIdEditText = (EditText) findViewById(R.id.reference_id_editText);
        mSubjectEditText = (EditText) findViewById(R.id.subject_editText);
        mBodyEditText = (EditText) findViewById(R.id.body_editText);
        mAmountEditText = (EditText) findViewById(R.id.amount_editText);
        mCurrencyEditText = (EditText) findViewById(R.id.currency_editText);
        mVendorEditText = (EditText) findViewById(R.id.vendor_editText);
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
        mVendorEditText.setText("wechatpay"); // payment vendor wechatpay or alipay
        mIpnEditText.setText(CALLBACK_URL); //citcon payment callback url
        mCallbackEditText.setText("http://www.google.com"); // custom callback url to customization processing

        Button requestButton = (Button) findViewById(R.id.request_button);
        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CPayOrder order = new CPayOrder(mReferenceIdEditText.getText().toString(),
                        mSubjectEditText.getText().toString(),
                        mBodyEditText.getText().toString(),
                        mAmountEditText.getText().toString(),
                        mCurrencyEditText.getText().toString(),
                        mVendorEditText.getText().toString(),
                        mIpnEditText.getText().toString(),
                        mCallbackEditText.getText().toString(),
                        mSwitch.isChecked());

                CPaySDK.getInstance().requestOrder(order, new OrderResponse<CPayOrderResult>() {
                    @Override
                    public void gotOrderResult(final CPayOrderResult orderResult) {
                        if (orderResult != null) {

                        }
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
     * Env environment String  uat cny dev
     */


    @Override
    public void onResume() {
        super.onResume();
        CPaySDK.getInstance(DemoActivity.this, AUTH_TOKEN).onResume();
        CPaySDK.setEnv(ENV);
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
            registerReceiver(mInquireReceiver, filter);
        }
    }

    /**
     * <p>unregister BroadcastReceiver.
     */
    private void unregisterInquireReceiver() {
        if (mInquireReceiver != null)
            unregisterReceiver(mInquireReceiver);
    }
}
