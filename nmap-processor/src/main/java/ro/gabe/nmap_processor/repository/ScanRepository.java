package ro.gabe.nmap_processor.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ro.gabe.nmap_processor.model.Scan;

public interface ScanRepository extends MongoRepository<Scan, String> {

}
