CREATE SCHEMA IF NOT EXISTS Company_Database_Schema;

CREATE TABLE IF NOT EXISTS Company_Database_Schema.Company_Priority (
    company_id  INT AUTO_INCREMENT PRIMARY KEY,
    company_name VARCHAR(100),
    priority    INT
);

INSERT INTO Company_Database_Schema.Company_Priority (company_name, priority) VALUES ('Capgemini', 1);
INSERT INTO Company_Database_Schema.Company_Priority (company_name, priority) VALUES ('Netflix', 2);
INSERT INTO Company_Database_Schema.Company_Priority (company_name, priority) VALUES ('Sogeti', 3);
