package ro.gabe.nmap_processor.service;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import ro.gabe.nmap_processor.dto.PortDTO;
import ro.gabe.nmap_processor.dto.ScanDTO;
import ro.gabe.nmap_processor.model.Scan;
import ro.gabe.nmap_processor.repository.ScanRepository;

@Service
@RequiredArgsConstructor
public class ScanService {

  private final ScanRepository scanRepository;
  private final NmapService nmapService;
  private final ModelMapper mapper;

  public ScanDTO performScan(String target) {
    Set<PortDTO> portsFound = nmapService.performNmapScan(target);
    ScanDTO scanResult = ScanDTO.builder()
        .ip(target)
        .ports(portsFound)
        .build();
    Scan scanModel = mapper.map(scanResult, Scan.class);
    Scan savedScanResult = scanRepository.save(scanModel);
    return mapper.map(savedScanResult, ScanDTO.class);
  }

}
