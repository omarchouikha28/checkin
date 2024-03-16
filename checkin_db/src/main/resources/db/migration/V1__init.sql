CREATE TABLE IF NOT EXISTS klausur_dto
(
    id           INTEGER PRIMARY KEY,
    klausur_name text not null,
    praesenz     boolean,
    datum        DATE,
    von          TIME,
    bis          TIME
);

CREATE TABLE IF NOT EXISTS urlaub_dto
(
    id    INTEGER PRIMARY KEY AUTO_INCREMENT,
    datum DATE,
    von   TIME,
    bis   TIME
);

CREATE TABLE IF NOT EXISTS user_dto
(
    github_id varchar(255) primary key,
    feld int

);

CREATE TABLE IF NOT EXISTS klausur_ref_dto
(
    user_dto           varchar(255),
    klausur_dto        INTEGER,
    primary key (user_dto, klausur_dto)
);

CREATE TABLE IF NOT EXISTS urlaub_ref_dto
(
    user_dto   varchar(255),
    urlaub_dto INTEGER,
    primary key (user_dto, urlaub_dto)
);



