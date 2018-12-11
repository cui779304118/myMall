package cup.cw.mall.vo;

import cup.cw.mall.pojo.OrderItem;
import cup.cw.mall.pojo.Shipping;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * created by cuiwei on 2018/11/21
 */
@Data
public class OrderVo {
    private Long orderNo;

    private BigDecimal payment;

    private Integer paymentType;

    private String paymentTypeDesc;
    private Integer postage;

    private Integer status;


    private String statusDesc;

    private String paymentTime;

    private String sendTime;

    private String endTime;

    private String closeTime;

    private String createTime;

    //订单的明细
    private List<OrderItem> orderItemVoList;

    private String imageHost;
    private Integer shippingId;
    private String receiverName;

    private Shipping shippingVo;




}
