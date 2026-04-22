CREATE TABLE currencies (
    code VARCHAR(16) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    symbol VARCHAR(16) NOT NULL,
    decimal_places INT NOT NULL,
    symbol_position VARCHAR(16) NOT NULL
);
