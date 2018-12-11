package cup.cw.mall.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import cup.cw.mall.cache.LocalCacheManager;
import cup.cw.mall.common.Const;
import cup.cw.mall.common.RedisConst;
import cup.cw.mall.common.ServerResponse;
import cup.cw.mall.dao.ShippingMapper;
import cup.cw.mall.pojo.PageModel;
import cup.cw.mall.pojo.Shipping;
import cup.cw.mall.service.IShippingService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * created by cuiwei on 2018/11/18
 */
@Service("shippingService")
public class ShippingServiceImpl implements IShippingService {

    private static Logger logger = LoggerFactory.getLogger(ShippingServiceImpl.class);

    @Autowired
    private ShippingMapper shippingMapper;

    @Autowired
    private LocalCacheManager localCacheManager;

    /**
     * 添加地址信息
     * @param userId
     * @param shipping
     * @return
     */
    @Override
    public ServerResponse add(Integer userId, Shipping shipping) {
        if (!checkShipping(shipping)){
            return ServerResponse.createByError("参数错误，添加地址信息失败！");
        }
        shipping.setUserId(userId);
        try{
            int resultCode = shippingMapper.insertSelective(shipping);
            if (resultCode > Const.DB_CODE.OPERATE_FAILED){
                setShippingToRedis(shipping);
                return ServerResponse.createBySuccess("添加地址成功！",shipping);
            }
        }catch (Exception e){
            logger.error("插入shipping到数据库异常，userId={}",userId,e);
        }
        return ServerResponse.createByError("服务器异常，添加地址信息失败！");
    }

    private boolean checkShipping(Shipping shipping){
        return  !( //shipping.getId() == null || shipping.getUserId() == null
                   StringUtils.isEmpty(shipping.getReceiverName())
                || StringUtils.isEmpty(shipping.getReceiverMobile())
                || StringUtils.isEmpty(shipping.getReceiverProvince())
                || StringUtils.isEmpty(shipping.getReceiverCity())
                || StringUtils.isEmpty(shipping.getReceiverAddress())
                || StringUtils.isEmpty(shipping.getReceiverZip()));
    }

    private void setShippingToRedis(Shipping shipping){
        String SpKey = createShippingKey(shipping.getUserId());
        String spJsonStr = JSONObject.toJSONString(shipping);
        String spId = String.valueOf(shipping.getId());
        localCacheManager.hset(SpKey,spId,spJsonStr);
    }

    private String createShippingKey(Integer userId){
        String username = localCacheManager.hget(RedisConst.USER.USER_IDS,String.valueOf(userId));
        return RedisConst.SHIPPING.SHIPPING_USER_PREFFIX + username;
    }

    /**
     * 删除地址信息
     * @param userId
     * @param shippingId
     * @return
     */
    @Override
    public ServerResponse del(Integer userId, Integer shippingId) {
        try{
            int reCode = shippingMapper.deleteByPrimaryKey(shippingId);
            if (reCode > Const.DB_CODE.OPERATE_FAILED){
                String spKey = createShippingKey(userId);
                localCacheManager.hdel(spKey,String.valueOf(shippingId));
                return ServerResponse.createBySuccess("删除地址成功！");
            }
        }catch (Exception e){
            logger.error("删除地址信息失败，userId={},shippingId={}",userId,shippingId,e);
        }
        return ServerResponse.createByError("删除地址信息失败！");
    }

    /**
     * 更新地址信息
     * @param userId
     * @param shipping
     * @return
     */
    @Override
    public ServerResponse update(Integer userId, Shipping shipping) {
        if (shipping.getId() == null){
            return ServerResponse.createByError("参数错误，更新地址信息失败！");
        }
        shipping.setUserId(userId);
        try {
            int reCode = shippingMapper.updateByPrimaryKeySelective(shipping);
            if (reCode > Const.DB_CODE.OPERATE_FAILED){
                updateShippingToRedis(shipping);
                return ServerResponse.createBySuccess("更新地址成功！",shipping);
            }
        }catch (Exception e){
            logger.error("更新地址信息失败，userId={},shippingId={}",userId,shipping.getId(),e);
        }
        return ServerResponse.createByError("更新地址信息失败！");
    }

    private void updateShippingToRedis(Shipping shipping){
        Shipping shippingOld = getShpippingFromRedis(shipping.getUserId(),shipping.getId());
        if (!StringUtils.isEmpty(shipping.getReceiverName())){
            shippingOld.setReceiverName(shipping.getReceiverName());
        }
        if (!StringUtils.isEmpty(shipping.getReceiverProvince())){
            shippingOld.setReceiverProvince(shipping.getReceiverProvince());
        }
        if (!StringUtils.isEmpty(shipping.getReceiverCity())){
            shippingOld.setReceiverCity(shipping.getReceiverCity());
        }
        if (!StringUtils.isEmpty(shipping.getReceiverAddress())){
            shippingOld.setReceiverAddress(shipping.getReceiverAddress());
        }
        if (!StringUtils.isEmpty(shipping.getReceiverZip())){
            shippingOld.setReceiverZip(shipping.getReceiverZip());
        }
        setShippingToRedis(shippingOld);
    }

    /**
     * 查询地址信息
     * @param userId
     * @param shippingId
     * @return
     */
    @Override
    public ServerResponse query(Integer userId, Integer shippingId) {
        Shipping shipping = getShpippingFromRedis(userId,shippingId);
        if (shipping == null){
            try {
                shipping = shippingMapper.selectByPrimaryKey(shippingId);
                if (shipping == null){
                    return ServerResponse.createByError("查询失败，该用户对应地址信息不存在");
                }
                shipping.setCreateTime(null);
                shipping.setUpdateTime(null);
                setShippingToRedis(shipping);
            }catch (Exception e){
                logger.error("从数据库查询地址信息失败！userId={},shippingId={}",userId,shippingId,e);
                return ServerResponse.createByError("查询失败，服务器异常！");
            }
        }
        return ServerResponse.createBySuccess(shipping);
    }

    private Shipping getShpippingFromRedis(Integer userId,Integer shippingId){
        String spKey = createShippingKey(userId);
        String spJsonStr = localCacheManager.hget(spKey,String.valueOf(shippingId));
        return JSONObject.parseObject(spJsonStr,Shipping.class);
    }

    /**
     * 分页查询
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public ServerResponse list(Integer userId, Integer pageNum, Integer pageSize) {
        int totalRecords = getShppingCount(userId);
        if (totalRecords == 0){
            return ServerResponse.createByError("查询失败，该用户对应地址信息不存在");
        }
        PageModel pageModel = PageModel.builder().pageNum(pageNum)
                .pageSize(pageSize).totalRecords(totalRecords).build();
        try{
            List<Shipping> shippings = shippingMapper.list(pageModel);
            return ServerResponse.createBySuccess(shippings);
        }catch (Exception e){
            logger.error("从数据库查询地址信息失败！userId={}",userId,e);
        }
        return ServerResponse.createByError("查询失败，服务器异常！");
    }

    private int getShppingCount(Integer userId){
        String spKey = createShippingKey(userId);
        return (int) localCacheManager.hlen(spKey);
    }
}
