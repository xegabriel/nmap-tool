package ro.gabe.nmap_core.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import ro.gabe.nmap_core.dto.ScanDTO;
import ro.gabe.nmap_core.model.Scan;
import ro.gabe.nmap_core.repository.ScanRepository;

@Service
@RequiredArgsConstructor
public class ScanService {

  private final ScanRepository scanRepository;
  private final ModelMapper mapper;

  public Set<ScanDTO> getScanResults(String ip) {
    List<Scan> scans = scanRepository.findByIp(ip);

    return scans.stream()
        .map(scan -> mapper.map(scan, ScanDTO.class))
        .collect(Collectors.toSet());
  }

  public Set<ScanDTO> getScanChangesResults(String ip) {
    //TODO: To be implemented
    return new HashSet<>();
  }
}
