-- creates one database per microservice.
-- postgres runs this automatically on first container start.

CREATE DATABASE auth_db;
CREATE DATABASE user_db;
CREATE DATABASE ticket_db;

GRANT ALL PRIVILEGES ON DATABASE auth_db   TO user;
GRANT ALL PRIVILEGES ON DATABASE user_db   TO user;
GRANT ALL PRIVILEGES ON DATABASE ticket_db TO user;

