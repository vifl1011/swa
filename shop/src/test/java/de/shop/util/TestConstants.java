package de.shop.util;

import de.shop.artikelverwaltung.service.ProduktServiceTest;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.bestellverwaltung.service.BestellService;
import de.shop.bestellverwaltung.service.BestellServiceTest;
import de.shop.kundenverwaltung.service.KundeServiceTest;

public final class TestConstants {
	public static final String WEB_PROJEKT = "shop";
	
	// HTTP-Header
	public static final String ACCEPT = "Accept";
	public static final String LOCATION = "Location";
	
	// URLs und Pfade
	public static final String BASEURI;
	public static final int PORT;
	public static final String BASEPATH;
	
	static {
		BASEURI = System.getProperty("baseuri", "http://localhost");
		PORT = Integer.parseInt(System.getProperty("port", "8080"));
		BASEPATH = System.getProperty("basepath", "/shop/rest");
	}
	
	public static final String KUNDEN_PATH = "/kunden";
	public static final String KUNDEN_URI = BASEURI + ":" + PORT + BASEPATH + KUNDEN_PATH;
	public static final String KUNDEN_ID_PATH_PARAM = "kundeId";
	public static final String KUNDEN_ID_PATH = KUNDEN_PATH + "/{" + KUNDEN_ID_PATH_PARAM + "}";
	public static final String KUNDEN_NACHNAME_QUERY_PARAM = "nachname";
	public static final String KUNDEN_ID_FILE_PATH = KUNDEN_ID_PATH + "/file";
	
	public static final String BESTELLUNGEN_PATH = "/bestellung";
	public static final String BESTELLUNGEN_ID_PATH_PARAM = "bestellungId";
	public static final String BESTELLUNGEN_ID_PATH = BESTELLUNGEN_PATH + "/{" + BESTELLUNGEN_ID_PATH_PARAM + "}";
	public static final String BESTELLUNGEN_ID_KUNDE_PATH = BESTELLUNGEN_ID_PATH + "/kunde";
	public static final String BESTELLUNGEN_URI = BASEURI + ":" + PORT + BASEPATH + BESTELLUNGEN_PATH;
	
	public static final String BESTELLPOSITION_PATH = "/bestellposition";
	public static final String BESTELLPOSITION_ID_PATH_PARAM = "bestellpositionId";
	public static final String BESTELLPOSITION_ID_PATH = BESTELLPOSITION_PATH + "/{" + BESTELLPOSITION_ID_PATH_PARAM + "}";
	public static final String BESTELLPOSITION_ID_BESTELLUNG_PATH = BESTELLPOSITION_ID_PATH + "/bestellung";
	public static final String BESTELLPOSITION_ID_LIEFERUNG_PATH = BESTELLPOSITION_ID_PATH + "/lieferung";
	public static final String BESTELLPOSITION_ID_PRODUKT_PATH = BESTELLPOSITION_ID_PATH + "/produkt";
	public static final String BESTELLPOSITION_URI = BASEURI + ":" + PORT + BASEPATH + BESTELLPOSITION_PATH;
	
	public static final String LIEFERUNG_PATH = "/lieferung";
	public static final String LIEFERUNG_ID_PATH_PARAM = "lieferungId";
	public static final String LIEFERUNG_ID_PARAM = LIEFERUNG_PATH + "/{" + LIEFERUNG_ID_PATH_PARAM + "}";
	public static final String LIEFERUNG_URI = BASEURI + ":" + PORT + BASEPATH + LIEFERUNG_PATH;
	
	public static final String ARTIKEL_PATH = "/artikel";
	public static final String ARTIKEL_URI = BASEURI + ":" + PORT + BASEPATH + ARTIKEL_PATH;
	// neu
	public static final String PRODUKT_PATH = "/produkt";
	public static final String PRODUKT_URI = BASEURI + ":" + PORT + BASEPATH + PRODUKT_PATH;
	public static final String PRODUKT_ID_PATH_PARAM = "produktId";
	public static final String PRODUKT_ID_PATH = PRODUKT_PATH + "/{" + PRODUKT_ID_PATH_PARAM + "}";
	
	
	// Testklassen fuer Service- und Domain-Tests
	public static final Class<?>[] TEST_CLASSES = { };
	
	private TestConstants() {
	}
}
