package ro.gabe.nmap_core.service;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublishedTargetsCache {

  private static final String CACHE_PUBLISHED_TARGETS_PREFIX = "publishedTargetsCache::";
  private static final int TARGET_TTL_SECONDS = 60;
  private final RedisTemplate<String, Object> redisTemplate;

  public boolean isCached(String target) {
    String cacheKey = getKey(target);
    Object cachedValue = redisTemplate.opsForValue().get(cacheKey);
    log.info("Target {} exists in Redis cache: {}", target, cachedValue != null);
    return cachedValue != null;

  }

  public void cache(String target) {
    String cacheKey = getKey(target);
    log.info("Storing target {} in Redis cache", target);
    redisTemplate.opsForValue().set(cacheKey, true);
    redisTemplate.expire(cacheKey, TARGET_TTL_SECONDS, TimeUnit.SECONDS);
  }

  private String getKey(String target) {
    return CACHE_PUBLISHED_TARGETS_PREFIX + target;
  }
}
