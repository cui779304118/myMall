package cup.cw.mall.service;

import cup.cw.mall.common.ServerResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * created by cuiwei on 2018/11/12
 */
public interface ICartService {
    ServerResponse add(Integer userId, Integer productId, Integer count, HttpServletRequest request, HttpServletResponse response);
    ServerResponse update(Integer userId, Integer productId, Integer count, HttpServletRequest request, HttpServletResponse response);
    ServerResponse delete(Integer userId,String productIds, HttpServletRequest request, HttpServletResponse response);
    ServerResponse list(Integer userId, HttpServletRequest request, HttpServletResponse response);
    ServerResponse selectOrUnselect(Integer productId, Integer checked, HttpServletRequest request, HttpServletResponse response);
    ServerResponse getCartProductCount(HttpServletRequest request, HttpServletResponse response);
    ServerResponse listCart(Integer userId);
    boolean changeChecked(Integer userId,Integer productId,Integer checked);
    boolean deleteAll(Integer userId);
}
