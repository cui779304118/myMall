package cup.cw.mall.pojo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class OrderItem {
    private Integer id;

    private Integer userId;

    private Long orderNo;

    private Integer productId;

    private String productName;

    private String productImage;

    private BigDecimal currentUnitPrice;

    private Integer quantity;

    private BigDecimal totalPrice;

    private Date createTime;

    private Date updateTime;

    public OrderItem(Integer id, Integer userId, Long orderNo, Integer productId,
                     String productName, String productImage, BigDecimal currentUnitPrice,
                     Integer quantity, BigDecimal totalPrice, Date createTime, Date updateTime) {
        this.id = id;
        this.userId = userId;
        this.orderNo = orderNo;
        this.productId = productId;
        this.productName = productName;
        this.productImage = productImage;
        this.currentUnitPrice = currentUnitPrice;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public OrderItem() {
        super();
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "id=" + id +
                ", userId=" + userId +
                ", orderNo=" + orderNo +
                ", productId=" + productId +
                ", productName='" + productName + '\'' +
                ", productImage='" + productImage + '\'' +
                ", currentUnitPrice=" + currentUnitPrice +
                ", quantity=" + quantity +
                ", totalPrice=" + totalPrice +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}