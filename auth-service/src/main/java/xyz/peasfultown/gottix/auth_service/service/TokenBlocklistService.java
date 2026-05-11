package xyz.peasfultown.gottix.auth_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;

@Service
@Slf4j
public class TokenBlocklistService {
    private static final String BLOCKLIST_PREFIX = "blocklist:";

    private final StringRedisTemplate redisTemplate;

    @Autowired
    public TokenBlocklistService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void block(String jti, Date expiry) {
        long ttlMillis = expiry.getTime() - System.currentTimeMillis();
        if (ttlMillis <= 0) {
            // Already expired — no need to blocklist
            return;
        }
        String key = BLOCKLIST_PREFIX + jti;
        redisTemplate.opsForValue().set(key, "1", Duration.ofMillis(ttlMillis));
        log.debug("Blocked token jti={} for {}ms", jti, ttlMillis);
    }

    // Returns true if the token's jti is in the blocklist (i.e. logged out).
    public boolean isBlocked(String jti) {
        return redisTemplate.hasKey(BLOCKLIST_PREFIX + jti);
    }
}
