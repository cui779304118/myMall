package cup.cw.mall.service;

import cup.cw.mall.common.ServerResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * created by cuiwei on 2018/11/19
 */
public interface IOrderService {

    /**
     * 创建订单
     * @param userId
     * @param shippingId
     * @param payType
     * @return
     */
    ServerResponse createOrder(Integer userId, Integer shippingId,Integer payType);

    /**
     * 取消订单
     * @param userId
     * @param orderNo
     * @return
     */
    ServerResponse cancelOrder(Integer userId,Long orderNo);

    /**
     *获取已经选中的购物车订单信息的
     */
    ServerResponse getOrderCartProduct(Integer userId);

    /**
     * 获取订单详情
     */
    ServerResponse getOrderDetail(Integer userId,Long orderNo);

    /**
     * 查看订单列表
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
    ServerResponse getOrderList(Integer userId,int pageNum,int pageSize);

    /**
     * 支付并生成二维码
     * @param orderNo
     * @param userId
     * @param path
     * @return
     */
    ServerResponse payByP2P(Long orderNo,Integer userId,String path);

    /**
     * 通过支付宝生成的页面重定向
     * @param response
     * @param orderNo
     * @param userId
     * @param path
     * @return
     */
    ServerResponse payByRedirect(HttpServletResponse response,Long orderNo,Integer userId,String path);

    /**
     * 支付宝回调接口
     * @param params
     * @return
     */
    ServerResponse aliCallback(Map<String,String> params);

    /**
     * 通过支付宝退款接口
     * @param orderNo
     * @param userId
     * @return
     */
    ServerResponse refund(Long orderNo,Integer userId,String refundAmount, String refundReason);

    /**
     * 支付宝退款查询接口
     * @param orderNo
     * @return
     */
    ServerResponse refundQuery(Integer userId,Long orderNo);

    /**
     * 支付宝付款查询接口
     * @param userId
     * @param orderNo
     * @return
     */
    ServerResponse payQuery(Integer userId,Long orderNo);

    /**
     * 下载对账单
     * @param billType
     * @param billDate
     */
    ServerResponse downloadPayBill(String billType,String billDate);

    /**
     * 分页展示所有订单
     * @param pageNum
     * @param pageSize
     * @param isListAll 1 展示所有订单，0 展示未发货订单
     * @return
     */
    ServerResponse orderList(int pageNum,int pageSize,int isListAll);

    /**
     * 发货
     * @param userId
     * @param orderNo
     * @return
     */
    ServerResponse sendGoods(Integer userId, Long orderNo);
}
