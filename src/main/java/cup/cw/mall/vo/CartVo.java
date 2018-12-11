package cup.cw.mall.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * created by cuiwei on 2018/11/14
 */
@Data
public class CartVo {
    private List<CartProductVo> cartProductVoList;
    private BigDecimal cartTotalPrice;
    private Boolean allChecked;
    private String imageHost;

    @Override
    public String toString() {
        return "CartVo{" +
                "cartProductVoList=" + cartProductVoList +
                ", cartTotalPrice=" + cartTotalPrice +
                ", allChecked=" + allChecked +
                ", imageHost='" + imageHost + '\'' +
                '}';
    }
}
