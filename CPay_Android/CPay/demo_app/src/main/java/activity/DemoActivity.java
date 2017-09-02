package activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import citcon.cpay.R;
import sdk.CPaySDK;
import sdk.interfaces.InquireResponse;
import sdk.interfaces.OrderResponse;
import sdk.models.CPayInquireResult;
import sdk.models.CPayOrder;
import sdk.models.CPayOrderResult;

public class DemoActivity extends AppCompatActivity
{
    private EditText mReferenceIdEditText, mSubjectEditText, mBodyEditText, mAmountEditText,
            mCurrencyEditText, mVendorEditText, mIpnEditText, mCallbackEditText;
    private Switch mSwitch;
    private TextView mResultTextView;
    private ScrollView mScrollView;
    private BroadcastReceiver mInquireReceiver;

    private final String AUTH_TOKEN = "8BE127BDEEC749B182F02966618E3588";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        ActivityCompat.requestPermissions(this,
                new String[]{ Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE },
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

        mReferenceIdEditText.setText("1ZLLJULOCRW3LAU");
        mSubjectEditText.setText("测试");
        mBodyEditText.setText("我是测试数据");
        mAmountEditText.setText("1");
        mCurrencyEditText.setText("USD");
        mVendorEditText.setText("alipay");
        mIpnEditText.setText("http://www.xxx.com");
        mCallbackEditText.setText("http://www.google.com");

        Button requestButton = (Button) findViewById(R.id.request_button);
        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                CPayOrder order = new CPayOrder(mReferenceIdEditText.getText().toString(),
                        mSubjectEditText.getText().toString(),
                        mBodyEditText.getText().toString(),
                        mAmountEditText.getText().toString(),
                        mCurrencyEditText.getText().toString(),
                        mVendorEditText.getText().toString(),
                        mIpnEditText.getText().toString(),
                        mCallbackEditText.getText().toString(),
                        mSwitch.isChecked());
                CPaySDK.getInstance().requestOrder(order, new OrderResponse<CPayOrderResult>()
                {
                    @Override
                    public void gotOrderResult(final CPayOrderResult orderResult)
                    {
                        if(orderResult != null)
                        {
                        }
                    }
                });
            }
        });

        mInquireReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent) {
                CPayInquireResult response = (CPayInquireResult) intent.getSerializableExtra("inquire_result");
                String emerging = "";
                emerging += "CHECK RESULT:\n\n";
                if(response.mId != null)
                {
                    emerging += "ORDER ID: " + response.mId + "\n";
                }
                if(response.mType != null)
                {
                    emerging += "TYPE: " + response.mType + "\n";
                }
                if(response.mAmount != null)
                {
                    emerging += "AMOUNT: " + response.mAmount + "\n";
                }
                if(response.mTime != null)
                {
                    emerging += "TIME: " + response.mTime + "\n";
                }
                if(response.mReference != null)
                {
                    emerging += "REFERENCE: " + response.mReference + "\n";
                }
                if(response.mStatus != null)
                {
                    emerging += "STATUS: " + response.mStatus + "\n";
                }
                if(response.mCurrency != null)
                {
                    emerging += "CURRENCY: " + response.mCurrency + "\n";
                }
                if(response.mNote != null)
                {
                    emerging += "NOTE: " + response.mNote + "\n";
                }

                mResultTextView.setText(emerging);

                mScrollView.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });
            }
        };
    }

    @Override
    public void onResume()
    {
        super.onResume();

        CPaySDK.getInstance(DemoActivity.this, AUTH_TOKEN).onResume();

        registerInquireReceiver();
    }

    @Override
    public void onPause()
    {
        super.onPause();

        unregisterInquireReceiver();
    }

    private void registerInquireReceiver()
    {
        if(mInquireReceiver != null)
        {
            IntentFilter filter = new IntentFilter();
            filter.addAction("CPAY_INQUIRE_ORDER");
            registerReceiver(mInquireReceiver, filter);
        }
    }

    private void unregisterInquireReceiver()
    {
        if(mInquireReceiver != null)
            unregisterReceiver(mInquireReceiver);
    }
}
