package cup.cw.mall.dao;

import cup.cw.mall.pojo.PageModel;
import cup.cw.mall.pojo.Shipping;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ShippingMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Shipping record);

    int insertSelective(Shipping record);

    Shipping selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Shipping record);

    int updateByPrimaryKey(Shipping record);

    int countByPrimaryKey(Integer userId);

    List<Shipping> list(PageModel pageModel);
}