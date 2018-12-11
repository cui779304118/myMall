package cup.cw.mall.controller.backend;

import cup.cw.mall.common.Const;
import cup.cw.mall.common.ResponseCode;
import cup.cw.mall.common.ServerResponse;
import cup.cw.mall.pojo.Product;
import cup.cw.mall.service.IFileService;
import cup.cw.mall.service.IProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


/**
 * created by cuiwei on 2018/10/19
 */
@RestController
@RequestMapping(value = "manager/product")
public class ProductManagerController {

    private final static Logger logger = LoggerFactory.getLogger(ProductManagerController.class);

    @Autowired
    private IProductService productService;
    @Autowired
    private IFileService fileService;

    @RequestMapping(value = "save.do", method = RequestMethod.POST)
    public ServerResponse<String> setProduct(Product product, HttpSession session) {
        return productService.saveOrUpdateProduct(product);
    }

    @RequestMapping(value = "/set_sale_status.do", method = RequestMethod.POST)
    public ServerResponse setSaleStatus(HttpSession session, Integer productId, Integer status) {
//        ServerResponse checkResponse = CheckUtil.checkIsLogin(session);
//        if (!checkResponse.isSuccess()) {
//            return checkResponse;
//        }
//        User user = (User) checkResponse.getData();
//        if (user.getRole() != Const.Role.ROLE_ADMIN) {
//            return ServerResponse.createByError(Const.RESPONSE_MESSAGE.NO_PERMISSION);
//        }
        if (productId == null || status == null) {
            return ServerResponse.createByError(Const.RESPONSE_MESSAGE.PARAMS_ERROR);
        }
        return productService.setStatus(productId, status);
    }

    /**
     * 获取商品详情
     */
    @RequestMapping(value = "/detail.do", method = RequestMethod.POST)
    public ServerResponse getDetail(HttpSession session, Integer productId) {
//        ServerResponse checkResponse = CheckUtil.checkIsLogin(session);
//        if (!checkResponse.isSuccess()) {
//            return checkResponse;
//        }
//        User user = (User) checkResponse.getData();
//        if (user.getRole() != Const.Role.ROLE_ADMIN) {
//            return ServerResponse.createByError(Const.RESPONSE_MESSAGE.NO_PERMISSION);
//        }
        if (productId == null) {
            return ServerResponse.createByError(ResponseCode.ILLEGAL_ARGUMENT.getCode(), Const.RESPONSE_MESSAGE.PARAMS_ERROR);
        }
        return productService.getDetail(productId);
    }

    /**
     * 分页显示所有商品
     * TODO 研究分页插件的使用
     */
    @RequestMapping(value = "/list.do", method = RequestMethod.POST)
    public ServerResponse getList(HttpSession session, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                  @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                                  @RequestParam(value = "orderItem", defaultValue = "0") int orderItem,
                                  @RequestParam(value = "orderOption", defaultValue = "0") int orderOption) {
//        ServerResponse checkResponse = CheckUtil.checkIsLogin(session);
//        if (!checkResponse.isSuccess()) {
//            return checkResponse;
//        }
//        User user = (User) checkResponse.getData();
//        if (user.getRole() != Const.Role.ROLE_ADMIN) {
//            return ServerResponse.createByError(Const.RESPONSE_MESSAGE.NO_PERMISSION);
//        }
        return productService.getProductList(pageNum, pageSize, orderItem, orderOption);
    }

    /**
     * 通过productId或者productName来查询商品
     */
    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public ServerResponse search(
            @RequestParam(value = "categoryId", defaultValue = "0") int categoryId,
            @RequestParam(value = "productId", defaultValue = "0") int productId,
            @RequestParam(value = "productName", defaultValue = "") String productName,
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "orderItem", defaultValue = "0") int orderItem,
            @RequestParam(value = "orderOption", defaultValue = "0") int orderOption) {
//        ServerResponse checkResponse = CheckUtil.checkIsLogin(session);
//        if (!checkResponse.isSuccess()) {
//            return checkResponse;
//        }
//        User user = (User) checkResponse.getData();
//        if (user.getRole() != Const.Role.ROLE_ADMIN) {
//            return ServerResponse.createByError(Const.RESPONSE_MESSAGE.NO_PERMISSION);
//        }
        return productService.searchProduct(productName, categoryId, productId, pageNum, pageSize, orderItem, orderOption);
    }

    /**
     * 上传文件
     *
     * @param request
     * @param file
     * @return
     */
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public ServerResponse upload(HttpServletRequest request, @RequestParam(value = "uploadFile", required = false) MultipartFile file) {
//        ServerResponse checkResponse = CheckUtil.checkIsLogin(request.getSession());
//        if (!checkResponse.isSuccess()) {
//            return checkResponse;
//        }
//        User user = (User) checkResponse.getData();
//        if (user.getRole() != Const.Role.ROLE_ADMIN) {
//            return ServerResponse.createByError(Const.RESPONSE_MESSAGE.NO_PERMISSION);
//        }
        if (file == null) {
            return ServerResponse.createByError("上传文件为空！");
        }
        return fileService.upload(file, "productPic");
    }

}
