CREATE TABLE currencies (
    code VARCHAR(16) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    symbol VARCHAR(16) NOT NULL,
    decimal_places INTEGER NOT NULL,
    symbol_position VARCHAR(16) NOT NULL
);

INSERT INTO currencies (code, name, symbol, decimal_places, symbol_position) VALUES
    ('EUR', 'Euro', '€', 2, 'before'),
    ('USD', 'US Dollar', '$', 2, 'before');
