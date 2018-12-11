package cup.cw.mall.common;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;

import java.util.Set;

/**
 * created by cuiwei on 2018/10/14
 * 常量类
 */
public class Const {

    public static final String CURRENT_USER = "currentUser";
    public static final String EMAIL = "email";
    public static final String USERNAME = "username";
    public static final String MANAGER = "manager";

    public static final Set<String> VALID_PARAMS = ImmutableSet.<String>builder()
            .add(EMAIL).add(USERNAME).build();

    public static class Role {
        public static final int ROLE_CUSTOMER = 0;//普通用户
        public static final int ROLE_ADMIN = 1;//管理员
    }

    public static class DB_CODE {
        public static final int OPERATE_SUCCESS = 1;
        public static final int OPERATE_FAILED = 0;
    }

    public static class CACHE {
        public static final String PREFIX_TOKEN = "token_";
        public static final int INTI_CAPACITY = 1000;
        public static final int MAX_CAPACITY = 10000;
    }

    //产品的状态
    public enum ProductStatusEnum{
        ON_SALE(1,"online"),OFF_SALE(0,"offline");
        private int code;
        private String value;

        ProductStatusEnum(int code, String value){
            this.code = code;
            this.value = value;
        }

        public String getValue(){
            return this.value;
        }

        public int getCode(){
            return code;
        }
    }

    public enum CategorySortCode{
        HIGN_SORT(10,"最高排序级别"),MEDIUM_SORT(5,"中等排序级别"),LOW_SORT(0,"低等排序级别");
        private int code;
        private String value;

        CategorySortCode(int code, String value){
            this.code = code;
            this.value =value;
        }

        public String getValue(){
            return this.value;
        }

        public int getCode(){
            return code;
        }
    }

    /**
     * 响应常量
     */
    public static class RESPONSE_MESSAGE{
        public static final String NEED_LOGIN = "您还没有登录，请先登录!";
        public static final String NO_PERMISSION = "对不起，您没有权限!";
        public static final String PARAMS_ERROR = "参数错误，请重新输入!";
    }
    /**
     * 分页排序方向
     */
    public static class ORDER_SETTING{
        public static final Map<Integer,String> ORDER_ITEMS = ImmutableMap.<Integer,String>builder()
            .put(0,"price").put(1,"stock").build();
        public static final Map<Integer,String> ORDER_OPTIONS = ImmutableMap.<Integer,String>builder()
            .put(0,"ASC").put(1,"DESC").build();
    }

    //interceptor不需要拦截的url
    public static Set<String> NOT_INERCEPT_URL_SET = ImmutableSet.<String>builder()
            .add("login").add("register").build();

    public interface CART{
        String CART_COOKIE = "cart_cookie";//未登录状态存购物车信息的cookie
        int COOKIE_EXPIRE_TIME = 24*60*60;//24小时
        int CHECKED = 1;//购物车状态，已经被确定
        int UN_CHECKED = 0;//购物车状态，还没有被确定
        String  LIMIT_NUM_FAIL = "LIMIT_NUM_FAIL";//限制失败
        String  LIMIT_NUM_SUCCESS = "LIMIT_NUM_SUCCESS";//限制成功
    }

    public interface Order{
        int UPDATE_RETRY_TIMES = 10;

        int ORDER_LIST_ALL = 1;
        int ORDER_LIST_NOT_SEND = 0;
    }

    public  enum PayPlatformEnum{
        ALIPAY(0,"支付宝");
        private  String value;
        private  int code;
        PayPlatformEnum(int code,String value){
            this.code   = code;
            this.value  = value;
        }

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }
    }

    public enum PayTypeEnum{
        ZAI_XIAN(0,"在线支付");
        final int code;
        final String value;

        PayTypeEnum(int code, String value){
            this.code = code;
            this.value =value;
        }
        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }

        public static String getValueByCode(int code){
            for(PayTypeEnum payTypeEnum : PayTypeEnum.values()){
                if (payTypeEnum.code == code){
                    return payTypeEnum.value;
                }
            }
            return "未知支付类型";
        }

    }

    public enum OrderStatusEnum{
        CANCELED(0,"已取消"),
        NO_PAY(10,"未支付"),
        PAID(20,"已付款"),
        SHIPPED(40,"已发货"),
        ORDER_SUCCESS(50,"订单完成"),
        ORDER_CLOSE(60,"订单关闭");

        final int code;
        final String value;

        OrderStatusEnum(int code,String value){
            this.code = code;
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }

        public static  OrderStatusEnum codeOf(int code){
            for(OrderStatusEnum orderStatusEnum : values()){
                if(orderStatusEnum.getCode() == code){
                    return  orderStatusEnum;
                }
            }
            throw  new RuntimeException("没有找到相应的枚举");
        }

    }

    public interface  AlipayCallback{
        String RESPONSE_SUCCESS = "success";
        String RESPONSE_FAILED = "failed";

        String TRADE_STATUS_WAIT_BUYER_PAY = "WAIT_BUYER_PAY";
        String TRADE_STATUS_TRADE_SUCCESS = "TRADE_SUCCESS";

    }
}
