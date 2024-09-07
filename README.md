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
# Monitoring & Observability
# Current Architecture
# Proposed Architecture
## Next Steps
# How to run for development (MacOS)
``` shell
brew install nmap
nmap --version
cd ../nmap-tool
docker ps
# Stop the container you are interested in debugging
docker container stop nmap-core
# Create a Srping Boot Run Configuration (https://www.jetbrains.com/guide/java/tutorials/hello-world/creating-a-run-configuration/)
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
# Swagger UI
http://localhost:8080/swagger-ui/index.html#/
