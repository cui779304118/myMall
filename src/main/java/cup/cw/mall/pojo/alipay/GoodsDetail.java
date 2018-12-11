package cup.cw.mall.pojo.alipay;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * created by cuiwei on 2018/12/2
 */
@Data
public class GoodsDetail {
    @JSONField(name = "goods_id")
    private String goodsId;
    @JSONField(name = "alipay_goods_id")
    private String alipayGoodsId;
    @JSONField(name = "goods_name")
    private String goodsName;
    private int quantity;
    private String price;
    @JSONField(name = "goods_category")
    private String goodsCategory;
    private String body;
    @JSONField(name = "show_url")
    private String showUrl;

    @Override
    public String toString() {
        return "GoodsDetail{" +
                "goodsId='" + goodsId + '\'' +
                ", alipayGoodsId='" + alipayGoodsId + '\'' +
                ", goodsName='" + goodsName + '\'' +
                ", quantity=" + quantity +
                ", price='" + price + '\'' +
                ", goodsCategory='" + goodsCategory + '\'' +
                ", body='" + body + '\'' +
                ", showUrl='" + showUrl + '\'' +
                '}';
    }
}
