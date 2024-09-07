#!/bin/sh

until nmap -p 27017 mongo | grep "open"; do
  echo "Waiting for MongoDB to be available..."
  sleep 2
done

echo "MongoDB is up and running!"