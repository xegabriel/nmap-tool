package ro.gabe.nmap_core.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import ro.gabe.nmap_core.model.Scan;

public interface ScanRepository extends MongoRepository<Scan, String> {

  Page<Scan> findByIp(String ip, Pageable pageable);
}
