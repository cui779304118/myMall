package cup.cw.mall.cache.redis;


import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.lang.reflect.Method;

/**
 * created by cuiwei on 2018/11/8
 */
public class JedisProxy implements MethodInterceptor {

    private JedisPool jedisPool;

    public JedisProxy(JedisPool jedisPool){
        this.jedisPool = jedisPool;
    }

    public static Jedis getInstance(JedisPool jedisPool){
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(Jedis.class);
        enhancer.setCallback(new JedisProxy(jedisPool));
        return (Jedis) enhancer.create();
    }

    //jedis实际调用的方法
    @Override
    public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        Jedis jedis = null;
        try{
            jedis = jedisPool.getResource();
            return method.invoke(jedis,args);
        }finally {
            if (jedis != null){
                jedis.close();
            }
        }
    }
}
