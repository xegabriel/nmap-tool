package ro.gabe.nmap_core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class NmapCoreApplication {

  public static void main(String[] args) {
    SpringApplication.run(NmapCoreApplication.class, args);
  }

}
