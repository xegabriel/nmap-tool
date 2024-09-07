# nmap-tool
REST API that processes, stores, and serves nmap results
# Quick Start
```shell
# Start the application
docker-compose up -d --build
# Stop the application
docker-compose down
```
# How to run for development
```shell
brew install nmap
nmap --version
cd ../nmap-tool
docker-compose up -d
# To stop it, run:
docker-compose down
```
# Swagger UI
http://localhost:8080/swagger-ui/index.html#/