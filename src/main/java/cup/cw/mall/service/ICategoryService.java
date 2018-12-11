package cup.cw.mall.service;

import cup.cw.mall.common.ServerResponse;

/**
 * created by cuiwei on 2018/11/5
 */
public interface ICategoryService {

    /**
     * 添加种类
     * @param categoryName
     * @param parentId
     * @return
     */
    ServerResponse addCategory(String categoryName, Integer parentId);

    /**
     * 修改产品种类名称
     * @param categoryName
     * @param categoryId
     * @return
     */
    ServerResponse updateCategoryName(String categoryName, Integer categoryId);

    /**
     * 查询当前节点子节点的信息，平级的不递归
     * @param categoryId
     * @return
     */
    ServerResponse getChildParallelCategory(Integer categoryId);

    /**
     * 深度查询，查询此id 下的所有后代的id（递归）
     * @param categoryId
     * @return
     */
    ServerResponse getChildAllCategory(Integer categoryId);

}
