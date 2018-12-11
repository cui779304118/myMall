package cup.cw.mall.pojo;

import lombok.Data;

import java.util.Date;
@Data
public class Shipping {
    private Integer id;

    private Integer userId;

    private String receiverName;

    private String receiverMobile;

    private String receiverProvince;

    private String receiverCity;

    private String receiverAddress;

    private String receiverZip;

    private Date createTime;

    private Date updateTime;

    public Shipping(Integer id, Integer userId, String receiverName, String receiverMobile, String receiverProvince, String receiverCity, String receiverAddress, String receiverZip, Date createTime, Date updateTime) {
        this.id = id;
        this.userId = userId;
        this.receiverName = receiverName;
        this.receiverMobile = receiverMobile;
        this.receiverProvince = receiverProvince;
        this.receiverCity = receiverCity;
        this.receiverAddress = receiverAddress;
        this.receiverZip = receiverZip;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public Shipping() {
        super();
    }

    @Override
    public String toString() {
        return "Shipping{" +
                "id=" + id +
                ", userId=" + userId +
                ", receiverName='" + receiverName + '\'' +
                ", receiverMobile='" + receiverMobile + '\'' +
                ", receiverProvince='" + receiverProvince + '\'' +
                ", receiverCity='" + receiverCity + '\'' +
                ", receiverAddress='" + receiverAddress + '\'' +
                ", receiverZip='" + receiverZip + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}