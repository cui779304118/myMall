package cup.cw.mall.cache;

import cup.cw.mall.cache.redis.RedisClient;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * created by cuiwei on 2018/11/8
 */
@Component
@Lazy
public class LocalCacheManager extends RedisClient {
    private static final String HOST_NAME = "local";
//    private static final String HOST_NAME = "remote";

    @Override
    public String getHostName() {
        return HOST_NAME;
    }
}
