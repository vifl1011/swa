package de.shop.service;

import static de.shop.ui.main.Prefs.username;
import static de.shop.util.Constants.KUNDEN_PATH;
import static java.net.HttpURLConnection.HTTP_CONFLICT;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;

import android.text.TextUtils;
import android.util.Log;
import de.shop.R;
import de.shop.ShopApp;
import de.shop.data.AbstractKunde;
import de.shop.data.Bestellung;
import de.shop.data.Firmenkunde;
import de.shop.data.Privatkunde;

final class Mock {
	private static final String LOG_TAG = Mock.class.getSimpleName();
	
	static HttpResponse<AbstractKunde> sucheKundeById(Long id) {
    	if (id <= 0 || id >= 1000) {
    		return new HttpResponse<AbstractKunde>(HTTP_NOT_FOUND, "Kein Kunde gefunden mit ID " + id);
    	}
    	
    	int dateinameId;
    	if (id % 3 == 0) {
    		dateinameId = R.raw.mock_firmenkunde;
    	}
    	else {
    		dateinameId = R.raw.mock_privatkunde;
    	}
    	
    	JsonReader jsonReader = null;
    	JsonObject jsonObject;
    	try {
    		jsonReader = ShopApp.jsonReaderFactory.createReader(ShopApp.open(dateinameId));
    		jsonObject = jsonReader.readObject();
    	}
    	// FIXME Ab Java 7 gibt es try-with-resources fuer Autoclosable
    	finally {
    		if (jsonReader != null) {
    			jsonReader.close();
    		}
    	}
    	
    	final AbstractKunde kunde = jsonObject.getString("type").equals("P")
    	                            ? new Privatkunde()
    	                            : new Firmenkunde();

    	kunde.fromJsonObject(jsonObject);
    	kunde.id = id;
		
    	final HttpResponse<AbstractKunde> result = new HttpResponse<AbstractKunde>(HTTP_OK, jsonObject.toString(), kunde);
    	return result;
	}

	static HttpResponse<AbstractKunde> sucheKundenByNachname(String nachname) {
		if (nachname.startsWith("X")) {
			return new HttpResponse<AbstractKunde>(HTTP_NOT_FOUND, "Keine Kunde gefunden mit Nachname " + nachname);
		}
		
		JsonReader jsonReader = null;
    	JsonArray jsonArray;
    	try {
    		jsonReader = ShopApp.jsonReaderFactory.createReader(ShopApp.open(R.raw.mock_kunden));
    		jsonArray = jsonReader.readArray();
    	}
    	finally {
    		if (jsonReader != null) {
    			jsonReader.close();
    		}
    	}
		
    	final List<JsonObject> jsonObjectList = jsonArray.getValuesAs(JsonObject.class);
		final ArrayList<AbstractKunde> kunden = new ArrayList<AbstractKunde>();
   		for (JsonObject jsonObject : jsonObjectList) {
           	final AbstractKunde kunde = jsonObject.getString("type").equals("P")
   					                    ? new Privatkunde()
   			                            : new Firmenkunde();
			kunde.fromJsonObject(jsonObject);
			kunde.nachname = nachname;
   			kunden.add(kunde);
   		}
    	
    	final HttpResponse<AbstractKunde> result = new HttpResponse<AbstractKunde>(HTTP_OK, jsonArray.toString(), kunden);
		return result;
    }

	static List<Long> sucheBestellungenIdsByKundeId(Long id) {
		if (id % 2 == 0) {
			return Collections.emptyList();
		}
		
		final int anzahl = (int) ((id % 3) + 3);  // 3 - 5 Bestellungen
		final List<Long> ids = new ArrayList<Long>(anzahl);
		
		// Bestellung IDs sind letzte Dezimalstelle, da 3-5 Bestellungen (s.o.)
		// Kunde-ID wird vorangestellt und deshalb mit 10 multipliziert
		for (int i = 0; i < anzahl; i++) {
			ids.add((long) (id * 10 + 2 * i + 1));
		}
		return ids;
	}

    static List<Long> sucheKundeIdsByPrefix(String kundeIdPrefix) {
		int dateinameId = -1;
    	if ("1".equals(kundeIdPrefix)) {
    		dateinameId = R.raw.mock_ids_1;
    	}
    	else if ("10".equals(kundeIdPrefix)) {
    		dateinameId = R.raw.mock_ids_10;
    	}
    	else if ("11".equals(kundeIdPrefix)) {
    		dateinameId = R.raw.mock_ids_11;
    	}
    	else if ("2".equals(kundeIdPrefix)) {
    		dateinameId = R.raw.mock_ids_2;
    	}
    	else if ("20".equals(kundeIdPrefix)) {
    		dateinameId = R.raw.mock_ids_20;
    	}
    	else {
    		return Collections.emptyList();
    	}
    	
    	JsonReader jsonReader = null;
    	JsonArray jsonArray;
    	try {
    		jsonReader = ShopApp.jsonReaderFactory.createReader(ShopApp.open(dateinameId));
    		jsonArray = jsonReader.readArray();
    	}
    	finally {
    		if (jsonReader != null) {
    			jsonReader.close();
    		}
    	}
    	
    	final List<Long> result = new ArrayList<Long>(jsonArray.size());
    	final List<JsonNumber> jsonNumberList = jsonArray.getValuesAs(JsonNumber.class);
	    for (JsonNumber jsonNumber : jsonNumberList) {
	    	final Long id = Long.valueOf(jsonNumber.longValue());
	    	result.add(id);
    	}
    	
    	Log.d(LOG_TAG, "ids= " + result.toString());
    	
    	return result;
    }

    static List<String> sucheNachnamenByPrefix(String nachnamePrefix) {
    	if (TextUtils.isEmpty(nachnamePrefix)) {
    		return Collections.emptyList();
    	}
    	
		int dateinameNachnamen = -1;
		if (nachnamePrefix.startsWith("A")) {
    		dateinameNachnamen = R.raw.mock_nachnamen_a;
    	}
    	else if (nachnamePrefix.startsWith("D")) {
    		dateinameNachnamen = R.raw.mock_nachnamen_d;
    	}
    	else {
    		return Collections.emptyList();
    	}
    	
    	JsonReader jsonReader = null;
    	JsonArray jsonArray;
    	try {
    		jsonReader = ShopApp.jsonReaderFactory.createReader(ShopApp.open(dateinameNachnamen));
    		jsonArray = jsonReader.readArray();
    	}
    	finally {
    		if (jsonReader != null) {
    			jsonReader.close();
    		}
    	}
    	
    	final List<JsonString> jsonStringList = jsonArray.getValuesAs(JsonString.class);
    	final List<String> result = new ArrayList<String>(jsonArray.size());
	    for (JsonString jsonString : jsonStringList) {
	    	final String nachname = jsonString.getString();
	    	result.add(nachname);
	    }
		
    	Log.d(LOG_TAG, "nachnamen= " + result.toString());
    	return result;
    }
    
    static HttpResponse<AbstractKunde> createKunde(AbstractKunde kunde) {
    	kunde.id = Long.valueOf(kunde.nachname.length());  // Anzahl der Buchstaben des Nachnamens als emulierte neue ID
    	Log.d(LOG_TAG, "createKunde: " + kunde);
    	Log.d(LOG_TAG, "createKunde: " + kunde.toJsonObject());
    	final HttpResponse<AbstractKunde> result = new HttpResponse<AbstractKunde>(HTTP_CREATED, KUNDEN_PATH + "/1", kunde);
    	return result;
    }

    static HttpResponse<AbstractKunde> updateKunde(AbstractKunde kunde) {
    	Log.d(LOG_TAG, "updateKunde: " + kunde);
    	
    	if (TextUtils.isEmpty(username)) {
    		return new HttpResponse<AbstractKunde>(HTTP_UNAUTHORIZED, null);
    	}
    	
    	if ("x".equals(username)) {
    		return new HttpResponse<AbstractKunde>(HTTP_FORBIDDEN, null);
    	}
    	
    	if ("y".equals(username)) {
    		return new HttpResponse<AbstractKunde>(HTTP_CONFLICT, "Die Email-Adresse existiert bereits");
    	}
    	
    	Log.d(LOG_TAG, "updateKunde: " + kunde.toJsonObject());
    	return new HttpResponse<AbstractKunde>(HTTP_NO_CONTENT, null, kunde);
    }

    static HttpResponse<Void> deleteKunde(Long kundeId) {
    	Log.d(LOG_TAG, "deleteKunde: " + kundeId);
    	return new HttpResponse<Void>(HTTP_NO_CONTENT, null);
    }

    static HttpResponse<Bestellung> sucheBestellungById(Long id) {
		final Bestellung bestellung = new Bestellung(id, new Date());
		
		final JsonObject jsonObject = bestellung.toJsonObject();
		final HttpResponse<Bestellung> result = new HttpResponse<Bestellung>(HTTP_OK, jsonObject.toString(), bestellung);
		Log.d(LOG_TAG, result.resultObject.toString());
		return result;
	}
    
    private Mock() {}
}
