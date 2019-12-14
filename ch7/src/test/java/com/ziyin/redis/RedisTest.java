package com.ziyin.redis;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;

import java.util.*;

;

/**
 * @author ziyin
 * @create 2019-12-14 9:44
 */

@SpringBootTest
@RunWith(SpringRunner.class)
public class RedisTest {

	@Autowired
	private RedisTemplate<Object,Object> redisTemplate;

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	@Test
	public void test() throws Exception{
			redisTemplate.opsForValue().set("key1", "value1");
			// 注意这里使用了JDK的序列化器，所以Redis保存的时候不是整数，不能运算
			redisTemplate.opsForValue().set("int_key", "1");
			stringRedisTemplate.opsForValue().set("int", "1");
			// 使用运算
			stringRedisTemplate.opsForValue().increment("int", 1);
			// 获取底层Jedis连接
			Jedis jedis = (Jedis) stringRedisTemplate.getConnectionFactory().getConnection().getNativeConnection();
			// 减一操作,这个命令RedisTemplate不支持，所以笔者先获取底层的连接再操作
			jedis.decr("int");
			Map<String, String> hash = new HashMap<String, String>();
			hash.put("field1", "value1");
			hash.put("field2", "value2");
			// 存入一个散列数据类型
			stringRedisTemplate.opsForHash().putAll("hash", hash);
			// 新增一个字段
			stringRedisTemplate.opsForHash().put("hash", "field3", "value3");
			// 绑定散列操作的key，这样可以连续对同一个散列数据类型进行操作
			BoundHashOperations hashOps = stringRedisTemplate.boundHashOps("hash");
			// 删除两个个字段
			hashOps.delete("field1", "field2");
			// 新增一个字段
			hashOps.put("filed4", "value5");
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("success", true);
	}

	@Test
	public void testList() throws Exception{
		// 插入两个列表,注意它们再链表的顺序
		// 链表从左到右顺序为v10,v8,v6,v4,v2
		stringRedisTemplate.opsForList().leftPushAll("list1", "v2", "v4", "v6", "v8", "v10");
		// 链表从左到右顺序为v1,v2,v3,v4,v5,v6
		stringRedisTemplate.opsForList().rightPushAll("list2", "v1", "v2", "v3", "v4", "v5", "v6");
		// 绑定list2链表操作
		BoundListOperations<String, String> listOps = stringRedisTemplate.boundListOps("list2");
		// 从右边弹出一个成员
		Object result1 = listOps.rightPop();
		// 获取定位元素，Redis从0开始计算,这里值为v2,注意如果中间有leftPop操作, 后面元素的索引位置将会全部减1
		Object result2 = listOps.index(1);
		// 从左边插入链表
		listOps.leftPush("v0");
		// 求链表长度
		Long size = listOps.size();
		// 求链表下标区间成员，整个链表下标范围为0到size-1，这里不取最后一个元素
		List<String> elements = listOps.range(0, size - 2);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("success", true);
	}

	@Test
	public void testSet() throws Exception{
		// 请注意：这里v1重复2次，由于集合不允许重复，所以只是插入5个成员到集合中
		redisTemplate.opsForSet().add("set1", "v1", "v1", "v2", "v3", "v4", "v5");
		redisTemplate.opsForSet().add("set2", "v2", "v4", "v6", "v8");
		// 绑定set1集合操作
		BoundSetOperations setOps = redisTemplate.boundSetOps("set1");
		// 增加两个元素
		setOps.add("v6", "v7");
		// 删除两个元素
		setOps.remove("v1", "v7");
		// scan 迭代集合
		ScanOptions.ScanOptionsBuilder scanOptionsBuilder = ScanOptions.scanOptions();
		ScanOptions scanOptions = scanOptionsBuilder.count(Integer.MAX_VALUE).build();
		Cursor c = setOps.scan(scanOptions);
		while (c.hasNext()) {
//			System.out.println(new String((byte[]) c.next()));

			System.out.println(c.next());
		}
		// 返回所有元素
		Set set1 = setOps.members();
		// 求成员数
		Long size = setOps.size();
		// 求交集 v2,v4,v6
		Set inter = setOps.intersect("set2");
		// 求交集，并且用新集合inter保存
		setOps.intersectAndStore("set2", "inter");
		// 求差集  v3,v5 只对set1 求出差 并不包含set2的差集
		Set diff = setOps.diff("set2");
		// 求差集，并且用新集合diff保存
		setOps.diffAndStore("set2", "diff");
		// 求并集
		Set union = setOps.union("set2");
		// 求并集，并且用新集合union保存
		setOps.unionAndStore("set2", "union");
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("success", true);
	}

	@Test
	public void testZSet() throws Exception{
		Set<ZSetOperations.TypedTuple<String>> typedTupleSet = new HashSet<>();
		for (int i = 1; i <= 9; i++) {
			// 分数
			double score = i * 0.1;
			// 创建一个TypedTuple对象，存入值和分数
			ZSetOperations.TypedTuple<String> typedTuple = new DefaultTypedTuple<String>("value" + i, score);
			typedTupleSet.add(typedTuple);
		}
		// 往有序集合插入元素
		stringRedisTemplate.opsForZSet().add("zset1", typedTupleSet);
		// 绑定zset1有序集合操作
		BoundZSetOperations<String, String> zsetOps = stringRedisTemplate.boundZSetOps("zset1");
		// 增加一个元素
		zsetOps.add("value10", 0.26);
		Set<String> setRange = zsetOps.range(1, 6);
		// 按分数排序获取有序集合, 默认是升序
		Set<String> setScore = zsetOps.rangeByScore(0.2, 0.6);
		// 定义值范围
		RedisZSetCommands.Range range = new RedisZSetCommands.Range();
		range.gt("value3");// 大于value3
		// range.gte("value3");// 大于等于value3
		// range.lt("value8");// 小于value8
		range.lte("value8");// 小于等于value8
		// 按值排序，请注意这个排序是按字符串排序
		Set<String> setLex = zsetOps.rangeByLex(range);
		// 删除元素
		zsetOps.remove("value9", "value2");
		// 求分数
		Double score = zsetOps.score("value8");
		// 在下标区间下，按分数排序，同时返回value和score
		Set<ZSetOperations.TypedTuple<String>> rangeSet = zsetOps.rangeWithScores(1, 6);
		// 在分数区间下，按分数排序，同时返回value和score, 因为分数都是小于1的,所以返回是空集合
		Set<ZSetOperations.TypedTuple<String>> scoreSet = zsetOps.rangeByScoreWithScores(1, 6);
		// 按从大到小排序
		Set<String> reverseSet = zsetOps.reverseRange(2, 8);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("success", true);
	}

//	@Test
//	public void testMulti() throws Exception{
//		redisTemplate.opsForValue().set("key1", "value1");
//	List list = redisTemplate.execute(
//				new SessionCallback<Object>() {
//					@Override
//					public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
//						// 设置要监控key1
//						operations.watch("key1");
//						// 开启事务，在exec命令执行前，全部都只是进入队列
//						operations.multi();
//						operations.opsForValue().set("key2", "value2");
//						operations.opsForValue().increment("key1", 1);// ①
//						// 获取值将为null，因为redis只是把命令放入队列，
//						Object value2 = operations.opsForValue().get("key2");
//						System.out.println("命令在队列，所以value为null【" + value2 + "】");
//						operations.opsForValue().set("key3", "value3");
//						Object value3 = operations.opsForValue().get("key3");
//						System.out.println("命令在队列，所以value为null【" + value3 + "】");
//						// 执行exec命令，将先判别key1是否在监控后被修改过，如果是不执行事务，否则执行事务
//						return operations.exec();// ②
//					}
//				};);
//		System.out.println(list);
//		Map<String, Object> map = new HashMap<String, Object>();
//		map.put("success", true);
//	}


	@Test
	public void testMulti() throws Exception{
		redisTemplate.opsForValue().set("key1", "value1");
		List list = redisTemplate.execute(new SessionCallback<List>() {
			@Override
			public List execute(RedisOperations operations) throws DataAccessException {
				// 设置要监控key1
				operations.watch("key1");
				// 开启事务，在exec命令执行前，全部都只是进入队列
				operations.multi();
				operations.opsForValue().set("key2", "value2");
				operations.opsForValue().increment("key1", 1);// ①
				// 获取值将为null，因为redis只是把命令放入队列，
				Object value2 = operations.opsForValue().get("key2");
				System.out.println("命令在队列，所以value为null【" + value2 + "】");
				operations.opsForValue().set("key3", "value3");
				Object value3 = operations.opsForValue().get("key3");
				System.out.println("命令在队列，所以value为null【" + value3 + "】");
				// 执行exec命令，将先判别key1是否在监控后被修改过，如果是不执行事务，否则执行事务
				return operations.exec();// ②
			}
		});
		System.out.println(list);

	}

	@Test
	public void testPipeline() {
		Long start = System.currentTimeMillis();
		List list = (List) redisTemplate.executePipelined(new SessionCallback()  {
			@Override
			public Object execute(RedisOperations operations) throws DataAccessException {
				for (int i = 1; i <= 100000; i++) {
					operations.opsForValue().set("pipeline_" + i, "value_" + i);
					String value = (String) operations.opsForValue().get("pipeline_" + i);
					if (i == 100000) {
						System.out.println("命令只是进入队列，所以值为空【" + value + "】");
					}
				}
				return null;
			}
			});
		Long end = System.currentTimeMillis();
		System.out.println("耗时：" + (end - start) + "毫秒。");  //558毫秒。->：7928毫秒。
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("success", true);
	}

	@Test
	public void testRedisPublish() throws Exception{
		redisTemplate.convertAndSend("topic1","redis publish hello");
	}

	@Test
	public void testLua() {
		DefaultRedisScript<String> rs = new DefaultRedisScript<String>();
		// 设置脚本
		rs.setScriptText("return 'Hello Redis'");
		// 定义返回类型，注意如果没有这个定义Spring不会返回结果
		rs.setResultType(String.class);
		RedisSerializer<String> stringSerializer = redisTemplate.getStringSerializer();
		// 执行Lua脚本
		String str = (String) redisTemplate.execute(rs, stringSerializer, stringSerializer, null);

		System.out.println(str);  //Hello Redis
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("str", str);
	}
}
