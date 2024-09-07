package ro.gabe.nmap_processor.service;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import ro.gabe.nmap_processor.dto.PortDTO;

@Slf4j
@Service
public class NmapService {

  private static final String PORT_ID = "portid";
  private static final String STATE = "state";
  private static final String SERVICE = "service";
  private static final String NAME = "name";
  public static final String PORT = "port";

  public Set<PortDTO> performNmapScan(String ipAddress) {
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    try {
      validateIp(ipAddress);
      String xmlResult = executeNmapCommand(ipAddress);
      return parseNmapXML(xmlResult);
    } catch (Exception e) {
      log.error("Exception occurred during nmap scan: {}", e.getLocalizedMessage());
      throw new RuntimeException("Exception occurred during nmap scan: " + e.getMessage(), e);
    } finally {
      stopWatch.stop();
      log.info("NMAP scan performed for {} in {} seconds", ipAddress, stopWatch.getTotalTimeSeconds());
    }
  }

  private void validateIp(String ipAddress) {
    // Re-validate the ip to prevent RCE
    try {
      // DNS resolution
      InetAddress.getByName(ipAddress);
    } catch (UnknownHostException e) {
      throw new RuntimeException("Invalid ipAddress " + ipAddress);
    }
  }

  private String executeNmapCommand(String ipAddress) throws IOException, InterruptedException {
    StringBuilder xmlResult = new StringBuilder();
    String command = buildNmapCommand(ipAddress);
    Process process = startProcess(command);
    readProcessOutput(process, xmlResult);
    checkForErrors(process);
    return xmlResult.toString();
  }

  private String buildNmapCommand(String ipAddress) {
    return "nmap -T4 -oX - " + ipAddress;
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
      throw new RuntimeException("Error executing nmap command: " + errorOutput);
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


  // Method to parse the Nmap XML output and extract relevant information
  private Set<PortDTO> parseNmapXML(String xmlContent) throws ParserConfigurationException, IOException, SAXException {

    // Parse the XML
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    InputStream inputStream = new ByteArrayInputStream(xmlContent.getBytes());
    Document doc = builder.parse(inputStream);
    doc.getDocumentElement().normalize();

    // Get the port elements
    NodeList portNodes = doc.getElementsByTagName(PORT);

    Set<PortDTO> ports = new HashSet<>();
    for (int i = 0; i < portNodes.getLength(); i++) {
      Node portNode = portNodes.item(i);

      if (portNode.getNodeType() == Node.ELEMENT_NODE) {
        ports.add(extractPort((Element) portNode));
      }
    }

    return ports;
  }

  private PortDTO extractPort(Element portElement) {
    String portId = portElement.getAttribute(PORT_ID);
    String state = portElement.getElementsByTagName(STATE).item(0).getAttributes().getNamedItem(STATE)
        .getNodeValue();
    String service = portElement.getElementsByTagName(SERVICE).item(0).getAttributes().getNamedItem(NAME)
        .getNodeValue();
    return PortDTO.builder()
        .port(Long.valueOf(portId))
        .state(state)
        .service(service)
        .build();
  }
}