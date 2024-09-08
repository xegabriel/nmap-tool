package ro.gabe.nmap_core.service;

import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import ro.gabe.nmap_core.dto.ScanDTO;
import ro.gabe.nmap_core.exceptions.NotFoundException;
import ro.gabe.nmap_core.model.Port;
import ro.gabe.nmap_core.model.Scan;
import ro.gabe.nmap_core.repository.ScanRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScanService {

  private final ScanRepository scanRepository;
  private final ModelMapper mapper;

  public Page<ScanDTO> getScanResults(String ip, Pageable pageable) {
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    Page<Scan> scans = scanRepository.findByIp(ip, pageable);

    if (scans.getTotalElements() == 0) {
      log.warn("Not scans available for IP: {}.", ip);
      throw new NotFoundException();
    }

    Page<ScanDTO> scansPage = scans.map(scan -> mapper.map(scan, ScanDTO.class));
    stopWatch.stop();
    log.info("Query for IP: {} completed in {} ms. Total scans found: {}", ip, stopWatch.getTotalTimeMillis(),
        scans.getTotalElements());
    return scansPage;
  }

  public ScanDTO getScanChangesResults(String ip) {
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<Scan> scans = scanRepository.findByIp(ip, pageable);

    if (scans.getTotalElements() < 2) {
      log.warn("Not enough scan history available for IP: {}. Total scans found: {}", ip, scans.getTotalElements());
      throw new NotFoundException();
    }

    Scan mostRecentScan = scans.getContent().get(0);
    Scan previousScan = scans.getContent().get(1);
    ScanDTO scanDiffDTO = calculateDiff(mostRecentScan, previousScan);

    stopWatch.stop();
    log.info("Scan diff calculation completed for IP: {} in {} ms. New ports found: {}", ip,
        stopWatch.getTotalTimeMillis(), scanDiffDTO.getPorts().size());
    return scanDiffDTO;
  }

  public ScanDTO calculateDiff(Scan mostRecentScan, Scan previousScan) {
    Set<Port> newPorts = new HashSet<>(mostRecentScan.getPorts());
    newPorts.removeAll(previousScan.getPorts());

    Scan diffScan = new Scan();
    diffScan.setIp(mostRecentScan.getIp());
    diffScan.setCreatedAt(mostRecentScan.getCreatedAt());
    diffScan.setPorts(newPorts);

    return mapper.map(diffScan, ScanDTO.class);
  }
}
