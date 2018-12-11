package cup.cw.mall.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 结合产品和购物车的一个抽象对象
 * created by cuiwei on 2018/11/14
 */
@Data
public class CartProductVo {

    private Integer id;// 购物车产品id
    private Integer userId;//用户id
    private Integer productId;//产品id
    private Integer quantity;//数量
    private String productName;//产品名称
    private String productSubtitle;//产品的子标题
    private String productMainImage;//产品主图片
    private BigDecimal productPrice;//产品单价
    private Integer productStatus;//产品状态
    private BigDecimal productTotalPrice;//购物车中该产品总价格
    private Integer productStock;//库存
    private Integer productChecked;//此商品是否勾选
    private String limitQuantity;//限制数量的一个返回结果

    @Override
    public String toString() {
        return "CartProductVo{" +
                "id=" + id +
                ", userId=" + userId +
                ", productId=" + productId +
                ", quantity=" + quantity +
                ", productName='" + productName + '\'' +
                ", productSubtitle='" + productSubtitle + '\'' +
                ", productMainImage='" + productMainImage + '\'' +
                ", productPrice=" + productPrice +
                ", productStatus=" + productStatus +
                ", productTotalPrice=" + productTotalPrice +
                ", productStock=" + productStock +
                ", productChecked=" + productChecked +
                ", limitQuantity='" + limitQuantity + '\'' +
                '}';
    }
}
