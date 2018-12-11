package cup.cw.mall.vo;

import cup.cw.mall.pojo.OrderItem;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * created by cuiwei on 2018/11/23
 */
@Data
public class OrderProductVo {

    private List<OrderItem> orderItemList;
    private BigDecimal totalPrice;
    private String imageHost;

}
