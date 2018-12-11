package cup.cw.mall.controller.portal;

import cup.cw.mall.common.Const;
import cup.cw.mall.common.ResponseCode;
import cup.cw.mall.common.ServerResponse;
import cup.cw.mall.pojo.User;
import cup.cw.mall.service.IUserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

/**
 * created by cuiwei on 2018/10/14
 */
@RestController
@RequestMapping("/user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private IUserService userService;

    /**
     * 注册接口
     *
     * @return
     */
    @RequestMapping(value = "/register.do", method = RequestMethod.POST)
    public ServerResponse<String> register(User user) {
        String username = user.getUsername();
        String password = user.getPassword();
        String email = user.getEmail();

        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password) || StringUtils.isEmpty(email)) {
            logger.warn("param error, param={}", new Object[]{user.toString()});
            return ServerResponse.createByError("param error!");
        }
        return userService.register(user);
    }

    /**
     * 登陆接口
     *
     * @param username
     * @param password
     * @param session
     * @return
     */
    @RequestMapping(value = "/login.do")
    public ServerResponse<User> login(String username, String password, HttpSession session) {
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            return ServerResponse.createByError("username or password is empty!");
        }
        ServerResponse<User> response = userService.login(username, password);
        User user = null;
        if (response.isSuccess() && (user = response.getData()) != null) {
            session.setAttribute(Const.CURRENT_USER, user);
        }
        return response;
    }

    /**
     * 登出接口
     *
     * @param session
     * @return
     */
    @RequestMapping(value = "/logout.do", method = RequestMethod.POST)
    public ServerResponse<String> logout(HttpSession session) {
//        if (session == null || session.getAttribute(Const.CURRENT_USER) == null) {
//            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(),Const.RESPONSE_MESSAGE.NEED_LOGIN);
//        }
        session.invalidate();
        return ServerResponse.createBySuccess("logout success!");
    }

    /**
     * 参数校验接口
     *
     * @param str
     * @param type
     * @return
     */
    @RequestMapping(value = "/check_valid.do", method = RequestMethod.POST)
    public ServerResponse<String> checkValid(String str, String type) {
        if (StringUtils.isEmpty(str)){
            return ServerResponse.createByError("check param is empty, please input again!");
        }
        if (!Const.VALID_PARAMS.contains(type)) {
            return ServerResponse.createByError("no such check type, please input again!");
        }
        return userService.checkValid(str, type);
    }

    /**
     * 获取用户信息接口
     *
     * @param session
     * @return
     */
    @RequestMapping(value = "get_user_info.do", method = RequestMethod.POST)
    public ServerResponse<User> getUserInfo(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
//        if (user == null) {
//            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(),Const.RESPONSE_MESSAGE.NEED_LOGIN);
//        } else {
//            return ServerResponse.createBySuccess(user);
//        }
        return ServerResponse.createBySuccess(user);
    }

    /**
     * 获取忘记密码提示
     *
     * @param username
     * @return
     */
    @RequestMapping(value = "forget_get_question.do", method = RequestMethod.POST)
    public ServerResponse<String> forgetGetQuestion(String username) {
        if (StringUtils.isEmpty(username)) {
            return ServerResponse.createByError("the param is empty, please input!");
        }
        return userService.selectQuestion(username);
    }

    /**
     * 校验密码答案
     * @param username
     * @param question
     * @param answer
     * @return
     */
    @RequestMapping(value = "/forget_check_answer.do", method = RequestMethod.POST)
    public ServerResponse<String> forgetCheckAnswer(String username, String question, String answer) {
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(question)
                || StringUtils.isEmpty(answer)) {
            return ServerResponse.createByError("非法请求！");
        }
        return userService.checkAnswer(username, question, answer);
    }

    /**
     * 根据密保检验结果，重置密码
     * @param username
     * @param passwordNew
     * @param questionCheckToken
     * @return
     */
    @RequestMapping(value = "/forget_reset_password.do", method = RequestMethod.POST)
    public ServerResponse<String> forgetRestPassword(String username, String passwordNew, String questionCheckToken) {
        return userService.forgetResetPassword(username, passwordNew, questionCheckToken);
    }

    /**
     * 登录态修改密码
     * @param passwordOld
     * @param passwordNew
     * @param session
     * @return
     */
    @RequestMapping(value = "/reset_password.do", method = RequestMethod.POST)
    public ServerResponse<String> resetPassword(String passwordOld, String passwordNew, HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
//        if (user == null) {
//            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(),Const.RESPONSE_MESSAGE.NEED_LOGIN);
//        }
        return userService.resetPassword(passwordOld, passwordNew, user.getUsername());
    }

    /**
     * 更新个人信息
     * @param session
     * @param user
     * @return
     */
    @RequestMapping(value = "/update_information.do", method = RequestMethod.POST)
    public ServerResponse<User> updateInformation(HttpSession session,User user){
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
//        if (currentUser == null) {
//            return ServerResponse.createByError(ResponseCode.NEED_LOGIN.getCode(),Const.RESPONSE_MESSAGE.NEED_LOGIN);
//        }
        user.setId(currentUser.getId());
        user.setUsername(currentUser.getUsername());
        ServerResponse<User> response = userService.updateUserInfo(user);
        if (response.isSuccess()){//更新session中的内容
            session.setAttribute(Const.CURRENT_USER,response.getData());
        }
        return response;
    }

    /**
     * 获取个人信息
     * @param session
     * @return
     */
    @RequestMapping(value = "/get_information.do",method = RequestMethod.POST)
    public ServerResponse<User> getInformation(HttpSession session){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        return userService.queryUserInfo(user.getUsername());
    }

}
