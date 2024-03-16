CREATE TABLE IF NOT EXISTS klausur_dto
(
    id   INTEGER PRIMARY KEY,
    klausur_name text not null,
    praesenz boolean,
    datum DATE,
    von  TIME,
    bis  TIME
);

CREATE TABLE IF NOT EXISTS urlaub_dto
(
    id   LONG PRIMARY KEY AUTO_INCREMENT,
    datum DATE,
    von  TIME,
    bis  TIME
);

CREATE TABLE IF NOT EXISTS user_dto
(
    github_id varchar(255) primary key,
    feld int
);

CREATE TABLE IF NOT EXISTS klausur_ref_dto
(
    user_dto varchar(255),
    klausur_dto  INTEGER,
    primary key(user_dto,klausur_dto)
);

CREATE TABLE IF NOT EXISTS urlaub_ref_dto
(
    user_dto varchar(255),
    urlaub_dto  INTEGER,
    primary key(user_dto,urlaub_dto)
);

INSERT INTO urlaub_dto(id, datum, von, bis)
VALUES (1, '2022-03-14', '09:45', '10:45'),
       (2, '2022-03-15', '10:45', '11:45'),
       (3, '2022-03-16', '08:45', '12:45'),
       (4, '2022-03-20', '11:45', '12:45');
