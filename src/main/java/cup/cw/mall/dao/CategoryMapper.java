package cup.cw.mall.dao;

import cup.cw.mall.pojo.Category;

import java.util.List;

public interface CategoryMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Category record);

    int insertSelective(Category record);

    Category selectByPrimaryKey(Integer id);

    List<Category> list(Category record);

    int updateByPrimaryKeySelective(Category record);

    int updateByPrimaryKey(Category record);
}