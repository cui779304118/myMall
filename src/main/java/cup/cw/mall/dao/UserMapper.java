package cup.cw.mall.dao;

import cup.cw.mall.pojo.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    User selectByUsername(String username);

    int updateByUserNameSelective(User record);

    int updateByPrimaryKey(User record);

    int checkUsername(String name);

    int checkEmail(String email);

    User selectByUserNameAndPassword(@Param("username") String username, @Param("password") String passwrod);

    String selectQuestionByUsername(String username);

    int checkAnswer(@Param("username") String username, @Param("question") String question, @Param("answer") String answer);

    int updatePasswordByUsername(@Param("username") String username, @Param("password") String password);

}