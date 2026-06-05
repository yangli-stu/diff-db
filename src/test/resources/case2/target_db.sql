-- case2 target (new): product gains price column, wider name, and an index
CREATE TABLE product (
    id INT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    price DECIMAL(10, 2) DEFAULT 0
);

CREATE INDEX idx_product_name ON product (name);
