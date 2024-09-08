package ro.gabe.nmap_core.unit.annotations.validators;

import org.junit.jupiter.api.Test;

import ro.gabe.nmap_core.annotations.validators.IPValidatorUtil;

import static org.junit.jupiter.api.Assertions.*;

public class IPValidatorUtilTest {

  @Test
  void testIsIpValid_ValidIp() {
    // Given
    String ip = "8.8.8.8"; // Google DNS IP

    // When
    boolean result = IPValidatorUtil.isIpValid(ip);

    // Then
    assertTrue(result);
  }

  @Test
  void testIsIpValid_InvalidIp() {
    // Given
    String ip = "999.999.999.999"; // Invalid IP format

    // When
    boolean result = IPValidatorUtil.isIpValid(ip);

    // Then
    assertFalse(result);
  }

  @Test
  void testIsIpValid_NullIp() {
    // Given
    String ip = null;

    // When
    boolean result = IPValidatorUtil.isIpValid(ip);

    // Then
    assertFalse(result);
  }

  @Test
  void testIsIpValid_EmptyIp() {
    // Given
    String ip = "";

    // When
    boolean result = IPValidatorUtil.isIpValid(ip);

    // Then
    assertFalse(result);
  }

  @Test
  void testIsIpValid_HostnameInsteadOfIp() {
    // Given
    String hostname = "example.com"; // A valid DNS hostname

    // When
    boolean result = IPValidatorUtil.isIpValid(hostname);

    // Then
    assertTrue(result); // It should return true because DNS resolution will work
  }

  @Test
  void testIsIpValid_MalformedIp() {
    // Given
    String malformedIp = "256.256.256.256"; // Invalid IP due to out-of-range numbers

    // When
    boolean result = IPValidatorUtil.isIpValid(malformedIp);

    // Then
    assertFalse(result);
  }

  @Test
  void testIsIpValid_LoopbackIp() {
    // Given
    String loopbackIp = "127.0.0.1"; // Loopback IP address

    // When
    boolean result = IPValidatorUtil.isIpValid(loopbackIp);

    // Then
    assertTrue(result);
  }

  @Test
  void testIsIpValid_InvalidFormat() {
    // Given
    String invalidFormatIp = "abc.def.ghi.jkl"; // Completely invalid format

    // When
    boolean result = IPValidatorUtil.isIpValid(invalidFormatIp);

    // Then
    assertFalse(result);
  }
}