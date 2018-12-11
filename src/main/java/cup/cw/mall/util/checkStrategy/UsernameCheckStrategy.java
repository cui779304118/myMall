package cup.cw.mall.util.checkStrategy;

import cup.cw.mall.cache.LocalCacheManager;
import cup.cw.mall.common.Const;
import cup.cw.mall.common.RedisConst;
import cup.cw.mall.common.ServerResponse;
import cup.cw.mall.dao.UserMapper;
import cup.cw.mall.util.CheckUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * created by cuiwei on 2018/10/14
 */
//校验username
@Component
public class UsernameCheckStrategy implements CheckUtil.CheckStrategy {

    private static Logger logger = LoggerFactory.getLogger(UsernameCheckStrategy.class);

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private LocalCacheManager localCacheManager;

    @Override
    public ServerResponse<String> check(String username) {
        boolean isExist = false;
        try{
            isExist = isUsernameInRedis(username);
        }catch (Exception e){
            logger.error("redis 查询用户名失败！", e);
            isExist = isUserNameInMysql(username);
        }
        if (isExist){
            return ServerResponse.createByError("该用户名已经被注册，请重新输入!");
        }else{
            return ServerResponse.createBySuccess("用户名校验成功!");
        }
    }

    public boolean isUsernameInRedis(String name){
        return localCacheManager.exists(RedisConst.USER.USER_INFO_PREFFIX + name);
    }

    public boolean isUserNameInMysql(String name){
        return Const.DB_CODE.OPERATE_FAILED == userMapper.checkUsername(name);
    }
}
