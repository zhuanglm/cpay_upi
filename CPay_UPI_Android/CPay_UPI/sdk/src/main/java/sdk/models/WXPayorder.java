package sdk.models;

public class WXPayorder {
    public String appid;
    public String partnerid;
    public String prepayid;
    public String noncestr;
    public String timestamp;
    public String sign;
    public String mPackage;
    public String extData; // Android only equal Citcon order_id
    public WXPayorder()
    {

    }
}
