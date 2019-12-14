package com.ziyin.redis;

import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class Chapter7Application {

	// RedisTemplate
	@Autowired
	private RedisTemplate redisTemplate;


	// 设置RedisTemplate的序列化器
	private void initRedisTemplate() {
		RedisSerializer stringSerializer = redisTemplate.getStringSerializer();
//		redisTemplate.setKeySerializer(stringSerializer);
//		redisTemplate.setHashKeySerializer(stringSerializer);
		FastJsonRedisSerializer<Object> fastJsonRedisSerializer = new FastJsonRedisSerializer<>(Object.class);



		// 全局开启AutoType，不建议使用
		// ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
		// 建议使用这种方式，小范围指定白名单
		ParserConfig.getGlobalInstance().addAccept("com.ziyin");

		// 设置值（value）的序列化采用FastJsonRedisSerializer。
		redisTemplate.setValueSerializer(fastJsonRedisSerializer);
		redisTemplate.setHashValueSerializer(fastJsonRedisSerializer);
		// 设置键（key）的序列化采用StringRedisSerializer。
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setHashKeySerializer(new StringRedisSerializer());


	}


	@Autowired
	private RedisConnectionFactory connectionFactory;

	// 自定义初始化方法
	@PostConstruct
	public void init() {
	    initRedisTemplate();
	}
	
	
//	@Bean(name = "redisCacheManager" )
//	public RedisCacheManager initRedisCacheManager() {
//		// Redis加锁的写入器
//		RedisCacheWriter writer= RedisCacheWriter.lockingRedisCacheWriter(connectionFactory);
//		// 启动Redis缓存的默认设置
//		RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
//		// 设置JDK序列化器
//		config = config.serializeValuesWith(SerializationPair.fromSerializer(new JdkSerializationRedisSerializer()));
//		// 禁用前缀
//		config = config.disableKeyPrefix();
//		//设置10分钟超时
//		config = config.entryTtl(Duration.ofMinutes(10));
//		// 创建缓Redis存管理器
//		RedisCacheManager redisCacheManager = new RedisCacheManager(writer, config);
//		return redisCacheManager;
//	}
	
	

//	// Redis连接工厂
//	@Autowired
//	private RedisConnectionFactory connectionFactory = null;
//
//	// Redis消息监听器
//	@Autowired
//	private MessageListener redisMsgListener = null;
//
//	// 任务池
//	private ThreadPoolTaskScheduler taskScheduler = null;
//
//	/**
//	 * 创建任务池，运行线程等待处理Redis的消息
//	 * 
//	 * @return
//	 */
//	@Bean
//	public ThreadPoolTaskScheduler initTaskScheduler() {
//		if (taskScheduler != null) {
//			return taskScheduler;
//		}
//		taskScheduler = new ThreadPoolTaskScheduler();
//		taskScheduler.setPoolSize(20);
//		return taskScheduler;
//	}
//
//	/**
//	 * 定义Redis的监听容器
//	 * 
//	 * @return 监听容器
//	 */
//	@Bean
//	public RedisMessageListenerContainer initRedisContainer() {
//		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
//		// Redis连接工厂
//		container.setConnectionFactory(connectionFactory);
//		// 设置运行任务池
//		container.setTaskExecutor(initTaskScheduler());
//		// 定义监听渠道，名称为topic1
//		Topic topic = new ChannelTopic("topic1");
//		// 使用监听器监听Redis的消息
//		container.addMessageListener(redisMsgListener, topic);
//		return container;
//	}
//
	public static void main(String[] args) {
		SpringApplication.run(Chapter7Application.class, args);
	}

}
