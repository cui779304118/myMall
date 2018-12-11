package cup.cw.mall.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * created by cuiwei on 2018/11/16
 */
public class CookieUtil {
    public static Cookie getCookie(HttpServletRequest request, String key){
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies){
            if (key.equals(cookie.getName())){
                return cookie;
            }
        }
        return null;
    }

    public static void addCookie(HttpServletResponse response, String name, String value, int expireTime){
        Cookie cookie = new Cookie(name,value);
        cookie.setMaxAge(expireTime);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    public static void delCookie(HttpServletResponse response,String cookieName){
        Cookie cookie = new Cookie(cookieName,null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }


}
