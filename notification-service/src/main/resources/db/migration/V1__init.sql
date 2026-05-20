CREATE SCHEMA IF NOT EXISTS notification;

DO $$
BEGIN
    CREATE TYPE notification.notification_type AS ENUM
    ('TICKET_CREATED', 'TICKET_ASSIGNED', 'TICKET_STATUS_CHANGED',
    'TICKET_RESOLVED', 'TICKET_CLOSED', 'TICKET_REOPENED');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

CREATE TABLE IF NOT EXISTS notification.notification (
    id                  UUID PRIMARY KEY,
    user_id             UUID NOT NULL,
    message             TEXT NOT NULL,
    type                notification_type NOT NULL,
    ticket_id           UUID NOT NULL,
    is_read             BOOLEAN NOT NULL DEFAULT FALSE,
    read_at             TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_notification_user_id
    ON notification.notification (user_id);

CREATE INDEX IF NOT EXISTS idx_notification_unread
    ON notification.notification (user_id, is_read)
    WHERE is_read = FALSE;

CREATE INDEX IF NOT EXISTS idx_notification_created_at
    ON notification.notification (created_at DESC);
