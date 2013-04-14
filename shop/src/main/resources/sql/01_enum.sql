-- ===============================================================================
-- Jede SQL-Anweisung muss in genau 1 Zeile
-- Kommentare durch -- am Zeilenanfang
-- ===============================================================================

-- ===============================================================================
-- Tabellen fuer Enum-Werte *einmalig* anlegen und jeweils Werte einfuegen
-- Beim ALLERERSTEN Aufruf die Zeilen mit "DROP TABLE ..." durch -- auskommentieren
-- ===============================================================================

DROP TABLE mimetype;
CREATE TABLE mimetype(id NUMBER(1) NOT NULL PRIMARY KEY, txt VARCHAR2(32) NOT NULL UNIQUE) CACHE;
INSERT INTO mimetype VALUES (0, 'image/gif');
INSERT INTO mimetype VALUES (1, 'image/jpeg');
INSERT INTO mimetype VALUES (2, 'image/pjpeg');
INSERT INTO mimetype VALUES (3, 'image/png');
INSERT INTO mimetype VALUES (4, 'video/mp4');
INSERT INTO mimetype VALUES (5, 'audio/wav');

DROP TABLE multimedia_type;
CREATE TABLE multimedia_type(id NUMBER(1) NOT NULL PRIMARY KEY, txt VARCHAR2(8) NOT NULL UNIQUE) CACHE;
INSERT INTO multimedia_type VALUES (0, 'IMAGE');
INSERT INTO multimedia_type VALUES (1, 'VIDEO');
INSERT INTO multimedia_type VALUES (2, 'AUDIO');

DROP TABLE rolle;
CREATE TABLE rolle(id NUMBER(1) NOT NULL PRIMARY KEY, name VARCHAR2(32) NOT NULL) CACHE;
INSERT INTO rolle VALUES (0, 'admin');
INSERT INTO rolle VALUES (1, 'mitarbeiter');
INSERT INTO rolle VALUES (2, 'abteilungsleiter');
INSERT INTO rolle VALUES (3, 'kunde');


-- ===============================================================================
-- Fremdschluessel in den bereits *generierten* Tabellen auf die obigen "Enum-Tabellen" anlegen
-- ===============================================================================
--ALTER TABLE file_tbl ADD CONSTRAINT multimedia__type_fk FOREIGN KEY (multimedia_type_fk) REFERENCES multimedia_type(id);
ALTER TABLE kunde_rolle ADD CONSTRAINT kunde_rolle__rolle_fk FOREIGN KEY (rolle_fk) REFERENCES rolle;
--ALTER TABLE lieferung ADD CONSTRAINT lieferung__transport_art_fk FOREIGN KEY (transport_art_fk) REFERENCES transport_art;
