package cup.cw.mall.service.impl;

import com.alipay.api.*;
import com.alipay.api.request.*;
import com.alipay.api.response.*;
import com.alipay.demo.trade.config.Configs;
import cup.cw.mall.common.AlipayConst;
import cup.cw.mall.pojo.alipay.builder.*;
import cup.cw.mall.pojo.alipay.result.AlipayWebResult;
import cup.cw.mall.service.IThirdPayService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * created by cuiwei on 2018/11/30
 */
@Service("alipayService")
public class AlipayServiceImpl implements IThirdPayService {

    private static Logger logger = LoggerFactory.getLogger(AlipayServiceImpl.class);

    private AlipayClient client;

    public AlipayServiceImpl() {
        this.client = new ClientBuilder().build();
    }

    private void checkRequestBuilder(AbsRequestBuilder builder) {
        if (builder == null) {
            throw new NullPointerException("requestBuilder is  NULL!!!");
        }
        if (!builder.validate()) {
            throw new IllegalStateException("requestBuilder check failed,builder = " + builder.toString());
        }
    }

    @Override
    public AlipayWebResult pay(AlipayWebPayRequestBuilder alipayWebPayRequestBuilder) {
        this.checkRequestBuilder(alipayWebPayRequestBuilder);
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setReturnUrl(alipayWebPayRequestBuilder.getReturnUrl());
        request.setNotifyUrl(alipayWebPayRequestBuilder.getNotifyUrl());
        request.setBizContent(alipayWebPayRequestBuilder.toJsonString());
        logger.info("调用支付宝支付接口，bizContent:{}", request.getBizContent());

        AlipayWebResult alipayWebResult = new AlipayWebResult();
        AlipayTradePagePayResponse response = null;
        try {
            response = client.pageExecute(request);
        } catch (AlipayApiException e) {
            logger.error("调用支付宝接口错误", e);
            alipayWebResult.setTradeStatus(AlipayConst.TradeStatus.FAILED);
            return alipayWebResult;
        }
        alipayWebResult.setResponse(response);
        if (response != null && response.isSuccess()) {
            alipayWebResult.setTradeStatus(AlipayConst.TradeStatus.SUCCESS);
        } else {
            alipayWebResult.setTradeStatus(AlipayConst.TradeStatus.FAILED);
        }
        return alipayWebResult;
    }

    @Override
    public AlipayWebResult refund(AlipayWebRefundRequestBuilder alipayWebRefundRequestBuilder) {
        this.checkRequestBuilder(alipayWebRefundRequestBuilder);
        AlipayTradeRefundRequest refundRequest = new AlipayTradeRefundRequest();
        refundRequest.setBizContent(alipayWebRefundRequestBuilder.toJsonString());
        logger.info("调用支付宝退款接口，bizContent:{}", alipayWebRefundRequestBuilder.toJsonString());
        AlipayWebResult alipayWebResult = new AlipayWebResult();
        AlipayTradeRefundResponse refundResponse;
        try {
            refundResponse = client.execute(refundRequest);
        } catch (AlipayApiException e) {
            logger.error("调用支付宝退款接口失败，", e);
            alipayWebResult.setTradeStatus(AlipayConst.TradeStatus.FAILED);
            return alipayWebResult;
        }
        alipayWebResult.setResponse(refundResponse);
        if (refundResponse != null && refundResponse.isSuccess()) {
            alipayWebResult.setTradeStatus(AlipayConst.TradeStatus.SUCCESS);
        } else {
            alipayWebResult.setTradeStatus(AlipayConst.TradeStatus.FAILED);
        }
        return alipayWebResult;
    }

    @Override
    public AlipayWebResult refundQuery(AlipayWebRefundQueryRequestBuilder alipayWebRefundQueryRequestBuilder) {
        this.checkRequestBuilder(alipayWebRefundQueryRequestBuilder);
        AlipayTradeFastpayRefundQueryRequest refundQueryRequest = new AlipayTradeFastpayRefundQueryRequest();
        refundQueryRequest.setBizContent(alipayWebRefundQueryRequestBuilder.toJsonString());
        logger.info("调用支付宝退款查询接口，bizContent:{}", alipayWebRefundQueryRequestBuilder.toJsonString());
        AlipayWebResult alipayWebResult = new AlipayWebResult();
        AlipayTradeFastpayRefundQueryResponse refundQueryResponse = null;
        try {
            refundQueryResponse = client.execute(refundQueryRequest);
        } catch (AlipayApiException e) {
            logger.error("调用支付宝退款查询接口失败，", e);
            alipayWebResult.setTradeStatus(AlipayConst.TradeStatus.FAILED);
            return alipayWebResult;
        }
        alipayWebResult.setResponse(refundQueryResponse);
        if (refundQueryResponse != null && refundQueryResponse.isSuccess()) {
            alipayWebResult.setTradeStatus(AlipayConst.TradeStatus.SUCCESS);
        }else {
            alipayWebResult.setTradeStatus(AlipayConst.TradeStatus.FAILED);
        }
        return alipayWebResult;
    }

    @Override
    public AlipayWebResult payQuery(AlipayWebPayQueryRequestBuilder alipayWebPayQueryRequestBuilder) {
        this.checkRequestBuilder(alipayWebPayQueryRequestBuilder);
        AlipayTradeQueryRequest queryRequest = new AlipayTradeQueryRequest();
        queryRequest.setBizContent(alipayWebPayQueryRequestBuilder.toJsonString());
        logger.info("调用支付宝付款查询接口，bizContent:{}", alipayWebPayQueryRequestBuilder.toJsonString());
        AlipayWebResult alipayWebResult = new AlipayWebResult();
        AlipayTradeQueryResponse queryResponse = null;
        try {
            queryResponse = client.execute(queryRequest);
        } catch (AlipayApiException e) {
            logger.error("调用支付宝付款查询接口失败，", e);
            alipayWebResult.setTradeStatus(AlipayConst.TradeStatus.FAILED);
            return alipayWebResult;
        }
        alipayWebResult.setResponse(queryResponse);
        if (queryResponse != null && queryResponse.isSuccess()) {
            alipayWebResult.setTradeStatus(AlipayConst.TradeStatus.SUCCESS);
        }else {
            alipayWebResult.setTradeStatus(AlipayConst.TradeStatus.FAILED);
        }
        return alipayWebResult;
    }

    @Override
    public AlipayWebResult downloadBill(AlipayWebBillDownRequestBuilder alipayWebBillDownRequestBuilder) {
        this.checkRequestBuilder(alipayWebBillDownRequestBuilder);
        AlipayDataDataserviceBillDownloadurlQueryRequest billDownloadurlQueryRequest =
                new AlipayDataDataserviceBillDownloadurlQueryRequest();
        billDownloadurlQueryRequest.setBizContent(alipayWebBillDownRequestBuilder.toJsonString());
        logger.info("调用支付宝对账下载接口，bizContent:{}", alipayWebBillDownRequestBuilder.toJsonString());
        AlipayWebResult alipayWebResult = new AlipayWebResult();
        AlipayDataDataserviceBillDownloadurlQueryResponse billDownloadurlQueryResponse = null;
        try {
            billDownloadurlQueryResponse = client.execute(billDownloadurlQueryRequest);
        } catch (AlipayApiException e) {
            logger.error("调用支付宝对账下载接口失败，", e);
            alipayWebResult.setTradeStatus(AlipayConst.TradeStatus.FAILED);
            return alipayWebResult;
        }
        alipayWebResult.setResponse(billDownloadurlQueryResponse);
        if (billDownloadurlQueryResponse != null && billDownloadurlQueryResponse.isSuccess()) {
            alipayWebResult.setTradeStatus(AlipayConst.TradeStatus.SUCCESS);
        }else {
            alipayWebResult.setTradeStatus(AlipayConst.TradeStatus.FAILED);
        }
        return alipayWebResult;
    }

    public static class ClientBuilder {
        private String gatewayUrl;
        private String appId;
        private String privateKey;
        private String format;
        private String charset;
        private String alipayPublicKey;
        private String signType;

        public ClientBuilder() {
        }

        public AlipayClient build() {
            if (StringUtils.isEmpty(this.gatewayUrl)) {
                this.gatewayUrl = Configs.getOpenApiDomain();
            }

            if (StringUtils.isEmpty(this.appId)) {
                this.appId = Configs.getAppid();
            }

            if (StringUtils.isEmpty(this.privateKey)) {
                this.privateKey = Configs.getPrivateKey();
            }

            if (StringUtils.isEmpty(this.format)) {
                this.format = "json";
            }

            if (StringUtils.isEmpty(this.charset)) {
                this.charset = "utf-8";
            }

            if (StringUtils.isEmpty(this.alipayPublicKey)) {
                this.alipayPublicKey = Configs.getAlipayPublicKey();
            }

            if (StringUtils.isEmpty(this.signType)) {
                this.signType = Configs.getSignType();
            }

            return new DefaultAlipayClient(gatewayUrl, appId, privateKey, format, charset, alipayPublicKey, signType);
        }

        public AlipayServiceImpl.ClientBuilder setAlipayPublicKey(String alipayPublicKey) {
            this.alipayPublicKey = alipayPublicKey;
            return this;
        }

        public AlipayServiceImpl.ClientBuilder setAppId(String appId) {
            this.appId = appId;
            return this;
        }

        public AlipayServiceImpl.ClientBuilder setCharset(String charset) {
            this.charset = charset;
            return this;
        }

        public AlipayServiceImpl.ClientBuilder setFormat(String format) {
            this.format = format;
            return this;
        }

        public AlipayServiceImpl.ClientBuilder setGatewayUrl(String gatewayUrl) {
            this.gatewayUrl = gatewayUrl;
            return this;
        }

        public AlipayServiceImpl.ClientBuilder setPrivateKey(String privateKey) {
            this.privateKey = privateKey;
            return this;
        }

        public AlipayServiceImpl.ClientBuilder setSignType(String signType) {
            this.signType = signType;
            return this;
        }

        public String getAlipayPublicKey() {
            return this.alipayPublicKey;
        }

        public String getSignType() {
            return this.signType;
        }

        public String getAppId() {
            return this.appId;
        }

        public String getCharset() {
            return this.charset;
        }

        public String getFormat() {
            return this.format;
        }

        public String getGatewayUrl() {
            return this.gatewayUrl;
        }

        public String getPrivateKey() {
            return this.privateKey;
        }
    }
}
