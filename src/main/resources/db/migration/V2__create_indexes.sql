CREATE INDEX idx_transactions_user_id ON transactions (user_id);
CREATE INDEX idx_transactions_type ON transactions (type);
CREATE INDEX idx_transactions_status ON transactions (status);
CREATE INDEX idx_transactions_created_at ON transactions (created_at);
CREATE INDEX idx_users_username ON users (username);