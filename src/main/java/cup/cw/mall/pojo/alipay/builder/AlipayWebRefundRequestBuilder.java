package cup.cw.mall.pojo.alipay.builder;

import com.alibaba.fastjson.annotation.JSONField;
import cup.cw.mall.pojo.alipay.GoodsDetail;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * created by cuiwei on 2018/12/6
 */
public class AlipayWebRefundRequestBuilder extends AbsRequestBuilder {

    private AlipayWebRefundRequestBuilder.BizContent bizContent = new AlipayWebRefundRequestBuilder.BizContent();

    @Override
    public boolean validate() {
        if (StringUtils.isEmpty(bizContent.refundAmount)){
            throw new NullPointerException("refund_amount is NULL!!!");
        }
        if (StringUtils.isEmpty(bizContent.outTradeNo) && StringUtils.isEmpty(bizContent.tradeNo)){
            throw new NullPointerException("out_trade_no and trade_no are both NULL!!");
        }
        return true;
    }

    @Override
    public Object getBizContent() {
        return this.bizContent;
    }

    public AlipayWebRefundRequestBuilder setOutTradeNo(String outTradeNo) {
        this.bizContent.outTradeNo = outTradeNo;
        return this;
    }

    public AlipayWebRefundRequestBuilder setTradeNo(String tradeNo) {
        this.bizContent.tradeNo = tradeNo;
        return this;
    }

    public AlipayWebRefundRequestBuilder setRefundAmount(String refundAmount) {
        this.bizContent.refundAmount = refundAmount;
        return this;
    }

    public AlipayWebRefundRequestBuilder setRefundCurrency(String refundCurrency) {
        this.bizContent.refundCurrency = refundCurrency;
        return this;
    }

    public AlipayWebRefundRequestBuilder setRefundReason(String refundReason) {
        this.bizContent.refundReason = refundReason;
        return this;
    }

    public AlipayWebRefundRequestBuilder setOutRequestNo(String outRequestNo) {
        this.bizContent.outRequestNo = outRequestNo;
        return this;
    }

    public AlipayWebRefundRequestBuilder setGoodsDetailList(List<GoodsDetail> goodsDetailList) {
        this.bizContent.goodsDetailList = goodsDetailList;
        return this;
    }

    @Data
    private static class BizContent{
        @JSONField(name = "out_trade_no")
        private String outTradeNo;
        @JSONField(name = "trade_no")
        private String tradeNo;
        @JSONField(name = "refund_amount")
        private String refundAmount;
        @JSONField(name = "refund_currency")
        private String refundCurrency;
        @JSONField(name = "refund_reason")
        private String refundReason;
        @JSONField(name = "out_request_no")
        private String outRequestNo;
        @JSONField(name = "goods_detail")
        private List<GoodsDetail> goodsDetailList;

        @Override
        public String toString() {
            return "BizContent{" +
                    "outTradeNo='" + outTradeNo + '\'' +
                    ", tradeNo='" + tradeNo + '\'' +
                    ", refundAmount='" + refundAmount + '\'' +
                    ", refundCurrency='" + refundCurrency + '\'' +
                    ", refundReason='" + refundReason + '\'' +
                    ", outRequestNo='" + outRequestNo + '\'' +
                    ", goodsDetailList=" + goodsDetailList +
                    '}';
        }

    }


}
