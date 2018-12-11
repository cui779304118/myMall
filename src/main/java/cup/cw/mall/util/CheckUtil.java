package cup.cw.mall.util;

import cup.cw.mall.common.Const;
import cup.cw.mall.common.ResponseCode;
import cup.cw.mall.common.ServerResponse;
import cup.cw.mall.pojo.User;

import javax.servlet.http.HttpSession;

/**
 * created by cuiwei on 2018/10/14
 */
public class CheckUtil {
    public static ServerResponse<String> checkValid(CheckStrategy checkStrategy,String param){
        return checkStrategy.check(param);
    }
    public static ServerResponse<User> checkIsLogin(HttpSession session){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(),Const.RESPONSE_MESSAGE.NEED_LOGIN);
        }else{
            return ServerResponse.createBySuccess(user);
        }
    }
    //校验策略
    public interface CheckStrategy{
        ServerResponse<String> check(String param);
    }
}



