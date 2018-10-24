package sdk.models;

/**
 * Created by alexandrudiaconu on 7/22/17.
 */

public class CPayOrderResult
{
    public String mRedirectUrl;
    public String mOrderId;
    public CPayOrder mOrder;
    public String mSignedString;
    public String mOrderSpec;
    public String mStatus = "4000";
    public String mMessage = "unknown";
    public String mCurrency = "USD";
    public CPayOrderResult()
    {

    }
}
