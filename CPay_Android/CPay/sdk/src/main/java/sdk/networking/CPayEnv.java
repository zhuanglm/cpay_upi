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
    public static final String KCP_ORDER_PATH = "payment/pay";
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
    private static final String URL_AMS_DEV = "https://dev.citconpay.com/";
    private static final String URL_AMS_UAT = "https://uat.citconpay.com/";
    private static final String URL_AMS_PROD = "https://citconpay.com/";

    // UNIONPAY
    private static final String URL_UNIONPAY_DEV = "https://dev.citconpay.com/";
    private static final String URL_UNIONPAY_UAT = "https://uat.citconpay.com/";
    private static final String URL_UNIONPAY_PROD = "https://citconpay.com/";

    // KCP
    private static final String URL_KCP_DEV = "https://dev.citconpay.com/";
    private static final String URL_KCP_QA = "https://qa.qa01.citconpay.com/";
    private static final String URL_KCP_UAT = "https://uat.citconpay.com/";
    private static final String URL_KCP_PROD = "https://citconpay.com/";

    public static String getEntryPoint(String currency, String vendor, CPayEntryType cType) {
        String baseURL = getBaseURL(currency, vendor);
        if(baseURL == null){
            return null;
        }
        return baseURL + getEntryPath(vendor, cType);

    }

    private static String getEntryPath(String vendor, CPayEntryType cType) {
        if (cType == CPayEntryType.ORDER) {
            switch (vendor) {
                case "gcash":
                case "dana":
                case "alipay_hk":
                case "kakaopay":
                    return AMS_ORDER_PATH;
                case "upop":
                    return AMS_ORDER_PATH;
                case "card":
                case "payco":
                case "naverpay":
                case "banktransfer":
                case "linepay" :
                case "paypay" :
                case "rakutenpay" :
                    return KCP_ORDER_PATH;
                default:
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

                //kcp test
            case "card":
            case "payco":
            case "naverpay":
            case "banktransfer":
            case "linepay" :
            case "paypay" :
            case "rakutenpay" :
                switch (env) {
                    case QA:
                        return URL_KCP_QA;
                    case DEV:
                        return URL_KCP_DEV;
                    case UAT:
                        return URL_KCP_UAT;
                    case PROD:
                        return URL_KCP_PROD;
                }
        }

        return null;
    }
}
