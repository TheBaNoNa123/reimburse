CREATE TABLE users (
                       id           BIGINT       NOT NULL AUTO_INCREMENT,
                       cognito_sub  VARCHAR(36)  NOT NULL,
                       email        VARCHAR(255) NOT NULL,
                       display_name VARCHAR(20)  NOT NULL,
                       PRIMARY KEY (id),
                       CONSTRAINT uk_users_cognito_sub UNIQUE (cognito_sub),
                       CONSTRAINT uk_users_email UNIQUE (email)
);