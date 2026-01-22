package com.example.preferences.config;

import com.example.preferences.model.UserPreference;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@EnableCaching
public class CacheConfig {

//    @Bean
//    public RedisCacheConfiguration cacheConfiguration(ObjectMapper objectMapper) {
//        JacksonJsonRedisSerializer<UserPreference> serializer =
//                new JacksonJsonRedisSerializer<>(objectMapper, UserPreference.class);
//
//        return RedisCacheConfiguration.defaultCacheConfig()
//                .serializeValuesWith(
//                        RedisSerializationContext.SerializationPair.fromSerializer(serializer)
//                );
//    }
    @Bean
    public RedisCacheConfiguration cacheConfiguration(ObjectMapper objectMapper) {

        JacksonJsonRedisSerializer<UserPreference> valueSerializer =
                new JacksonJsonRedisSerializer<>(objectMapper, UserPreference.class);

        return RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(valueSerializer)
                );
    }

    @Bean
    public ObjectMapper objectMapper() {
        return JsonMapper.builder()
                .findAndAddModules()
                .build();
    }

}
