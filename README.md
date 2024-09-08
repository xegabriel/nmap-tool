# Overview - nmap-tool
REST API that processes, stores, and serves nmap results
# Quick Start
```shell
# Clone the project
git clone git@github.com:xegabriel/nmap-tool.git
# Go to the project's root directory
cd ./nmap-tool
# Start the application
docker-compose up -d --build
# Now all the containers should be up
docker ps --format "table {{.ID}}\t{{.Names}}\t{{.Status}}"
# Stop the application
docker-compose down
```
# Getting started
The app exposes 3 endpoints as documented in the [swagger documentation](http://localhost:8080/swagger-ui/index.html).
1. `POST /api/scans/init`
   - Accepts a list of hostnames/ips.
   - The entries are validated through DNS resolution and, therefore, should be valid.
   - The maximum number of entries that can be sent at the same time is 10.
   - If the same entry is sent and was processed within the past 60 seconds, it will be skipped due to caching.
2. `GET /api/scans/{ip}`
   - Retrieves a paginated list of scans for a specified IP.
   - The default page is 0 and the default number of records is 10.
   - The entries are sorted in chronologically descending order.
3. `GET /api/scans/changes/{ip}`
   - Compares the two most recent records and returns any newly opened ports.
### Curls examples
``` shell
curl --request POST \
  --url http://localhost:8080/api/scans/init \
  --header 'Content-Type: application/json' \
  --data '{
	"targets": ["github.com"]
}'

curl --request GET \
  --url http://localhost:8080/api/scans/github.com

curl --request GET \
  --url http://localhost:8080/api/scans/changes/github.com
```
# Current Architecture
![NMAP Tool - Current Architecture](https://github.com/xegabriel/nmap-tool/blob/main/docs/nmap-tool-current-architecture.png?raw=true)
## Multihreaded NMAP Scanning Structure Example
``` shell
# nmap-processor
KafkaConsumerService
   ├── Target: github.com (Thread 1)
   │   ├── Interval 1 (Thread 1.1) - nmap scan nmap -p1-6553 -oX - github.com
   │   ├── Interval 2 (Thread 1.2) - nmap scan nmap -p6554-13106 -oX - github.com
   │   ├── Interval 3 (Thread 1.3) - nmap scan nmap -p13107-19659 -oX - github.com
   │   └── ... (up to port 65535)
   │
   └── Target: example.com (Thread 2)
       ├── Interval 1 (Thread 2.1) - nmap scan nmap -p1-6553 -oX - example.com
       ├── Interval 2 (Thread 2.2) - nmap scan nmap -p6554-13106 -oX - example.com
       ├── Interval 3 (Thread 2.3) - nmap scan nmap -p13107-19659 -oX - example.com
       └── ... (up to port 65535)
```
## Mongo Structure
``` shell
Database: nmap_scans
Collection: nmap-scans

Document Example:
+---------------------------------------------------+
| _id: 66dd9aa0646088686470b759                     |
| ip: "example.com"                                 |
| ports:                                            |
|  - port: 443                                      |
|    state: "open"                                  |
|    service: "https"                               |
|  - port: 80                                       |
|    state: "open"                                  |
|    service: "http"                                |
| createdAt: "2024-09-08T12:37:52.851Z"             |
+---------------------------------------------------+

Index: ip_createdAt_index
- Fields: ip, createdAt
```
Through [this script](https://github.com/xegabriel/nmap-tool/blob/main/mongo-init.js), a MongoDB index is automatically created to optimize query performance. Other scripts, such as generating dummy data, can also be added here for additional functionality.
# Proposed Architecture
![NMAP Tool - Proposed Architecture](https://github.com/xegabriel/nmap-tool/blob/main/docs/nmap-tool-proposed-architecture.png?raw=true)

# Observability & Monitoring
### Logs
``` shell
# Identify the containers
docker ps
# Check the container logs
docker logs -f nmap-core
docker logs -f nmap-processor
```
### Prometheus
The nmap-core service exposes a [metrics endpoint](http://localhost:8080/actuator/prometheus) which is scraped by prometheus using this [config](https://github.com/xegabriel/nmap-tool/blob/main/observability/prometheus/prometheus.yml).
The Prometheus UI can be accessed [here](http://localhost:9090/).
### Grafana
Grafana is linked to Prometheus to visualize and monitor the metrics collected. Prometheus serves as the data source for Grafana as [configured here](https://github.com/xegabriel/nmap-tool/blob/main/observability/grafana/datasources/datasources.yml). The Grafana UI is accessible [here](http://localhost:3000/login) (use the default credentials when first running the application `admin:admin`). A [dashboard](https://github.com/xegabriel/nmap-tool/blob/main/observability/grafana/dashboards/11378.json) is provided out-of-the-box.
<img width="1488" alt="image" src="https://github.com/user-attachments/assets/2449ba8b-ebe6-413c-8545-ac0362785572">
<img width="1469" alt="image" src="https://github.com/user-attachments/assets/19c11e5c-3c84-4aac-b84e-b9ff181b8a92">

## Next Steps
- **Deploy on Kubernetes:**
Deploy the application on Kubernetes for scalability and resilience. Define resources, health checks, and autoscaling in the Kubernetes configuration.
- **Set TTL in Database:**
Implement TTL to automatically delete older scan records. Use the `createdAt` field and configure the TTL index to avoid overaccumulation.
- **Implement CI/CD:**
Automate testing and deployment by setting up a CI/CD pipeline (e.g., Jenkins, GitHub Actions). Ensure automated testing and deploy on successful builds.
- **Extract Common Logic:**
Refactor common Nmap functions into a reusable library for modularization and scalability. Publish it for internal use.
- **Connect to OpenAI for Anomaly Detection:**
Use OpenAI to detect anomalies, like multiple open Telekom ports on different hosts. Integrate OpenAI's API to analyze scan results and flag irregularities.
# How to run for development (MacOS)
``` shell
brew install nmap
nmap --version
cd ../nmap-tool
docker ps
# Stop the container you are interested in debugging
docker container stop nmap-core
# Create a Spring Boot Run Configuration (https://www.jetbrains.com/guide/java/tutorials/hello-world/creating-a-run-configuration/)
# Set the bellow env vars and run the project
```
### Environment variables
``` shell
SPRING_DATA_MONGODB_HOST=localhost;
SPRING_DATA_MONGODB_PORT=27017;
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092;
SPRING_REDIS_HOST=localhost;
SPRING_REDIS_PORT=6379
```
### Other prerequisites
``` shell
# Install git
brew install git
# Install docker
brew install docker
# Install Java
brew install openjdk@11
# Install maven
brew install maven
```
