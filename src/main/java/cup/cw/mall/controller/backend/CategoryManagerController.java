package cup.cw.mall.controller.backend;

import cup.cw.mall.common.ServerResponse;
import cup.cw.mall.service.ICategoryService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * created by cuiwei on 2018/11/5
 */
@RequestMapping(value = "/manager/category")
@RestController
public class CategoryManagerController {

    private static Logger logger = LoggerFactory.getLogger(CategoryManagerController.class);

    @Autowired
    private ICategoryService categoryService;

    /**
     * 添加种类
     *
     * @param categoryName
     * @param parentId
     * @return
     */
    @RequestMapping(value = "add_category.do")
    public ServerResponse addCategory(String categoryName, @RequestParam(value = "parentId", defaultValue = "0") int parentId) {
        if (StringUtils.isEmpty(categoryName)){
            return ServerResponse.createByError("添加产品种类参数异常！");
        }
        return categoryService.addCategory(categoryName, parentId);
    }

    /**
     * 修改种类名称
     * @param categoryName
     * @param categoryId
     * @return
     */
    @RequestMapping(value = "set_categoryName.do")
    public ServerResponse setCategoryName(String categoryName, Integer categoryId){
        if (StringUtils.isEmpty(categoryName) || categoryId == null){
            return ServerResponse.createByError("修改产品种类名称失败，参数异常！");
        }
        return categoryService.updateCategoryName(categoryName,categoryId);
    }

    /**
     * 查询当前节点子节点的信息，平级的不递归
     * @param categoryId
     * @return
     */
    @RequestMapping(value = "get_category.do")
    public ServerResponse getChildParalleCategory(@RequestParam(value = "categoryId",defaultValue = "0" )Integer categoryId){
        return categoryService.getChildParallelCategory(categoryId);
    }

    /**
     * 深度查询，查询此id下的所有后代的id
     * @param categoryId
     * @return
     */
    @RequestMapping(value = "get_deep_category.do")
    public ServerResponse getChildDeepCategories(@RequestParam(value = "categoryId",defaultValue = "0" )Integer categoryId){
        return categoryService.getChildAllCategory(categoryId);
    }

}
