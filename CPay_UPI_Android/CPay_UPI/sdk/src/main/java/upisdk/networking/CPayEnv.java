package upisdk.networking;

import upisdk.CPayEntryType;
import upisdk.CPayMode;
import upisdk.CPayUPISDK;

/**
 * Created by citcon.
 */

public class CPayEnv {

    // NON-CNY
    public static final String USD = "USD";
    public static final String CNY = "CNY";

    // api type
    public static final String UPI_ORDER_PATH = "v1/charges";
    public static final String ORDER_PATH = "v1/charges";
    public static final String AMS_ORDER_PATH = "v1/charges";
    public static final String INQUIRE_PATH = "v1/transactions/";

    private static final String URL_DEV = "https://api.dev01.citconpay.com/";
    private static final String URL_UAT = "https://uat.citconpay.com/";
    private static final String URL_PROD = "https://citconpay.com/";
    private static final String URL_QA = "https://api.qa01.citconpay.com/";

    // CNY
    private static final String URL_RMB_DEV = "https://dev.citconpay.cn/";
    private static final String URL_RMB_UAT = "https://uat.citconpay.cn/";
    private static final String URL_RMB_PROD = "https://citconpay.cn/";

    //CN pay acceleration endpoints
    private static final String URL_CN_PROD = "https://api.citconpay.cn/";
    private static final String URL_CN_UAT = "https://api-uat.citconpay.cn/";
    private static final String URL_CN_DEV = "https://api-dev.citconpay.cn/";

    public static String getEntryPoint(String currency, String vendor, CPayEntryType cType, boolean isCNAcceleration) {
        String baseURL = isCNAcceleration ? getCNPayURL(currency, vendor) : getBaseURL(currency, vendor);
        if(baseURL == null){
            return null;
        }
        return baseURL + getEntryPath(vendor, cType);

    }

    public static String getEntryPoint(String vendor, String transaction) {
        String baseURL = getBaseURL("USD", vendor);
        if(baseURL == null){
            return null;
        }
        return baseURL + getEntryPath(vendor, CPayEntryType.INQUIRE) + transaction;
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
                    return UPI_ORDER_PATH;
                default:
                    return ORDER_PATH;
            }
        } else {
            return INQUIRE_PATH;
        }

    }

    public static String getBaseURL(String currency, String vendor) {
        CPayMode env = CPayUPISDK.getMode();
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
                            case QA:
                                return URL_QA;
                        }
                    case USD:
                    default:
                        switch (env) {
                            case DEV:
                                return URL_DEV;
                            case UAT:
                                return URL_UAT;
                            case PROD:
                                return URL_PROD;
                            case QA:
                                return URL_QA;
                        }
                }
            default:
                switch (env) {
                    case QA:
                        return URL_QA;
                    case DEV:
                        return URL_DEV;
                    case UAT:
                        return URL_UAT;
                    case PROD:
                        return URL_PROD;
                }
        }

        return null;
    }

    public static String getCNPayURL(String currency, String vendor) {
        CPayMode env = CPayUPISDK.getMode();
        switch (vendor) {
            case "alipay":
            case "wechatpay":
                switch (currency) {
                    case CNY:
                        switch (env) {
                            case DEV:
                                return URL_RMB_DEV;
                            case QA:
                                return URL_QA;
                            case UAT:
                                return URL_RMB_UAT;
                            case PROD:
                                return URL_RMB_PROD;
                        }
                    case USD:
                    default:
                        switch (env) {
                            case DEV:
                                return URL_CN_DEV;
                            case QA:
                                return URL_QA;
                            case UAT:
                                return URL_CN_UAT;
                            case PROD:
                                return URL_CN_PROD;
                        }
                }

            case "upop":
                switch (env) {
                    case DEV:
                        return URL_CN_DEV;
                    case QA:
                    case UAT:
                        return URL_CN_UAT;
                    case PROD:
                        return URL_CN_PROD;
                }

            default:
                switch (env) {
                    case QA:
                        return URL_QA;
                    case DEV:
                        return URL_DEV;
                    case UAT:
                        return URL_UAT;
                    case PROD:
                        return URL_PROD;
                }
        }

        return null;
    }
}
