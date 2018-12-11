package cup.cw.mall.controller.portal;

import cup.cw.mall.common.Const;
import cup.cw.mall.common.ServerResponse;
import cup.cw.mall.pojo.User;
import cup.cw.mall.service.ICartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


/**
 * created by cuiwei on 2018/11/12
 * 购物车
 */
@RestController
@RequestMapping("/cart")
public class CartController {

    private static Logger logger = LoggerFactory.getLogger(CartController.class);

    @Autowired
    private ICartService cartService;

    /**
     * 添加购物车
     * @param userId
     * @param productId
     * @param count
     * @return
     */
    @RequestMapping("/add.do")
    public ServerResponse add(HttpServletRequest request, HttpServletResponse response,Integer userId, Integer productId, Integer count){
        return cartService.add(userId,productId,count,request,response);
    }

    /**
     * 查询购物车
     * @param userId
     * @return
     */
    @RequestMapping("/list.do")
    public ServerResponse list(HttpServletRequest request, HttpServletResponse response,Integer userId){
        return cartService.list(userId,request,response);
    }

    /**
     * 更新购物车
     * @param userId
     * @param productId
     * @param count
     * @return
     */
    @RequestMapping("/update.do")
    public ServerResponse update(HttpServletRequest request, HttpServletResponse response,
                                 Integer userId, Integer productId, Integer count){
        return cartService.update(userId,productId,count,request,response);
    }

    /**
     * 删除产品，前端约定，删除多个产品，id用逗号分开
     * @param userId
     * @param productIds
     * @return
     */
    @RequestMapping("/delete.do")
    public ServerResponse delete(HttpServletRequest request, HttpServletResponse response,
                                 Integer userId,String productIds){
        return cartService.delete(userId,productIds,request,response);
    }

    /**
     * 全选
     * @param httpSession
     * @return
     */
    @RequestMapping("/select_all.do")
    public ServerResponse selectAll(HttpServletRequest request, HttpServletResponse response,HttpSession httpSession){
        return cartService.selectOrUnselect(null,Const.CART.CHECKED,request,response);
    }

    /**
     * 全不选
     * @param request
     * @return
     */
    @RequestMapping("/un_select_all.do")
    public ServerResponse unselectAll(HttpServletRequest request, HttpServletResponse response){
        return cartService.selectOrUnselect(null,Const.CART.UN_CHECKED,request,response);
    }

    /**
     * 选某一个产品
     * @param request
     * @param productId
     * @return
     */
    @RequestMapping("select.do")
    public ServerResponse select(HttpServletRequest request, HttpServletResponse response,Integer productId){
        return cartService.selectOrUnselect(productId,Const.CART.CHECKED,request,response);
    }

    /**
     * 不选某一个产品
     * @param request
     * @param productId
     * @return
     */
    @RequestMapping("un_select.do")
    public ServerResponse unSelect(HttpServletRequest request, HttpServletResponse response,Integer productId){
        return cartService.selectOrUnselect(productId,Const.CART.UN_CHECKED,request,response);
    }

    /**
     * 获取购物车数量
     * @param request
     * @return
     */
    @RequestMapping("get_cart_product_count.do")
    public ServerResponse getCartProductCount(HttpServletRequest request, HttpServletResponse response){
        return cartService.getCartProductCount(request,response);
    }

}
