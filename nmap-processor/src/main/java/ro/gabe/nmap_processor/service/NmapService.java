package ro.gabe.nmap_processor.service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import ro.gabe.nmap_processor.dto.PortDTO;

@Slf4j
@Service
public class NmapService {

  public Set<PortDTO> performNmapScan(String ipAddress) {
    StringBuilder xmlResult = new StringBuilder();
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    try {
      // Command to execute with XML output (-oX)
      String command = "nmap -T4 -oX - " + ipAddress;

      // Create a process builder to execute the command
      ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
      Process process = processBuilder.start();

      // Read the output of the command (XML)
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String line;
      while ((line = reader.readLine()) != null) {
        xmlResult.append(line);
      }

      // Wait for the process to complete and check if there were errors
      int exitCode = process.waitFor();
      if (exitCode != 0) {
        // Read the error stream if the command fails
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        StringBuilder errorOutput = new StringBuilder();
        while ((line = errorReader.readLine()) != null) {
          errorOutput.append(line).append("\n");
        }
        throw new RuntimeException("Error executing nmap command: " + errorOutput.toString());
      }

      // Parse the XML result to extract port, state, and service information
      String xmlContent = xmlResult.toString();
      return parseNmapXML(xmlContent);

    } catch (Exception e) {
      throw new RuntimeException("Exception occurred during nmap scan: " + e.getMessage(), e);
    } finally {
      stopWatch.stop();
      log.info("NMAP scan performed for {} in {} seconds", ipAddress, stopWatch.getTotalTimeSeconds());
    }
  }

  // Method to parse the Nmap XML output and extract relevant information
  private Set<PortDTO> parseNmapXML(String xmlContent) throws Exception {
    Set<PortDTO> ports = new HashSet<>();

    // Parse the XML
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    InputStream inputStream = new ByteArrayInputStream(xmlContent.getBytes());
    Document doc = builder.parse(inputStream);
    doc.getDocumentElement().normalize();

    // Get the port elements
    NodeList portNodes = doc.getElementsByTagName("port");

    for (int i = 0; i < portNodes.getLength(); i++) {
      Node portNode = portNodes.item(i);

      if (portNode.getNodeType() == Node.ELEMENT_NODE) {
        Element portElement = (Element) portNode;

        // Extract port number, state, and service
        String portId = portElement.getAttribute("portid");
        String state = portElement.getElementsByTagName("state").item(0).getAttributes().getNamedItem("state")
            .getNodeValue();
        String service = portElement.getElementsByTagName("service").item(0).getAttributes().getNamedItem("name")
            .getNodeValue();

        // Format and append the result
        ports.add(PortDTO.builder()
            .port(Long.valueOf(portId))
            .state(state)
            .service(service)
            .build());
      }
    }

    return ports;
  }
}