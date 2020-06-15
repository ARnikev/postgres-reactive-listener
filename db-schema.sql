CREATE TABLE flag
(
    id      TEXT    NOT NULL,
    type    TEXT    NOT NULL,
    enabled BOOLEAN NOT NULL,

    PRIMARY KEY (id)
);

CREATE TABLE limits
(
    id    TEXT    NOT NULL,
    type  TEXT    NOT NULL,
    value integer NOT NULL,

    PRIMARY KEY (id)
);

CREATE OR REPLACE FUNCTION notify_event() RETURNS TRIGGER AS
$$
DECLARE
    payload JSON;
BEGIN
    payload = row_to_json(NEW);
    PERFORM pg_notify('card_event_notification', payload::text);
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;;

CREATE TRIGGER notify_card_event
    AFTER INSERT
    ON card_event_record
    FOR EACH ROW
EXECUTE PROCEDURE notify_event();;

CREATE TABLE card_event_record
(
    id            TEXT    NOT NULL,
    event_type    TEXT    NOT NULL,
    synced        BOOLEAN NOT NULL,
    event_payload TEXT    not null,

    PRIMARY KEY (id)
);
