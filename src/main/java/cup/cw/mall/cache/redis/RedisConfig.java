package cup.cw.mall.cache.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * created by cuiwei on 2018/11/7
 * redis配置文件
 */
public class RedisConfig {
    private static Logger logger = LoggerFactory.getLogger(RedisConfig.class);

    //不同名称的redis环境对应的ip和端口
    private static Map<String,HostAndPort> hostAndPortConfigs;
    //配置jedisPool配置文件
    private static JedisPoolConfig jedisPoolConfig;
    //配置文件
    private static final String CONFIG_FILE = "redis.properties";
    static {
        InputStream ins = null;
        try {
            ins = RedisConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE);
            Properties pros = getConfigPros(ins);
            logger.info("读取redis.properties成功！");
            initConfig(pros);//初始化配置文件
            initHostAndPortConfigs(pros);//获取不同redis对应的ip和端口
            logger.info("加载redis.propertis成功！");
        } catch (Exception e) {
            logger.error("parse redis config file error",e);
        } finally {
            if (ins != null){
                try {
                    ins.close();
                } catch (IOException e) {
                    logger.error("parse redis config file error",e);
                }
            }
        }
    }
    private static Properties getConfigPros(InputStream ins) throws IOException {
        Properties pros = new Properties();
        pros.load(ins);
        return pros;
    }

    private static void initConfig(Properties pros) throws Exception{
        jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(Integer.valueOf(pros.getProperty("redis.maxActive","8")));
        jedisPoolConfig.setMaxIdle(Integer.valueOf(pros.getProperty("redis.maxIdle","8")));
        jedisPoolConfig.setMaxWaitMillis(Long.valueOf(pros.getProperty("redis.maxWait","1000")));
        jedisPoolConfig.setTestOnBorrow(Boolean.valueOf(pros.getProperty("redis.testOnBorrow","false")));
        jedisPoolConfig.setTestOnReturn(Boolean.valueOf(pros.getProperty("redis.testOnReturn","false")));
    }

    private static void initHostAndPortConfigs(Properties pros)throws Exception{
        hostAndPortConfigs = new HashMap<>();
        for (Object obj : pros.keySet()){
            String key = (String) obj;
            if (key != null && key.lastIndexOf(".redis.host") > 0){
                String hostName = key.substring(0,key.lastIndexOf(".redis.host"));
                String host = pros.getProperty(key);
                int port = Integer.valueOf(pros.getProperty(hostName + ".redis.port"));
                hostAndPortConfigs.put(hostName,new HostAndPort(host,port));
            }
        }
        if (hostAndPortConfigs.size() == 0){
            throw new Exception("there is no valid host and port");
        }
    }

    public static JedisPoolConfig getConfig(){
        return jedisPoolConfig;
    }

    public static Map<String,HostAndPort> getHostAndPortConfigs(){
        return hostAndPortConfigs;
    }
}
