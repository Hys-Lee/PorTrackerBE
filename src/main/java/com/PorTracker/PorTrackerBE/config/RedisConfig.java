// package com.PorTracker.PorTrackerBE.config;

// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.fasterxml.jackson.databind.SerializationFeature;
// import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
// // import java.io.ObjectInputFilter.Config;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.data.redis.connection.RedisConnectionFactory;
// import org.springframework.data.redis.core.RedisTemplate;
// import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
// import org.springframework.data.redis.serializer.StringRedisSerializer;

// @Configuration
// public class RedisConfig {
//     @Bean
//     public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory)
// {
//         RedisTemplate<String, Object> template = new RedisTemplate<>();
//         template.setConnectionFactory(connectionFactory);

//         // key는 문자열로
//         template.setKeySerializer(new StringRedisSerializer());
//         template.setHashKeySerializer(new StringRedisSerializer());

//         // 객체를 json으로 직렬화. (ObjectMapper는 java object <-> JSON 변환 담당)
//         ObjectMapper objectMapper =
//                 new ObjectMapper()
//                         .registerModule(new JavaTimeModule())
//                         .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

//         Jackson2JsonRedisSerializer<Object> serializer =
//                 new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);

//         template.setValueSerializer(serializer);
//         template.setHashValueSerializer(serializer);

//         return template;
//     }
// }
