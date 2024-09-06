package ro.gabe.nmap_core.annotations.validators;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.springframework.util.StringUtils;

public final class IPValidatorUtil {

  private IPValidatorUtil() {
  }

  public static boolean isIpValid(String ip) {
    boolean isIpValid = false;
    if (!StringUtils.hasLength(ip)) {
      return isIpValid;
    }
    try {
      // DNS resolution
      InetAddress.getByName(ip);
      isIpValid = true;
    } catch (UnknownHostException e) {
      isIpValid = false;
    }
    return isIpValid;
  }
}
