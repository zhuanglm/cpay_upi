package upisdk.models;

/**
 * Created by alexandrudiaconu on 7/22/17.
 */

public class CPayUPIOrderResult
{
    public String mRedirectUrl;
    public String mOrderId;
    public CPayUPIOrder mOrder;
    public String mSignedString;
    public String mOrderSpec;
    public String mStatus = "4000";
    public String mGateway;
    public String mMessage = "unknown";
    public String mCurrency = "USD";
    public String mTransCurrency;
    public CPayUPIOrderResult()
    {

    }
}
