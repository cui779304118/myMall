package cup.cw.mall.common;

/**
 * created by cuiwei on 2018/11/9
 * redis相关的常量
 */
public class RedisConst {
    //user相关的key
    public interface USER{
        String USER_INFO_PREFFIX = "user:info:";//用户信息前缀，后面加上userName
        String USER_EMAILS = "user:emails";//用户邮箱，用于校验，集合结构
        String USER_IDS = "user:ids";//用户名和id的对应表，hash结构
    }

    public interface PRODUCT{
        String INFO_PREFFIX = "product:info:";
    }

    public interface CATEGORY{
        String INFO_PREFFIX = "category:info:";
    }

    public interface  CART{
        String CART_USER_PROD_PREFFIX = "cart:";//用户购物车，后面加上用户名，list结构，例如：cart:cuiwei;
    }

    public interface SHIPPING{
        String SHIPPING_USER_PREFFIX = "shipping:";//用户地址信息，后面加上用户名，hash结构，例如：shipping:cuiwei;field为shippingId
    }

    public interface ORDER{
        String ORDER_PREFFIX = "order:";//用户订单信息，后面加上用户名，hash结构，例如：order:cuiwei;field为orderId
        String ORDER_ITEM_PREFFIX = "order:item:";//订单详情，后面加上orderId,list结构
        String ORDERS_KEY = "orders:all";//存储所有订单,list结构
        String ORDER_NOSEND_KEY = "orders:nosend";//为发货订单，list结构
    }

    public interface PAY_INFO{
        String USER_PREFFIX = "pay:info:";//用户支付信息，后面加上用户名，list结构


    }

}
