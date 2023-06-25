CREATE EXTENSION IF NOT EXISTS "uuid-ossp"; -- UUID functions/generation support

CREATE TABLE media_professional
(
    id         UUID PRIMARY KEY,
    imdb_id    VARCHAR(30) NOT NULL,
    full_name  VARCHAR(70) NOT NULL,
    occupation VARCHAR(50) NOT NULL
);

CREATE INDEX professionals_idx_full_name ON media_professional (full_name);

CREATE TABLE genres
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR
);

CREATE UNIQUE INDEX genres_idx_name ON genres (name);

-- MediaContent

CREATE TABLE media_contents
(
    id           UUID PRIMARY KEY,
    imdb_id      VARCHAR(30)  NOT NULL,
    title        VARCHAR(100) NOT NULL,
    description  VARCHAR(512) NOT NULL,
    release_date DATE         NOT NULL,
    type         VARCHAR(30)  NOT NULL
);

CREATE TABLE media_genres
(
    media_id UUID   NOT NULL,
    genre_id INTEGER NOT NULL,

    FOREIGN KEY (media_id) REFERENCES media_contents (id),
    FOREIGN KEY (genre_id) REFERENCES genres (id)
);

CREATE TABLE media_cast
(
    media_id        UUID NOT NULL,
    actor_id UUID NOT NULL,

    FOREIGN KEY (media_id) REFERENCES media_contents (id),
    FOREIGN KEY (actor_id) REFERENCES media_professional (id)
);

-- Two tables that inherit media_contents are movies and series

CREATE TABLE movies
(
    media_id             UUID NOT NULL,
    thumbnails_generated BOOLEAN DEFAULT FALSE,
    media_transcoded     BOOLEAN DEFAULT FALSE,
    duration_seconds     INTEGER,
    available_from       TIMESTAMP,

    FOREIGN KEY (media_id) REFERENCES media_contents (id)
);

-- Series inherits from media_contents
CREATE TABLE series
(
    media_id             UUID NOT NULL,
    thumbnails_generated BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (media_id) REFERENCES media_contents (id)
);

-- Series has episode collections
CREATE TABLE episode_collections
(
    id                   UUID PRIMARY KEY,
    title                VARCHAR(100) NOT NULL,
    description          VARCHAR(512) NOT NULL,
    thumbnails_generated BOOLEAN DEFAULT FALSE,
    series_id            UUID         NOT NULL,

    FOREIGN KEY (series_id) REFERENCES media_contents (id)
);

-- Each episode collection has at least 1 episode
CREATE TABLE episodes
(
    id                    UUID PRIMARY KEY,
    title                 VARCHAR(100) NOT NULL,
    description           VARCHAR(512) NOT NULL,
    release_date          DATE         NOT NULL,
    available_from        TIMESTAMP,
    thumbnails_generated  BOOLEAN DEFAULT FALSE,
    media_transcoded      BOOLEAN DEFAULT FALSE,
    episode_collection_id UUID         NOT NULL,

    FOREIGN KEY (episode_collection_id) REFERENCES episode_collections (id)
);


-- Inserting initial categories
INSERT INTO genres (name)
VALUES ('Action'),
       ('Adventure'),
       ('Animation'),
       ('Comedy'),
       ('Crime'),
       ('Documentary'),
       ('Drama'),
       ('Family'),
       ('Fantasy'),
       ('History'),
       ('Horror'),
       ('Music'),
       ('Mystery'),
       ('Romance'),
       ('Science Fiction'),
       ('TV Movie'),
       ('Thriller'),
       ('War'),
       ('Western');
