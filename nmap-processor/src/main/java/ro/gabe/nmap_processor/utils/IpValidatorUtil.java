package ro.gabe.nmap_processor.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import ro.gabe.nmap_processor.exceptions.InvalidIpException;

public final class IpValidatorUtil {

  private IpValidatorUtil() {
  }

  public static void validateIp(String ipAddress) {
    try {
      InetAddress.getByName(ipAddress);  // DNS resolution
    } catch (UnknownHostException e) {
      throw new InvalidIpException("Invalid ipAddress " + ipAddress);
    }
  }
}
