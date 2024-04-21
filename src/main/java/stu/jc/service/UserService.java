package stu.jc.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UserService {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    public int setRate(Integer userid) {
        // 为用户分配key
        String limitKey = "limit_" + userid;
        // 获取key的访问次数
        String limitNum = stringRedisTemplate.opsForValue().get(limitKey);
        int limit = -1;
        if (limitNum == null) {
            // 第一次初始化为0
            stringRedisTemplate.opsForValue().set(limitKey, "0", 3600, TimeUnit.SECONDS);
        } else {
            limit = Integer.parseInt(limitNum) + 1;
            stringRedisTemplate.opsForValue().set(limitKey, String.valueOf(limit), 3600, TimeUnit.SECONDS);
        }
        return limit;
    }

    public boolean isRateExceeded(Integer userid) {
        String limitKey = "limit_" + userid;
        String limitNum = stringRedisTemplate.opsForValue().get(limitKey);
        if (limitNum == null) {
            log.error("记录不存在");
            return true;
        }
        return Integer.parseInt(limitNum) > 10;
    }
}
