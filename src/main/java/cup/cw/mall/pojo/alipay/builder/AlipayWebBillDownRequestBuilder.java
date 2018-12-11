package cup.cw.mall.pojo.alipay.builder;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * created by cuiwei on 2018/12/8
 */
public class AlipayWebBillDownRequestBuilder extends AbsRequestBuilder {

    private AlipayWebBillDownRequestBuilder.BizContent bizContent =
            new AlipayWebBillDownRequestBuilder.BizContent();

    @Override
    public boolean validate() {
        if (StringUtils.isEmpty(this.bizContent.billType)){
            throw  new NullPointerException("bill_type is Null!!");
        }
        if (StringUtils.isEmpty(this.bizContent.billDate)){
            throw  new NullPointerException("bill_date is Null!!");
        }
        return true;
    }

    @Override
    public Object getBizContent() {
        return bizContent;
    }

    public AlipayWebBillDownRequestBuilder setBillDate(String billDate) {
        this.bizContent.billDate = billDate;
        return this;
    }

    public AlipayWebBillDownRequestBuilder setBillType(String billType) {
        this.bizContent.billType = billType;
        return this;
    }

    @Data
    private static class BizContent{
        @JSONField(name = "bill_type")
        private String billType;
        @JSONField(name = "bill_date")
        private String billDate;

        @Override
        public String toString() {
            return "BizContent{" +
                    "billType='" + billType + '\'' +
                    ", billDate='" + billDate + '\'' +
                    '}';
        }

    }
}
