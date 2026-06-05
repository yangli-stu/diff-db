-- case3 origin (old): account + a legacy table that should be dropped on upgrade
CREATE TABLE account (
    id INT PRIMARY KEY,
    balance INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_account_balance ON account (balance);

CREATE TABLE legacy_log (
    id INT PRIMARY KEY,
    message VARCHAR(500)
);
