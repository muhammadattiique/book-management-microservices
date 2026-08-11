-- Create separate databases for each microservice
CREATE DATABASE IF NOT EXISTS auth_db;
CREATE DATABASE IF NOT EXISTS book_db;
CREATE DATABASE IF NOT EXISTS loan_db;
CREATE DATABASE IF NOT EXISTS inventory_db;

-- Grant privileges to your custom user
GRANT ALL PRIVILEGES ON auth_db.* TO 'my_db'@'%';
GRANT ALL PRIVILEGES ON book_db.* TO 'my_db'@'%';
GRANT ALL PRIVILEGES ON loan_db.* TO 'my_db'@'%';
GRANT ALL PRIVILEGES ON inventory_db.* TO 'my_db'@'%';

FLUSH PRIVILEGES;