DO $$
BEGIN
    CREATE TYPE ticket_status AS ENUM ('OPENED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$
BEGIN
    CREATE TYPE ticket_priority AS ENUM('LOW', 'MEDIUM', 'HIGH');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

CREATE TABLE IF NOT EXISTS ticket (
    id                      UUID PRIMARY KEY,
    title                   VARCHAR(255) NOT NULL,
    description             TEXT NOT NULL,
    status                  ticket_status NOT NULL DEFAULT 'OPENED',
    priority                ticket_priority NOT NULL DEFAULT 'LOW',
    customer_id             UUID NOT NULL,
    assigned_agent_id       UUID,
    created_at              TIMESTAMPTZ NOT NULL,
    updated_at              TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS comment (
    id              UUID PRIMARY KEY,
    ticket_id       UUID NOT NULL,
    author_id       UUID NOT NULL,
    body            TEXT NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_comment_ticket_id FOREIGN KEY (ticket_id)
        REFERENCES ticket (id)
);