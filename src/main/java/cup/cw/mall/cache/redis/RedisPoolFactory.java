package cup.cw.mall.cache.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisPool;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * created by cuiwei on 2018/11/7
 */
public class RedisPoolFactory {
    private static final Logger logger = LoggerFactory.getLogger(RedisPoolFactory.class);

    private static Map<String,JedisPool> poolCache = new HashMap<>();

    static {
        //加载配置的所有redisPool
        registerPools();
    }

    private static void registerPools(){
        Map<String,HostAndPort> hostAndPortMap = RedisConfig.getHostAndPortConfigs();
        Set<Map.Entry<String,HostAndPort>> entrySet = hostAndPortMap.entrySet();
        for (Map.Entry<String,HostAndPort> entry : entrySet){
            poolCache.put(entry.getKey(),createPool(entry.getValue()));
        }
    }

    private static JedisPool createPool(HostAndPort hostAndPort){
        return new JedisPool(hostAndPort.getHost(),hostAndPort.getPort());
    }

    public static JedisPool getJedisPool(String hostName){
        return poolCache.get(hostName);
    }
}
