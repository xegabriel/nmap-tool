package ro.gabe.nmap_core.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ro.gabe.nmap_core.dto.ScanDTO;
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

  public Set<ScanDTO> getScanChangesResults(String ip) {
    //TODO: To be implemented
    return new HashSet<>();
  }
}
