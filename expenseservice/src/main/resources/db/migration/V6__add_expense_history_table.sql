CREATE TABLE expense_history(
                          id            BIGINT         NOT NULL AUTO_INCREMENT,
                          expense_id    BIGINT         NOT NULL,
                          expense_status VARCHAR(20)   NOT NULL,
                          actor         VARCHAR(255)   NOT NULL,
                          acted_at      DATETIME(6)    NOT NULL,
                          PRIMARY KEY (id),
                          CONSTRAINT uk_expense_history_expense FOREIGN KEY (expense_id) REFERENCES expenses (id)
);