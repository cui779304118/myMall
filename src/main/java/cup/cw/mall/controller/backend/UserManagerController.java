package cup.cw.mall.controller.backend;

import cup.cw.mall.common.Const;
import cup.cw.mall.common.ServerResponse;
import cup.cw.mall.pojo.User;
import cup.cw.mall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

/**
 * created by cuiwei on 2018/10/19
 *
 */
@RestController
@RequestMapping("/manager/user")
public class UserManagerController {

    @Autowired
    private IUserService userService;

    @RequestMapping(value = "/login.do", method = RequestMethod.POST)
    public ServerResponse<User> login(String username, String password, HttpSession session){
        ServerResponse<User> response = userService.login(username,password);
        if (response.isSuccess()){
            User user = response.getData();
            if (user.getRole() == Const.Role.ROLE_ADMIN){
                session.setAttribute(Const.CURRENT_USER,user);
                return response;
            }else {
                return ServerResponse.createByError("你不是管理员, 登录失败!");
            }
        }
        return response;
    }

}
