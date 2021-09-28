package sdk.networking;

import sdk.CPayEntryType;
import sdk.CPayMode;
import sdk.CPaySDK;

/**
 * Created by citcon.
 */

public class CPayEnv {

    // NON-CNY
    public static final String USD = "USD";
    public static final String CNY = "CNY";

    // api type
    public static final String ORDER_PATH = "payment/pay_app";
    public static final String AMS_ORDER_PATH = "payment/pay_app";
    public static final String INQUIRE_PATH = "payment/inquire";

    private static final String URL_USD_DEV = "https://dev.citconpay.com/";
    private static final String URL_USD_UAT = "https://uat.citconpay.com/";
    private static final String URL_USD_PROD = "https://citconpay.com/";

    // CNY
    private static final String URL_RMB_DEV = "https://dev.citconpay.cn/";
    private static final String URL_RMB_UAT = "https://uat.citconpay.cn/";
    private static final String URL_RMB_PROD = "https://citconpay.cn/";

    // AMS
    private static final String URL_AMS_DEV = "https://uat.citconpay.com/";
    private static final String URL_AMS_UAT = "https://uat.citconpay.com/";
    private static final String URL_AMS_PROD = "https://uat.citconpay.com/";

    // UNIONPAY
    private static final String URL_UNIONPAY_DEV = "https://uat.citconpay.com/";
    private static final String URL_UNIONPAY_UAT = "https://uat.citconpay.com/";
    private static final String URL_UNIONPAY_PROD = "https://uat.citconpay.com/";

    public static String getEntryPoint(String currency, String vendor, CPayEntryType cType) {
        String baseURL = getBaseURL(currency, vendor);
        if(baseURL == null){
            return null;
        }
        return baseURL + getEntryPath(vendor, cType);

    }

    private static String getEntryPath(String vendor, CPayEntryType cType) {
        if (cType == CPayEntryType.ORDER) {
            if (vendor.equals("gcash") || vendor.equals("dana") || vendor.equals("alipay_hk") || vendor.equals("kakaopay")) {
                return AMS_ORDER_PATH;
            }else if (vendor.equals("upop")) {
                return AMS_ORDER_PATH;
            } else {
                return ORDER_PATH;
            }
        } else {
            return INQUIRE_PATH;
        }

    }

    public static String getBaseURL(String currency, String vendor) {
        CPayMode env = CPaySDK.getMode();
        switch (vendor) {
            case "alipay":
            case "wechatpay":
                switch (currency) {
                    case CNY:
                        switch (env) {
                            case DEV:
                                return URL_RMB_DEV;
                            case UAT:
                                return URL_RMB_UAT;
                            case PROD:
                                return URL_RMB_PROD;
                        }
                    case USD:
                    default:
                        switch (env) {
                            case DEV:
                                return URL_USD_DEV;
                            case UAT:
                                return URL_USD_UAT;
                            case PROD:
                                return URL_USD_PROD;
                        }
                }
            case "gcash":
            case "dana":
            case "alipay_hk":
            case "kakaopay":
                switch (env) {
                    case DEV:
                        return URL_AMS_DEV;
                    case UAT:
                        return URL_AMS_UAT;
                    case PROD:
                        return URL_AMS_PROD;
                }
            case "upop":
                switch (env) {
                    case DEV:
                        return URL_UNIONPAY_DEV;
                    case UAT:
                        return URL_UNIONPAY_UAT;
                    case PROD:
                        return URL_UNIONPAY_PROD;
                }
        }

        return null;
    }
}
