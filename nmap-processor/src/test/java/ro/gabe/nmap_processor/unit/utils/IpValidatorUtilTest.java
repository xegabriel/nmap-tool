package ro.gabe.nmap_processor.unit.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import ro.gabe.nmap_processor.exceptions.InvalidIpException;
import ro.gabe.nmap_processor.utils.IpValidatorUtil;

import static org.junit.jupiter.api.Assertions.*;

public class IpValidatorUtilTest {

  @Test
  void testValidateIp_ValidIp() {
    // Given
    String validIp = "8.8.8.8"; // Google DNS

    // When & Then (no exception should be thrown)
    assertDoesNotThrow(() -> IpValidatorUtil.validateIp(validIp));
  }

  @Test
  void testValidateIp_InvalidIp() {
    // Given
    String invalidIp = "999.999.999.999"; // Invalid IP format

    // When
    Executable executable = () -> IpValidatorUtil.validateIp(invalidIp);

    // Then
    InvalidIpException exception = assertThrows(InvalidIpException.class, executable);
    assertTrue(exception.getMessage().contains("Invalid ipAddress " + invalidIp));
  }

  @Test
  void testValidateIp_EmptyIp() {
    // Given
    String emptyIp = "";

    // When
    Executable executable = () -> IpValidatorUtil.validateIp(emptyIp);

    // Then
    InvalidIpException exception = assertThrows(InvalidIpException.class, executable);
    assertTrue(exception.getMessage().contains("Invalid ipAddress " + emptyIp));
  }

  @Test
  void testValidateIp_NullIp() {
    // Given
    String nullIp = null;

    // When
    Executable executable = () -> IpValidatorUtil.validateIp(nullIp);

    // Then
    InvalidIpException exception = assertThrows(InvalidIpException.class, executable);
    assertTrue(exception.getMessage().contains("Invalid ipAddress null"));
  }

  @Test
  void testValidateIp_HostnameInsteadOfIp() {
    // Given
    String hostname = "example.com"; // Valid hostname

    // When & Then (no exception should be thrown)
    assertDoesNotThrow(() -> IpValidatorUtil.validateIp(hostname));
  }

  @Test
  void testValidateIp_MalformedIp() {
    // Given
    String malformedIp = "abc.def.ghi.jkl"; // Completely invalid format

    // When
    Executable executable = () -> IpValidatorUtil.validateIp(malformedIp);

    // Then
    InvalidIpException exception = assertThrows(InvalidIpException.class, executable);
    assertTrue(exception.getMessage().contains("Invalid ipAddress " + malformedIp));
  }
}