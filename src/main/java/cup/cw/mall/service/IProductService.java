package cup.cw.mall.service;

import cup.cw.mall.common.ServerResponse;
import cup.cw.mall.pojo.Product;

/**
 * created by cuiwei on 2018/10/19
 */
public interface IProductService {

    /**
     * 增加或者修改产品
     * @param product
     * @return
     */
    ServerResponse<String> saveOrUpdateProduct(Product product);

    /**
     * 设置产品状态
     * @param productId
     * @param status
     * @return
     */
    ServerResponse<String> setStatus(Integer productId, Integer status);

    /**
     * 获取商品详情
     * @param productId
     * @return
     */
    ServerResponse getDetail(Integer productId);

    /**
     * 分页获取商品信息
     * @param pageNum
     * @param pageSize
     * @return
     */
    ServerResponse getProductList(int pageNum, int pageSize, int orderItem, int orderOption);

    /**
     * 根据商品名或者id搜索商品
     * @param productName
     * @param productId
     * @param pageNum
     * @param pageSize
     * @param orderItem
     * @param orderOption
     * @return
     */
    ServerResponse searchProduct(String productName, Integer categoryId,Integer productId, int pageNum,int pageSize,int orderItem, int orderOption);


    void insertProductIntoRedis(Product product);
}
