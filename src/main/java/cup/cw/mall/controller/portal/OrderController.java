package cup.cw.mall.controller.portal;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.internal.util.StringUtils;
import com.alipay.demo.trade.config.Configs;
import com.google.common.collect.Maps;
import cup.cw.mall.common.AlipayConst;
import cup.cw.mall.common.Const;
import cup.cw.mall.common.ServerResponse;
import cup.cw.mall.pojo.User;
import cup.cw.mall.service.IOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * created by cuiwei on 2018/11/19
 */
@RestController
@RequestMapping("/order")
public class OrderController {

    private static Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private IOrderService orderService;

    /**
     * 创建订单
     * @param httpSession
     * @param shippingId
     * @param payType
     * @return
     */
    @RequestMapping("/create.do")
    public ServerResponse create(HttpSession httpSession,Integer shippingId,Integer payType){
        User user = (User)httpSession.getAttribute(Const.CURRENT_USER);
        return orderService.createOrder(user.getId(),shippingId,payType);
    }

    /**
     * 取消订单
     * @param httpSession
     * @param orderNo
     * @return
     */
    @RequestMapping("/cancel.do")
    public ServerResponse cancel(HttpSession httpSession, Long orderNo){
        User user = (User)httpSession.getAttribute(Const.CURRENT_USER);
        return orderService.cancelOrder(user.getId(),orderNo);
    }

    /**
     * 获取订单购物车选中的商品信息
     * @param httpSession
     * @return
     */
    @RequestMapping("/get_order_cart_product.do")
    public ServerResponse getOrderCartProduct(HttpSession httpSession){
        User user = (User)httpSession.getAttribute(Const.CURRENT_USER);
        return orderService.getOrderCartProduct(user.getId());
    }

    /**
     * 查看订单详情
     * @param httpSession
     * @param orderNo
     * @return
     */
    @RequestMapping("/detail.do")
    public ServerResponse detail(HttpSession httpSession, Long orderNo){
        User user = (User)httpSession.getAttribute(Const.CURRENT_USER);
        return orderService.getOrderDetail(user.getId(),orderNo);
    }

    /**
     * 查询订单列表
     * @param httpSession
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping("/list.do")
    public ServerResponse getOrderList(HttpSession httpSession,
                                       @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                       @RequestParam(value = "pageSize",defaultValue = "10")int pageSize){
        User user = (User)httpSession.getAttribute(Const.CURRENT_USER);
        return orderService.getOrderList(user.getId(),pageNum,pageSize);
    }

    /**
     * 订单号，生成二维码支付
     * @param request
     * @param orderNo
     * @return
     */
    @RequestMapping("pay.do")
    public void pay(HttpServletRequest request, HttpServletResponse response, Long orderNo){
        User user = (User) request.getSession().getAttribute(Const.CURRENT_USER);
        String path = request.getSession().getServletContext().getRealPath("upload");
        orderService.payByRedirect(response,orderNo,user.getId(),path);
    }


    /**
     * 处理回调，获取回调参数，传入service层
     * @param request
     * @return
     */
    @RequestMapping(value = "alipay_callback.do",method = RequestMethod.POST)
    public Object alipayCallback(HttpServletRequest request){
        //参数
        Map<String,String > params = Maps.newHashMap();
        //获取请求参数
        Map<String,String[]> requestParams = request.getParameterMap();
        Set<String> keySet = requestParams.keySet();
        for (String name : keySet){
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name,valueStr);
        }

        logger.info("支付宝回调，sign:{},trade_status:{},参数:{}",params.get("sign"),params.get("trade_status"),params.toString());

        //非常重要，验证回调的正确性，是不是支付宝发的，并且呢还要避免重复通知
        params.remove("sign_type");
        try {
            boolean alipayRSACheckedV2 = AlipaySignature.rsaCheckV2(params, Configs.getAlipayPublicKey(),"utf-8",Configs.getSignType());
            if(!alipayRSACheckedV2){
                return ServerResponse.createByError("非法请求,验证不通过，如果在恶意请求我就报警了");
            }
        } catch (AlipayApiException e) {
            logger.error("支付宝回调异常",e);
        }
        ServerResponse serverResponse = orderService.aliCallback(params);
        if(serverResponse.isSuccess()){
            return Const.AlipayCallback.RESPONSE_SUCCESS;
        }
        return Const.AlipayCallback.RESPONSE_FAILED;
    }

    /**
     * 退款接口
     */
    @RequestMapping("refund.do")
    public ServerResponse refund(HttpSession session,Long orderNo,String refundAmount, String refundReason){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        return orderService.refund(orderNo,user.getId(),refundAmount,refundReason);
    }

    /**
     * 退款结果查询接口
     * @param session
     * @param orderNo
     * @return
     */
    @RequestMapping("refund_query.do")
    public ServerResponse refundQuery(HttpSession session,Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        return orderService.refundQuery(user.getId(),orderNo);
    }

    /**
     * 付款结果查询接口
     * @param session
     * @param orderNo
     * @return
     */
    @RequestMapping("pay_query.do")
    public ServerResponse payQuery(HttpSession session,Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        return orderService.payQuery(user.getId(),orderNo);
    }

    /**
     * 下载账单接口
     * @param request
     * @param response
     * @param billType
     * @param billDate
     * @return
     * @throws IOException
     */
    @RequestMapping("down_bill.do")
    public ServerResponse downloadBill(HttpServletRequest request,HttpServletResponse response,
                             String billType,String billDate) throws IOException {
        if (!checkBillType(billType)){
            return ServerResponse.createByError("billType 参数设置错误！只能为“trade”,“signcustomer”");
        }
        if (!checkBillDate(billDate)){
            return ServerResponse.createByError("billDate 格式设置错误，应该为yyyy-MM-dd或者yyyy-MM");
        }
        ServerResponse serverResponse = orderService.downloadPayBill(billType,billDate);
        if (serverResponse.isSuccess()){
            String downloadUrl = (String) serverResponse.getData();
            response.sendRedirect(downloadUrl);
        }
        return ServerResponse.createByError("下载失败！");
    }

    private boolean checkBillType(String billType){
        return AlipayConst.BillDownload.BILL_TYPE_TRADE.equals(billType)
                || AlipayConst.BillDownload.BILL_TYPE_SIGNCUSTOMER.equals(billType);
    }

    private boolean checkBillDate(String billDate){
        return billDate.matches("[0-9]{4}-[0-1][0-9]-[0-3][0-9]")
                || billDate.matches("[0-9]{4}-[0-1][0-9]");
    }

}
