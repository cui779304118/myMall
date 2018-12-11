package cup.cw.mall.pojo.alipay;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * created by cuiwei on 2018/12/2
 */
@Data
public class ExtendParams {
    @JSONField(name = "sys_service_provider_id")
    private String sysServiceProviderId;
    @JSONField(name = "hb_fq_num")
    private String hbFqNum;
    @JSONField(name = "hb_fq_seller_percent")
    private String hbFqSellerPercent;

    @Override
    public String toString() {
        return "ExtendParams{" +
                "sysServiceProviderId='" + sysServiceProviderId + '\'' +
                ", hbFqNum='" + hbFqNum + '\'' +
                ", hbFqSellerPercent='" + hbFqSellerPercent + '\'' +
                '}';
    }
}
