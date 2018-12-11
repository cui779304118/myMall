package cup.cw.mall.pojo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class PayInfo {
    private Integer id;

    private Integer userId;

    private Long orderNo;

    private Integer payPlatform;

    private String platformNumber;

    private String platformStatus;

    private String buyerId;
    private BigDecimal refundAmount;

    private Date createTime;

    private Date updateTime;

    public PayInfo(Integer id, Integer userId, Long orderNo,
                   Integer payPlatform, String platformNumber,
                   String platformStatus,String buyerId,BigDecimal refundAmount,
                   Date createTime, Date updateTime) {
        this.id = id;
        this.userId = userId;
        this.orderNo = orderNo;
        this.payPlatform = payPlatform;
        this.platformNumber = platformNumber;
        this.platformStatus = platformStatus;
        this.buyerId = buyerId;
        this.refundAmount = refundAmount;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "PayInfo{" +
                "id=" + id +
                ", userId=" + userId +
                ", orderNo=" + orderNo +
                ", payPlatform=" + payPlatform +
                ", platformNumber='" + platformNumber + '\'' +
                ", platformStatus='" + platformStatus + '\'' +
                ", buyerId='" + buyerId + '\'' +
                ", refundAmount=" + refundAmount +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }

    public PayInfo() {
        super();
    }

}