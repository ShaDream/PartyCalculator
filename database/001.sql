CREATE TABLE participants
(
    id      BIGSERIAL PRIMARY KEY,
    chat_id BIGINT       NOT NULL,
    name    VARCHAR(100) NOT NULL,
    UNIQUE (chat_id, name)
);

CREATE TABLE receipts
(
    id      BIGSERIAL PRIMARY KEY,
    chat_id BIGINT NOT NULL,
    full_amount  REAL NOT NULL
);

CREATE TABLE receipt_transactions
(
    id               BIGSERIAL PRIMARY KEY,
    receipt_id       BIGINT,
    from_participant BIGINT,
    to_participant   BIGINT,
    amount           REAL NOT NULL,
    UNIQUE (receipt_id, from_participant, to_participant),
    FOREIGN KEY (receipt_id) REFERENCES receipts (id),
    FOREIGN KEY (from_participant) REFERENCES participants (id),
    FOREIGN KEY (to_participant) REFERENCES participants (id)
);

CREATE TABLE "group"
(
    id      BIGSERIAL PRIMARY KEY,
    chat_id BIGINT       NOT NULL,
    name    VARCHAR(100) NOT NULL,
    UNIQUE (chat_id, name)
);

CREATE TABLE group_member
(
    id             BIGSERIAL PRIMARY KEY,
    group_id       BIGINT,
    participant_id BIGINT,
    UNIQUE (group_id, participant_id),
    FOREIGN KEY (group_id) REFERENCES "group" (id),
    FOREIGN KEY (participant_id) REFERENCES participants (id)
);
