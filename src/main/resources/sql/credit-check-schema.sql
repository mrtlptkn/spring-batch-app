CREATE TABLE IF NOT EXISTS order_credit_checks (
order_id  BIGINT PRIMARY KEY,
customer  VARCHAR(100) NOT NULL,
approved  BOOLEAN NOT NULL
);