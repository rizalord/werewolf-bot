CREATE TABLE room_table (
    id      serial PRIMARY KEY,
    name    VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
)