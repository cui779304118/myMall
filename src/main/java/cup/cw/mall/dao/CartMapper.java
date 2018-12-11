package cup.cw.mall.dao;

import cup.cw.mall.pojo.Cart;
import org.apache.ibatis.annotations.Param;


public interface CartMapper {
    int deleteByPrimaryKey(Integer id);

    int batchDelByUserId(@Param("userId") Integer userId, @Param("ids") Integer[] ids);

    int delByUserId(@Param("userId")Integer userId,@Param("checked") Integer checked);

    int insert(Cart record);

    int insertSelective(Cart record);

    Cart selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Cart record);

    int updateByPrimaryKey(Cart record);
}