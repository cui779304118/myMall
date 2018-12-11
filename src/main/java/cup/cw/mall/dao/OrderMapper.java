package cup.cw.mall.dao;

import cup.cw.mall.pojo.Order;
import cup.cw.mall.pojo.PageModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Order record);

    int insertSelective(Order record);

    Order selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Order record);

    int updateByPrimaryKey(Order record);

    List<Order> selectSelective(Order order);

    List<Order> listByUserId(@Param("userId") Integer userId, @Param("pageModel")PageModel pageModel);
}