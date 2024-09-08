package ro.gabe.nmap_core.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import ro.gabe.nmap_core.controller.ScanController;
import ro.gabe.nmap_core.dto.ScanDTO;
import ro.gabe.nmap_core.dto.ScansDTO;
import ro.gabe.nmap_core.service.KafkaProducerService;
import ro.gabe.nmap_core.service.ScanService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ScanControllerTest {

  private MockMvc mockMvc;

  @Mock
  private KafkaProducerService kafkaProducerService;

  @Mock
  private ScanService scanService;

  @InjectMocks
  private ScanController scanController;

  @BeforeEach
  void setUp() {
    // Initialize Mockito annotations
    MockitoAnnotations.openMocks(this);

    mockMvc = MockMvcBuilders.standaloneSetup(scanController).build();
  }

  @Test
  void testSubmitTargetsForScan() throws Exception {
    // Given
    ScansDTO scansDTO = new ScansDTO();
    scansDTO.setTargets(Collections.singleton("192.168.1.1"));

    ScansDTO publishedScansDTO = new ScansDTO();
    publishedScansDTO.setTargets(Collections.singleton("192.168.1.1"));

    when(kafkaProducerService.publishTargetsForScan(any(ScansDTO.class))).thenReturn(publishedScansDTO);

    // When
    ResultActions result = mockMvc.perform(post("/api/scans/init")
        .contentType(MediaType.APPLICATION_JSON)
        .content(new ObjectMapper().writeValueAsString(scansDTO)));

    // Then
    result.andExpect(status().isCreated())
        .andExpect(jsonPath("$.targets").isArray())
        .andExpect(jsonPath("$.targets[0]").value("192.168.1.1"));
  }

  @Test
  void testGetScanResults_Success() throws Exception {
    // Given
    String ip = "192.168.1.1";
    Page<ScanDTO> scanPage = new PageImpl<>(Collections.singletonList(new ScanDTO()));

    when(scanService.getScanResults(eq(ip), any(Pageable.class))).thenReturn(scanPage);

    // When
    ResultActions result = mockMvc.perform(get("/api/scans/{ip}", ip)
        .param("page", "0")
        .param("size", "10"));

    // Then
    result.andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray());
  }

  @Test
  void testGetScanResults_NotFound() throws Exception {
    // Given
    String ip = "192.168.1.1";
    Pageable pageable = PageRequest.of(0, 10);
    Page<ScanDTO> scanPage = Page.empty(pageable);

    when(scanService.getScanResults(eq(ip), any(Pageable.class))).thenReturn(scanPage);

    // When
    ResultActions result = mockMvc.perform(get("/api/scans/{ip}", ip)
        .param("page", "0")
        .param("size", "10"));

    // Then
    result.andExpect(status().isNotFound());
  }

  @Test
  void testGetScanChangesResults_Success() throws Exception {
    // Given
    String ip = "192.168.1.1";
    ScanDTO scanDTO = new ScanDTO();

    when(scanService.getScanChangesResults(eq(ip))).thenReturn(scanDTO);

    // When
    ResultActions result = mockMvc.perform(get("/api/scans/changes/{ip}", ip));

    // Then
    result.andExpect(status().isOk())
        .andExpect(jsonPath("$.ip").doesNotExist());
  }

  @Test
  void testGetScanChangesResults_NotFound() throws Exception {
    // Given
    String ip = "192.168.1.1";

    when(scanService.getScanChangesResults(eq(ip))).thenReturn(null);

    // When
    ResultActions result = mockMvc.perform(get("/api/scans/changes/{ip}", ip));

    // Then
    result.andExpect(status().isNotFound());
  }
}