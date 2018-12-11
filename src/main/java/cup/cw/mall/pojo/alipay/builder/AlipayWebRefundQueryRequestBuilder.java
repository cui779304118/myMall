package cup.cw.mall.pojo.alipay.builder;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * created by cuiwei on 2018/12/7
 */
public class AlipayWebRefundQueryRequestBuilder extends AbsRequestBuilder {

    private AlipayWebRefundQueryRequestBuilder.BizContent bizContent =
            new AlipayWebRefundQueryRequestBuilder.BizContent();

    @Override
    public boolean validate() {
        if (StringUtils.isEmpty(bizContent.outRequestNo)){
            throw new NullPointerException("out_request_no is NULL!!!");
        }
        if (StringUtils.isEmpty(bizContent.outTradeNo) && StringUtils.isEmpty(bizContent.tradeNo)){
            throw new NullPointerException("out_trade_no and trade_no are both NULL!!");
        }
        return true;
    }

    @Override
    public Object getBizContent() {
        return bizContent;
    }


    public AlipayWebRefundQueryRequestBuilder setTradeNo(String tradeNo) {
        this.bizContent.tradeNo = tradeNo;
        return this;
    }

    public AlipayWebRefundQueryRequestBuilder setOutTradeNo(String outTradeNo) {
        this.bizContent.outTradeNo = outTradeNo;
        return this;
    }

    public AlipayWebRefundQueryRequestBuilder setOutRequestNo(String outRequestNo) {
        this.bizContent.outRequestNo = outRequestNo;
        return this;
    }

    @Data
    private static class BizContent{
        @JSONField(name = "trade_no")
        private String tradeNo;
        @JSONField(name = "out_trade_no")
        private String outTradeNo;
        @JSONField(name = "out_request_no")
        private String outRequestNo;

        @Override
        public String toString() {
            return "BizContent{" +
                    "tradeNo='" + tradeNo + '\'' +
                    ", outTradeNo='" + outTradeNo + '\'' +
                    ", outRequestNo='" + outRequestNo + '\'' +
                    '}';
        }

    }


}
