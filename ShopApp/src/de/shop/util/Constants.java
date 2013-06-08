package de.shop.util;

public final class Constants {
	public static final String KUNDE_KEY = "kunde";
	public static final String KUNDEN_KEY = "kunden";
	
	public static final int WISCHEN_MIN_DISTANCE = 30;        // Min. Laenge des Wischens in Pixel
	public static final int WISCHEN_MAX_OFFSET_PATH = 30;       // Max. Abweichung in der Y-Richtung in Pixel
	public static final int WISCHEN_THRESHOLD_VELOCITY = 30;  // Geschwindigkeit: Pixel pro Sekunde
	
	public static final int TAB_KUNDE_STAMMDATEN = 0;
	public static final int TAB_KUNDE_BESTELLUNGEN = 1;
	
	public static final String KUNDEN_PATH = "/kunden";
	public static final String NACHNAME_PATH = KUNDEN_PATH + "?nachname=";
	public static final String KUNDEN_PREFIX_PATH = KUNDEN_PATH + "/prefix";
	public static final String KUNDEN_ID_PREFIX_PATH = KUNDEN_PREFIX_PATH + "/id";
	public static final String NACHNAME_PREFIX_PATH = KUNDEN_PREFIX_PATH + "/nachname";
	public static final String BESTELLUNGEN_PATH = "/bestellungen";
	
	public static final short MIN_KATEGORIE = 1;
	public static final short MAX_KATEGORIE = 9;
	
	public static final String PROTOCOL_DEFAULT = "http";
	public static final String LOCALHOST_EMULATOR = "10.0.2.2";
	public static final String HOST_DEFAULT = LOCALHOST_EMULATOR;
	public static final String PORT_DEFAULT = "8080";
	public static final String PATH_DEFAULT = "/shop/rest";
	public static final String TIMEOUT_DEFAULT = "3";
	public static final boolean MOCK_DEFAULT = false;
	
	public static final String LOCALHOST = "localhost";
	
	public static final String DATE_FORMAT_JAXB = "yyyy-MM-dd'T'HH:mm:ssZ";
	
	private Constants() {}
}
