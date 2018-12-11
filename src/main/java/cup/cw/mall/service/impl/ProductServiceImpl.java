package cup.cw.mall.service.impl;

import cup.cw.mall.cache.LocalCacheManager;
import cup.cw.mall.common.Const;
import cup.cw.mall.common.RedisConst;
import cup.cw.mall.common.ServerResponse;
import cup.cw.mall.dao.CategoryMapper;
import cup.cw.mall.dao.ProductMapper;
import cup.cw.mall.dao.UserMapper;
import cup.cw.mall.pojo.Category;
import cup.cw.mall.pojo.PageModel;
import cup.cw.mall.pojo.Product;
import cup.cw.mall.service.IProductService;
import cup.cw.mall.util.DateTimeUtil;
import cup.cw.mall.util.PropertiesUtil;
import cup.cw.mall.vo.ProductDetailVo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * created by cuiwei on 2018/10/19
 */
@Service("productServiceImpl")
public class ProductServiceImpl implements IProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private LocalCacheManager localCacheManager;

    /**
     * 增加或者修改产品
     *
     * @param product
     * @return
     */
    @Override
    public ServerResponse<String> saveOrUpdateProduct(Product product) {
        if (product == null || product.getSubImages() == null) {
            return ServerResponse.createByError("保存或者更新产品信息失败！参数错误！");
        }
        String[] subImageArray = product.getSubImages().split(",");
        if (subImageArray.length > 0) {
            product.setMainImage(subImageArray[0]);
        }
        product.setStatus((byte) Const.ProductStatusEnum.ON_SALE.getCode());
        try {
            int resultCode = productMapper.saveOrUpdateSel(product);
            if (resultCode > Const.DB_CODE.OPERATE_FAILED) {
                insertProductIntoRedis(product);
                return ServerResponse.createBySuccess("保存或者更新产品信息成功！");
            }
        } catch (Exception e) {
            logger.error("保存或者更新产品信息到数据库失败！product={}", product.toString(), e);
        }
        return ServerResponse.createByError("保存或者更新产品信息失败！!");
    }

    public void insertProductIntoRedis(Product product) {
        Map<String, String> productMap = new HashMap<>();
        if (null != product.getId())
            productMap.put("id", String.valueOf(product.getId()));
        if (null != product.getCategoryId())
            productMap.put("categoryId", String.valueOf(product.getCategoryId()));
        if (!StringUtils.isEmpty(product.getName()))
            productMap.put("name", product.getName());
        if (!StringUtils.isEmpty(product.getSubtitle()))
            productMap.put("subtitle", product.getSubtitle());
        if (!StringUtils.isEmpty(product.getMainImage()))
            productMap.put("mainImage", product.getMainImage());
        if (!StringUtils.isEmpty(product.getSubImages()))
            productMap.put("subImages", product.getSubImages());
        if (null != product.getPrice())
            productMap.put("price", String.valueOf(product.getPrice()));
        if (null != product.getStock())
            productMap.put("stock", String.valueOf(product.getStock()));
        if (null != product.getStatus())
            productMap.put("status", String.valueOf(product.getStatus()));
        localCacheManager.hmset(RedisConst.PRODUCT.INFO_PREFFIX + product.getId(), productMap);
    }

    /**
     * 设置产品状态
     *
     * @param productId
     * @param status
     * @return
     */
    @Override
    public ServerResponse<String> setStatus(Integer productId, Integer status) {
        Product product = new Product();
        product.setId(productId);
        product.setStatus((byte) status.intValue());
        try {
            int updateCode = productMapper.updateByPrimaryKeySelective(product);
            if (updateCode > Const.DB_CODE.OPERATE_FAILED) {
                localCacheManager.hset(RedisConst.PRODUCT.INFO_PREFFIX + productId,"status",String.valueOf(status));
                return ServerResponse.createBySuccess("设置产品状态成功！");
            }
        } catch (Exception e) {
            logger.error("设置产品状态失败！productId={}", productId, e);
        }
        return ServerResponse.createByError("设置产品状态失败!");
    }

    /**
     * 获取商品详情
     *
     * @param productId
     * @return
     */
    @Override
    public ServerResponse getDetail(Integer productId) {
        Product product = new Product();
        Map<String,String> productMap = localCacheManager.hgetAll(RedisConst.PRODUCT.INFO_PREFFIX + productId);
        ProductDetailVo productDetailVo = new ProductDetailVo();
        if (CollectionUtils.isEmpty(productMap)) {
            try {
                product = productMapper.selectByPrimaryKey(productId);
                if (null == product){
                    return ServerResponse.createByError("该商品不存在或者已经删除!");
                }
                insertProductIntoRedis(product);//回写product到redis
                productDetailVo = assembleProductDetailVo(product);
            }catch (Exception e){
                logger.error("从数据库查询product失败，productId={}",productId,e);
            }
        }else {
            productDetailVo = assembleProductDetailVo(productMap);
        }
        return ServerResponse.createBySuccess(productDetailVo);
    }

    private ProductDetailVo assembleProductDetailVo(Product product) {
        ProductDetailVo productDetailVo = new ProductDetailVo();
        productDetailVo.setId(product.getId());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setName(product.getName());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setStock(product.getStock());
        productDetailVo.setStatus(product.getStatus().intValue());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImages(product.getSubImages());
        productDetailVo.setSubtitle(product.getSubtitle());

        productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://weicui.top/"));

        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        Integer parentCategoryId = (category == null) ? 0 : category.getParentId();
        productDetailVo.setParentCategoryId(parentCategoryId);
        productDetailVo.setCreateTime(DateTimeUtil.dateToStr(product.getCreateTime()));
        productDetailVo.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));
        return productDetailVo;
    }

    private ProductDetailVo assembleProductDetailVo(Map<String,String> productMap) {
        ProductDetailVo productDetailVo = new ProductDetailVo();
        productDetailVo.setId(Integer.valueOf(productMap.get("id")));
        productDetailVo.setCategoryId(Integer.valueOf(productMap.get("categoryId")));
        productDetailVo.setName(productMap.get("name"));
        productDetailVo.setPrice(new BigDecimal(productMap.get("price")));
        productDetailVo.setStock(Integer.valueOf(productMap.get("stock")));
        productDetailVo.setStatus(Integer.valueOf(productMap.get("status")));
        productDetailVo.setMainImage(productMap.get("mainImage"));
        productDetailVo.setSubImages(productMap.get("subImages"));
        productDetailVo.setSubtitle(productMap.get("subtitle"));

        productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix", "http://weicui.top/"));

        Category category = categoryMapper.selectByPrimaryKey(Integer.valueOf(productMap.get("categoryId")));
        Integer parentCategoryId = (category == null) ? 0 : category.getParentId();
        productDetailVo.setParentCategoryId(parentCategoryId);
        return productDetailVo;
    }

    /**
     * 分页获取商品信息
     *
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public ServerResponse getProductList(int pageNum, int pageSize, int orderItem, int orderOption) {
//        int totalRecords = 0;
//        try {
//            totalRecords = productMapper.count(new Product());
//        } catch (Exception e) {
//            logger.error("查询产品数量异常！", e);
//            return ServerResponse.createBySuccess("数据库异常！");
//        }
//        if (totalRecords == 0) {
//            logger.warn("库存里没有商品，请核对详情！");
//            return ServerResponse.createBySuccess("该商品已下架或者已经被删除！");
//        }
//        PageModel pageModel = PageModel.builder().pageNum(pageNum).
//                pageSize(pageSize).totalRecords(totalRecords).
//                orderItem(Const.ORDER_SETTING.ORDER_ITEMS.get(orderItem)).
//                orderOption(Const.ORDER_SETTING.ORDER_OPTIONS.get(orderOption)).build();
//        List<Product> products = null;
//        try {
//            Product searchCondition = new Product();
//            products = productMapper.list(searchCondition, pageModel);
//        } catch (Exception e) {
//            logger.error("查询产品列表异常！", e);
//            return ServerResponse.createBySuccess("查询失败，数据库异常！");
//        }
//        pageModel.setDataList(getProductDetailVoList(products));
//        return ServerResponse.createBySuccess(pageModel);
        return searchProduct(null,0,0,pageNum,pageSize,orderItem,orderOption);
    }

    /**
     * 根据商品名或者id搜寻商品
     *
     * @param productName
     * @param productId
     * @param pageNum
     * @param pageSize
     * @param orderItem
     * @param orderOption
     * @return
     */
    @Override
    public ServerResponse searchProduct(String productName, Integer categoryId, Integer productId,  int pageNum, int pageSize, int orderItem, int orderOption) {
        PageModel pageModel = null;
        if (productId != 0) {//用productId精确查询
            ProductDetailVo productDetailVo = (ProductDetailVo) getDetail(productId).getData();
            List<ProductDetailVo> detailVoList = new ArrayList<>();
            detailVoList.add(productDetailVo);
            pageModel = PageModel.builder().build();
            pageModel.setDataList(detailVoList);
        } else {//通过productName来模糊查找
            Product product = new Product();
            if (categoryId != null && categoryId != 0){
                product.setCategoryId(categoryId);
            }
            if (!StringUtils.isEmpty(productName)){
                productName = "%" + productName + "%";
                product.setName(productName);
            }
            try {
                int totalRecords = productMapper.count(product);
                if (totalRecords == 0) {
                    return ServerResponse.createByError("该商品已下架或者已经被删除！");
                }
                pageModel = PageModel.builder().pageNum(pageNum)
                        .pageSize(pageSize).totalRecords(totalRecords)
                        .orderItem(Const.ORDER_SETTING.ORDER_ITEMS.get(orderItem))
                        .orderOption(Const.ORDER_SETTING.ORDER_OPTIONS.get(orderOption)).build();
                List<Product> products = productMapper.list(product, pageModel);
                pageModel.setDataList(getProductDetailVoList(products));
            } catch (Exception e) {
                logger.error("查询product失败,productName:{}", new Object[]{productName}, e);
                return ServerResponse.createByError("查询失败，数据库异常！");
            }
        }
        return ServerResponse.createBySuccess(pageModel);
    }

    private List<ProductDetailVo> getProductDetailVoList(List<Product> products) {
        List<ProductDetailVo> productDetailVos = new ArrayList<>();
        for (Product pro : products) {
            productDetailVos.add(assembleProductDetailVo(pro));
        }
        return productDetailVos;
    }
}
