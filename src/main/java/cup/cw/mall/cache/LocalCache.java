package cup.cw.mall.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import cup.cw.mall.common.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * created by cuiwei on 2018/10/15
 * guava实现的本地缓存
 */
public class LocalCache {
    private static final Logger logger = LoggerFactory.getLogger(LocalCache.class);
    private static LoadingCache<String,String> loadingCache = CacheBuilder.newBuilder().initialCapacity(Const.CACHE.INTI_CAPACITY)
            .maximumSize(Const.CACHE.MAX_CAPACITY).expireAfterAccess(10,TimeUnit.MINUTES).build(new CacheLoader<String, String>() {
                @Override
                public String load(String s) throws Exception {
                    return null;
                }
            });

    public static void setKey(String key,String value){
        loadingCache.put(key,value);
    }

    public static String getValue(String key){
        try {
           return loadingCache.get(key);
        } catch (ExecutionException e) {
            logger.error("local cache get value error,exception:{}",new Object[]{e.getMessage()});
        }
        return null;
    }

}
