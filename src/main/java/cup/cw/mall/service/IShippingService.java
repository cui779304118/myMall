package cup.cw.mall.service;

import cup.cw.mall.common.ServerResponse;
import cup.cw.mall.pojo.Shipping;

/**
 * created by cuiwei on 2018/11/18
 */
public interface IShippingService {

    ServerResponse add(Integer userId, Shipping shipping);

    ServerResponse del(Integer userId,Integer shippingId);

    ServerResponse update(Integer userId,Shipping shipping);

    ServerResponse query(Integer userId, Integer shippingId);

    ServerResponse list(Integer userId,Integer pageNum,Integer pageSize);

}
