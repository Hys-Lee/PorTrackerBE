// package com.PorTracker.PorTrackerBE.global.config;

// import org.redisson.Redisson;
// import org.redisson.api.RedissonClient;
// import org.redisson.config.Config;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;

// @Configuration
// public class RedissonConfig {
//     @Value("${spring.data.redis.host}")
//     private String host;

//     @Value("${spring.data.redis.port}")
//     private int port;

//     @Value("${spring.data.redis.pw : null}")
//     private String redissPw;

//     @Bean
//     public RedissonClient redissonClient() {
//         Config config = new Config();
//         if (redissPw != null) {

//             config.useSingleServer()
//                     .setAddress("redis://" + host + ":" + port)
//                     .setPassword(redissPw)
//                     .setSslEnableEndpointIdentification(true);
//         } else {
//             config.useSingleServer().setAddress("redis://" + host + ":" + port);
//         }
//         return Redisson.create(config);
//     }
// }
