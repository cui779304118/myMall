package cup.cw.mall.pojo.alipay.result;

import com.alipay.api.AlipayResponse;
import cup.cw.mall.common.AlipayConst;
import lombok.Data;

/**
 * created by cuiwei on 2018/11/30
 */
@Data
public class AlipayWebResult{
    private AlipayConst.TradeStatus tradeStatus;
    private AlipayResponse response;

    public AlipayWebResult(){}

    public AlipayWebResult(AlipayResponse response){
        this.response = response;
    }

    public boolean isTradeSuccess() {
        return this.response != null && AlipayConst.TradeStatus.SUCCESS.equals(this.tradeStatus);
    }
}
