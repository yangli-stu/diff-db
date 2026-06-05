-- case1 target (new / desired): person + orders
CREATE TABLE person (
    id INT PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE orders (
    id INT PRIMARY KEY,
    amount DECIMAL(10, 2) DEFAULT 0
);
