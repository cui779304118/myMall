package cup.cw.mall.dao;

import cup.cw.mall.pojo.PageModel;
import cup.cw.mall.pojo.Product;
import org.apache.ibatis.annotations.Param;

import java.sql.SQLException;
import java.util.List;

public interface ProductMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Product record);

    int insertSelective(Product record);

    Product selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Product record);

    int updateStockByCas(Product record);

    int updateByPrimaryKey(Product record);

    int saveOrUpdateSel(Product product);

    int count(Product product)throws Exception;

    List<Product> list(@Param("product") Product product,@Param("pageModel") PageModel pageModel) throws Exception;

}