package cup.cw.mall.controller.backend;

import cup.cw.mall.common.Const;
import cup.cw.mall.common.ServerResponse;
import cup.cw.mall.pojo.User;
import cup.cw.mall.service.IOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

/**
 * created by cuiwei on 2018/12/10
 */
@RestController
@RequestMapping("/manager/order")
public class OrderManagerController {

    private static Logger logger = LoggerFactory.getLogger(OrderManagerController.class);

    @Autowired
    private IOrderService orderService;

    @RequestMapping("/list.do")
    public ServerResponse orderList(@RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                    @RequestParam(value = "pageSize",defaultValue = "10")int pageSize,
                                    @RequestParam(value = "isListAll",defaultValue = "0") int isListAll){
        return orderService.orderList(pageNum,pageSize,isListAll);
    }

    @RequestMapping("/detail.do")
    public ServerResponse detail(HttpSession session, Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        return orderService.getOrderDetail(user.getId(),orderNo);
    }

    @RequestMapping("/send_goods.do")
    public ServerResponse sendGoods(HttpSession session,Long orderNo){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        return orderService.sendGoods(user.getId(),orderNo);
    }
}
