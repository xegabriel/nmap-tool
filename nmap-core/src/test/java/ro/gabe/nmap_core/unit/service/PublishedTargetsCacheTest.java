package ro.gabe.nmap_core.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import ro.gabe.nmap_core.service.PublishedTargetsCache;

public class PublishedTargetsCacheTest {

  @Mock
  private RedisTemplate<String, Object> redisTemplate;

  @Mock
  private ValueOperations<String, Object> valueOperations;

  @InjectMocks
  private PublishedTargetsCache publishedTargetsCache;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
  }

  @Test
  void testIsCached_TargetExists() {
    // Given
    String target = "test-target";
    String cacheKey = "publishedTargetsCache::" + target;
    when(valueOperations.get(cacheKey)).thenReturn(true);

    // When
    boolean result = publishedTargetsCache.isCached(target);

    // Then
    assertTrue(result);
    verify(valueOperations).get(cacheKey);
  }

  @Test
  void testIsCached_TargetDoesNotExist() {
    // Given
    String target = "test-target";
    String cacheKey = "publishedTargetsCache::" + target;
    when(valueOperations.get(cacheKey)).thenReturn(null);

    // When
    boolean result = publishedTargetsCache.isCached(target);

    // Then
    assertFalse(result);
    verify(valueOperations).get(cacheKey);
  }

  @Test
  void testCache_TargetIsCached() {
    // Given
    String target = "test-target";
    String cacheKey = "publishedTargetsCache::" + target;

    // When
    publishedTargetsCache.cache(target);

    // Then
    verify(valueOperations).set(cacheKey, true);
    verify(redisTemplate).expire(cacheKey, 60, TimeUnit.SECONDS);
  }

  @Test
  void testGetKey() {
    // Given
    String target = "test-target";

    // When
    String cacheKey = publishedTargetsCache.getKey(target);

    // Then
    assertEquals("publishedTargetsCache::test-target", cacheKey);
  }
}
