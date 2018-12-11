package cup.cw.mall.cache.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Tuple;

import java.util.*;

/**
 * created by cuiwei on 2018/11/7
 */
public abstract class RedisClient {

    private static final Logger logger = LoggerFactory.getLogger(RedisClient.class);

    private String hostName;
    private JedisPool pool;

    public RedisClient() {
        this.hostName = getHostName();
        pool = RedisPoolFactory.getJedisPool(hostName);
    }

    public abstract String getHostName();

    private Jedis getJedis() {
        return JedisProxy.getInstance(pool);
    }

    public boolean set(String key, String value, int expireTime) {
        try {
            Jedis jedis = getJedis();
            jedis.set(key, value);
            jedis.expire(key, expireTime);
        } catch (Exception e) {
            logger.error("set key error,hostName:{},key:{},value:{},expireTime:{}",
                    hostName, key, value, expireTime);
            return false;
        }
        return true;
    }

    public boolean set(String key, String value) {
        try {
            Jedis jedis = getJedis();
            jedis.set(key, value);
        } catch (Exception e) {
            logger.error("set key error,hostName:{},key:{},value:{}",
                    hostName, key, value);
            return false;
        }
        return true;
    }

    public String get(String key) {
        String value = null;
        try {
            Jedis jedis = getJedis();
            value = jedis.get(key);
            if (logger.isDebugEnabled()) {
                final Object[] logValue = new Object[]{key, value == null ? "null" : value};
                logger.debug("get key:{}, value:{}", logValue);
            }
        } catch (Exception e) {
            logger.error("get key error,hostName:{},key:{}", hostName, key, e);
        }
        return value;
    }

    public void del(String key) throws Exception {
        try {
            Jedis jedis = getJedis();
            jedis.del(key);
            if (logger.isDebugEnabled()) {
                final Object[] logValue = new Object[]{key};
                logger.debug("del key:{},", logValue);
            }
        } catch (Exception e) {
            logger.error("del key error,hostName:{},key:{}", hostName, key, e);
        }
    }

    public Long incr(String key) throws Exception {
        Long value = null;
        try {
            Jedis jedis = getJedis();
            value = jedis.incr(key);
        } catch (Exception e) {
            logger.error("incr key error,hostName:{},key:{}", hostName, key, e);
        }
        return value;
    }

    public Long incrBy(String key, long integer) throws Exception {
        Long value = null;
        try {
            Jedis jedis = getJedis();
            value = jedis.incrBy(key, integer);
        } catch (Exception e) {
            logger.error("incrBy key error,hostName:{},key:{},integer{}}", hostName, key, integer, e);
        }
        return value;
    }

    public Long expire(String key, int seconds) throws Exception {
        Long value = null;
        try {
            Jedis jedis = getJedis();
            value = jedis.expire(key, seconds);
        } catch (Exception e) {
            logger.error("expire key error,hostName:{},key:{},expireSeconds:{}", hostName, key, seconds, e);
        }
        return value;
    }

    public Long expireAt(String key, long unixTime) throws Exception {
        Long value = null;
        try {
            Jedis jedis = getJedis();
            value = jedis.expireAt(key, unixTime);
        } catch (Exception e) {
            logger.error("expire key error,hostName:{},key:{},expireSeconds:{}}", hostName, key, unixTime, e);
        }
        return value;
    }

    public Long ttl(String key) throws Exception {
        Long value = null;
        try {
            Jedis jedis = getJedis();
            value = jedis.ttl(key);
        } catch (Exception e) {
            logger.error("ttl key error,hostName:{},key:{}", hostName, key, e);
        }
        return value;
    }

    /**
     * 封装list操作
     */
    public List<String> lrange(String key, long start, long end) {
        List<String> result = new ArrayList<String>();
        try {
            Jedis jedis = getJedis();
            return jedis.lrange(key, start, end);
        } catch (Exception e) {
            logger.error("lrange key error,hostName:{},key:{},start:{},end:{}",
                    hostName, key, start, end);
        }
        return result;
    }

    public Long lpush(String key, String value) {
        Long returnValue = null;
        try {
            Jedis jedis = getJedis();
            returnValue = jedis.lpush(key, value);
        } catch (Exception e) {
            logger.error("lpush key error,hostName:{},key:{},value:{}",
                    new Object[]{hostName, key, value, e});
        }
        return returnValue;
    }

    public void rpush(String key, String value) {
        try {
            Jedis jedis = getJedis();
            jedis.rpush(key, value);
        } catch (Exception e) {
            logger.error("rpush key error,hostName:{},key:{},value:{}",
                    new Object[]{hostName, key, value, e});
        }
    }

    public long llen(String key) {
        try {
            Jedis jedis = getJedis();
            return jedis.llen(key);
        } catch (Exception e) {
            logger.error("llen key error,hostName:{},key:{}",
                    new Object[]{hostName, key, e});
        }
        return -1;
    }

    public long hlen(String key) {
        try {
            Jedis jedis = getJedis();
            return jedis.hlen(key);
        } catch (Exception e) {
            logger.error("llen key error,hostName:{},key:{}",
                    new Object[]{hostName, key, e});
        }
        return -1;
    }

    public long lrem(String key, long count, String value) {
        try {
            Jedis jedis = getJedis();
            return jedis.lrem(key, count, value);
        } catch (Exception e) {
            logger.error("lrem key error,hostName:{},key:{},count:{},value:{}",
                    new Object[]{hostName, key, count, value, e});
        }
        return 0;
    }

    /**
     * 裁剪一个list
     *
     * @param key
     * @param start
     * @param end
     */
    public void ltrim(String key, long start, long end) {
        try {
            Jedis jedis = getJedis();
            jedis.ltrim(key, start, end);
        } catch (Exception e) {
            logger.error("ltrim key error,hostName:{},key:{},start:{},end:{}",
                    new Object[]{hostName, key, start, end, e});
        }
    }

    public boolean exists(String key) {
        try {
            Jedis jedis = getJedis();
            return jedis.exists(key);
        } catch (Exception e) {
            logger.error("exists key error,hostName:{},key:{}",
                    new Object[]{hostName, key, e});
        }
        return false;
    }

    /**
     * 封装set操作
     */
    public Set<String> smembers(String key) {
        Set<String> members = new HashSet<String>();
        try {
            Jedis jedis = getJedis();
            return jedis.smembers(key);
        } catch (Exception e) {
            logger.error("smembers key error,hostName:{},key:{}", hostName, key, e);
        }
        return members;
    }

    public List<String> srandmember(String key, int count) {
        List<String> list = new ArrayList<>();
        try {
            Jedis jedis = getJedis();
            return jedis.srandmember(key, count);
        } catch (Exception e) {
            logger.error("srandmember key error,hostName:{},key:{},count:{}", hostName, key, count, e);
        }
        return list;
    }

    public Long scard(String key) {
        try {
            Jedis jedis = getJedis();
            return jedis.scard(key);
        } catch (Exception e) {
            logger.error("scard key error,hostName:{},key:{}", hostName, key, e);
        }
        return null;
    }

    public Long sadd(String key, String member) {
        try {
            Jedis jedis = getJedis();
            return jedis.sadd(key, member);
        } catch (Exception e) {
            logger.error("sadd key error,hostName:{},key:{},member:{}", hostName, key, member, e);
        }
        return null;
    }

    public Boolean sismember(String key, String member) {
        try {
            Jedis jedis = getJedis();
            return jedis.sismember(key, member);
        } catch (Exception e) {
            logger.error("sismember key error,hostName:{},key:{},memeber:{}", hostName, key, member, e);
        }
        return false;
    }

    /**
     * 封装Hash操作
     */
    public String hmset(String key, Map<String, String> hash) {
        try {
            Jedis jedis = getJedis();
            return jedis.hmset(key, hash);
        } catch (Exception e) {
            logger.error("hmset key error,hostName:{},key:{},hash:{}", hostName, key, hash.toString(), e);
        }
        return null;
    }

    public List<String> hmget(String key, String... fields) {
        List<String> list = new ArrayList<String>();
        try {
            Jedis jedis = getJedis();
            list = jedis.hmget(key, fields);
        } catch (Exception e) {
            logger.error("hmget key error,hostName:{},key:{},fields:{}",
                    hostName, key, Arrays.toString(fields), e);
        }
        return list;
    }

    public Map<String, String> hgetAll(String key) {
        Map<String, String> hash = new HashMap<String, String>();
        try {
            Jedis jedis = getJedis();
            hash = jedis.hgetAll(key);
        } catch (Exception e) {
            logger.error("hgetAll key error,hostName:{},key:{}", hostName, key, e);
        }
        return hash;
    }

    public void hset(String key, String field, String value) {
        try {
            Jedis jedis = getJedis();
            jedis.hset(key, field, value);
        } catch (Exception e) {
            logger.error("hset key error,hostName:{},key:{},field:{},value:{}", hostName, key, field, value, e);
        }
    }

    public String hget(String key, String field) {
        String value = null;
        try {
            Jedis jedis = getJedis();
            value = jedis.hget(key, field);
        } catch (Exception e) {
            logger.error("hget key error,hostName:{},key:{},field:{}", hostName, key, field, e);
        }
        return value;
    }

    public Long hdel(String key, String field) {
        Long value = null;
        try {
            Jedis jedis = getJedis();
            value = jedis.hdel(key, field);
        } catch (Exception e) {
            logger.error("hdel key error,hostName:{},key:{},field:{}",
                    new Object[]{hostName, key, field, e});
        }
        return value;
    }

    public Long hincr(String key, String field, long incr) {
        Long value = null;
        try {
            Jedis jedis = getJedis();
            value = jedis.hincrBy(key, field, incr);
        } catch (Exception e) {
            logger.error("hincr key error,hostName:{},key:{},field:{},incr:{}",
                    hostName, key, field, incr, e);
        }
        return value;
    }

    public boolean hexists(String key, String field) {
        try {
            Jedis jedis = getJedis();
            return jedis.hexists(key, field);
        } catch (Exception e) {
            logger.error("hdel key error,hostName:{},key:{}",
                    new Object[]{hostName, key, e});
        }
        return false;
    }

    public Long hsetnx(String key, String field, String value) {
        try {
            Jedis jedis = getJedis();
            return jedis.hsetnx(key, field, value);
        } catch (Exception e) {
            logger.error("hsetnx key error,hostName:{},key:{},value:{}",
                    hostName, key, value, e);
        }
        return null;
    }

    public void zadd(String key, double score, String member) {
        try {
            Jedis jedis = getJedis();
            jedis.zadd(key, score, member);
        } catch (Exception e) {
            logger.error("zadd key error,hostName:{},key:{},score:{},member:{}",
                    hostName, key, score, member, e);
        }
    }

    public void zadd(String key, Map<String, Double> scoreMembers) {
        try {
            Jedis jedis = getJedis();
            jedis.zadd(key, scoreMembers);
        } catch (Exception e) {
            logger.error("zadd key error,hostName:{},key:{},scoreMembers:{}",
                    hostName, key, scoreMembers, e);
        }
    }

    public Double zincrby(String key, double score, String member) {
        Double value = null;
        try {
            Jedis jedis = getJedis();
            value = jedis.zincrby(key, score, member);
        } catch (Exception e) {
            logger.error("zincrby key error,hostName:{},key:{},score:{},member:{}",
                    hostName, key, score, member, e);
        }
        return value;
    }

    public Set<String> zrange(String key, long start, long end) {
        Set<String> value = null;
        try {
            Jedis jedis = getJedis();
            value = jedis.zrange(key, start, end);
        } catch (Exception e) {
            logger.error("zrange key error,hostName:{},key:{},start:{},end:{}", hostName, key, start, end, e);
        }
        return value;
    }

    public Set<String> zrevrange(String key, long start, long end) {
        Set<String> value = null;
        try {
            Jedis jedis = getJedis();
            value = jedis.zrevrange(key, start, end);
        } catch (Exception e) {
            logger.error("zrevrange key error,hostName:{},key:{},start:{},end:{}", hostName, key, start, end, e);
        }
        return value;
    }

    public Set<Tuple> zrangeWithScores(String key, long start, long end) {
        Set<Tuple> value = null;
        try {
            Jedis jedis = getJedis();
            value = jedis.zrangeWithScores(key, start, end);
        } catch (Exception e) {
            logger.error("zrangeWithScores key error,hostName:{},key:{},start:{},end:{}",
                    hostName, key, start, end, e);
        }
        return value;
    }

    public Set<Tuple> zrevrangeWithScores(String key, long start, long end) {
        Set<Tuple> value = null;
        try {
            Jedis jedis = getJedis();
            value = jedis.zrevrangeWithScores(key, start, end);
        } catch (Exception e) {
            logger.error("zrevrangeWithScores key error,hostName:{},key:{},start:{},end:{}",
                    hostName, key, start, end, e);
        }
        return value;
    }

    public Set<String> zrangeByScore(String key, double start, double end, int offset, int count) {
        Set<String> value = null;
        try {
            Jedis jedis = getJedis();
            value = jedis.zrangeByScore(key, start, end, offset, count);
        } catch (Exception e) {
            logger.error("zrangeByScore key error,hostName:{},key:{},start:{},end:{},offset:{},count:{}",
                    hostName, key, start, end, offset, count, e);
        }
        return value;
    }

    public Set<String> zrevrangeByScore(String key, double end, double start, int offset, int count) {
        Set<String> value = null;
        try {
            Jedis jedis = getJedis();
            value = jedis.zrevrangeByScore(key, end, start, offset, count);
        } catch (Exception e) {
            logger.error("zrevrangeByScore key error,hostName:{},key:{},end:{},start:{},offset:{},count:{}",
                    hostName, key, end, start, offset, count, e);
        }
        return value;
    }

    public Set<Tuple> zrangeByScoreWithScores(String key, double start, double end, int offset, int count) {
        Set<Tuple> value = null;
        try {
            Jedis jedis = getJedis();
            value = jedis.zrangeByScoreWithScores(key, start, end, offset, count);
        } catch (Exception e) {
            logger.error("zrangeByScoreWithScores key error,hostName:{},key:{},start:{},end:{},offset:{},count:{}",
                    hostName, key, start, end, offset, count, e);
        }
        return value;
    }

    public Set<Tuple> zrevrangeByScoreWithScores(String key, double end, double start, int offset, int count) {
        Set<Tuple> value = null;
        try {
            Jedis jedis = getJedis();
            value = jedis.zrevrangeByScoreWithScores(key, end, start, offset, count);
        } catch (Exception e) {
            logger.error("zrevrangeByScoreWithScores key error,hostName:{},key:{},end:{},start:{},offset:{},count:{}",
                    hostName, key, end, start, offset, count, e);
        }
        return value;
    }

    public Long zcard(String key) {
        Long value = null;
        try {
            Jedis jedis = getJedis();
            value = jedis.zcard(key);
        } catch (Exception e) {
            logger.error("zcard key error,hostName:{},key:{}",
                    hostName, key, e);
        }
        return value;
    }

    public Long zremrangeByRank(String key, long start, long end) {
        Long value = null;
        try {
            Jedis jedis = getJedis();
            value = jedis.zremrangeByRank(key, start, end);
        } catch (Exception e) {
            logger.error("zremrangeByRank key error,hostName:{},key:{},start:{},end:{}",
                    hostName, key, start, end, e);
        }
        return value;
    }

    public Long zremrangeByScore(String key, double start, double end) {
        Long value = null;
        try {
            Jedis jedis = getJedis();
            value = jedis.zremrangeByScore(key, start, end);
        } catch (Exception e) {
            logger.error("zremrangeByScore key error,hostName:{},key:{},start:{},end:{}",
                    hostName, key, start, end, e);
        }
        return value;
    }

    public Long zrem(String key, String... member) {
        Long value = null;
        try {
            Jedis jedis = getJedis();
            value = jedis.zrem(key, member);
        } catch (Exception e) {
            logger.error("zrem key error,hostName:{},key:{},member:{}",
                    hostName, key, member, e);
        }
        return value;
    }

    public Double zscore(String key, String member) {
        Double value = null;
        try {
            Jedis jedis = getJedis();
            value = jedis.zscore(key, member);
        } catch (Exception e) {
            logger.error("zscore key error,hostName:{},key:{},member:{}",
                    hostName, key, member, e);
        }
        return value;
    }

    public Long setnx(String key, String value) {
        try {
            Jedis jedis = getJedis();
            return jedis.setnx(key, value);
        } catch (Exception e) {
            logger.error("setnx key error,hostName:{},key:{},value:{}",
                    hostName, key, value, e);
        }
        return null;
    }
}
