package cup.cw.mall.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * created by cuiwei on 2018/10/22
 */
@Data
public class ProductDetailVo {

    private Integer id;

    private Integer categoryId;

    private String name;

    private String subtitle;

    private String mainImage;

    private String subImages;

    private String detail;

    private BigDecimal price;

    private Integer stock;

    private Integer status;

    private String createTime;

    private String updateTime;

    private  String  imageHost;
    private  Integer parentCategoryId;

}
