-- ===============================================================================
-- Jede SQL-Anweisung muss in genau 1 Zeile
-- Kommentare durch -- am Zeilenanfang
-- ===============================================================================

-- ===============================================================================
-- Indexe in den *generierten* Tabellen anlegen
-- ===============================================================================
CREATE INDEX adresse__kunde_index ON adresse(kunde_id);
CREATE INDEX kunde_rolle__kunde_index ON kunde_rolle(kunde_fk);
CREATE INDEX bestellung__kunde_index ON bestellung(kunde_id);
CREATE INDEX bestpos__bestellung_index ON bestellposition(bestellung_id);
CREATE INDEX bestpos__produkt_index ON bestellposition(produkt_id);
