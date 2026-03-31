CREATE TABLE currencies (
    code VARCHAR(16) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    symbol VARCHAR(32) NOT NULL,
    decimal_places INTEGER NOT NULL,
    symbol_position VARCHAR(32) NOT NULL
);
