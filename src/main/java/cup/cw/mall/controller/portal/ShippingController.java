package cup.cw.mall.controller.portal;

import com.github.pagehelper.PageInfo;
import cup.cw.mall.common.Const;
import cup.cw.mall.common.ServerResponse;
import cup.cw.mall.pojo.Shipping;
import cup.cw.mall.pojo.User;
import cup.cw.mall.service.IShippingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

/**
 * created by cuiwei on 2018/11/18
 * 物流信息
 */
@RestController
@RequestMapping("/shipping")
public class ShippingController {

    private Logger logger = LoggerFactory.getLogger(ShippingController.class);

    @Autowired
    IShippingService shippingService;

    @RequestMapping("/add.do")
    public ServerResponse add(HttpSession session,Shipping shipping){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        return shippingService.add(user.getId(),shipping);
    }

    @RequestMapping("/del.do")
    public ServerResponse del(HttpSession session,Integer shippingId){
        if (shippingId == null){
            return ServerResponse.createByError("删除地址信息失败！");
        }
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        return shippingService.del(user.getId(),shippingId);
    }

    @RequestMapping("/update.do")
    public ServerResponse update(HttpSession session,Shipping shipping){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        return shippingService.update(user.getId(),shipping);
    }

    @RequestMapping("select.do")
    public ServerResponse select(HttpSession session, Integer shippingId){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if (shippingId == null){
            return ServerResponse.createByError("查询地址信息失败！");
        }
        return  shippingService.query(user.getId(),shippingId);
    }

    @RequestMapping("list.do")
    public ServerResponse list(HttpSession session,
                                         @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                         @RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        Integer userId = user.getId();
        return shippingService.list(userId,pageNum,pageSize);
    }
}
