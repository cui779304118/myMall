package cup.cw.mall.service;

import cup.cw.mall.common.ServerResponse;
import cup.cw.mall.pojo.Product;
import cup.cw.mall.pojo.User;

/**
 * created by cuiwei on 2018/10/14
 * 服务接口
 */
public interface IUserService {
    /**
     * 注册服务
     *
     * @param user
     * @return
     */
    ServerResponse<String> register(User user);

    /**
     * 登录服务
     *
     * @param username
     * @param password
     * @return
     */
    ServerResponse<User> login(String username, String password);

    /**
     * 前端参数校验服务
     *
     * @param str
     * @param type
     * @return
     */
    ServerResponse<String> checkValid(String str, String type);

    /**
     * 忘记密码，查询问题
     *
     * @param username
     * @return
     */
    ServerResponse<String> selectQuestion(String username);

    /**
     * 校验问题是否正确
     *
     * @param username
     * @param question
     * @param answer
     * @return
     */
    ServerResponse<String> checkAnswer(String username, String question, String answer);

    /**
     * 通过密保重置密码
     *
     * @param username
     * @param passwordNew
     * @param questionCheckToken
     * @return
     */
    ServerResponse<String> forgetResetPassword(String username, String passwordNew, String questionCheckToken);

    /**
     * 登录状态重置密码
     * @param passwordOld
     * @param passwordNew
     * @param username
     * @return
     */
    ServerResponse<String> resetPassword(String passwordOld, String passwordNew, String username);

    /**
     * 更新个人信息
     * @param user
     * @return
     */
    ServerResponse<User> updateUserInfo(User user);

    /**
     * 查询个人信息
     * @param userName
     * @return
     */
    ServerResponse<User> queryUserInfo(String userName);


}
