package com.td.common_service.service.jedis;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * redis 的操作开放接口
 */
public interface RedisService {

	// =============================common============================

	/**
	 * 检查是否连接成功
	 *
	 * @return
	 */
	Object ping();

	/**
	 * 指定缓存失效时间
	 *
	 * @param key  键
	 * @param time 时间(秒)
	 * @return
	 */
	boolean expire(String key, long time);

	/**
	 * 根据key 获取过期时间
	 *
	 * @param key 键 不能为null
	 * @return 时间(秒) 返回0代表为永久有效
	 */
	long getExpire(String key);

	/**
	 * 判断key是否存在
	 *
	 * @param key 键
	 * @return true 存在 false不存在
	 */
	boolean hasKey(String key);

	/**
	 * 删除缓存
	 *
	 * @param key 可以传一个值 或多个
	 */
	void del(String... key);

	// ============================String=============================

	/**
	 * 递增
	 *
	 * @param key   键
	 * @param delta 要增加几(大于0)
	 * @return
	 */
	long incr(String key, long delta);

	/**
	 * 递减
	 *
	 * @param key   键
	 * @param delta 要减少几(小于0)
	 * @return
	 */
	long decr(String key, long delta);

	// ================================Map=================================

	/**
	 * HashGet
	 *
	 * @param key  键 不能为null
	 * @param item 项 不能为null
	 * @return 值
	 */
	Object hget(String key, String item);

	/**
	 * 获取hashKey对应的所有键值
	 *
	 * @param key 键
	 * @return 对应的多个键值
	 */
	Map<Object, Object> hmget(String key);

	/**
	 * HashSet
	 *
	 * @param key 键
	 * @param map 对应多个键值
	 * @return true 成功 false 失败
	 */
	boolean hmset(String key, Map<String, Object> map);

	/**
	 * HashSet 并设置时间
	 *
	 * @param key  键
	 * @param map  对应多个键值
	 * @param time 时间(秒)
	 * @return true成功 false失败
	 */
	boolean hmset(String key, Map<String, Object> map, long time);

	/**
	 * 向一张hash表中放入数据,如果不存在将创建
	 *
	 * @param key   键
	 * @param item  项
	 * @param value 值
	 * @return true 成功 false失败
	 */
	boolean hset(String key, String item, Object value);

	/**
	 * 向一张hash表中放入数据,如果不存在将创建
	 *
	 * @param key   键
	 * @param item  项
	 * @param value 值
	 * @param time  时间(秒) 注意:如果已存在的hash表有时间,这里将会替换原有的时间
	 * @return true 成功 false失败
	 */
	boolean hset(String key, String item, Object value, long time);

	/**
	 * 删除hash表中的值
	 *
	 * @param key  键 不能为null
	 * @param item 项 可以使多个 不能为null
	 */
	void hdel(String key, Object... item);

	/**
	 * 判断hash表中是否有该项的值
	 *
	 * @param key  键 不能为null
	 * @param item 项 不能为null
	 * @return true 存在 false不存在
	 */
	boolean hHasKey(String key, String item);

	/**
	 * hash递增 如果不存在,就会创建一个 并把新增后的值返回
	 *
	 * @param key  键
	 * @param item 项
	 * @param by   要增加几(大于0)
	 * @return
	 */
	double hincr(String key, String item, double by);

	/**
	 * hash递减
	 *
	 * @param key  键
	 * @param item 项
	 * @param by   要减少记(小于0)
	 * @return
	 */
	double hdecr(String key, String item, double by);

	// ============================set=============================

	/**
	 * 根据key获取Set中的所有值
	 *
	 * @param key 键
	 * @return
	 */
	Set<Object> sGet(String key);

	/**
	 * 根据value从一个set中查询,是否存在
	 *
	 * @param key   键
	 * @param value 值
	 * @return true 存在 false不存在
	 */
	boolean sHasKey(String key, Object value);

	/**
	 * 将数据放入set缓存
	 *
	 * @param key    键
	 * @param values 值 可以是多个
	 * @return 成功个数
	 */
	long sSet(String key, Object... values);

	/**
	 * 将set数据放入缓存
	 *
	 * @param key    键
	 * @param time   时间(秒)
	 * @param values 值 可以是多个
	 * @return 成功个数
	 */
	long sSetAndTime(String key, long time, Object... values);

	/**
	 * 获取set缓存的长度
	 *
	 * @param key 键
	 * @return
	 */
	long sGetSetSize(String key);

	/**
	 * 移除值为value的
	 *
	 * @param key    键
	 * @param values 值 可以是多个
	 * @return 移除的个数
	 */
	long setRemove(String key, Object... values);

	// ===============================list=================================

	/**
	 * 获取list缓存的内容
	 *
	 * @param key   键
	 * @param start 开始
	 * @param end   结束 0 到 -1代表所有值
	 * @return
	 */
	List<Object> lGet(String key, long start, long end);

	/**
	 * 获取list缓存的长度
	 *
	 * @param key 键
	 * @return
	 */
	long lGetListSize(String key);

	/**
	 * 通过索引 获取list中的值
	 *
	 * @param key   键
	 * @param index 索引 index>=0时， 0 表头，1 第二个元素，依次类推；index<0时，-1，表尾，-2倒数第二个元素，依次类推
	 * @return
	 */
	Object lGetIndex(String key, long index);

	/**
	 * 将list放入缓存
	 *
	 * @param key   键
	 * @param value 值
	 * @return
	 */
	boolean lSet(String key, Object value);

	/**
	 * 将list放入缓存
	 *
	 * @param key   键
	 * @param value 值
	 * @param time  时间(秒)
	 * @return
	 */
	boolean lSet(String key, Object value, long time);

	/**
	 * 将list放入缓存
	 *
	 * @param key   键
	 * @param value 值
	 * @return
	 */
	boolean lSet(String key, List<Object> value);

	/**
	 * 将list放入缓存
	 *
	 * @param key   键
	 * @param value 值
	 * @param time  时间(秒)
	 * @return
	 */
	boolean lSet(String key, List<Object> value, long time);

	/**
	 * 根据索引修改list中的某条数据
	 *
	 * @param key   键
	 * @param index 索引
	 * @param value 值
	 * @return
	 */
	boolean lUpdateIndex(String key, long index, Object value);

	/**
	 * 移除N个值为value
	 *
	 * @param key   键
	 * @param count 移除多少个
	 * @param value 值
	 * @return 移除的个数
	 */
	long lRemove(String key, long count, Object value);

	/**
	 * 创建 SETNX
	 * @param key key
	 * @param value value
	 * @param expire 过期时间 秒
	 * @return
	 */
	public boolean setLock(String key, Serializable value, long expire);
	//    /**
	//     * 通过key删除
	//     *
	//     * @param keys
	//     */
	//    public abstract long del(String... keys);
	//
	//    /**
	//     * 添加key value 并且设置存活时间(byte)
	//     *
	//     * @param key
	//     * @param value
	//     * @param liveTime
	//     */
	//    public abstract void set(byte[] key, byte[] value, long liveTime);
	//
	//    /**
	//     * 添加key value
	//     *
	//     * @param key
	//     * @param value
	//     */
	//    public abstract void set(String key, String value);
	//
	//    /**
	//     * 添加key value (字节)(序列化)
	//     *
	//     * @param key
	//     * @param value
	//     */
	//    public abstract void set(byte[] key, byte[] value);
	//
	//    /**
	//     * 获取redis value (String)
	//     * @param key
	//     * @return
	//     */
	//    Object get(String key);
	//
	//    /**
	//     * 通过正则匹配keys
	//     *
	//     * @param pattern
	//     * @return
	//     */
	//    public abstract Setkeys(String pattern);
	//
	//    /**
	//     * 检查key是否已经存在
	//     *
	//     * @param key
	//     * @return
	//     */
	//    public abstract boolean exists(String key);
	//
	//    /**
	//     * 清空redis 所有数据
	//     *
	//     * @return
	//     */
	//    public abstract String flushDB();
	//
	//    /**
	//     * 查看redis里有多少数据
	//     */
	//    public abstract long dbSize();

	boolean set(String key, Object value);

	boolean set(String key, Object value, long expireTime);

	boolean exists(final String key);

	<T> T get(String key, Class<T> clazz);

	Object get(String key);

	void delete(String key);
	
}
