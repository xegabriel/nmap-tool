// Connect to the database
db = db.getSiblingDB('nmap_scans');

// Create index on 'ip' and 'createdAt' in the 'nmap-scans' collection
db["nmap-scans"].createIndex(
  { ip: 1, createdAt: 1 },
  { name: "ip_createdAt_index" }
);