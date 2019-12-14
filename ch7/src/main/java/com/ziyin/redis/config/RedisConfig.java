package com.ziyin.redis.config;

import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

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




//	@Bean(name = "redisConnectionFactory")
////	public RedisConnectionFactory initConnectionFactory() {
////		if (this.connectionFactory != null) {
////			return this.connectionFactory;
////		}
////		JedisPoolConfig poolConfig = new JedisPoolConfig();
////		// 最大空闲数
////		poolConfig.setMaxIdle(50);
////		// 最大连接数
////		poolConfig.setMaxTotal(100);
////		// 最大等待毫秒数
////		poolConfig.setMaxWaitMillis(2000);
////		// 创建Jedis连接工厂
////		JedisConnectionFactory connectionFactory = new JedisConnectionFactory(poolConfig);
////		// 配置Redis连接服务器
////		RedisStandaloneConfiguration rsc = connectionFactory.getStandaloneConfiguration();
////		rsc.setHostName("192.168.10.128");
////		rsc.setPort(6379);
////		rsc.setPassword(RedisPassword.of("123456"));
////		this.connectionFactory = connectionFactory;
////		return connectionFactory;
////	}
	
//	@Bean(name="redisTemplate")
//	public RedisTemplate<Object, Object> initRedisTemplate() {
//	    RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
//	    redisTemplate.setConnectionFactory(initConnectionFactory());
//	    RedisSerializer<String> stringRedisSerializer = redisTemplate.getStringSerializer();
//	    redisTemplate.setKeySerializer(stringRedisSerializer);
//	    redisTemplate.setHashKeySerializer(stringRedisSerializer);
//	    redisTemplate.setHashValueSerializer(stringRedisSerializer);
//	  return redisTemplate;
//	}
}