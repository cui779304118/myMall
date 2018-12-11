package cup.cw.mall.service.impl;

import com.google.common.collect.Maps;
import cup.cw.mall.cache.LocalCacheManager;
import cup.cw.mall.common.Const;
import cup.cw.mall.cache.LocalCache;
import cup.cw.mall.common.RedisConst;
import cup.cw.mall.common.ServerResponse;
import cup.cw.mall.dao.UserMapper;
import cup.cw.mall.pojo.User;
import cup.cw.mall.service.IUserService;
import cup.cw.mall.util.CheckUtil;
import cup.cw.mall.util.MD5Util;
import cup.cw.mall.util.checkStrategy.EmailCheckStrategy;
import cup.cw.mall.util.checkStrategy.UsernameCheckStrategy;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.UUID;
import java.util.Map;


/**
 * created by cuiwei on 2018/10/14
 */
@Service("UserService")
public class UserServiceImpl implements IUserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UsernameCheckStrategy usernameCheckStrategy;
    @Autowired
    private EmailCheckStrategy emailCheckStrategy;
    @Autowired
    private LocalCacheManager localCacheManager;

    /**
     * 注册服务实现类
     *
     * @param user
     * @return
     */
    @Override
    public ServerResponse<String> register(User user) {
        ServerResponse<String> response = CheckUtil.checkValid(usernameCheckStrategy, user.getUsername());
        if (!response.isSuccess()) {
            return response;
        }
        response = CheckUtil.checkValid(emailCheckStrategy, user.getEmail());
        if (!response.isSuccess()) {
            return response;
        }
        user.setRole((byte) Const.Role.ROLE_CUSTOMER);//设置角色
        user.setPassword(MD5Util.MD5Encode(user.getPassword(), "UTF-8"));//将password以md5码形式保存
        try {
            userMapper.insert(user);
            insertUsertoRedis(user);
        } catch (Exception e) {
            logger.error("insert into mysql error! param:{},exception:{}", new Object[]{user.toString(), e.getMessage()});
            return ServerResponse.createByError("注册失败, 服务器异常!");
        }

        return ServerResponse.createBySuccess("register success!");
    }

    private void insertUsertoRedis(User user){
        String key = RedisConst.USER.USER_INFO_PREFFIX + user.getUsername();
        Map<String,String> userMap = Maps.newHashMap();
        if (user.getId() != null){
            userMap.put("id",String.valueOf(user.getId()));
            localCacheManager.hset(RedisConst.USER.USER_IDS,String.valueOf(user.getId()),user.getUsername());
        }
        if (!StringUtils.isEmpty(user.getUsername()))
        userMap.put("username",user.getUsername());
        if (!StringUtils.isEmpty(user.getPassword()))
        userMap.put("password",user.getPassword());
        if (!StringUtils.isEmpty(user.getPhone()))
        userMap.put("phone",user.getPhone());
        if (!StringUtils.isEmpty(user.getQuestion()))
        userMap.put("question",user.getQuestion());
        if (!StringUtils.isEmpty(user.getAnswer()))
        userMap.put("answer",user.getAnswer());
        if (user.getRole() != null)
        userMap.put("role",String.valueOf(user.getRole()));
        if (!StringUtils.isEmpty(user.getEmail())){
            userMap.put("email",user.getEmail());
            localCacheManager.sadd(RedisConst.USER.USER_EMAILS,user.getEmail());
        }
        localCacheManager.hmset(key,userMap);
    }

    /**
     * 登陆接口
     * @param username
     * @param password
     * @return
     */
    @Override
    public ServerResponse<User> login(String username, String password) {
        if (!usernameCheckStrategy.isUsernameInRedis(username)) {
            return ServerResponse.createByError("登录失败，用户名不存在！");
        }
        String passwordMD5 = MD5Util.MD5Encode(password, "UTF-8");
        return checkUsernameAndPassword(username,passwordMD5);
    }

    private ServerResponse<User> checkUsernameAndPassword(String username, String password){
        try{
            List<String> userProsList = localCacheManager.hmget(
                    RedisConst.USER.USER_INFO_PREFFIX + username,"id","password","role");
            if (password.equals(userProsList.get(1))){
                User user = new User();
                user.setId(Integer.valueOf(userProsList.get(0)));
                user.setUsername(username);
                user.setRole(Byte.valueOf(userProsList.get(2)));
                return ServerResponse.createBySuccess("登录成功!",user);
            }
        }catch (Exception e){
            logger.error("通过redis校验登录失败，username={}",username,e);
            try{
                User user = userMapper.selectByUserNameAndPassword(username, password);
                if (user != null){
                    return ServerResponse.createBySuccess("登录成功!",user);
                }
            }catch (Exception ex){
                logger.error("通过mysql校验用户名密码错误，username={}",username,e);
                ServerResponse.createByError("登录失败,服务器故障！");
            }
        }
        return ServerResponse.createByError("登录失败,密码错误！");
    }

    /**
     * 参数检验接口
     *
     * @param str
     * @param type
     * @return
     */
    @Override
    public ServerResponse<String> checkValid(String str, String type) {
        ServerResponse<String> response;
        switch (type) {
            case Const.USERNAME:
                response = CheckUtil.checkValid(usernameCheckStrategy, str);
                break;
            case Const.EMAIL:
                response = CheckUtil.checkValid(emailCheckStrategy, str);
                break;
            default:
                response = ServerResponse.createByError("没有该条校验规则!");
        }
        return response;
    }

    /**
     * 忘记密码查询问题
     *
     * @param username
     * @return
     */
    @Override
    public ServerResponse<String> selectQuestion(String username) {
        ServerResponse<String> response = CheckUtil.checkValid(usernameCheckStrategy, username);
        if (response.isSuccess()) {
            return ServerResponse.createByError("查询失败，用户名不存在！");
        }
        String question = localCacheManager.hget(RedisConst.USER.USER_INFO_PREFFIX + username,"question");
        if (StringUtils.isEmpty(question)){
            return ServerResponse.createByError("查询失败，没有该用户名对应的密保问题！");
        }
        return ServerResponse.createBySuccess(question);
    }

    /**
     * 校验问题是否正确
     *
     * @param username
     * @param question
     * @param answer
     * @return
     */
    @Override
    public ServerResponse<String> checkAnswer(String username, String question, String answer) {
        List<String> queAndAns = localCacheManager.hmget(RedisConst.USER.USER_INFO_PREFFIX + username,"question","answer");
        boolean isRight = (question.equals(queAndAns.get(0)) && answer.equals(queAndAns.get(1)));
        if (isRight) {
            String checkToken = UUID.randomUUID().toString();
            LocalCache.setKey(Const.CACHE.PREFIX_TOKEN + username, checkToken);
            return ServerResponse.createBySuccess("密保验证成功！", checkToken);
        }
        return ServerResponse.createByError("密保答案错误！");
    }


    /**
     * 通过密保重置密码
     *
     * @param username
     * @param passwordNew
     * @param questionCheckToken
     * @return
     */
    @Override
    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String questionCheckToken) {
        if (StringUtils.isEmpty(questionCheckToken)) {
            return ServerResponse.createByError("参数错误, 请重新输入questionCheckToken");
        }
        ServerResponse<String> response = CheckUtil.checkValid(usernameCheckStrategy, username);
        if (response.isSuccess()) {
            return ServerResponse.createByError("参数错误, 该用户名不存在!");
        }
        String token = LocalCache.getValue(Const.CACHE.PREFIX_TOKEN + username);
        if (StringUtils.isEmpty(token)) {
            return ServerResponse.createByError("重置密码失败，questionCheckToken已经过期");
        }
        if (!questionCheckToken.equals(token)) {
            return ServerResponse.createByError("重置密码失败, questionCheckToken错误! ");
        }
        String passwordMD5 = MD5Util.MD5Encode(passwordNew, "UTF-8");
        int resultCode = userMapper.updatePasswordByUsername(username,passwordMD5);
        localCacheManager.hset(RedisConst.USER.USER_INFO_PREFFIX + username,"password",passwordMD5);
        if (resultCode > Const.DB_CODE.OPERATE_FAILED) {
            return ServerResponse.createBySuccess("密码重置成功!");
        }
        return ServerResponse.createByError("重置密码失败，请稍后重试！");
    }

    /**
     * 登录状态重置密码
     *
     * @param passwordOld
     * @param passwordNew
     * @param username
     * @return
     */
    @Override
    public ServerResponse<String> resetPassword(String passwordOld, String passwordNew, String username) {
        ServerResponse response = checkUsernameAndPassword(username,MD5Util.MD5Encode(passwordOld, "UTF-8"));
        if (!response.isSuccess()) {
            return ServerResponse.createByError("旧密码验证错误，请重新输入！");
        }
        String passwordNewMD5 = MD5Util.MD5Encode(passwordNew, "UTF-8");
        int resultCode = userMapper.updatePasswordByUsername(username,passwordNewMD5);
        localCacheManager.hset(RedisConst.USER.USER_INFO_PREFFIX + username,"password",passwordNewMD5);
        if (resultCode > Const.DB_CODE.OPERATE_FAILED) {
            return ServerResponse.createBySuccess("恭喜您，修改密码成功!");
        }
        return ServerResponse.createByError("对不起，修改密码失败，请稍后重试！");
    }

    /**
     * 更新个人信息
     *
     * @param user
     * @return
     */
    @Override
    public ServerResponse<User> updateUserInfo(User user) {
        //username不能更改
        User userUpdate = new User();
        userUpdate.setUsername(user.getUsername());
        userUpdate.setId(user.getId());
        userUpdate.setPhone(user.getPhone());
        userUpdate.setQuestion(user.getQuestion());
        userUpdate.setAnswer(user.getAnswer());
        //email要进行校验，校验email是否已经存在
        String emailUpdate = user.getEmail();
        if (!StringUtils.isEmpty(emailUpdate)) {
            ServerResponse<String> checkResponse = CheckUtil.checkValid(emailCheckStrategy, emailUpdate);
            if (!checkResponse.isSuccess()) {
                return ServerResponse.createByError(checkResponse.getMsg());
            }
            userUpdate.setEmail(emailUpdate);
        }
        try{
            int updateCode = userMapper.updateByUserNameSelective(userUpdate);
            if (updateCode > Const.DB_CODE.OPERATE_FAILED) {
                insertUsertoRedis(userUpdate);
                return ServerResponse.createBySuccess("修改个人信息成功!", userUpdate);
            }
        }catch (Exception e){
            logger.error("更新用户信息失败，user:{}",user.toString(),e);
        }
        return ServerResponse.createByError("修改个人信息失败！请稍后重试！");
    }

    /**
     * 查询个人信息
     *
     * @param username
     * @return
     */
    @Override
    public ServerResponse<User> queryUserInfo(String username) {
        User user = new User();
        List<String> userProsList = localCacheManager.hmget(RedisConst.USER.USER_INFO_PREFFIX + username,
                "username","email","phone","question","answer");
        if (!CollectionUtils.isEmpty(userProsList)){
            user.setUsername(userProsList.get(0));
            user.setEmail(userProsList.get(1));
            user.setPhone(userProsList.get(2));
            user.setQuestion(userProsList.get(3));
            user.setAnswer(userProsList.get(4));
        }else {
            try{
                user = userMapper.selectByUsername(username);
            }catch (Exception e){
                logger.error("根据username查询user信息错误，username={}",username,e);
            }
            if (user == null){
                return ServerResponse.createByError("该用户不存在！");
            }
            user.setPassword(null);
            insertUsertoRedis(user);//回写到redis
        }
        return ServerResponse.createBySuccess(user);
    }
}
