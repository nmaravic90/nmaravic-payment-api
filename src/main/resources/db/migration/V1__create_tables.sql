CREATE TABLE transactions
(
    id            UUID           NOT NULL,
    type          VARCHAR(20)    NOT NULL,
    status        VARCHAR(20)    NOT NULL,
    amount        DECIMAL(19, 4) NOT NULL,
    currency      VARCHAR(3)     NOT NULL,
    user_id       VARCHAR(255)   NOT NULL,
    bill_code     VARCHAR(255),
    sender_id     VARCHAR(255),
    receiver_id   VARCHAR(255),
    license_plate VARCHAR(50),
    zone          VARCHAR(10),
    valid_until   TIMESTAMP,
    created_at    TIMESTAMP      NOT NULL,
    CONSTRAINT pk_transactions PRIMARY KEY (id),
    CONSTRAINT chk_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_type CHECK (type IN ('TRANSFER', 'BILL', 'PARKING', 'QR_CODE')),
    CONSTRAINT chk_status CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED')),
    CONSTRAINT chk_currency_length CHECK (LENGTH(currency) = 3)
);

CREATE TABLE users
(
    id         UUID           NOT NULL,
    username   VARCHAR(255)   NOT NULL,
    email      VARCHAR(255)   NOT NULL,
    balance    DECIMAL(19, 4) NOT NULL DEFAULT 0,
    currency   VARCHAR(3)     NOT NULL,
    created_at TIMESTAMP      NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT chk_balance_positive CHECK (balance >= 0),
    CONSTRAINT chk_currency_length CHECK (LENGTH(currency) = 3)
);

CREATE TABLE idempotency_records
(
    id            UUID         NOT NULL,
    response_body TEXT         NOT NULL,
    created_at    TIMESTAMP    NOT NULL,
    CONSTRAINT pk_idempotency_records PRIMARY KEY (id)
);