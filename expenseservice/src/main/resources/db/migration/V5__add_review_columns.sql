ALTER TABLE expenses
    ADD COLUMN reviewed_by VARCHAR(255),
    ADD COLUMN reviewed_at DATETIME(6)