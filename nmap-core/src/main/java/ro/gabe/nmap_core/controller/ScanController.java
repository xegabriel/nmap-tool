package ro.gabe.nmap_core.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
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
  public ResponseEntity<Map<String, Object>> submitTargetsForScan(@Valid @RequestBody ScansDTO scansDTO) {
    Map<String, Object> response = new HashMap<>();
    kafkaProducerService.publishTargetsForScan(scansDTO);
    response.put("status", "success");
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  @GetMapping("/{ip}")
  public ResponseEntity<Set<ScanDTO>> getScanResults(@PathVariable @NotEmpty @ValidIP String ip) {
    //TODO: Implement pagination
    Set<ScanDTO> scanResults = scanService.getScanResults(ip);
    if (scanResults.isEmpty()) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } else {
      return new ResponseEntity<>(scanResults, HttpStatus.OK);
    }
  }

  @GetMapping("/changes/{ip}")
  public ResponseEntity<Set<ScanDTO>> getScanChangesResults(@PathVariable @NotEmpty @ValidIP String ip) {
    Set<ScanDTO> scanResults = scanService.getScanChangesResults(ip);
    if (scanResults.isEmpty()) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } else {
      return new ResponseEntity<>(scanResults, HttpStatus.OK);
    }
  }
}
