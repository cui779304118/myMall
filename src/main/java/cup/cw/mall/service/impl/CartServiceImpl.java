package cup.cw.mall.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import cup.cw.mall.cache.LocalCacheManager;
import cup.cw.mall.common.Const;
import cup.cw.mall.common.RedisConst;
import cup.cw.mall.common.ServerResponse;
import cup.cw.mall.dao.CartMapper;
import cup.cw.mall.pojo.Cart;
import cup.cw.mall.pojo.User;
import cup.cw.mall.service.ICartService;
import cup.cw.mall.service.IProductService;
import cup.cw.mall.util.CheckUtil;
import cup.cw.mall.util.CookieUtil;
import cup.cw.mall.vo.CartProductVo;
import cup.cw.mall.vo.CartVo;
import cup.cw.mall.vo.ProductDetailVo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * created by cuiwei on 2018/11/12
 */
@Service("cartService")
public class CartServiceImpl implements ICartService {

    private static Logger logger = LoggerFactory.getLogger(CartServiceImpl.class);

    private static final String PRODUCT_ID = "productId";
    private static final String QUANTITY = "quantity";
    private static final String CHECKED = "checked";

    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private LocalCacheManager localCacheManager;
    @Autowired
    private IProductService productService;

    private enum DbOperation {
        INSERT, UPDATE
    }

    /**
     * 添加商品到购物车，如果已经存在则直接增加count，不存在则添加购物车
     *
     * @param userId
     * @param productId
     * @param count
     * @return
     */
    @Override
    public ServerResponse add(Integer userId, Integer productId, Integer count, HttpServletRequest request, HttpServletResponse response) {
        boolean isLogin = CheckUtil.checkIsLogin(request.getSession()).isSuccess();
        if (isLogin) {
            covertCookieCart(userId, request, response);
            boolean isAddSuccess = addCart(userId, productId, count);
            if (isAddSuccess) {
                return listCart(userId);
            } else {
                return ServerResponse.createByError("添加购物车失败");
            }
        } else {
            JSONObject cartJsonMap = addCookieCart(request, response, productId, count);
            return listCookieCart(cartJsonMap);
        }
    }

    //每个接口操作前都应该调用这个方法，把cookie购物车中的商品保存在后台，并删除cookie
    private void covertCookieCart(Integer userId, HttpServletRequest request, HttpServletResponse response) {
        //查询是否有该cookie
        Cookie cookieCart = CookieUtil.getCookie(request, Const.CART.CART_COOKIE);
        if (cookieCart == null) {
            return;
        }
        JSONObject cartJsonMap = JSONObject.parseObject(cookieCart.getValue());
        Set<Map.Entry<String, Object>> cartSet = cartJsonMap.entrySet();
        for (Map.Entry<String, Object> obj : cartSet) {
            JSONObject jsonObj = (JSONObject) obj.getValue();
            addCart(userId, jsonObj.getInteger(PRODUCT_ID), jsonObj.getInteger(QUANTITY));
        }
        CookieUtil.delCookie(response, Const.CART.CART_COOKIE);
    }

    //登录状态下添加购物车，存储在redis和mysql
    private boolean addCart(Integer userId, Integer productId, Integer count) {
        String cartKey = createCartKey(userId);
        if (StringUtils.isEmpty(cartKey)) {
            return false;
        }
        Cart proInCart = getProductInCartByRedis(cartKey, productId);
        if (proInCart != null) { //如果购物车中已经存在该产品，则增加数量即可
            Integer newQuantity = proInCart.getQuantity() + count;
            String oldProInCartJson = JSONObject.toJSONString(proInCart);
            proInCart.setQuantity(newQuantity);
            return updateCart(cartKey, oldProInCartJson, proInCart);
        } else { //购物车汇总不存在该产品，添加到购物车即可
            if (!isProductExit(productId)){
                logger.warn("添加的商品不存在，productId={}",productId);
                return false;
            }
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(productId);
            cart.setQuantity(count);
            cart.setChecked(Const.CART.CHECKED);
            boolean isInserted = saveOrUpdateCartByMysql(cart, DbOperation.INSERT);
            if (!isInserted) {
                return false;
            }
            insertCartIntoRedis(cartKey, cart);
        }
        return true;
    }

    private boolean isProductExit(Integer productId){
        return  !CollectionUtils.isEmpty(localCacheManager.hgetAll(RedisConst.PRODUCT.INFO_PREFFIX + productId));
    }

    private JSONObject addCookieCart(HttpServletRequest request,
                                     HttpServletResponse response, Integer productId, Integer count) {
        //查询是否有该cookie
        Cookie cookieCart = CookieUtil.getCookie(request, Const.CART.CART_COOKIE);
        JSONObject cartJsonMap;
        if (cookieCart == null) {//不存在，说明是第一次添加购物车
            JSONObject cartJson = createCookieCartJson(productId, count, Const.CART.CHECKED);
            cartJsonMap = new JSONObject();
            cartJsonMap.put(String.valueOf(productId), cartJson);
        } else {//存在，说明是更新购物车
            cartJsonMap = JSONObject.parseObject(cookieCart.getValue());
            JSONObject cartJson = cartJsonMap.getJSONObject(String.valueOf(productId));
            if (cartJson == null) {//购物车中没有该产品，直接添加该产品
                cartJson = createCookieCartJson(productId, count, Const.CART.CHECKED);
            } else {//购物车中有该产品，修改其数量
                Integer newQuantity = cartJson.getInteger(QUANTITY) + count;
                cartJson.put(QUANTITY, newQuantity);
            }
            cartJsonMap.put(String.valueOf(productId), cartJson);
        }
        CookieUtil.addCookie(response, Const.CART.CART_COOKIE, cartJsonMap.toJSONString(), Const.CART.COOKIE_EXPIRE_TIME);
        return cartJsonMap;
    }

    private JSONObject createCookieCartJson(Integer productId, Integer count, Integer checked) {
        JSONObject cartJson = new JSONObject();
        cartJson.put(PRODUCT_ID, productId);
        cartJson.put(QUANTITY, count);
        cartJson.put(CHECKED, checked);
        return cartJson;
    }

    private String createCartKey(Integer userId) {
        String username = localCacheManager.hget(RedisConst.USER.USER_IDS, String.valueOf(userId));
        if (StringUtils.isEmpty(username)) {
            logger.error("通过userId查找username失败,userId={}", userId);
            return null;
        }
        return RedisConst.CART.CART_USER_PROD_PREFFIX + username;
    }

    private boolean saveOrUpdateCartByMysql(Cart cart, DbOperation operation) {
        try {
            int resultCode = -1;
            if (DbOperation.INSERT.equals(operation)) {
                resultCode = cartMapper.insertSelective(cart);
            } else if (DbOperation.UPDATE.equals(operation)) {
                resultCode = cartMapper.updateByPrimaryKeySelective(cart);
            }
            if (resultCode > Const.DB_CODE.OPERATE_FAILED) {
                return true;
            }
        } catch (Exception e) {
            logger.error("添加或者更新购物车失败！userId={},productId={}", cart.getUserId(), cart.getProductId(), e);
        }
        return false;
    }

    private Cart getProductInCartByRedis(String cartKey, Integer productId) {
        List<String> productList = localCacheManager.lrange(cartKey, 0, -1);
        Stream<String> prosStream = productList.stream();
        List<String> proFilters = prosStream.filter((s) -> (s.contains("\"productId\":" + productId)))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(proFilters)) {
            return JSONObject.parseObject(proFilters.get(0), Cart.class);
        }
        return null;
    }

    private void insertCartIntoRedis(String cartKey, Cart cart) {
        localCacheManager.lpush(cartKey, JSONObject.toJSONString(cart));
    }

    /**
     * 更新购物车
     *
     * @param userId
     * @param productId
     * @param count
     * @return
     */
    @Override
    public ServerResponse update(Integer userId, Integer productId, Integer count,
                                 HttpServletRequest request, HttpServletResponse response) {
        boolean isLogin = CheckUtil.checkIsLogin(request.getSession()).isSuccess();
        if (isLogin) {
            covertCookieCart(userId, request, response);
            if (!updateCartLogin(userId, productId, count)) {
                return ServerResponse.createByError("更新购物车失败");
            }
            return listCart(userId);
        } else {
            JSONObject cartJsonMap = updateCartCookie(productId, count, request, response);
            if (cartJsonMap == null) {
                return ServerResponse.createByError("更新购物车失败");
            }
            return listCookieCart(cartJsonMap);
        }
    }

    private boolean updateCartLogin(Integer userId, Integer productId, Integer count) {
        String cartKey = createCartKey(userId);
        Cart proInCart = getProductInCartByRedis(cartKey, productId);
        if (proInCart == null) {
            logger.error("更新购物车异常！userId={},productId={}", userId, productId);
            return false;
        }
        String oldProInCartJson = JSONObject.toJSONString(proInCart);
        proInCart.setQuantity(count);
        if (!updateCart(cartKey, oldProInCartJson, proInCart)) {
            return false;
        }
        return true;
    }

    private JSONObject updateCartCookie(Integer productId, Integer count,
                                        HttpServletRequest request, HttpServletResponse response) {
        Cookie cookieCart = CookieUtil.getCookie(request, Const.CART.CART_COOKIE);
        if (cookieCart == null) {
            return null;
        }
        JSONObject cartMapJson = JSONObject.parseObject(cookieCart.getValue());
        JSONObject cartJson = cartMapJson.getJSONObject(String.valueOf(productId));
        cartJson.put(QUANTITY, count);
        CookieUtil.addCookie(response, Const.CART.CART_COOKIE, cartMapJson.toJSONString(), Const.CART.COOKIE_EXPIRE_TIME);
        return cartMapJson;
    }

    private boolean updateCart(String cartKey, String oldProInCartJson, Cart proInCartNew) {
        String newProInCartJson = JSON.toJSONString(proInCartNew);
        boolean isUpdated = saveOrUpdateCartByMysql(proInCartNew, DbOperation.UPDATE);
        if (!isUpdated) {
            return false;
        }
        localCacheManager.lrem(cartKey, 0, oldProInCartJson);
        localCacheManager.lpush(cartKey, newProInCartJson);
        return true;
    }

    /**
     * 删除购物车中产品
     *
     * @param userId
     * @param productIds
     * @param request
     * @param response
     * @return
     */
    @Override
    public ServerResponse delete(Integer userId, String productIds,
                                 HttpServletRequest request, HttpServletResponse response) {
        boolean isLogin = CheckUtil.checkIsLogin(request.getSession()).isSuccess();
        String[] proIds = productIds.split(",");
        if (isLogin) {
            covertCookieCart(userId, request, response);
            if (!deleteCartProsInMysql(userId, proIds)) {
                return ServerResponse.createByError("删除购物车中产品失败");
            }
            if (!deleteCartProsInRedis(userId, proIds)) {
                return ServerResponse.createByError("删除购物车中产品失败");
            }
            return listCart(userId);
        } else {
            Cookie cookieCart = CookieUtil.getCookie(request, Const.CART.CART_COOKIE);
            JSONObject cartJsonMap = null;
            if (cookieCart != null) {
                cartJsonMap = JSONObject.parseObject(cookieCart.getValue());
                for (String proId : proIds)
                    cartJsonMap.remove(proId);
                CookieUtil.addCookie(response, Const.CART.CART_COOKIE, cartJsonMap.toJSONString(), Const.CART.COOKIE_EXPIRE_TIME);
            }
            return listCookieCart(cartJsonMap);
        }
    }

    private boolean deleteCartProsInMysql(Integer userId, String[] proIds) {
        Integer[] proIntIds = new Integer[proIds.length];
        for (int i = 0; i < proIds.length; i++) {
            proIntIds[i] = Integer.valueOf(proIds[i]);
        }
        try {
            int relCode = cartMapper.batchDelByUserId(userId, proIntIds);
            if (relCode > Const.DB_CODE.OPERATE_FAILED) {
                return true;
            }
        } catch (Exception e) {
            logger.error("批量删除购物车中产品失败！userId={},productIds={}", userId, proIntIds, e);
        }
        return false;
    }

    private boolean deleteCartProsInRedis(Integer userId, String[] proIds) {
        String cartKey = createCartKey(userId);
        List<String> prosInCart = localCacheManager.lrange(cartKey, 0, -1);//获取购物车所有商品
        Stream<String> prosStream = prosInCart.stream();
        // 需要被删除的条件
        Predicate<String> delPres = (s) -> (s.contains(proIds[0]));
        for (String proId : proIds) {
            delPres = delPres.or((s) -> (s.contains(proId)));
        }
        List<String> toDelPros = prosStream.filter(delPres).collect(Collectors.toList());
        long resultCode = 0;
        for (String delPros : toDelPros) {
            resultCode += localCacheManager.lrem(cartKey, 0, delPros);
        }
        return resultCode != 0;
    }

    /**
     * 选择或者不选择购物车中的产品
     *
     * @param productId
     * @param checked
     * @param request
     * @param response
     * @return
     */
    @Override
    public ServerResponse selectOrUnselect(Integer productId, Integer checked,
                                           HttpServletRequest request, HttpServletResponse response) {
        boolean isLogin = CheckUtil.checkIsLogin(request.getSession()).isSuccess();
        boolean isSuccess;
        if (isLogin) {
            User user = (User) request.getSession().getAttribute(Const.CURRENT_USER);
            Integer userId = user.getId();
            covertCookieCart(userId, request, response);
            if (null != productId) {//改变某一个product的选择状态
                isSuccess = changeChecked(userId, productId, checked);
            } else {
                isSuccess = allChangeChecked(userId, checked);
            }
            return isSuccess ? listCart(userId) : ServerResponse.createByError("修改产品确认选项失败！");
        } else {
            Cookie cookieCart = CookieUtil.getCookie(request, Const.CART.CART_COOKIE);
            if (cookieCart != null) {
                JSONObject cartJsonMap = JSONObject.parseObject(cookieCart.getValue());
                if (productId != null) {
                    JSONObject cartJson = cartJsonMap.getJSONObject(String.valueOf(productId));
                    cartJson.put(CHECKED, checked);
                } else {
                    Set<Map.Entry<String, Object>> cookieCartPros = cartJsonMap.entrySet();
                    for (Map.Entry<String, Object> obj : cookieCartPros) {
                        ((JSONObject) obj.getValue()).put(CHECKED, checked);
                    }
                }
                CookieUtil.addCookie(response, Const.CART.CART_COOKIE, cartJsonMap.toJSONString(), Const.CART.COOKIE_EXPIRE_TIME);
                return listCookieCart(cartJsonMap);
            }
            return ServerResponse.createByError("修改产品确认选项失败！");
        }
    }

    public boolean changeChecked(Integer userId, Integer productId, Integer checked) {
        String cartKey = createCartKey(userId);
        Cart proInCart = getProductInCartByRedis(cartKey, productId);
        if (proInCart == null) {
            logger.error("redis中查不到用户的购物车信息，userId={},productId={}", userId, productId);
            return false;
        }
        String oldProInCartJson = JSONObject.toJSONString(proInCart);
        proInCart.setChecked(checked);
        if (!updateCart(cartKey, oldProInCartJson, proInCart)) {
            return false;
        }
        return true;
    }

    private boolean allChangeChecked(Integer userId, Integer checked) {
        String cartKey = createCartKey(userId);
        List<String> proInCartJsonList = localCacheManager.lrange(cartKey, 0, -1);
        for (String pro : proInCartJsonList) {
            Cart proInCart = JSONObject.parseObject(pro, Cart.class);
            if (!proInCart.getChecked().equals(checked)) {
                String oldProInCartJson = JSONObject.toJSONString(proInCart);
                proInCart.setChecked(checked);
                if (!updateCart(cartKey, oldProInCartJson, proInCart)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 获取购物车中产品数量
     *
     * @param request
     * @param response
     * @return
     */
    @Override
    public ServerResponse getCartProductCount(HttpServletRequest request, HttpServletResponse response) {
        boolean isLogin = CheckUtil.checkIsLogin(request.getSession()).isSuccess();
        if (isLogin) {
            User user = (User) request.getSession().getAttribute(Const.CURRENT_USER);
            Integer userId = user.getId();
            covertCookieCart(userId, request, response);
            String cartKey = createCartKey(userId);
            long cartCount = localCacheManager.llen(cartKey);
            return ServerResponse.createBySuccess(cartCount);
        } else {
            Cookie cookieCart = CookieUtil.getCookie(request, Const.CART.CART_COOKIE);
            if (cookieCart == null) {
                return ServerResponse.createBySuccess(0);
            }
            JSONObject cartJsonMap = JSONObject.parseObject(cookieCart.getValue());
            return ServerResponse.createBySuccess(cartJsonMap.size());
        }
    }

    /**
     * 查询购物车
     *
     * @param userId
     * @return
     */
    @Override
    public ServerResponse list(Integer userId, HttpServletRequest request, HttpServletResponse response) {
        boolean isLogin = CheckUtil.checkIsLogin(request.getSession()).isSuccess();
        if (isLogin) {
            covertCookieCart(userId, request, response);
            return listCart(userId);
        } else {
            Cookie cookieCart = CookieUtil.getCookie(request, Const.CART.CART_COOKIE);
            if (cookieCart == null) {
                return ServerResponse.createByError("购物车为空！");
            }
            return listCookieCart(JSONObject.parseObject(cookieCart.getValue()));
        }
    }

    //列表显示cookie中的购物车信息
    private ServerResponse listCookieCart(JSONObject cookieCartMap) {
        CartVo cartVo;
        Set<Map.Entry<String, Object>> cookieCartPros = cookieCartMap.entrySet();
        List<Cart> carts = Lists.newArrayList();
        for (Map.Entry<String, Object> cookieCartPro : cookieCartPros) {
            JSONObject cartPro = (JSONObject) cookieCartPro.getValue();
            Cart cart = new Cart();
            cart.setProductId(cartPro.getInteger(PRODUCT_ID));
            cart.setQuantity(cartPro.getInteger(QUANTITY));
            cart.setChecked(cartPro.getInteger(CHECKED));
            carts.add(cart);
        }
        cartVo = createCartVo(carts, false);
        return ServerResponse.createBySuccess(cartVo);
    }

    @Override
    public ServerResponse listCart(Integer userId) {
        String cartKey = createCartKey(userId);
        if (StringUtils.isEmpty(cartKey)) {
            return ServerResponse.createByError("购物车为空！");
        }
        List<String> proInCarts = localCacheManager.lrange(cartKey, 0, -1);
        List<Cart> carts = parseStr2Obj(proInCarts, Cart.class);
        return ServerResponse.createBySuccess(createCartVo(carts, true));
    }

    private <E> List<E> parseStr2Obj(List<String> list, Class<E> clss) {
        List<E> objList = Lists.newArrayList();
        for (String str : list) {
            objList.add(JSONObject.parseObject(str, clss));
        }
        return objList;
    }

    private CartVo createCartVo(List<Cart> carts, boolean isLogin) {
        CartVo cartVo = new CartVo();//购物车显示Vo
        List<CartProductVo> cartProductVos = Lists.newArrayList();//购物车产品信息list
        BigDecimal cartTotalPrice = new BigDecimal(0);//购物车总价格
        int checks = 0;//购物车中产品选中数
        for (Cart cartPro : carts) {//遍历购物车中产品
            CartProductVo cartProductVo = new CartProductVo();//购物车产品信息Vo
            Integer userId = cartPro.getUserId();
            Integer productId = cartPro.getProductId();
            if (cartPro.getId() != null)
                cartProductVo.setId(cartPro.getId());
            if (userId != null)
                cartProductVo.setUserId(userId);
            Integer quantity = cartPro.getQuantity();
            cartProductVo.setProductId(productId);
            cartProductVo.setProductChecked(cartPro.getChecked());

            //查询产品详细信息
            ServerResponse proInfo = productService.getDetail(productId);
            if (!proInfo.isSuccess()) continue;
            ProductDetailVo product = (ProductDetailVo) proInfo.getData();
            Integer productStock = product.getStock();//产品库存
            cartProductVo.setProductStock(productStock);
            BigDecimal productPrice = product.getPrice();//商品单价
            cartProductVo.setLimitQuantity(Const.CART.LIMIT_NUM_SUCCESS);
            //判断购物车数量是否超过产品余量
            if (isLogin && quantity > productStock) {
                quantity = productStock;
                cartProductVo.setLimitQuantity(Const.CART.LIMIT_NUM_FAIL);
                updateCartLogin(userId, productId, productStock);//更新购物车信息
            }

            cartProductVo.setQuantity(quantity);
            cartProductVo.setProductName(product.getName());
            cartProductVo.setProductSubtitle(product.getSubtitle());
            cartProductVo.setProductMainImage(product.getMainImage());
            cartProductVo.setProductStatus(product.getStatus());
            cartProductVo.setProductPrice(productPrice);
            //设置产品总价格
            BigDecimal productTotalPrice = productPrice.multiply(new BigDecimal(quantity));
            cartProductVo.setProductTotalPrice(productTotalPrice);
            //如果已经选中，则算入总价格中
            if (cartPro.getChecked() == Const.CART.CHECKED) {
                cartTotalPrice = cartTotalPrice.add(productTotalPrice);
                checks++;
            }
            cartProductVos.add(cartProductVo);
        }
        cartVo.setCartProductVoList(cartProductVos);
        cartVo.setCartTotalPrice(cartTotalPrice);
        cartVo.setAllChecked(checks == carts.size());
        return cartVo;
    }

    @Override
    public boolean deleteAll(Integer userId) {
        try {
            int reCode = cartMapper.delByUserId(userId, Const.CART.CHECKED);
            if (reCode > Const.DB_CODE.OPERATE_FAILED){
                deleteAllInRedis(userId);
                return true;
            }
        }catch (Exception e){
            logger.error("Mysql删除购物车信息失败，userId={}",userId);
        }
        return false;
    }

    private boolean deleteAllInRedis(Integer userId){
        String cartKey = createCartKey(userId);
        List<String> prosInCart = localCacheManager.lrange(cartKey, 0, -1);//获取购物车所有商品
        Stream<String> prosStream = prosInCart.stream();
        // 需要被删除的条件
        Predicate<String> delPres = (s) -> (s.contains("\"checked\":" + Const.CART.CHECKED));
        List<String> toDelPros = prosStream.filter(delPres).collect(Collectors.toList());
        long resultCode = 0;
        for (String delPros : toDelPros) {
            resultCode += localCacheManager.lrem(cartKey, 0, delPros);
        }
        return resultCode != 0;
    }
}