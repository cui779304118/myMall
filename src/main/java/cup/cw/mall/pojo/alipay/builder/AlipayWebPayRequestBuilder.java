package cup.cw.mall.pojo.alipay.builder;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.gson.annotations.SerializedName;
import cup.cw.mall.pojo.alipay.ExtendParams;
import cup.cw.mall.pojo.alipay.GoodsDetail;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * created by cuiwei on 2018/11/30
 */
public class AlipayWebPayRequestBuilder extends AbsRequestBuilder {
    private String notifyUrl;
    private String returnUrl;
    private AlipayWebPayRequestBuilder.BizContent bizContent = new AlipayWebPayRequestBuilder.BizContent();

    @Override
    public boolean validate() {
        if (StringUtils.isEmpty(notifyUrl)){
            throw new NullPointerException("notify_url should not be NULL!");
        }
        if (StringUtils.isEmpty(returnUrl)){
            throw new NullPointerException("return_url should not be NULL!");
        }
       if (StringUtils.isEmpty(this.bizContent.outTradeNo)){
           throw new NullPointerException("out_trace_no should not be NULL!");
       }
        if (StringUtils.isEmpty(this.bizContent.productCode)){
            throw new NullPointerException("product_code should not be NULL!");
        }
        if (StringUtils.isEmpty(this.bizContent.totalAmount)){
            throw new NullPointerException("total_amount should not be NULL!");
        }
        if (StringUtils.isEmpty(this.bizContent.subject)){
            throw new NullPointerException("subject should not be NULL!");
        }
        return true;
    }

    @Override
    public Object getBizContent() {
        return this.bizContent;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public AlipayWebPayRequestBuilder setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
        return this;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public AlipayWebPayRequestBuilder setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
        return this;
    }

    public AlipayWebPayRequestBuilder setOutTraceNo(String outTraceNo) {
        this.bizContent.outTradeNo = outTraceNo;
        return this;
    }

    public AlipayWebPayRequestBuilder setProductCode(String productCode) {
        this.bizContent.productCode = productCode;
        return this;
    }

    public AlipayWebPayRequestBuilder setTotalAmount(String totalAmount) {
        this.bizContent.totalAmount = totalAmount;
        return this;
    }

    public AlipayWebPayRequestBuilder setSubject(String subject) {
        this.bizContent.subject = subject;
        return this;
    }

    public AlipayWebPayRequestBuilder setBody(String body) {
        this.bizContent.body = body;
        return this;
    }

    public AlipayWebPayRequestBuilder setGoodsDetail(List<GoodsDetail> goodsDetailList) {
        this.bizContent.goodsDetailList = goodsDetailList;
        return this;
    }

    public AlipayWebPayRequestBuilder setPassbackParams(String passbackParams) {
        this.bizContent.passbackParams = passbackParams;
        return this;
    }

    public AlipayWebPayRequestBuilder setExtendParams(ExtendParams extendParams) {
        this.bizContent.extendParams = extendParams;
        return this;
    }

    public AlipayWebPayRequestBuilder setTimeoutExpress(String timeoutExpress) {
        this.bizContent.timeoutExpress = timeoutExpress;
        return this;
    }

    public AlipayWebPayRequestBuilder setEnablePayChannels(String enablePayChannels) {
        this.bizContent.enablePayChannels = enablePayChannels;
        return this;
    }

    public AlipayWebPayRequestBuilder setDisablePayChannels(String disablePayChannels) {
        this.bizContent.disablePayChannels = disablePayChannels;
        return this;
    }

    public AlipayWebPayRequestBuilder setAutoToken(String autoToken) {
        this.bizContent.autoToken = autoToken;
        return this;
    }

    public AlipayWebPayRequestBuilder setQrPayMode(String qrPayMode) {
        this.bizContent.qrPayMode = qrPayMode;
        return this;
    }

    public AlipayWebPayRequestBuilder setQrcodeWidth(String qrcodeWidth) {
        this.bizContent.qrcodeWidth = qrcodeWidth;
        return this;
    }

    @Data
    private static class BizContent{
        @JSONField(name = "out_trade_no")
        private String outTradeNo;
        @JSONField(name = "product_code")
        private String productCode;
        @JSONField(name = "total_amount")
        private String totalAmount;
        @JSONField(name = "subject")
        private String subject;
        @JSONField(name = "body")
        private String body;
        @JSONField(name = "goods_detail")
        private List<GoodsDetail> goodsDetailList;
        @JSONField(name = "passback_params")
        private String passbackParams;
        @SerializedName("extend_params")
        private ExtendParams extendParams;
        @JSONField(name = "timeout_express")
        private String timeoutExpress;
        @JSONField(name = "enable_pay_channels")
        private String enablePayChannels;
        @JSONField(name = "disable_pay_channels")
        private String disablePayChannels;
        @JSONField(name = "auto_token")
        private String autoToken;
        @JSONField(name = "qr_pay_mode")
        private String qrPayMode;
        @JSONField(name = "qrcode_width")
        private String qrcodeWidth;

        @Override
        public String toString() {
            return "BizContent{" +
                    "outTradeNo='" + outTradeNo + '\'' +
                    ", productCode='" + productCode + '\'' +
                    ", totalAmount='" + totalAmount + '\'' +
                    ", subject='" + subject + '\'' +
                    ", body='" + body + '\'' +
                    ", goodsDetailList=" + goodsDetailList +
                    ", passbackParams='" + passbackParams + '\'' +
                    ", extendParams=" + extendParams +
                    ", timeoutExpress='" + timeoutExpress + '\'' +
                    ", enablePayChannels='" + enablePayChannels + '\'' +
                    ", disablePayChannels='" + disablePayChannels + '\'' +
                    ", autoToken='" + autoToken + '\'' +
                    ", qrPayMode='" + qrPayMode + '\'' +
                    ", qrcodeWidth='" + qrcodeWidth + '\'' +
                    '}';
        }
    }
}
