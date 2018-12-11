package cup.cw.mall.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import cup.cw.mall.cache.LocalCacheManager;
import cup.cw.mall.common.Const;
import cup.cw.mall.common.RedisConst;
import cup.cw.mall.common.ServerResponse;
import cup.cw.mall.dao.CategoryMapper;
import cup.cw.mall.pojo.Category;
import cup.cw.mall.service.ICategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;


/**
 * created by cuiwei on 2018/11/5
 */
@Service("categoryService")
public class CategoryServiceImpl implements ICategoryService {

    private static Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private LocalCacheManager localCacheManager;

    @Override
    public ServerResponse addCategory(String categoryName, Integer parentId) {
        Category category = new Category();
        category.setParentId(parentId);
        category.setName(categoryName);
        category.setStatus((byte)Const.ProductStatusEnum.ON_SALE.getCode());
        category.setSortOrder(Const.CategorySortCode.MEDIUM_SORT.getCode());//默认排序级别是中等级别
        try{
            int resultCode = categoryMapper.insert(category);
            if (resultCode > Const.DB_CODE.OPERATE_FAILED){
                insertCategoryIntoRedis(category);
                return ServerResponse.createBySuccess("添加产品种类成功！");
            }
        }catch (Exception e){
            logger.error("mysql添加产品失败，categoryName={},parentId={}",categoryName,parentId,e);
            return ServerResponse.createByError("服务器异常，添加产品种类失败！");
        }
        return ServerResponse.createByError("添加产品种类失败！");

    }

    private void insertCategoryIntoRedis(Category category){
        Map<String,String> cateMap = new HashMap<>();
        cateMap.put("id",String.valueOf(category.getId()));
        cateMap.put("parentId",String.valueOf(category.getParentId()));
        cateMap.put("name",category.getName());
        cateMap.put("status",String.valueOf(category.getStatus()));
        cateMap.put("sortOrder",String.valueOf(category.getSortOrder()));
        localCacheManager.hmset(RedisConst.CATEGORY.INFO_PREFFIX + category.getId(),cateMap);
    }


    @Override
    public ServerResponse updateCategoryName(String categoryName, Integer categoryId) {
       Category category = new Category();
       category.setName(categoryName);
       category.setId(categoryId);
       try{
           int resultCode = categoryMapper.updateByPrimaryKeySelective(category);
           if (resultCode > Const.DB_CODE.OPERATE_FAILED){
               localCacheManager.hset(RedisConst.CATEGORY.INFO_PREFFIX + categoryId,"name",categoryName);
               return ServerResponse.createBySuccess("修改产品种类名称成功！");
           }
       } catch (Exception e){
           logger.error("mysql修改产品名称异常，categoryName={},categoryId={}",categoryName,categoryId,e);
           return ServerResponse.createBySuccess("服务器异常，修改产品种类名称失败！");
       }
       return ServerResponse.createByError("修改产品种类名称失败！");

    }

    @Override
    public ServerResponse getChildParallelCategory(Integer categoryId) {
        Category category = new Category();
        category.setParentId(categoryId);

        List<Category> categories = null;
        try{
            categories = categoryMapper.list(category);
            if (CollectionUtils.isEmpty(categories)){
                return ServerResponse.createByError("未找到当前分类的子分类");
            }
            sortCategories(categories);//根据sortOrder排序
        }catch (Exception e){
            logger.error("mysql 通过parentId 查询产品失败！parentId={}",categoryId,e);
            return ServerResponse.createByError("服务器异常，查询失败！");
        }
        return ServerResponse.createBySuccess(categories);
    }

    @Override
    public ServerResponse getChildAllCategory(Integer categoryId) {
        //用set是为了防止有交集，会重复
        JSONObject categories = new JSONObject();
        categories = findChildCategories(categories,categoryId);
        if (CollectionUtils.isEmpty(categories)){
            return ServerResponse.createByError("未找到当前分类的子分类");
        }
        return ServerResponse.createBySuccess(categories);
    }

    private void sortCategories(List<Category> categories){//从大到小排序
        Collections.sort(categories, new Comparator<Category>() {
            @Override
            public int compare(Category o1, Category o2) {
                return o2.getSortOrder() - o1.getSortOrder();
            }
        });
    }

    private JSONObject findChildCategories(JSONObject curLevel, Integer categoryId){
        try {
            Category category = categoryMapper.selectByPrimaryKey(categoryId);
            if (category == null){
                return curLevel;
            }
            curLevel.put("curCatg",category);
            JSONArray subCate = new JSONArray();//子类
            Category categoryCondition = new Category();
            categoryCondition.setParentId(categoryId);
            List<Category> categories = categoryMapper.list(categoryCondition);
            sortCategories(categories);
            for (Category categorySel : categories){
                JSONObject curCatg = new JSONObject();
                curCatg.put("curCatg",categorySel);
                subCate.add(curCatg);
                findChildCategories(curCatg,categorySel.getId());
            }
            curLevel.put("subCatg",subCate);
        }catch (Exception e){
            logger.error("mysql 查询category失败！categoryId={}",categoryId,e);
        }
        return curLevel;
    }
}
