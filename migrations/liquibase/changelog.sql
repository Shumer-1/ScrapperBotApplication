--liquibase formatted sql

-- changeset admin:1
CREATE TABLE users (
    id BIGSERIAL NOT NULL,
    tg_id BIGINT NOT NULL,
    username VARCHAR(200) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (tg_id),
    UNIQUE (username)
);

-- changeset admin:2
CREATE TABLE tracking_link (
    id BIGSERIAL NOT NULL,
    user_id BIGINT NOT NULL,
    link VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

-- changeset admin:2a
ALTER TABLE tracking_link
    ADD CONSTRAINT fk_tracking_link_users FOREIGN KEY (user_id) REFERENCES users(id);

-- changeset admin:3
CREATE TABLE tags (
    id BIGSERIAL NOT NULL,
    tag VARCHAR(50) NOT NULL,
    PRIMARY KEY (id)
);

-- changeset admin:4
CREATE TABLE filter (
    id BIGSERIAL NOT NULL,
    filter VARCHAR(50) NOT NULL,
    PRIMARY KEY (id)
);

-- changeset admin:5
CREATE TABLE link_and_tags (
    id BIGSERIAL NOT NULL,
    tag_id BIGINT NOT NULL,
    link_id BIGINT NOT NULL,
    PRIMARY KEY (id)
);

-- changeset admin:5a
ALTER TABLE link_and_tags
    ADD CONSTRAINT fk_link_and_tags_tags FOREIGN KEY (tag_id) REFERENCES tags(id);

-- changeset admin:5b
ALTER TABLE link_and_tags
    ADD CONSTRAINT fk_link_and_tags_tracking_link FOREIGN KEY (link_id) REFERENCES tracking_link(id);

-- changeset admin:6
CREATE TABLE link_and_filters (
    id BIGSERIAL NOT NULL,
    filter_id BIGINT NOT NULL,
    link_id BIGINT NOT NULL,
    PRIMARY KEY (id)
);

-- changeset admin:6a
ALTER TABLE link_and_filters
    ADD CONSTRAINT fk_link_and_filters_filter FOREIGN KEY (filter_id) REFERENCES filter(id);

-- changeset admin:6b
ALTER TABLE link_and_filters
    ADD CONSTRAINT fk_link_and_filters_tracking_link FOREIGN KEY (link_id) REFERENCES tracking_link(id);

-- changeset admin:7
ALTER TABLE tracking_link
    ADD COLUMN last_updated TIMESTAMP;

-- changeset admin:8
CREATE INDEX idx_users_tg_id ON users(tg_id);
CREATE INDEX idx_users_username ON users(username);

-- changeset admin:9
CREATE INDEX idx_tracking_link_user_id ON tracking_link(user_id);

-- changeset admin:10
CREATE INDEX idx_tracking_link_link ON tracking_link(link);

-- changeset admin:11
CREATE INDEX idx_tracking_link_last_updated ON tracking_link(last_updated);
