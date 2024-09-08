package ro.gabe.nmap_core.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import ro.gabe.nmap_core.exceptions.KafkaDispatchingException;
import ro.gabe.nmap_core.service.KafkaProducerService;
import ro.gabe.nmap_core.service.PublishedTargetsCache;

public class KafkaProducerServiceTest {
  @Mock
  private KafkaTemplate<String, String> kafkaTemplate;

  @Mock
  private PublishedTargetsCache publishedTargetsCache;

  @InjectMocks
  private KafkaProducerService kafkaProducerService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testPublishTargetForScan_CachedTarget() {
    // Given
    String target = "test-target";
    when(publishedTargetsCache.isCached(target)).thenReturn(true);

    // When
    String result = kafkaProducerService.publishTargetForScan(target);

    // Then
    assertNull(result);
    verify(kafkaTemplate, never()).send(anyString(), anyString());
    verify(publishedTargetsCache, never()).cache(target);
  }

  @Test
  void testPublishTargetForScan_NotCachedTarget_SuccessfulSend() throws ExecutionException, InterruptedException {
    // Given
    String target = "test-target";
    when(publishedTargetsCache.isCached(target)).thenReturn(false);

    ListenableFuture<SendResult<String, String>> future = mock(ListenableFuture.class);
    when(future.get()).thenReturn(mock(SendResult.class));
    when(kafkaTemplate.send(anyString(), anyString())).thenReturn(future);

    // When
    String result = kafkaProducerService.publishTargetForScan(target);

    // Then
    assertEquals(target, result);
    verify(kafkaTemplate).send(KafkaProducerService.TOPIC, target);
    verify(publishedTargetsCache).cache(target);
  }

  @Test
  void testPublishTargetForScan_NotCachedTarget_FailedSend_ExecutionException() throws ExecutionException, InterruptedException {
    // Given
    String target = "test-target";
    when(publishedTargetsCache.isCached(target)).thenReturn(false);

    ListenableFuture<SendResult<String, String>> future = mock(ListenableFuture.class);
    when(future.get()).thenThrow(new ExecutionException("Error", new RuntimeException()));
    when(kafkaTemplate.send(anyString(), anyString())).thenReturn(future);

    // When & Then
    KafkaDispatchingException exception = assertThrows(KafkaDispatchingException.class,
        () -> kafkaProducerService.publishTargetForScan(target));

    assertTrue(exception.getMessage().contains("Failed to send " + target));
    verify(publishedTargetsCache, never()).cache(target);
  }
}
