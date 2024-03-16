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
    id   INTEGER AUTO_INCREMENT PRIMARY KEY,
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

INSERT INTO klausur_dto(id, klausur_name, praesenz, datum, von, bis)
VALUES (1, 'Rechnerarchitektur', true, '2022-03-14', '09:45', '10:45'),
       (2, 'Propra', true, '2022-03-24', '10:00', '10:45'),
       (3, 'Aldat', true, '2022-03-13', '10:00', '12:45'),
       (4, 'Datenbanken', true, '2022-03-25', '11:00', '11:45'),
       (5, 'Propra2', false, '2022-03-22', '11:00', '12:45');



