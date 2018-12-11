package cup.cw.mall.common;

/**
 * created by cuiwei on 2018/11/30
 */
public class AlipayConst {

    public enum TradeStatus{
        SUCCESS,
        FAILED,
        UNKNOWN;

        TradeStatus(){}
    }

    public interface Params{
        String NOTIFY_URL = "alipay.callback.notify_url";
        String RETURN_URL = "alipay.callback.return_url";
        String PRODUCT_CODE = "FAST_INSTANT_TRADE_PAY";
    }

    public interface BillDownload{
        String BILL_TYPE_TRADE = "trade";
        String BILL_TYPE_SIGNCUSTOMER = "signcustomer";
        String BILL_DATE_FORMAT_DATE = "yyyy-MM-dd";
        String BILL_DATE_FORMAT_MONTH = "yyyy-MM";
        String BILL_DOWNLOAD_URL_KEY = "bill_download_url";
    }


}
