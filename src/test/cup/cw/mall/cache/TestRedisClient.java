package cup.cw.mall.cache;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * created by cuiwei on 2018/11/8
 * 测试RedisClient
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
public class TestRedisClient {
    @Autowired
    RemoteCacheManager remoteCacheManager;

    @Test
    public void testSet(){
        String key = "test:set2";
        String value = "value2";
        boolean result = remoteCacheManager.set(key,value);
        if (result){
            System.out.println("set操作成功！");
        }else {
            System.out.println("set操作失败！");
        }
    }

    @Test
    public void testGet(){
        String key = "test:set2";
        String result = remoteCacheManager.get(key);
        System.out.println("get操作结果：" + result);
    }

}
