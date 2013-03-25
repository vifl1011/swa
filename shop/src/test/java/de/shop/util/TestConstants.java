package de.shop.util;

public final class TestConstants {
	public static final String WEB_PROJEKT = "shop2";
	
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
		BASEPATH = System.getProperty("basepath", "/shop2/rest");
	}
	
	public static final String KUNDEN_PATH = "/kunden";
	public static final String KUNDEN_URI = BASEURI + ":" + PORT + BASEPATH + KUNDEN_PATH;
	public static final String KUNDEN_ID_PATH_PARAM = "kundeId";
	public static final String KUNDEN_ID_PATH = KUNDEN_PATH + "/{" + KUNDEN_ID_PATH_PARAM + "}";
	public static final String KUNDEN_NACHNAME_QUERY_PARAM = "nachname";
	public static final String KUNDEN_ID_FILE_PATH = KUNDEN_ID_PATH + "/file";
	
	public static final String BESTELLUNGEN_PATH = "/bestellungen";
	public static final String BESTELLUNGEN_ID_PATH_PARAM = "bestellungId";
	public static final String BESTELLUNGEN_ID_PATH = BESTELLUNGEN_PATH + "/{" + BESTELLUNGEN_ID_PATH_PARAM + "}";
	public static final String BESTELLUNGEN_ID_KUNDE_PATH = BESTELLUNGEN_ID_PATH + "/kunde";
	
	public static final String ARTIKEL_PATH = "/artikel";
	public static final String ARTIKEL_URI = BASEURI + ":" + PORT + BASEPATH + ARTIKEL_PATH;
	
	// Testklassen fuer Service- und Domain-Tests
	public static final Class<?>[] TEST_CLASSES = { };
	
	private TestConstants() {
	}
}
