const NMAP_SCANS_COLLECTION = "nmap-scans";

// Connect to the database
db = db.getSiblingDB('nmap_scans');

// Create index on 'ip' and 'createdAt' in the 'nmap-scans' collection
db[NMAP_SCANS_COLLECTION].createIndex(
  { ip: 1, createdAt: -1 },
  { name: "ip_createdAt_index" }
);

db[NMAP_SCANS_COLLECTION].insertMany([
  {
    "_id": ObjectId("66ddca51f2b5d27840d91cc5"),
    "ip": "example.com",
    "ports": [
      {
        "port": NumberLong("443"),
        "state": "open",
        "service": "https"
      }
    ],
    "createdAt": ISODate("2023-08-07T16:01:21.146Z"),
    "_class": "ro.gabe.nmap_processor.model.Scan"
  },
  {
    "_id": ObjectId("66ddca51f2b5d27840d91cc6"),
    "ip": "example.com",
    "ports": [
      {
        "port": NumberLong("443"),
        "state": "open",
        "service": "https"
      },
      {
        "port": NumberLong("22"),
        "state": "open",
        "service": "ssh"
      }
    ],
    "createdAt": ISODate("2024-09-08T17:01:21.146Z"),
    "_class": "ro.gabe.nmap_processor.model.Scan"
  }
]);