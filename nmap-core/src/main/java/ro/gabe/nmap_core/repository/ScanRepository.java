package ro.gabe.nmap_core.repository;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import ro.gabe.nmap_core.model.Scan;

public interface ScanRepository extends MongoRepository<Scan, String> {

  List<Scan> findByIp(String ip);
}
