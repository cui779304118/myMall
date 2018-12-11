package cup.cw.mall.util.checkStrategy;

import cup.cw.mall.cache.LocalCacheManager;
import cup.cw.mall.common.Const;
import cup.cw.mall.common.RedisConst;
import cup.cw.mall.common.ServerResponse;
import cup.cw.mall.dao.UserMapper;
import cup.cw.mall.util.CheckUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * created by cuiwei on 2018/10/14
 */
//校验邮箱
@Component
public class EmailCheckStrategy implements CheckUtil.CheckStrategy{

    //^表明一行以什么开头；^[0-9a-z]表明要以数字或小写字母开头；\\w*表明匹配任意个大写小写字母或数字或下划线
    //***.***.***格式的域名，其中*为小写字母或数字;第一个括号代表有至少一个***.匹配单元，而[0-9a-z]$表明以小写字母或数字结尾
    private static final String EMAIL_CHECK_REGEX = "^[0-9a-z]+\\w*@([0-9a-z]+\\.)+[0-9a-z]+$";

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LocalCacheManager localCacheManager;

    @Override
    public ServerResponse<String> check(String email) {
        if (!isCorrectFormat(email)){
            return ServerResponse.createByError("邮箱格式不正确!");
        }
//        int resultCode = userMapper.checkEmail(email);
        if (isEmailInRedis(email)) {
            return ServerResponse.createByError("该邮箱已经被注册，请重新输入!");
        }
        return ServerResponse.createBySuccess("邮箱校验成功!");
    }
    private boolean isCorrectFormat(String email){
        return email.matches(EMAIL_CHECK_REGEX);
    }

    private boolean isEmailInRedis(String email){
        return localCacheManager.sismember(RedisConst.USER.USER_EMAILS,email);
    }
}
