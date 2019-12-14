package com.ziyin.redis.config;

import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.time.Duration;

/**** imports ****/
@Configuration
public class RedisConfig {

	private ThreadPoolTaskScheduler taskScheduler;

	@Autowired
	private RedisConnectionFactory connectionFactory;

	// Redis消息监听器
	@Autowired
	private MessageListener redisMsgListener;

	@Bean(name = "redisTemplate")
	@SuppressWarnings("unchecked")
	@ConditionalOnMissingBean(name = "redisTemplate")
	public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<Object, Object> template = new RedisTemplate<>();

		//使用fastjson序列化
		FastJsonRedisSerializer fastJsonRedisSerializer = new FastJsonRedisSerializer(Object.class);
		ParserConfig.getGlobalInstance().addAccept("com.ziyin.");

		// value值的序列化采用fastJsonRedisSerializer
		template.setValueSerializer(fastJsonRedisSerializer);
		template.setHashValueSerializer(fastJsonRedisSerializer);
		// key的序列化采用StringRedisSerializer
		template.setKeySerializer(new StringRedisSerializer());
		template.setHashKeySerializer(fastJsonRedisSerializer);
		template.setConnectionFactory(redisConnectionFactory);
		return template;
	}

		/**
	 * 创建任务池，运行线程等待处理Redis的消息
	 *
	 * @return
	 */
	@Bean
	public ThreadPoolTaskScheduler initTaskScheduler() {
		if (taskScheduler != null) {
			return taskScheduler;
		}
		taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.setPoolSize(20);
		return taskScheduler;
	}

		/**
	 * 定义Redis的监听容器
	 *
	 * @return 监听容器
	 */
	@Bean
	public RedisMessageListenerContainer initRedisContainer() {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		// Redis连接工厂
		container.setConnectionFactory(connectionFactory);
		// 设置运行任务池
		container.setTaskExecutor(initTaskScheduler());
		// 定义监听渠道，名称为topic1
		Topic topic = new ChannelTopic("topic1");
		// 使用监听器监听Redis的消息
		container.addMessageListener(redisMsgListener, topic);
		return container;
	}

	/**
	 * 自定义配置较多的时候可以使用代码配置, 较少使用application配置,
	 * 但是只能使用一种, 不能两种都使用
	 * @return
	 */
	@Bean(name = "redisCacheManager" )
	public RedisCacheManager initRedisCacheManager() {
		// Redis加锁的写入器
		RedisCacheWriter writer= RedisCacheWriter.lockingRedisCacheWriter(connectionFactory);
		// 启动Redis缓存的默认设置
		RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();
		// 设置JDK序列化器
		config = config.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new FastJsonRedisSerializer(Object.class)));
		// 禁用前缀
		config = config.disableKeyPrefix();
		//设置10分钟超时
		config = config.entryTtl(Duration.ofMinutes(10));
		// 创建缓Redis存管理器
		RedisCacheManager redisCacheManager = new RedisCacheManager(writer, config);
		return redisCacheManager;
	}


}