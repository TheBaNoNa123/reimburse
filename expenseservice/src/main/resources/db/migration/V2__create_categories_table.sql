CREATE TABLE categories (
                       id           BIGINT       NOT NULL AUTO_INCREMENT,
                       category     VARCHAR(100) NOT NULL,
                       description  VARCHAR(255),
                       PRIMARY KEY (id),
                       CONSTRAINT uk_categories_category UNIQUE (category)
);