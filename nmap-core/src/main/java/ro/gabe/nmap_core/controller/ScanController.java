package ro.gabe.nmap_core.controller;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ro.gabe.nmap_core.annotations.ValidIP;
import ro.gabe.nmap_core.dto.ScanDTO;
import ro.gabe.nmap_core.dto.ScansDTO;
import ro.gabe.nmap_core.service.KafkaProducerService;
import ro.gabe.nmap_core.service.ScanService;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/scans")
public class ScanController {

  private final KafkaProducerService kafkaProducerService;
  private final ScanService scanService;

  @PostMapping("/init")
  public ResponseEntity<ScansDTO> submitTargetsForScan(@Valid @RequestBody ScansDTO scansDTO) {
    ScansDTO publishedScanDto = kafkaProducerService.publishTargetsForScan(scansDTO);
    if (publishedScanDto.getTargets().containsAll(scansDTO.getTargets())) {
      return new ResponseEntity<>(publishedScanDto, HttpStatus.CREATED);
    } else {
      return new ResponseEntity<>(publishedScanDto, HttpStatus.MULTI_STATUS);
    }
  }

  @GetMapping("/{ip}")
  public ResponseEntity<Page<ScanDTO>> getScanResults(
      @PathVariable @NotEmpty @ValidIP String ip,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {

    Pageable pageable = PageRequest.of(page, size);
    Page<ScanDTO> scanResults = scanService.getScanResults(ip, pageable);

    return new ResponseEntity<>(scanResults, HttpStatus.OK);
  }

  @GetMapping("/changes/{ip}")
  public ResponseEntity<ScanDTO> getScanChangesResults(@PathVariable @NotEmpty @ValidIP String ip) {
    ScanDTO scanResults = scanService.getScanChangesResults(ip);
    return new ResponseEntity<>(scanResults, HttpStatus.OK);
  }
}
