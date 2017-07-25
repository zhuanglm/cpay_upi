package sdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.util.List;
import sdk.interfaces.InquireResponse;
import sdk.interfaces.OrderResponse;
import sdk.models.CPayInquireResult;
import sdk.models.CPayOrder;
import sdk.models.CPayOrderResult;
import sdk.networking.APIManager;

/**
 * Created by alexandrudiaconu on 7/20/17.
 */

public class CPaySDK
{
    private static CPaySDK sInstance;
    private APIManager mApiManager;
    private OrderResponse<CPayOrderResult> mOrderListener;
    private InquireResponse<CPayInquireResult> mInquireListener;
    public String mToken;
    private Activity mActivity;
    private CPayOrderResult mOrderResult;

    private CPaySDK(Context context)
    {
        mApiManager = APIManager.getInstance(context);
    }

    public static synchronized CPaySDK getInstance(Activity activity, String token)
    {
        if (sInstance == null)
            sInstance = new CPaySDK(activity);

        sInstance.mActivity = activity;
        sInstance.mToken = token;

        return sInstance;
    }

    public static synchronized CPaySDK getInstance()
    {
        if (sInstance == null)
        {
            throw new IllegalStateException(CPaySDK.class.getSimpleName() +
                    " is not initialized, call getInstance(...) first in the main Activity class");
        }
        return sInstance;
    }

    public void requestOrder(CPayOrder order, final OrderResponse<CPayOrderResult> listener)
    {
        mOrderListener = listener;
        mApiManager.requestOrder(order);
    }

    public void inquireOrder(CPayOrderResult orderResult, final InquireResponse<CPayInquireResult> listener)
    {
        mInquireListener = listener;
        mApiManager.inquireOrder(orderResult);
    }

    @SuppressWarnings("deprecation")
    public void gotOrder(CPayOrderResult orderResult)
    {
        if(orderResult != null)
        {
            mOrderResult = orderResult;

            if(orderResult.mRedirectUrl != null)
            {
                //wechat
                final ViewGroup viewGroup = (ViewGroup) mActivity.findViewById(android.R.id.content);
                final WebView webView = new WebView(mActivity);
                webView.getSettings().setJavaScriptEnabled(true);
                webView.getSettings().setLoadsImagesAutomatically(true);
                webView.setBackgroundColor(ContextCompat.getColor(mActivity, android.R.color.darker_gray));
                webView.setWebViewClient(new WebViewClient()
                {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url)
                    {
                        if (url.startsWith("weixin://"))
                        {
                            Uri wechatUri = Uri.parse(url);
                            Intent wechatIntent = new Intent(Intent.ACTION_VIEW, wechatUri);
                            PackageManager packageManager = mActivity.getPackageManager();
                            List<ResolveInfo> activities = packageManager.queryIntentActivities(wechatIntent, 0);
                            boolean isIntentSafe = activities.size() > 0;
                            if (isIntentSafe)
                            {
                                mActivity.startActivity(wechatIntent);
                            }

                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable(){
                                @Override
                                public void run()
                                {
                                    viewGroup.removeView(webView);
                                }
                            }, 5000);

                            return true;
                        }
                        return false;
                    }

                    @Override
                    public void onLoadResource(WebView view, String url)
                    {
                        super.onLoadResource(view, url);
                    }

                    @Override
                    public void onPageStarted(WebView view, String url, Bitmap favicon)
                    {
                        super.onPageStarted(view, url, favicon);
                    }
                });
                viewGroup.addView(webView, 1);
                webView.loadUrl(orderResult.mRedirectUrl);
            }
            else
            {
                //alipay
            }
        }
        else
        {
            mOrderListener.gotOrderResult(null);
        }
    }

    public void inquiredOrder(CPayInquireResult inquireResult)
    {
        mInquireListener.gotInquireResult(inquireResult);
    }

    public void onResume()
    {
        if(mOrderResult != null)
        {
            mOrderListener.gotOrderResult(mOrderResult);
        }
    }
}
