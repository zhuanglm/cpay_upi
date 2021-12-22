package sdk.models;

public enum CPayMethodType {
    PayTypeAlipay("alipay"),       // Alipay CN
    PayTypeWechatSDK("wechatpay"),    // Wechat Pay
    PayTypeWechatH5("wechatpay_h5"),     // Wechat H5
    PayTypeAlipayHK("alipay_hk"),     // Alipay HK
    PayTypeKakao("kakaopay"),        // Kakao Pay
    PayTypeUnion("upop"),        // Union Pay
    PayTypeDana("dana"),         // Dana Pay
    PayTypeGcash("gcash"),        // Gcash Pay
    PayTypeTruemoney("truemoney"),    // Truemoney Pay
    PayTypeJkopay("jkopay"),       // Jkopay
    PayTypeEasypaisa("easypaisa"),    // Easypaisa
    PayTypeBkash("bkash"),        // Bkash
    PayTypeCC("cc"),           // CC Pay
    PayTypeTouchnGo("touchngo"),     // Touch'N Go
    PayTypeUnknown("unknown");       // Unknown Payment Method

    private final String mMethodType;
    CPayMethodType(String type) {
        mMethodType = type;
    }

    public String getType() {
        return mMethodType;
    }
}
