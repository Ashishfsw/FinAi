-- Portfolio Service Database Schema for PostgreSQL
-- This file creates the initial database schema for the portfolio service

-- Enable UUID extension if needed
-- CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    risk_profile VARCHAR(50) NOT NULL DEFAULT 'MODERATE',
    total_investment_value DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    target_allocation DECIMAL(15, 2) NOT NULL DEFAULT 100.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_risk_profile CHECK (risk_profile IN ('CONSERVATIVE', 'MODERATE', 'AGGRESSIVE'))
);

-- Create asset_holdings table
CREATE TABLE IF NOT EXISTS asset_holdings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    symbol VARCHAR(50) NOT NULL,
    asset_name VARCHAR(255) NOT NULL,
    asset_type VARCHAR(50) NOT NULL,
    quantity DECIMAL(19, 4) NOT NULL,
    average_cost_price DECIMAL(19, 4) NOT NULL,
    current_price DECIMAL(19, 4),
    current_value DECIMAL(15, 2),
    unrealized_pnl DECIMAL(15, 2),
    unrealized_pnl_percentage DECIMAL(5, 2),
    purchased_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_asset_type CHECK (asset_type IN ('STOCK', 'BOND', 'ETF', 'MUTUAL_FUND', 'CRYPTO', 'COMMODITY', 'CASH')),
    CONSTRAINT chk_quantity_positive CHECK (quantity > 0),
    CONSTRAINT chk_cost_price_positive CHECK (average_cost_price > 0),
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create rebalancing_recommendations table
CREATE TABLE IF NOT EXISTS rebalancing_recommendations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    symbol VARCHAR(50),
    action VARCHAR(50),
    recommended_quantity DECIMAL(19, 4),
    recommended_amount DECIMAL(15, 2),
    current_allocation DECIMAL(5, 2),
    target_allocation DECIMAL(5, 2),
    allocation_difference DECIMAL(5, 2),
    reasoning TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    executed_at TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    CONSTRAINT chk_status CHECK (status IN ('PENDING', 'APPROVED', 'EXECUTED', 'REJECTED')),
    CONSTRAINT fk_user_recommendation FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_risk_profile ON users(risk_profile);

CREATE INDEX IF NOT EXISTS idx_asset_holdings_user_id ON asset_holdings(user_id);
CREATE INDEX IF NOT EXISTS idx_asset_holdings_symbol ON asset_holdings(symbol);
CREATE INDEX IF NOT EXISTS idx_asset_holdings_asset_type ON asset_holdings(asset_type);
CREATE INDEX IF NOT EXISTS idx_asset_holdings_user_symbol ON asset_holdings(user_id, symbol);

CREATE INDEX IF NOT EXISTS idx_rebalancing_user_id ON rebalancing_recommendations(user_id);
CREATE INDEX IF NOT EXISTS idx_rebalancing_status ON rebalancing_recommendations(status);
CREATE INDEX IF NOT EXISTS idx_rebalancing_created_at ON rebalancing_recommendations(created_at DESC);

-- Create a function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create triggers for automatic updated_at management
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_asset_holdings_updated_at BEFORE UPDATE ON asset_holdings
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Insert sample data for testing (optional)
-- Uncomment the following lines to insert sample data

-- INSERT INTO users (username, email, full_name, risk_profile) VALUES
-- ('john_doe', 'john@example.com', 'John Doe', 'MODERATE'),
-- ('jane_smith', 'jane@example.com', 'Jane Smith', 'AGGRESSIVE');

-- INSERT INTO asset_holdings (user_id, symbol, asset_name, asset_type, quantity, average_cost_price, current_price) VALUES
-- (1, 'AAPL', 'Apple Inc.', 'STOCK', 100, 150.00, 175.50),
-- (1, 'GOOGL', 'Alphabet Inc.', 'STOCK', 50, 120.00, 135.00),
-- (2, 'TSLA', 'Tesla Inc.', 'STOCK', 25, 200.00, 220.00);
