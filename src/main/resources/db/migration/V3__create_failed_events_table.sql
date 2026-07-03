CREATE TABLE failed_events (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id    UUID,
    original_topic    VARCHAR(255),
    payload           TEXT NOT NULL,
    exception_class   VARCHAR(500),
    exception_message TEXT,
    failed_at         TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_failed_events_transaction_id ON failed_events (transaction_id);