package com.PorTracker.PorTrackerBE.service;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final StringRedisTemplate redisTemplate;

    /**
     * 데이터를 저장함.
     *
     * @param key : 저장할 이름 ex) "user:123:sheet"
     * @param value : 저장할 값 ex) "spreadsheet_id_abc"
     * @param timeoutMinutes : 유효시간 (분)
     */
    public void setValues(String key, String value, long timeoutMinutes) {
        redisTemplate.opsForValue().set(key, value, timeoutMinutes, TimeUnit.MINUTES);
    }

    public String getValues(String key) {
        return redisTemplate.opsForValue().get(key);
    }
}
