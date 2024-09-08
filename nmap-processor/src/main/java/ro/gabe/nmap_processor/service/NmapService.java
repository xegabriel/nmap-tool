package ro.gabe.nmap_processor.service;

import static ro.gabe.nmap_processor.utils.IpValidatorUtil.validateIp;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import ro.gabe.nmap_processor.dto.PortDTO;
import ro.gabe.nmap_processor.exceptions.NmapScanException;

@Slf4j
@Service
public class NmapService {

  public static final String PORT = "port";
  private static final String PORT_ID = "portid";
  private static final String STATE = "state";
  private static final String SERVICE = "service";
  private static final String NAME = "name";
  private static final String MISSING = "missing";
  private static final String STATE_OPEN = "open";

  @Value("${nmap.processor.total-ports}")
  private int totalPorts;
  @Value("${nmap.processor.thread-count}")
  private int threadCount;

  public Set<PortDTO> performNmapScan(String ipAddress) {
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);

    try {
      validateIp(ipAddress);
      List<Future<Set<PortDTO>>> futures = submitPortScanTasks(ipAddress, executor);
      return collectScanResults(futures, ipAddress);
    } catch (Exception e) {
      if (e instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }
      log.error("Exception occurred during nmap scan for {}: {}", ipAddress, e.getLocalizedMessage());
      throw new NmapScanException("Exception occurred during nmap scan: " + e.getMessage(), e);
    } finally {
      executor.shutdown();
      stopWatch.stop();
      log.info("NMAP scan performed for {} in {} seconds", ipAddress, stopWatch.getTotalTimeSeconds());
    }
  }

  private List<Future<Set<PortDTO>>> submitPortScanTasks(String ipAddress, ExecutorService executor) {
    int portInterval = totalPorts / threadCount;
    List<Future<Set<PortDTO>>> futures = new ArrayList<>();

    for (int i = 0; i < threadCount; i++) {
      int startPort = calculateStartPort(i, portInterval);
      int endPort = calculateEndPort(i, portInterval);

      log.info("Submitting range scan {}-{} for {}", startPort, endPort, ipAddress);
      Callable<Set<PortDTO>> task = () -> executeNmapRangeScan(ipAddress, startPort, endPort);
      futures.add(executor.submit(task));
    }
    return futures;
  }

  private int calculateStartPort(int i, int portInterval) {
    return i * portInterval + 1;
  }

  private int calculateEndPort(int i, int portInterval) {
    return (i == threadCount - 1) ? totalPorts : (i + 1) * portInterval;
  }

  private Set<PortDTO> executeNmapRangeScan(String ipAddress, int startPort, int endPort)
      throws IOException, InterruptedException, ParserConfigurationException, SAXException {
    StringBuilder xmlResult = new StringBuilder();
    String command = buildNmapCommand(ipAddress, startPort, endPort);
    Process process = startProcess(command);
    readProcessOutput(process, xmlResult);
    checkForErrors(process);
    return parseNmapXML(xmlResult.toString());
  }

  private String buildNmapCommand(String ipAddress, int startPort, int endPort) {
    return "nmap -p" + startPort + "-" + endPort + " -oX - " + ipAddress;
  }

  private Process startProcess(String command) throws IOException {
    ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
    return processBuilder.start();
  }

  private void readProcessOutput(Process process, StringBuilder xmlResult) throws IOException {
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        xmlResult.append(line);
      }
    }
  }

  private void checkForErrors(Process process) throws IOException, InterruptedException {
    int exitCode = process.waitFor();
    if (exitCode != 0) {
      String errorOutput = readErrorStream(process);
      throw new NmapScanException("Error executing nmap command: " + errorOutput);
    }
  }

  private String readErrorStream(Process process) throws IOException {
    StringBuilder errorOutput = new StringBuilder();
    try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
      String line;
      while ((line = errorReader.readLine()) != null) {
        errorOutput.append(line).append("\n");
      }
    }
    return errorOutput.toString();
  }

  private Set<PortDTO> parseNmapXML(String xmlContent) throws ParserConfigurationException, IOException, SAXException {

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Set<PortDTO> ports = new HashSet<>();
    try (InputStream inputStream = new ByteArrayInputStream(xmlContent.getBytes())) {
      Document doc = builder.parse(inputStream);
      doc.getDocumentElement().normalize();

      // Get the port elements
      NodeList portNodes = doc.getElementsByTagName(PORT);

      for (int i = 0; i < portNodes.getLength(); i++) {
        Node portNode = portNodes.item(i);

        if (portNode.getNodeType() == Node.ELEMENT_NODE) {
          PortDTO extractedPort = extractPort((Element) portNode);
          if (extractedPort != null) {
            ports.add(extractedPort);
          }
        }
      }
    }

    return ports;
  }

  private PortDTO extractPort(Element portElement) {
    String portId = portElement.getAttribute(PORT_ID);
    String state = portElement.getElementsByTagName(STATE).item(0) != null ?
        portElement.getElementsByTagName(STATE).item(0).getAttributes().getNamedItem(STATE).getNodeValue() : MISSING;

    String service = portElement.getElementsByTagName(SERVICE).item(0) != null ?
        portElement.getElementsByTagName(SERVICE).item(0).getAttributes().getNamedItem(NAME).getNodeValue() : MISSING;
    if (!STATE_OPEN.equalsIgnoreCase(state)) {
      return null;
    }
    return PortDTO.builder()
        .port(Long.valueOf(portId))
        .state(state)
        .service(service)
        .build();
  }

  private Set<PortDTO> collectScanResults(List<Future<Set<PortDTO>>> futures, String ipAddress)
      throws InterruptedException {
    Set<PortDTO> allResults = new HashSet<>();
    for (Future<Set<PortDTO>> future : futures) {
      try {
        allResults.addAll(future.get());
      } catch (ExecutionException e) {
        log.error("Error occurred during scanning in one of the tasks for IP: {}. Error: {}", ipAddress,
            e.getCause().getMessage());
        throw new NmapScanException("Error during scanning", e);
      }
    }
    return allResults;
  }
}