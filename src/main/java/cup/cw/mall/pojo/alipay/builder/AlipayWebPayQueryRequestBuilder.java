package cup.cw.mall.pojo.alipay.builder;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * created by cuiwei on 2018/12/8
 */
public class AlipayWebPayQueryRequestBuilder extends AbsRequestBuilder {

    private AlipayWebPayQueryRequestBuilder.BizContent bizContent =
            new AlipayWebPayQueryRequestBuilder.BizContent();

    @Override
    public boolean validate() {
        if (StringUtils.isEmpty(bizContent.outTradeNo) && StringUtils.isEmpty(bizContent.tradeNo)){
            throw new NullPointerException("out_trade_no and trade_no are both NULL!!");
        }
        return true;
    }

    public AlipayWebPayQueryRequestBuilder setTradeNo(String tradeNo) {
        this.bizContent.tradeNo = tradeNo;
        return this;
    }

    public AlipayWebPayQueryRequestBuilder setOutTradeNo(String outTradeNo) {
        this.bizContent.outTradeNo = outTradeNo;
        return this;
    }

    @Override
    public Object getBizContent() {
        return bizContent;
    }

    @Data
    private static class BizContent {
        @JSONField(name = "trade_no")
        private String tradeNo;
        @JSONField(name = "out_trade_no")
        private String outTradeNo;

        @Override
        public String toString() {
            return "BizContent{" +
                    "tradeNo='" + tradeNo + '\'' +
                    ", outTradeNo='" + outTradeNo + '\'' +
                    '}';
        }
    }

}
