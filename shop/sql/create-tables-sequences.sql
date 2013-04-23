CREATE SEQUENCE hibernate_sequence START WITH ${sequence.start};

CREATE TABLE kunde (
  id INTEGER NOT NULL,
  name VARCHAR2(45) NOT NULL,
  vorname VARCHAR2(45) NOT NULL,
  geschlecht VARCHAR2(1) NULL,
  login VARCHAR2(20) NOT NULL,
  passwort VARCHAR2(255) NOT NULL,
  email VARCHAR2(255) NOT NULL,
  rabatt FLOAT DEFAULT 1 NOT NULL,
  erzeugt TIMESTAMP NULL,
  aktualisiert TIMESTAMP NULL,
  PRIMARY KEY(id)
) CACHE;

CREATE TABLE adresse (
  id INTEGER NOT NULL,
  kunde_id INTEGER NOT NULL,
  ort VARCHAR2(45) NULL,
  plz VARCHAR2(10) NULL,
  strasse VARCHAR2(30) NULL,
  hausnummer VARCHAR2(30) NULL,
  erzeugt TIMESTAMP NULL,
  aktualisiert TIMESTAMP NULL,
  PRIMARY KEY(id),
  FOREIGN KEY(kunde_id)
    REFERENCES kunde(id)
) CACHE;
CREATE INDEX adresse_kunde_index ON adresse(kunde_id);

CREATE TABLE lieferung (
  id INTEGER NOT NULL,
  art VARCHAR2(30) NULL,
  versanddatum TIMESTAMP NULL,
  erzeugt TIMESTAMP NULL,
  aktualisiert TIMESTAMP NULL,
  PRIMARY KEY(id)
) CACHE;

CREATE TABLE produkt (
  id INTEGER NOT NULL,
  version INTEGER NULL,
  bezeichnung VARCHAR2(30) NULL,
  preis FLOAT NULL,
  groesse VARCHAR2(30) NULL,
  farbe VARCHAR2(30) NULL,
  vorrat NUMBER  DEFAULT 0 NULL,
  erzeugt TIMESTAMP NULL,
  aktualisiert TIMESTAMP NULL,
  PRIMARY KEY(id)
) CACHE;

CREATE TABLE bestellung (
  id INTEGER NOT NULL,
  kunde_id INTEGER NOT NULL,
  bestellzeitpunkt TIMESTAMP NULL,
  bestellstatus VARCHAR2(30) NULL,
  gesamtpreis FLOAT NULL,
  gezahlt NUMBER DEFAULT 0 NULL,
  erzeugt TIMESTAMP NULL,
  aktualisiert TIMESTAMP NULL,
  idx NUMBER DEFAULT 0 NOT NULL,
  PRIMARY KEY(id),
  FOREIGN KEY(kunde_id)
    REFERENCES kunde(id)
) CACHE;
CREATE INDEX bestellung_kunde_index ON bestellung(kunde_id);

CREATE TABLE bestellposition (
  id INTEGER NOT NULL,
  bestellung_id INTEGER NOT NULL,
  lieferung_id INTEGER NOT NULL,
  produkt_id INTEGER NOT NULL,
  menge NUMBER NULL,
  idx NUMBER  DEFAULT 0 NOT NULL,
  einzelpreis NUMBER NULL,
  erzeugt TIMESTAMP NULL,
  aktualisiert TIMESTAMP NULL,
  PRIMARY KEY(id),
  FOREIGN KEY(produkt_id)
    REFERENCES produkt(id),
  FOREIGN KEY(lieferung_id)
    REFERENCES lieferung(id),
  FOREIGN KEY(bestellung_id)
    REFERENCES bestellung(id)
) CACHE;
CREATE INDEX bestellposition_produkt on bestellposition(produkt_id);
CREATE INDEX bestellposition_lieferung on bestellposition(lieferung_id);
CREATE INDEX bestellposition_bestellung on bestellposition(bestellung_id);
