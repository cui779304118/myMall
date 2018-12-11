package cup.cw.mall.controller.portal;

import cup.cw.mall.cache.LocalCacheManager;
import cup.cw.mall.common.ServerResponse;
import cup.cw.mall.dao.ProductMapper;
import cup.cw.mall.service.IProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * created by cuiwei on 2018/11/11
 */
@RestController
@RequestMapping("/product")
public class ProductController {

    private static Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private LocalCacheManager localCacheManager;

    @Autowired
    private IProductService productService;

    @RequestMapping("/detail.do")
    public ServerResponse getDetail(Integer productId) {
        return productService.getDetail(productId);
    }

    @RequestMapping("/list.do")
    public ServerResponse list(@RequestParam(value = "keyword", required = false) String keyword,
                               @RequestParam(value = "categoryId", defaultValue = "0") int categoryId,
                               @RequestParam(value = "productId", defaultValue = "0") int productId,
                               @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                               @RequestParam(value = "orderBy", defaultValue = "") String orderBy,
                               @RequestParam(value = "orderItem", defaultValue = "0") Integer orderItem,// 排序关键字
                               @RequestParam(value = "orderOption", defaultValue = "0") Integer orderOption) {//升序还是降序
        return productService.searchProduct(keyword, categoryId, productId, pageNum, pageSize, orderItem, orderOption);
    }


}
