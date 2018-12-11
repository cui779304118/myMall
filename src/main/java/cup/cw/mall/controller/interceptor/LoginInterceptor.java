package cup.cw.mall.controller.interceptor;

import com.alibaba.fastjson.JSON;
import cup.cw.mall.common.Const;
import cup.cw.mall.common.ServerResponse;
import cup.cw.mall.pojo.User;
import cup.cw.mall.util.CheckUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * created by cuiwei on 2018/11/1
 */
public class LoginInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoginInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        String requestUrl = httpServletRequest.getRequestURI();
        if (checkUrl(requestUrl)){//如果访问的路径不需要登录，则直接返回
            return true;
        }
        try{
            // 检查是否登录
            ServerResponse response = CheckUtil.checkIsLogin(httpServletRequest.getSession());
            if (!response.isSuccess()){
                retErrorMessage(response,httpServletResponse);
                return false;
            }
            // 登录成功后，检查是否是管理员
            if (requestUrl.contains(Const.MANAGER)){
                User user = (User) response.getData();
                if (user.getRole() != Const.Role.ROLE_ADMIN) {
                    retErrorMessage(ServerResponse.createByError(Const.RESPONSE_MESSAGE.NO_PERMISSION),httpServletResponse);
                    return false;
                }
            }
        }catch (Exception e){
            logger.error("检查登录状态异常！",e);
            return false;
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }

    private boolean checkUrl(String url){
        for (String urlStr : Const.NOT_INERCEPT_URL_SET){
            if (url.contains(urlStr)){
                return true;
            }
        }
        return false;
    }

    private void retErrorMessage(ServerResponse response,HttpServletResponse httpServletResponse) throws IOException {
        httpServletResponse.setCharacterEncoding("UTF-8");
        httpServletResponse.setContentType("application/json;charset=utf-8");
        String responseStr = JSON.toJSONString(response);
        httpServletResponse.getWriter().append(responseStr);
        httpServletResponse.getWriter().close();
    }

}
