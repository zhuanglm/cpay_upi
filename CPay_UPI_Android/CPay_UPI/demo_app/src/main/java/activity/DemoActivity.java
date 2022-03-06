package activity;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;

import java.util.HashMap;
import java.util.Locale;

import citcon.cpay.BuildConfig;
import citcon.cpay.R;
import upisdk.CPayLaunchType;
import upisdk.CPayUPISDK;
import upisdk.models.CPayUPIInquireResult;
import upisdk.models.CPayUPIOrder;

public class DemoActivity extends AppCompatActivity {
    private final static String TAG = "DemoActivity";

    //mModeSpinner mTokenSpinner mCurrencySpinner mAmountEditText
    private final static int[][] PRESET =
            {
                    {1, 4, 6, 500}, // sbps dev JGY
                    {1, 0, 3, 500}, // kcp qa krw
                    {0, 3, 0, 1}, // upop uat usd
                    {0, 3, 0, 1}, // wechatpay? uat usd
                    {0, 3, 0, 1}, // alipay?
                    {0, 3, 3, 100}, // kakaopay
                    {0, 3, 5, 30000}, // dana
                    {0, 3, 2, 100}}; //alipay hk uat hkd

    private EditText mReferenceIdEditText;
    private EditText mSubjectEditText;
    private EditText mBodyEditText;
    private EditText mAmountEditText;
    private Spinner mCurrencySpinner;
    private Spinner mVendorSpinner;
    private Spinner mKCPSpinner;
    private Spinner mSBSpinner;
    private Spinner mModeSpinner;
    private Spinner mTokenSpinner;
    private EditText mIpnEditText;
    private EditText mCallbackEditText;
    private SwitchCompat mDuplicateSwitch;
    private SwitchCompat mCNPaySwitch;
    private TextView mResultTextView;
    private ScrollView mScrollView;

    private EditText mExtKey1;
    private EditText mExtKey2;
    private EditText mExtValue1;
    private EditText mExtValue2;

    private Activity mActivity;

    //    boolean testUSD = true;
//    private static final String AMS_TOKEN = "XYIL2W9BCQSTNN1CXUQ6WEH9JQYZ3VLM";
//    private static final String UNIONPAY_TOKEN = "52A92BB2E055434DBAC0CC4585C242B2";
//    private static final String USD_TOKEN = "9FBBA96E77D747659901CCBF787CDCF1";
//    private static final String CNY_TOKEN = "CNYAPPF6A0FE479A891BF45706A690AE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        mActivity = this;
        CPayUPISDK.initInstance(DemoActivity.this, null);

        //String CURRENCY = "USD";

//        if (testUSD) {

        String REF_ID = "202108170633";
//        AUTH_TOKEN = UNIONPAY_TOKEN;
        String IPN_URL = "https://merchant.com/ipn.php";
        String CALLBACK_URL = "https://dev.citcon-inc.com";
        /*} else {
            REF_ID = "CNY-mobile-test";
            AUTH_TOKEN = "CNYAPPF6A0FE479A891BF45706A690AE";
            CALLBACK_URL = "http://52.87.248.227/ipn.php";
            CURRENCY = "CNY";
        }*/


        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);

        mReferenceIdEditText = findViewById(R.id.reference_id_editText);
        mSubjectEditText = findViewById(R.id.subject_editText);
        mBodyEditText = findViewById(R.id.body_editText);
        mAmountEditText = findViewById(R.id.amount_editText);
        mCurrencySpinner = findViewById(R.id.currency_spinner);
        mVendorSpinner = findViewById(R.id.vendor_spinner);
        mKCPSpinner = findViewById(R.id.kcp_types);
        mSBSpinner = findViewById(R.id.sbps_types);
        mModeSpinner = findViewById(R.id.mode_spinner);
        mTokenSpinner = findViewById(R.id.token_spinner);
        mIpnEditText = findViewById(R.id.ipn_editText);
        mCallbackEditText = findViewById(R.id.callback_editText);
        mDuplicateSwitch = findViewById(R.id.duplicate_switch);
        mCNPaySwitch = findViewById(R.id.switch_cnpay);
        mResultTextView = findViewById(R.id.result_textView);
        mScrollView = findViewById(R.id.scrollView);
        mExtKey1 = findViewById(R.id.key1_edittext);
        mExtKey2 = findViewById(R.id.key2_edittext);
        mExtValue1 = findViewById(R.id.value1_edittext);
        mExtValue2 = findViewById(R.id.value2_edittext);

        mReferenceIdEditText.setText(REF_ID); //Citcon Referance ID
        mSubjectEditText.setText("Test"); // order subject
        mBodyEditText.setText("Test data"); // order body

        TextView sdkVersionNumber = findViewById(R.id.tv_sdk_version);
        sdkVersionNumber.setText(BuildConfig.VERSION_NAME);

        mIpnEditText.setText(IPN_URL); //citcon payment callback url
        mCallbackEditText.setText(CALLBACK_URL); // custom callback url to customization processing

        Observer<CPayUPIInquireResult> resultObserver = response -> {
            String emerging = "";
            emerging += "CHECK RESULT:\n\n";
            if (response == null) {
                emerging += "NULL response";
            } else {
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

            mScrollView.post(() -> mScrollView.fullScroll(ScrollView.FOCUS_DOWN));
        };
        CPayUPISDK.mInquireResult.observe(this, resultObserver);

        mVendorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setPreSet(position);
                if (position == 0) {
                    mKCPSpinner.setVisibility(View.GONE);
                    mSBSpinner.setVisibility(View.VISIBLE);
                } else if (position == 1) {
                    mKCPSpinner.setVisibility(View.VISIBLE);
                    mSBSpinner.setVisibility(View.GONE);
                } else {
                    mKCPSpinner.setVisibility(View.GONE);
                    mSBSpinner.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                setPreSet(0);
            }
        });

        Button requestButton = findViewById(R.id.request_button);
        requestButton.setOnClickListener(v -> {

            CPayUPISDK.setMode(mModeSpinner.getSelectedItem().toString());
            CPayUPISDK.setToken(/*mTokenSpinner.getSelectedItem().toString()*/
                    "UPI_eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1lIjoia2ZjX3VwaV91c2QiLCJpYXQiOjE2NDYzMzUzMTQsImV4cCI6MTY0ODA2ODA0OTgyN30.6-m-JWtLW9d6YTbYM7u9IW-WxbFL75_Em8guk-Ev40o");

            String key1 = mExtKey1.getText().toString().trim();
            String key2 = mExtKey2.getText().toString().trim();
            String value1 = mExtValue1.getText().toString().trim();
            String value2 = mExtValue2.getText().toString().trim();

            HashMap<String, String> ext = new HashMap<>();
            if (!TextUtils.isEmpty(key1) && !TextUtils.isEmpty(value1)) {
                ext.put(key1, value1);
            }

            if (!TextUtils.isEmpty(key2) && !TextUtils.isEmpty(value2)) {
                ext.put(key2, value2);
            }

            /*CPayOrder order = new CPayOrder(mReferenceIdEditText.getText().toString(),
                    mSubjectEditText.getText().toString(),
                    mBodyEditText.getText().toString(),
                    mAmountEditText.getText().toString(),
                    mCurrencySpinner.getSelectedItem().toString(),
                    mVendorSpinner.getSelectedItem().toString(),
                    mIpnEditText.getText().toString(),
                    mCallbackEditText.getText().toString(),
                    mSwitch.isChecked(),
                    ext);*/

            CPayUPIOrder otherOrder = new CPayUPIOrder.Builder()
                    .setLaunchType(CPayLaunchType.OTHERS)
                    .setReferenceId(mReferenceIdEditText.getText().toString())
                    .setSubject(mSubjectEditText.getText().toString())
                    .setBody(mBodyEditText.getText().toString())
                    .setAmount(mAmountEditText.getText().toString())
                    .setCurrency(mCurrencySpinner.getSelectedItem().toString())
                    .setVendor(mVendorSpinner.getSelectedItem().toString())
                    .setIpnUrl(mIpnEditText.getText().toString())
                    .setCallbackUrl(mCallbackEditText.getText().toString())
                    .setMobileCallback("https://exampe.com/mobile")
                    .setCallbackFailUrl("https://exampe.com/fail")
                    .setCallbackCancelUrl("https://exampe.com/cancel")
                    .setAllowDuplicate(mDuplicateSwitch.isChecked())
                    .enableCNPayAcceleration(mCNPaySwitch.isChecked())
                    .setCountry(Locale.CANADA)
                    .setExt(ext)
                    .build();

            CPayUPIOrder kcpOrder = new CPayUPIOrder.Builder()
                    .setLaunchType(CPayLaunchType.URL)
                    .setReferenceId(mReferenceIdEditText.getText().toString())
                    .setSubject(mSubjectEditText.getText().toString())
                    .setBody(mBodyEditText.getText().toString())
                    .setAmount(mAmountEditText.getText().toString())
                    .setCurrency(mCurrencySpinner.getSelectedItem().toString())
                    .setVendor(mKCPSpinner.getSelectedItem().toString())
                    .setIpnUrl(mIpnEditText.getText().toString())
                    .setCallbackUrl(mCallbackEditText.getText().toString())
                    .setAllowDuplicate(mDuplicateSwitch.isChecked())
                    .setSource("app_h5")
                    .setAutoCapture(true)
                    .setCountry(Locale.KOREA)
                    .setNote("note dddd")
                    .setMobileCallback("https://exampe.com/mobile")
                    .setCallbackFailUrl("https://exampe.com/fail")
                    .setCallbackCancelUrl("https://exampe.com/cancel")
                    .enableCNPayAcceleration(mCNPaySwitch.isChecked())
                    .build();

            CPayUPIOrder sbOrder = new CPayUPIOrder.Builder()
                    .setLaunchType(CPayLaunchType.URL)
                    .setReferenceId(mReferenceIdEditText.getText().toString())
                    .setSubject(mSubjectEditText.getText().toString())
                    .setBody(mBodyEditText.getText().toString())
                    .setAmount(mAmountEditText.getText().toString())
                    .setCurrency(mCurrencySpinner.getSelectedItem().toString())
                    .setVendor(mSBSpinner.getSelectedItem().toString())
                    .setIpnUrl(mIpnEditText.getText().toString())
                    .setCallbackUrl(mCallbackEditText.getText().toString())
                    .setAllowDuplicate(mDuplicateSwitch.isChecked())
                    .setSource("app_h5")
                    .setAutoCapture(true)
                    .setCountry(Locale.JAPAN)
                    .setNote("note dddd")
                    .setMobileCallback("https://exampe.com/mobile")
                    .setCallbackFailUrl("https://exampe.com/fail")
                    .setCallbackCancelUrl("https://exampe.com/cancel")
                    .enableCNPayAcceleration(false)
                    .build();

            CPayUPIOrder order;
            if(mVendorSpinner.getSelectedItem().toString().equals("kcp")) {
                order = kcpOrder;
            } else if(mVendorSpinner.getSelectedItem().toString().equals("sbps")) {
                order = sbOrder;
            } else {
                order = otherOrder;
            }

            CPayUPISDK.getInstance().requestOrder(mActivity, order, orderResult -> {
                if (orderResult == null) {
                    Log.e(TAG, "requestOrder failed: orderResult null");
                    Toast.makeText(getApplicationContext(), "Error: Get requestOrder null failed", Toast.LENGTH_LONG).show();
                    return;
                }

                if (!orderResult.mStatus.equals("0") && !orderResult.mStatus.equals("initiated")) {
                    Log.e(TAG, "requestOrder failed, status: " + orderResult.mStatus + " message: " + orderResult.mMessage);
                    Toast.makeText(getApplicationContext(), "Error: Get requestOrder failed, status: " + orderResult.mStatus + " message: " + orderResult.mMessage, Toast.LENGTH_LONG).show();
                    return;
                }

                Log.d(TAG, "requestOrder success");
            });
        });

    }

    private void setPreSet(int i) {
        mModeSpinner.setSelection(PRESET[i][0]);
        mTokenSpinner.setSelection(PRESET[i][1]);
        mCurrencySpinner.setSelection(PRESET[i][2]); // currency USD
        mAmountEditText.setText("" + PRESET[i][3]); // amount
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

        CPayUPISDK.getInstance().onResume();
//        if (testUSD) {
//            CPaySDK.setMode(CPayMode.DEV);
//        } else {
//            CPaySDK.setMode(CPayMode.PROD);
//        }

    }

    /**
     * <p>onPause to unregister BroadcastReceiver.
     */

    @Override
    public void onPause() {
        super.onPause();

    }

}
