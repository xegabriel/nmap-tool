package ro.gabe.nmap_core.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ro.gabe.nmap_core.dto.PortDTO;
import ro.gabe.nmap_core.dto.ScanDTO;
import ro.gabe.nmap_core.exceptions.HistoryNotAvailableException;
import ro.gabe.nmap_core.model.Port;
import ro.gabe.nmap_core.model.Scan;
import ro.gabe.nmap_core.repository.ScanRepository;

@Service
@RequiredArgsConstructor
public class ScanService {

  private final ScanRepository scanRepository;
  private final ModelMapper mapper;

  public Page<ScanDTO> getScanResults(String ip, Pageable pageable) {
    Page<Scan> scans = scanRepository.findByIp(ip, pageable);

    return scans.map(scan -> mapper.map(scan, ScanDTO.class));
  }

  public ScanDTO getScanChangesResults(String ip) {
    Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<Scan> scans = scanRepository.findByIp(ip, pageable);

    if (scans.getTotalElements() < 2) {
      throw new HistoryNotAvailableException();
    }

    Scan mostRecentScan = scans.getContent().get(0);
    Scan previousScan = scans.getContent().get(1);

    Set<Port> newPorts = new HashSet<>(mostRecentScan.getPorts());
    newPorts.removeAll(previousScan.getPorts()); // Remove ports that are in both scans

    Scan diffScan = new Scan();
    diffScan.setIp(ip);
    diffScan.setCreatedAt(mostRecentScan.getCreatedAt());  // Optional: set the date of the latest scan
    diffScan.setPorts(newPorts);

    // Convert to ScanDTO (assuming you're using a mapper for conversion)
    return mapper.map(diffScan, ScanDTO.class);
  }
}
