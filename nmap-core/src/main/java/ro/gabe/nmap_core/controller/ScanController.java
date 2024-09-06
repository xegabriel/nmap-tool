package ro.gabe.nmap_core.controller;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.gabe.nmap_core.dto.ClientDTO;
import ro.gabe.nmap_core.dto.ScanRequestDTO;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/scans")
public class ScanController {
  @PostMapping("/init")
  public ResponseEntity<Map<String, Object>> submitTargetsForScan(@RequestBody ScanRequestDTO scanRequestDTO) {
    Map<String, Object> response = new HashMap<>();
    //TODO: Implement target validation
    //TODO: Implement kafka dispatching
    response.put("status", "success");
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  @GetMapping("/{ip}")
  public Set<ClientDTO> getScanResults(@PathVariable String ip) {
    //TODO: Implement mongo retrieval
    //TODO: Implement pagination
    //TODO: Implament validation
    return new HashSet<>();
  }

  @GetMapping("/changes/{ip}")
  public Set<ClientDTO> getScanChangesResults(@PathVariable String ip) {
    //TODO: Implement mongo retrieval
    //TODO: Implament validation
    return new HashSet<>();
  }
}
