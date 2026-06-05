-- case3 target (new): only account; legacy_log removed
CREATE TABLE account (
    id INT PRIMARY KEY,
    balance INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_account_balance ON account (balance);
