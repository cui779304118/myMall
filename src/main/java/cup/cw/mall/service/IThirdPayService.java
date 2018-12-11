package cup.cw.mall.service;

import cup.cw.mall.pojo.alipay.builder.*;
import cup.cw.mall.pojo.alipay.result.AlipayWebResult;

/**
 * 第三方支付服务
 * created by cuiwei on 2018/11/30
 */
public interface IThirdPayService {

    /**
     * 支付
     * @return
     */
    AlipayWebResult pay(AlipayWebPayRequestBuilder alipayWebPayRequestBuilder);

    /**
     * 支付结果查询
     * @param alipayWebPayQueryRequestBuilder
     * @return
     */
    AlipayWebResult payQuery(AlipayWebPayQueryRequestBuilder alipayWebPayQueryRequestBuilder);

    /**
     * 退款
     * @param alipayWebRefundRequestBuilder
     * @return
     */
    AlipayWebResult refund(AlipayWebRefundRequestBuilder alipayWebRefundRequestBuilder);


    /**
     * 查询退款信息
     * @param alipayWebRefundQueryRequestBuilder
     * @return
     */
    AlipayWebResult refundQuery(AlipayWebRefundQueryRequestBuilder alipayWebRefundQueryRequestBuilder);

    /**
     * 对账账单下载
     * @param alipayWebBillDownRequestBuilder
     * @return
     */
    AlipayWebResult downloadBill(AlipayWebBillDownRequestBuilder alipayWebBillDownRequestBuilder);

}
