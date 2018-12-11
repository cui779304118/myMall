package cup.cw.mall.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import cup.cw.mall.cache.LocalCacheManager;
import cup.cw.mall.common.AlipayConst;
import cup.cw.mall.common.Const;
import cup.cw.mall.common.RedisConst;
import cup.cw.mall.common.ServerResponse;
import cup.cw.mall.dao.OrderItemMapper;
import cup.cw.mall.dao.OrderMapper;
import cup.cw.mall.dao.PayInfoMapper;
import cup.cw.mall.dao.ProductMapper;
import cup.cw.mall.pojo.*;
import cup.cw.mall.pojo.alipay.GoodsDetail;
import cup.cw.mall.pojo.alipay.builder.*;
import cup.cw.mall.pojo.alipay.result.AlipayWebResult;
import cup.cw.mall.service.*;
import cup.cw.mall.util.DateTimeUtil;
import cup.cw.mall.util.FtpUtil;
import cup.cw.mall.util.PropertiesUtil;
import cup.cw.mall.vo.CartProductVo;
import cup.cw.mall.vo.CartVo;
import cup.cw.mall.vo.OrderProductVo;
import cup.cw.mall.vo.OrderVo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * created by cuiwei on 2018/11/19
 */
@Service("orderService")
public class OrderServiceImpl implements IOrderService {
    private static Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    private static AlipayTradeService tradeService;

    static {
        /** 一定要在创建AlipayTradeService之前调用Configs.init()设置默认参数
         *  Configs会读取classpath下的zfbinfo.properties文件配置信息，如果找不到该文件则确认该文件是否在classpath目录
         */
        Configs.init("zfbinfo.properties");

        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();
    }

    @Resource(name = "alipayService")
    private IThirdPayService alipayService;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private LocalCacheManager localCacheManager;

    @Autowired
    private ICartService cartService;

    @Autowired
    private IProductService productService;

    @Autowired
    private IShippingService shippingService;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private PayInfoMapper payInfoMapper;

    @Override
    public ServerResponse createOrder(Integer userId, Integer shippingId, Integer payType) {
        ServerResponse orderCartProductVoRes = getOrderCartProduct(userId);
        if (!orderCartProductVoRes.isSuccess()) {
            return ServerResponse.createByError("创建订单失败！");
        }
        OrderProductVo orderProductVo = (OrderProductVo) orderCartProductVoRes.getData();
        List<OrderItem> orderItemList = orderProductVo.getOrderItemList();
        //获取订单总价格
        BigDecimal payment = orderProductVo.getTotalPrice();
        //生成订单
        Order order = createOrder(userId, shippingId, payment, payType);
        //将订单号插入各订单商品记录中，并扣减库存
        for (int i = 0; i < orderItemList.size(); i++) {
            OrderItem item = orderItemList.get(i);
            boolean isReduced = reduceStock(item.getProductId(), item.getQuantity());
            if (isReduced) {//余量扣减成功
                item.setOrderNo(order.getOrderNo());
            } else {//余量扣减不成功，从orderItem列表删除，并从总价格中减去
                orderItemList.remove(i);
                BigDecimal newPayment = order.getPayment().subtract(item.getTotalPrice());
                order.setPayment(newPayment);
                cartService.changeChecked(userId, item.getProductId(), Const.CART.UN_CHECKED);
            }
        }
        //插入订单，mysql和redis
        if (!insertOrderToMysql(order)) {
            return ServerResponse.createByError("系统错误，生成订单失败！");
        }
        String orderJsonStr = insertOrderToRedis(order);
        localCacheManager.lpush(RedisConst.ORDER.ORDERS_KEY, orderJsonStr);
        localCacheManager.lpush(RedisConst.ORDER.ORDER_NOSEND_KEY, orderJsonStr);
        //插入订单商品详情，mysql和redis
        if (!insertOrderItemsToMysql(orderItemList)) {
            return ServerResponse.createByError("系统错误，生成订单失败！");
        }
        insertOrderItemsToRedis(orderItemList);
        //清空购物车
        clearCart(userId);
        //生成订单vo
        OrderVo orderVo = createOrderVo(order, orderItemList);
        return ServerResponse.createBySuccess(orderVo);
    }

    private ServerResponse createOrderItemList(List<CartProductVo> cartProductVos) {
        List<OrderItem> orderItemList = Lists.newArrayList();
        if (CollectionUtils.isEmpty(cartProductVos)) {
            return ServerResponse.createByError("创建订单失败，购物车为空！");
        }
        for (CartProductVo productVo : cartProductVos) {
            //排除没有选中的产品
            if (productVo.getProductChecked().equals(Const.CART.UN_CHECKED)) {
                continue;
            }
            if (productVo.getProductStatus() != Const.ProductStatusEnum.ON_SALE.getCode()) {
                return ServerResponse.createByError("创建订单失败，产品【" + productVo.getProductName() + "】不是在售卖状态！");
            }
            OrderItem orderItem = new OrderItem();
            orderItem.setUserId(productVo.getUserId());
            orderItem.setProductId(productVo.getProductId());
            orderItem.setProductName(productVo.getProductName());
            orderItem.setProductImage(productVo.getProductMainImage());
            orderItem.setQuantity(productVo.getQuantity());
            orderItem.setCurrentUnitPrice(productVo.getProductPrice());
            orderItem.setTotalPrice(productVo.getProductTotalPrice());
            orderItemList.add(orderItem);
        }
        if (CollectionUtils.isEmpty(orderItemList)) {
            return ServerResponse.createByError("创建订单失败，没有选择商品！");
        }
        return ServerResponse.createBySuccess(orderItemList);
    }

    private Order createOrder(Integer userId, Integer shippingId, BigDecimal payment, Integer payType) {
        Order order = new Order();
        order.setOrderNo(genOrderNo());
        order.setUserId(userId);
        order.setShippingId(shippingId);
        order.setPayment(payment);
        order.setPaymentType(payType);
        order.setStatus((byte) Const.OrderStatusEnum.NO_PAY.getCode());
        order.setCreateTime(new Date());
        order.setPostage(0);
        return order;
    }

    private Long genOrderNo() {
        Long curTimeLong = System.currentTimeMillis();
        return curTimeLong + new Random().nextInt(100);
    }

    private boolean insertOrderToMysql(Order order) {
        try {
            int reCode = orderMapper.insertSelective(order);
            if (reCode > Const.DB_CODE.OPERATE_FAILED) {
                return true;
            }
        } catch (Exception e) {
            logger.error("插入订单到mysql失败,userId={},shippingId={}", order.getUserId(), order.getShippingId(), e);
        }
        return false;
    }

    private boolean insertOrderItemsToMysql(List<OrderItem> orderItems) {
        try {
            int reCode = orderItemMapper.batchInsert(orderItems);
            if (reCode > Const.DB_CODE.OPERATE_FAILED) {
                return true;
            }
        } catch (Exception e) {
            logger.error("插入订单到mysql失败,userId={}", orderItems.get(0).getUserId(), e);
        }
        return false;
    }

    private void insertOrderItemsToRedis(List<OrderItem> orderItems) {
        String orderNo = String.valueOf(orderItems.get(0).getOrderNo());
        String orderItemKey = createOrderItemKey(orderNo);
        for (OrderItem item : orderItems) {
            String itemJsonStr = JSONObject.toJSONString(item);
            localCacheManager.lpush(orderItemKey, itemJsonStr);
        }
    }

    private String insertOrderToRedis(Order order) {
        String orderKey = createOrderKey(order.getUserId());
        String orderIdStr = String.valueOf(order.getOrderNo());
        String orderJsonStr = JSONObject.toJSONString(order);
        localCacheManager.hset(orderKey, orderIdStr, orderJsonStr);
        return orderJsonStr;
    }

    private String createOrderKey(Integer userId) {
        String username = localCacheManager.hget(RedisConst.USER.USER_IDS, String.valueOf(userId));
        return RedisConst.ORDER.ORDER_PREFFIX + username;
    }

    private String createOrderItemKey(String orderNo) {
        return RedisConst.ORDER.ORDER_ITEM_PREFFIX + orderNo;
    }

    //TODO:考虑并发，产品数量不够的情况,CAS实现
    private boolean reduceStock(Integer productId, Integer quantity) {
        int tryTimes = Const.Order.UPDATE_RETRY_TIMES;
        for (int i = 0; i < tryTimes; i++) {
            try {
                //查询产品详细信息
                Product product = productMapper.selectByPrimaryKey(productId);
                if (product == null) return false;
                if (product.getStock() < quantity) {
                    if (product.getStock() == 0) return false;
                    quantity = product.getStock();
                }
                product.setId(productId);
                product.setStock(quantity);
                int reCode = productMapper.updateStockByCas(product);
                if (reCode == Const.DB_CODE.OPERATE_FAILED) continue;
                productService.insertProductIntoRedis(product);
                return true;
            } catch (Exception e) {
                logger.error("查询或更新产品失败，productId={}", productId, e);
            }
        }
        return false;
    }

    private boolean clearCart(Integer userId) {
        return cartService.deleteAll(userId);
    }

    private OrderVo createOrderVo(Order order, List<OrderItem> orderItems) {
        OrderVo orderVo = new OrderVo();
        orderVo.setOrderNo(order.getOrderNo());
        orderVo.setPayment(order.getPayment());
        Integer payType = order.getPaymentType();
        orderVo.setPaymentType(payType);
        orderVo.setPaymentTypeDesc(Const.PayTypeEnum.getValueByCode(payType));
        orderVo.setPostage(order.getPostage());
        orderVo.setStatus((int) order.getStatus());
        orderVo.setStatusDesc(Const.OrderStatusEnum.codeOf((int) order.getStatus()).getValue());

        Integer shippingId = order.getShippingId();
        orderVo.setShippingId(shippingId);
        ServerResponse shippingRes = shippingService.query(order.getUserId(), shippingId);
        if (shippingRes.isSuccess()) {
            Shipping shipping = (Shipping) shippingRes.getData();
            orderVo.setReceiverName(shipping.getReceiverName());
            orderVo.setShippingVo(shipping);
        }

        orderVo.setPaymentTime(DateTimeUtil.dateToStr(order.getPaymentTime()));
        orderVo.setSendTime(DateTimeUtil.dateToStr(order.getSendTime()));
        orderVo.setEndTime(DateTimeUtil.dateToStr(order.getEndTime()));
        orderVo.setCreateTime(DateTimeUtil.dateToStr(order.getCreateTime()));
        orderVo.setCloseTime(DateTimeUtil.dateToStr(order.getCloseTime()));

        orderVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        orderVo.setOrderItemVoList(orderItems);
        return orderVo;
    }

    @Override
    public ServerResponse cancelOrder(Integer userId, Long orderNo) {
        Order orderSel = getOrderByRedis(userId, orderNo);
        if (orderSel == null) {
            logger.warn("取消不存在订单，userId=[{}],orderNo=[{}]", userId, orderNo);
            return ServerResponse.createByError("取消订单失败，不存在该订单");
        }

        if (!orderSel.getStatus().equals((byte) Const.OrderStatusEnum.NO_PAY.getCode())) {
            return ServerResponse.createByError("取消订单失败，该订单已付款");
        }
        Order orderUpdate = new Order();
        orderUpdate.setId(orderSel.getId());
        orderUpdate.setStatus((byte) Const.OrderStatusEnum.CANCELED.getCode());
        try {
            int reCode = orderMapper.updateByPrimaryKeySelective(orderUpdate);
            if (reCode > Const.DB_CODE.OPERATE_FAILED) {
                orderSel.setStatus((byte) Const.OrderStatusEnum.CANCELED.getCode());
                insertOrderToRedis(orderSel);
                return ServerResponse.createByError("取消订单成功");
            }
        } catch (Exception e) {
            logger.error("修改订单状态失败，userId=[{}],orderNo=[{}]", userId, orderNo, e);
        }
        return ServerResponse.createByError("取消订单失败，系统错误");
    }

    private Order getOrderByRedis(Integer userId, Long orderNo) {
        String orderKey = createOrderKey(userId);
        String orderStr = localCacheManager.hget(orderKey, String.valueOf(orderNo));
        if (StringUtils.isEmpty(orderStr)) return null;
        return JSONObject.parseObject(orderStr, Order.class);
    }

    @Override
    public ServerResponse getOrderCartProduct(Integer userId) {
        ServerResponse cartVoRes = cartService.listCart(userId);
        if (!cartVoRes.isSuccess()) {
            logger.error("获取购物车信息失败，userId={}", userId);
            return ServerResponse.createByError("获取购物车信息失败！");
        }
        CartVo cartVo = (CartVo) cartVoRes.getData();
        List<CartProductVo> cartProductVos = cartVo.getCartProductVoList();
        //从购物车信息中，产生orderItemList
        ServerResponse orderItemRes = createOrderItemList(cartProductVos);
        if (!orderItemRes.isSuccess()) {
            return orderItemRes;
        }
        List<OrderItem> orderItemList = (List<OrderItem>) orderItemRes.getData();
        //获取订单总价格
        BigDecimal totalPrice = cartVo.getCartTotalPrice();
        OrderProductVo orderProductVo = new OrderProductVo();
        orderProductVo.setOrderItemList(orderItemList);
        orderProductVo.setTotalPrice(totalPrice);
        orderProductVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return ServerResponse.createBySuccess(orderProductVo);
    }

    @Override
    public ServerResponse getOrderDetail(Integer userId, Long orderNo) {
        Order order = getOrderByRedis(userId, orderNo);
        List<OrderItem> orderItemList = getOrderItemListByRedis(orderNo);
        if (order == null || CollectionUtils.isEmpty(orderItemList)) {
            return ServerResponse.createByError("查询失败，没有该订单");
        }
        return ServerResponse.createBySuccess(createOrderVo(order, orderItemList));
    }

    private List<OrderItem> getOrderItemListByRedis(Long orderNo) {
        List<OrderItem> orderItemList = Lists.newArrayList();
        String orderItemKey = createOrderItemKey(String.valueOf(orderNo));
        List<String> orderItemStrs = localCacheManager.lrange(orderItemKey, 0, -1);
        for (String itemStr : orderItemStrs) {
            OrderItem orderItem = JSONObject.parseObject(itemStr, OrderItem.class);
            orderItemList.add(orderItem);
        }
        return orderItemList;
    }

    @Override
    public ServerResponse getOrderList(Integer userId, int pageNum, int pageSize) {
        String orderKey = createOrderKey(userId);
        int totalRecords = (int) localCacheManager.hlen(orderKey);
        PageModel pageModel = PageModel.builder().totalRecords(totalRecords)
                .pageNum(pageNum).pageSize(pageSize).build();
        try {
            List<Order> orderList = orderMapper.listByUserId(userId, pageModel);
            pageModel.setDataList(orderList);
            return ServerResponse.createBySuccess(pageModel);
        } catch (Exception e) {
            logger.error("分页查询订单列表失败，userId={},pageNum={},pageSize={}", userId, pageNum, pageSize, e);
        }
        return ServerResponse.createByError("查询失败，系统错误");
    }

    /**
     * 当面付，生成二维码的方式
     *
     * @param orderNo
     * @param userId
     * @param path
     * @return
     */
    @Override
    public ServerResponse payByP2P(Long orderNo, Integer userId, String path) {
        Map<String, String> resultMap = Maps.newHashMap();
        //首先获取订单详情
        Order order = getOrderByRedis(userId, orderNo);
        if (order == null) {
            logger.error("没有查询到该订单，userId={},orderNo={}", userId, orderNo);
            return ServerResponse.createByError("没有该订单！");
        }
        resultMap.put("orderNo", String.valueOf(order.getOrderNo()));

        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo = order.getOrderNo().toString();

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店消费”
        String subject = "myMall扫码支付，订单号：" + outTradeNo;

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = String.valueOf(order.getPayment().doubleValue());

        // (必填) 付款条码，用户支付宝钱包手机app点击“付款”产生的付款条码
        String authCode = "286648048691290423"; // 条码示例，286648048691290423
        // (可选，根据需要决定是否使用) 订单可打折金额，可以配合商家平台配置折扣活动，如果订单部分商品参与打折，可以将部分商品总价填写至此字段，默认全部商品可打折
        // 如果该值未传入,但传入了【订单总金额】,【不可打折金额】 则该值默认为【订单总金额】- 【不可打折金额】
        // String discountableAmount = "1.00"; //

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
//        String body = new StringBuilder().append("订单:").append(outTradeNo).append(",购买商品共").append(totalAmount).append("元").toString();
        String body = "订单:" + outTradeNo + ",购买商品共" + totalAmount + "元";

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
//        ExtendParams extendParams = new ExtendParams();
//        extendParams.setSysServiceProviderId("2088102176459669");

        // 支付超时，线下扫码交易定义为5分钟
        String timeoutExpress = "5m";

        String appAuthToken = "应用授权令牌";//根据真实值填写

        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = Lists.newArrayList();
        // 创建一个商品信息，参数含义分别为商品id（使用国标）、名称、单价（单位为分）、数量，如果需要添加商品类别，详见GoodsDetail
        List<OrderItem> orderItemList = getOrderItemListByRedis(orderNo);
//        for (OrderItem orderItem : orderItemList) {
//            GoodsDetail goods = GoodsDetail.newInstance(orderItem.getProductId().toString(), orderItem.getProductName(),
//                    orderItem.getTotalPrice().multiply(new BigDecimal(1000L)).longValue(),orderItem.getQuantity());
//            goodsDetailList.add(goods);
//        }

        String callbackUrl = PropertiesUtil.getProperty("alipay.callback.url");

        // 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId)
                .setTimeoutExpress(timeoutExpress)
                .setNotifyUrl(callbackUrl);//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
//                .setGoodsDetailList(goodsDetailList);

        // 调用tradePay方法获取当面付应答
        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);
        switch (result.getTradeStatus()) {
            case SUCCESS:
                logger.info("支付宝预下单成功:");

                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);

                File folder = new File(path);
                if (!folder.exists()) {
                    folder.setWritable(true);
                    folder.mkdirs();
                }

                //需要修改为运行机器上的路径
                String qrPath = String.format(path + "/qr-%s.png", response.getOutTradeNo());
                String qrFileName = String.format("qr-%s.png", response.getOutTradeNo());
                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, qrPath);

                File targetFile = new File(path, qrFileName);

                FtpUtil.uploadFile(Lists.newArrayList(targetFile));
                logger.info("qrPath:" + qrPath);
                String qrUrl = PropertiesUtil.getProperty("ftp.server.http.prefix") + "pub/" + targetFile.getName();
                resultMap.put("qrUrl", qrUrl);

                return ServerResponse.createBySuccess(resultMap);
            case FAILED:
                logger.error("支付宝预下单失败!!!userId={},orderNo={}", userId, orderNo);
                return ServerResponse.createByError("支付宝预下单失败!!!");
            case UNKNOWN:
                logger.error("系统异常，预下单状态未知!!!userId={},orderNo={}", userId, orderNo);
                return ServerResponse.createByError("系统异常，预下单状态未知!!!");
            default:
                logger.error("不支持的交易状态，交易返回异常!!!userId={},orderNo={}", userId, orderNo);
                return ServerResponse.createByError("不支持的交易状态，交易返回异常!!!");
        }
    }

    /**
     * 通过支付宝网站重定向方式付款
     *
     * @param response
     * @param orderNo
     * @param userId
     * @param path
     * @return
     */
    @Override
    public ServerResponse payByRedirect(HttpServletResponse response, Long orderNo, Integer userId, String path) {
        //首先获取订单详情
        Order order = getOrderByRedis(userId, orderNo);
        if (order == null) {
            logger.error("没有查询到该订单，userId={},orderNo={}", userId, orderNo);
            return ServerResponse.createByError("没有该订单！");
        }
        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo = order.getOrderNo().toString();

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店消费”
        String subject = "myMall网站支付，订单号：" + outTradeNo;

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = String.valueOf(order.getPayment().doubleValue());

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
//        String body = new StringBuilder().append("订单:").append(outTradeNo).append(",购买商品共").append(totalAmount).append("元").toString();
        String body = "订单:" + outTradeNo + ",购买商品共" + totalAmount + "元";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
//        ExtendParams extendParams = new ExtendParams();
//        extendParams.setSysServiceProviderId("2088102176459669");

        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = Lists.newArrayList();
        // 创建一个商品信息，参数含义分别为商品id（使用国标）、名称、单价（单位为分）、数量，如果需要添加商品类别，详见GoodsDetail
        List<OrderItem> orderItemList = getOrderItemListByRedis(orderNo);
        for (OrderItem orderItem : orderItemList) {
            GoodsDetail goods = new GoodsDetail();
            goods.setShowUrl("www.baidu.com");
            goodsDetailList.add(goods);
        }
        String notifyUrl = PropertiesUtil.getProperty(AlipayConst.Params.NOTIFY_URL);
        String returnUrl = PropertiesUtil.getProperty(AlipayConst.Params.RETURN_URL);
        String productCode = AlipayConst.Params.PRODUCT_CODE;

        AlipayWebPayRequestBuilder requestBuilder = new AlipayWebPayRequestBuilder()
                .setBody(body).setSubject(subject).setTotalAmount(totalAmount)
                .setOutTraceNo(outTradeNo).setBody(body).setNotifyUrl(notifyUrl)
                .setReturnUrl(returnUrl).setProductCode(productCode);

        AlipayWebResult webResult = alipayService.pay(requestBuilder);
        if (webResult.isTradeSuccess()) {
            AlipayResponse alipayResponse = webResult.getResponse();
            dumpResponse(alipayResponse);
            String form = alipayResponse.getBody();
            outputForm(response, form);
            logger.info("通过支付宝支付成功，userId={},orderNo={}", order.getUserId(), order.getOrderNo());
            return ServerResponse.createBySuccess();
        } else {
            logger.info("通过支付宝支付失败，userId={},orderNo={}", order.getUserId(), order.getOrderNo());
            return ServerResponse.createByError();
        }
    }

    private void outputForm(HttpServletResponse response, String form) {
        try {
            response.setContentType("text/html;charset=utf-8");
            response.getWriter().write(form);
            response.getWriter().flush();
            response.getWriter().close();
        } catch (IOException e) {
            logger.error("输出支付宝重定向表单失败", e);
        }
    }


    /**
     * 通过支付宝支付后对调处理
     *
     * @param
     */
    @Override
    public ServerResponse aliCallback(Map<String, String> params) {
        Long orderNo = Long.valueOf(params.get("out_trade_no"));//订单号
        String tradeNo = params.get("trade_no");//交易号
        String tradeStatus = params.get("trade_status");//交易状态
        String appId = params.get("app_id");
        String buyerId = params.get("buyer_id");
        BigDecimal totalAmount = new BigDecimal(params.get("total_amount")).setScale(2, BigDecimal.ROUND_HALF_UP);
        if (!Configs.getAppid().equals(appId)) {
            logger.error("支付宝回调失败，appid不正确，tradeNo:{}，appid={}", tradeNo, appId);
            return ServerResponse.createByError("appid错误");
        }
        Order order = getOrderFromRedisByOrderNo(orderNo);
        if (order == null) {
            logger.error("支付宝回调失败，出现不存在的订单，orderNo:{},tradeNo:{}", orderNo, tradeNo);
            return ServerResponse.createByError("不存在该订单！");
        }
        BigDecimal payment = order.getPayment().setScale(2, BigDecimal.ROUND_HALF_UP);
        if (!payment.equals(totalAmount)) {
            logger.error("支付宝回调失败，订单金额错误，tradeNo:{},payment:{},amount:{}", tradeNo, payment, totalAmount);
            return ServerResponse.createByError("订单金额错误！");
        }
        if (order.getStatus() >= Const.OrderStatusEnum.PAID.getCode()) {
            logger.error("支付宝重复回调！orderNo:{},tradeNo:{}", orderNo, tradeNo);
            return ServerResponse.createByError("支付宝重复回调！");
        }
        //如果支付成功，则修改订单状态（mysql和redis）
        if (Const.AlipayCallback.TRADE_STATUS_TRADE_SUCCESS.equals(tradeStatus)) {
            order.setPaymentTime(DateTimeUtil.strToDate(params.get("gmt_payment")));
            order.setStatus((byte) Const.OrderStatusEnum.PAID.getCode());
            updateOrder(order);
        }
        //将支付信息插入Mysql和Redis
        PayInfo payInfo = new PayInfo();
        payInfo.setUserId(order.getUserId());
        payInfo.setOrderNo(order.getOrderNo());
        payInfo.setPayPlatform(Const.PayPlatformEnum.ALIPAY.getCode());
        payInfo.setPlatformNumber(tradeNo);
        payInfo.setPlatformStatus(tradeStatus);
        payInfo.setBuyerId(buyerId);
        payInfo.setRefundAmount(new BigDecimal(0));

        try {
            int reCode = payInfoMapper.insertSelective(payInfo);
            if (reCode > Const.DB_CODE.OPERATE_FAILED) {
                insertPayInfoToRedis(payInfo);
                logger.info("支付宝回调成功，orderNo:{},tradeNo:{}", orderNo, tradeNo);
                return ServerResponse.createBySuccess();
            }
        } catch (Exception e) {
            logger.error("插入payInfo到Mysql失败，userId={},orderNo={}", payInfo.getUserId(), payInfo.getOrderNo(), e);
        }
        return ServerResponse.createByError();
    }

    private void insertPayInfoToRedis(PayInfo payInfo) {
        String payInfoKey = createPayInfoKey(payInfo.getUserId());
        String payInfoStr = JSONObject.toJSONString(payInfo);
        localCacheManager.hset(payInfoKey, String.valueOf(payInfo.getOrderNo()), payInfoStr);
    }

    private String createPayInfoKey(Integer userId) {
        String username = localCacheManager.hget(RedisConst.USER.USER_IDS, String.valueOf(userId));
        return RedisConst.PAY_INFO.USER_PREFFIX + username;
    }

    //简单打印应答
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            logger.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                logger.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            logger.info("body:" + response.getBody());
        }
    }

    private Order getOrderFromRedisByOrderNo(Long orderNo) {
        String orderItemKey = createOrderItemKey(String.valueOf(orderNo));
        List<String> orderItemList = localCacheManager.lrange(orderItemKey, 0, 1);
        if (CollectionUtils.isEmpty(orderItemList)) {
            return null;
        }
        OrderItem orderItem = JSONObject.parseObject(orderItemList.get(0), OrderItem.class);
        return getOrderByRedis(orderItem.getUserId(), orderNo);
    }

    private void updateOrder(Order updateOrder) {
        try {
            int reCode = orderMapper.updateByPrimaryKeySelective(updateOrder);
            if (reCode > Const.DB_CODE.OPERATE_FAILED) {
                insertOrderToRedis(updateOrder);
            }
        } catch (Exception e) {
            logger.error("修改订单状态失败，userId=[{}],orderNo=[{}]", updateOrder.getUserId(), updateOrder.getOrderNo(), e);
        }
    }

    @Override
    public ServerResponse refund(Long orderNo, Integer userId, String refundAmountStr, String refundReason) {
        String outTradeNo = String.valueOf(orderNo);
        Order order = getOrderByRedis(userId, orderNo);
        if (order == null) {
            logger.error("不存在该订单，userId:{},orderNo:{}", userId, orderNo);
            return ServerResponse.createByError("退款失败，不存在该订单!");
        }
        BigDecimal refundAmount = new BigDecimal(refundAmountStr).setScale(2, BigDecimal.ROUND_HALF_UP);
        BigDecimal payment = order.getPayment();
        if (refundAmount.doubleValue() > payment.doubleValue()) {
            logger.error("退款金额大于付款金额，userId:{},orderNo:{}", userId, orderNo);
            return ServerResponse.createByError("退款失败，退款金额大于付款金额!");
        }
        AlipayWebRefundRequestBuilder alipayWebRefundRequestBuilder = new AlipayWebRefundRequestBuilder()
                .setOutTradeNo(outTradeNo).setRefundAmount(refundAmountStr).setRefundReason(refundReason);
        AlipayWebResult alipayWebResult = alipayService.refund(alipayWebRefundRequestBuilder);
        if (alipayWebResult != null && alipayWebResult.isTradeSuccess()
                && updatePayInfo(userId, orderNo, refundAmount)) {
            return ServerResponse.createBySuccess("退款成功!");
        } else {
            return ServerResponse.createByError("退款失败!");
        }
    }

    private boolean updatePayInfo(Integer userId, Long orderNo, BigDecimal refundAmount) {
        String payInfoKey = createPayInfoKey(userId);
        String orderNoStr = String.valueOf(orderNo);
        String payInfoJsonStr = localCacheManager.hget(payInfoKey, orderNoStr);
        PayInfo payInfo = JSONObject.parseObject(payInfoJsonStr, PayInfo.class);
        BigDecimal refundAmountUpt = payInfo.getRefundAmount().add(refundAmount);
        payInfo.setRefundAmount(refundAmountUpt);
        try {
            int errCode = payInfoMapper.updateByPrimaryKeySelective(payInfo);
            if (errCode > Const.DB_CODE.OPERATE_FAILED) {
                localCacheManager.hset(payInfoKey, orderNoStr, JSONObject.toJSONString(payInfo));
                return true;
            }
        } catch (Exception e) {
            logger.error("更新pageInfo失败，userId={},orderNo={}", userId, orderNo, e);
        }
        return false;
    }

    private PayInfo getPayInfoFromRedis(Integer userId, Long orderNo) {
        String payInfoKey = createPayInfoKey(userId);
        String orderNoStr = String.valueOf(orderNo);
        String payInfoJsonStr = localCacheManager.hget(payInfoKey, orderNoStr);
        return JSONObject.parseObject(payInfoJsonStr, PayInfo.class);
    }

    @Override
    public ServerResponse refundQuery(Integer userId, Long orderNo) {
        String tradeNo = getPayInfoFromRedis(userId, orderNo).getPlatformNumber();
        String outTradeNo = String.valueOf(orderNo);
        AlipayWebRefundQueryRequestBuilder alipayWebRefundQueryRequestBuilder =
                new AlipayWebRefundQueryRequestBuilder().setTradeNo(tradeNo)
                        .setOutRequestNo(outTradeNo).setOutRequestNo(tradeNo);
        AlipayWebResult alipayWebResult = alipayService.refundQuery(alipayWebRefundQueryRequestBuilder);
        if (alipayWebResult == null || !alipayWebResult.isTradeSuccess()) {
            return ServerResponse.createByError("退款失败！");
        }
        String resBodyJsonStr = alipayWebResult.getResponse().getBody();
        JSONObject resJson = JSONObject.parseObject(resBodyJsonStr);
        return ServerResponse.createBySuccess(resJson.get("alipay_trade_fastpay_refund_query_response"));
    }

    @Override
    public ServerResponse payQuery(Integer userId, Long orderNo) {
        String outTradeNo = String.valueOf(orderNo);
        AlipayWebPayQueryRequestBuilder alipayWebPayQueryRequestBuilder =
                new AlipayWebPayQueryRequestBuilder().setOutTradeNo(outTradeNo);
        AlipayWebResult alipayWebResult = alipayService.payQuery(alipayWebPayQueryRequestBuilder);
        if (alipayWebResult == null || !alipayWebResult.isTradeSuccess()) {
            return ServerResponse.createByError("付款失败！");
        }
        String resBodyJsonStr = alipayWebResult.getResponse().getBody();
        JSONObject resJson = JSONObject.parseObject(resBodyJsonStr).getJSONObject("alipay_trade_query_response");
        JSONObject resData = new JSONObject();
        resData.put("付款状态", resJson.get("msg"));
        resData.put("购买用户账号", resJson.get("buyer_logon_id"));
        resData.put("订单号", resJson.get("out_trade_no"));
        resData.put("交易日期", resJson.get("send_pay_date"));
        resData.put("付款金额", resJson.get("total_amount"));
        resData.put("支付宝付款交易号", resJson.get("trade_no"));
        return ServerResponse.createBySuccess(resData);
    }

    @Override
    public ServerResponse downloadPayBill(String billType, String billDate) {
        AlipayWebBillDownRequestBuilder billDownRequestBuilder =
                new AlipayWebBillDownRequestBuilder()
                        .setBillType(billType).setBillDate(billDate);

        AlipayWebResult alipayWebResult = alipayService.downloadBill(billDownRequestBuilder);
        if (alipayWebResult == null || !alipayWebResult.isTradeSuccess()) {
            return ServerResponse.createByError("下载账单失败！");
        }
        String resBodyJsonStr = alipayWebResult.getResponse().getBody();
        JSONObject resJson = JSONObject.parseObject(resBodyJsonStr)
                .getJSONObject("alipay_data_dataservice_bill_downloadurl_query_response");
        return ServerResponse.createBySuccess(resJson.get(AlipayConst.BillDownload.BILL_DOWNLOAD_URL_KEY));
    }

    @Override
    public ServerResponse orderList(int pageNum, int pageSize, int isListAll) {
        int st = (pageNum - 1) * pageSize;
        int en = st + pageSize - 1;
        long totalRecords;
        List<String> orderStrList;
        if (isListAll == Const.Order.ORDER_LIST_ALL) {
            totalRecords = localCacheManager.llen(RedisConst.ORDER.ORDERS_KEY);
            orderStrList = localCacheManager.lrange(RedisConst.ORDER.ORDERS_KEY, st, en);
        } else {
            totalRecords = localCacheManager.llen(RedisConst.ORDER.ORDER_NOSEND_KEY);
            orderStrList = localCacheManager.lrange(RedisConst.ORDER.ORDER_NOSEND_KEY,st,en);
        }
        List<OrderVo> orderVos = createOrderVoList(orderStrList);
        PageModel pageModel = new PageModel.PageModelBuilder()
                .pageNum(pageNum).pageSize(pageSize).totalRecords((int)totalRecords).build();
        pageModel.setDataList(orderVos);
        return ServerResponse.createBySuccess(pageModel);
    }

    private List<OrderVo> createOrderVoList(List<String> orderStrList){
        List<OrderVo> orderVos = Lists.newArrayList();
        for (String orderStr : orderStrList){
            Order order = JSONObject.parseObject(orderStr,Order.class);
            List<OrderItem> orderItems = getOrderItemListByRedis(order.getOrderNo());
            OrderVo orderVo = createOrderVo(order,orderItems);
            orderVos.add(orderVo);
        }
        return orderVos;
    }

    @Override
    public ServerResponse sendGoods(Integer userId, Long orderNo) {
        Order order = getOrderByRedis(userId,orderNo);
        if (order != null){
            if (order.getStatus() == (byte)Const.OrderStatusEnum.PAID.getCode()){
                order.setStatus((byte)Const.OrderStatusEnum.SHIPPED.getCode());
                order.setSendTime(new Date());
                try {
                    int reCode = orderMapper.updateByPrimaryKeySelective(order);
                    if (reCode > Const.DB_CODE.OPERATE_FAILED) {
                        insertOrderToRedis(order);
                        //TODO:需要该表order在redis中的存储结构
                        order.setStatus((byte)Const.OrderStatusEnum.NO_PAY.getCode());
                        String orderOldStr = JSONObject.toJSONString(order);
                        localCacheManager.lrem(RedisConst.ORDER.ORDER_NOSEND_KEY,0,orderOldStr);
                        return ServerResponse.createByError("发货成功");
                    }
                } catch (Exception e) {
                    logger.error("修改订单状态失败，userId=[{}],orderNo=[{}]", userId, orderNo, e);
                }
            }
            return ServerResponse.createByError("发货失败");
        }
        return ServerResponse.createByError("发货失败，订单不存在");
    }
}
