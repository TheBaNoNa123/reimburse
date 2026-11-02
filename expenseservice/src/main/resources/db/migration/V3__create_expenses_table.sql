CREATE TABLE expenses (
                          id            BIGINT         NOT NULL AUTO_INCREMENT,
                          status        VARCHAR(20)    NOT NULL,
                          title         VARCHAR(100)   NOT NULL,
                          description   VARCHAR(255),
                          amount        DECIMAL(10, 2) NOT NULL,
                          expense_date  DATE           NOT NULL,
                          category_id   BIGINT         NOT NULL,
                          user_id       BIGINT         NOT NULL,
                          PRIMARY KEY (id),
                          CONSTRAINT uk_expenses_category FOREIGN KEY (category_id) REFERENCES categories (id),
                          CONSTRAINT uk_expenses_user     FOREIGN KEY (user_id)     REFERENCES users (id)
);