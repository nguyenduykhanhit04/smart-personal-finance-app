CREATE DATABASE IF NOT EXISTS personal_finance_app
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE personal_finance_app;

CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    firebase_uid VARCHAR(128) UNIQUE,
    full_name VARCHAR(100),
    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(20),
    avatar_url TEXT,
    password_hash VARCHAR(255) NULL,
    auth_provider VARCHAR(30) DEFAULT 'firebase',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS accounts ( 
    account_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    account_name VARCHAR(100) NOT NULL,
    account_type VARCHAR(50),
    balance DECIMAL(15,2) DEFAULT 0,
    currency VARCHAR(10) DEFAULT 'VND',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_accounts_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NULL,
    category_name VARCHAR(100) NOT NULL,
    category_type VARCHAR(20) NOT NULL,
    icon VARCHAR(100),
    color VARCHAR(20),
    is_default BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_categories_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    account_id INT NOT NULL,
    category_id INT NULL,
    title VARCHAR(150) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    transaction_date DATE NOT NULL,
    note TEXT,
    status VARCHAR(20) DEFAULT 'confirmed',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_transactions_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_transactions_account
        FOREIGN KEY (account_id)
        REFERENCES accounts(account_id),
    CONSTRAINT fk_transactions_category
        FOREIGN KEY (category_id)
        REFERENCES categories(category_id)
);

CREATE TABLE IF NOT EXISTS budgets (
    budget_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    category_id INT NULL,
    budget_name VARCHAR(100) NOT NULL,
    amount_limit DECIMAL(15,2) NOT NULL,
    daily_amount_limit DECIMAL(15,2) NULL,
    spent_amount DECIMAL(15,2) DEFAULT 0,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_budgets_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_budgets_category
        FOREIGN KEY (category_id)
        REFERENCES categories(category_id)
        ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS transaction_images (
    image_id INT AUTO_INCREMENT PRIMARY KEY,
    transaction_id INT NOT NULL,
    image_url TEXT NOT NULL,
    ocr_text TEXT,
    uploaded_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transaction_images_transaction
        FOREIGN KEY (transaction_id)
        REFERENCES transactions(transaction_id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS ai_scan_logs (
    ai_scan_log_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    transaction_id INT NULL,
    raw_ocr_text TEXT,
    detected_merchant VARCHAR(150),
    detected_amount DECIMAL(15,2),
    detected_date DATE,
    suggested_category_id INT NULL,
    actual_category_id INT NULL,
    was_corrected BOOLEAN DEFAULT FALSE,
    confirmed_at DATETIME NULL,
    confidence_score DECIMAL(5,4),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ai_scan_logs_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_ai_scan_logs_transaction
        FOREIGN KEY (transaction_id)
        REFERENCES transactions(transaction_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_ai_scan_logs_suggested_category
        FOREIGN KEY (suggested_category_id)
        REFERENCES categories(category_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_ai_scan_logs_actual_category
        FOREIGN KEY (actual_category_id)
        REFERENCES categories(category_id)
        ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS recurring_transactions (
    recurring_id INT AUTO_INCREMENT PRIMARY KEY,

    user_id INT NOT NULL,
    account_id INT NOT NULL,
    category_id INT NULL,

    title VARCHAR(150) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,

    repeat_type VARCHAR(20) NOT NULL,
    repeat_interval INT DEFAULT 1,

    start_date DATE NOT NULL,
    end_date DATE NULL,
    next_run_date DATE NOT NULL,

    note TEXT,
    is_active BOOLEAN DEFAULT TRUE,

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_recurring_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_recurring_account
        FOREIGN KEY (account_id)
        REFERENCES accounts(account_id),

    CONSTRAINT fk_recurring_category
        FOREIGN KEY (category_id)
        REFERENCES categories(category_id)
        ON DELETE SET NULL
);


CREATE INDEX idx_accounts_user_id ON accounts(user_id);
CREATE INDEX idx_categories_user_id ON categories(user_id);
CREATE INDEX idx_transactions_user_id ON transactions(user_id);
CREATE INDEX idx_transactions_account_id ON transactions(account_id);
CREATE INDEX idx_transactions_transaction_date ON transactions(transaction_date);
CREATE INDEX idx_budgets_user_id ON budgets(user_id);

CREATE TABLE IF NOT EXISTS ai_product_logs (
    ai_product_log_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    transaction_id INT NULL,
    raw_yolo_json TEXT NOT NULL,
    suggested_category_id INT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ai_product_logs_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_ai_product_logs_transaction
        FOREIGN KEY (transaction_id)
        REFERENCES transactions(transaction_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_ai_product_logs_suggested_category
        FOREIGN KEY (suggested_category_id)
        REFERENCES categories(category_id)
);

CREATE INDEX idx_ai_product_logs_transaction_id ON ai_product_logs(transaction_id);
CREATE INDEX idx_ai_product_logs_user_id ON ai_product_logs(user_id);

