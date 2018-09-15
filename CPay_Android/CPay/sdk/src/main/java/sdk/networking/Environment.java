package sdk.networking;

/**
 * Created by alexandrudiaconu on 7/22/17.
 */

public abstract class Environment
{


    private static final String URL_ROOT = "https://uat.citconpay.com/";
//    public static final String URL_PAY = URL_ROOT + "payment/pay_mobile2";

    public static final String URL_PAY = "http://uat.citconpay.com/payment/pay_app";
    public static final String URL_INQUIRE = URL_ROOT + "payment/inquire.php";
    public static final String URL_INQUIRE_WX = URL_ROOT + "payment/inquire";

    public static final String URL_PAY_CN = "http://cny.citcon-mkt.com/payment/pay_app";
    public static final String URL_INQUIRE_WX_CN = "http://cny.citcon-mkt.com/payment/inquire";

}
