package de.shop.artikelverwaltung.rest;

import static com.jayway.restassured.RestAssured.given;
import static de.shop.util.TestConstants.ACCEPT;
import static de.shop.util.TestConstants.ARTIKEL_URI;
import static de.shop.util.TestConstants.PRODUKT_ID_PATH;
import static de.shop.util.TestConstants.PRODUKT_ID_PATH_PARAM;
import static de.shop.util.TestConstants.PRODUKT_PATH;
import static de.shop.util.TestConstants.KUNDEN_URI;
import static de.shop.util.TestConstants.LOCATION;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_OK;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;

import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import java.util.Set;
import java.util.logging.Logger;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.jayway.restassured.response.Response;

import de.shop.util.AbstractResourceTest;
import de.shop.util.Log;
import static java.net.HttpURLConnection.HTTP_CONFLICT;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;


@RunWith(Arquillian.class)
@FixMethodOrder(NAME_ASCENDING)
public class ProduktResourceTest extends AbstractResourceTest {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	

	private static final Long PRODUKT_ID_VORHANDEN = Long.valueOf(502);
	private static final Long PRODUKT_ID_UPDATE = Long.valueOf(503);
	private static final Long PRODUKT_ID_NICHT_VORHANDEN = Long.valueOf(1000);
	private static final String NEUE_BEZEICHNUNG = "Jackewiehose";
	private static final Float PRODUKT_PREIS = Float.valueOf(50);
	private static final String FARBE = "Blau";
	private static final String GROESSE = "42";
	private static final int VORRAT = 5;


	
	@Test
	public void findProduktById() {
		LOGGER.finer("BEGINN");
		
		// Given
		final Long produktId = PRODUKT_ID_VORHANDEN;
		
		// When
		final Response response = given().header(ACCEPT, APPLICATION_JSON)
				                         .pathParameter(PRODUKT_ID_PATH_PARAM, produktId)
				                         .get(PRODUKT_ID_PATH);
		
		// Then
		assertThat(response.getStatusCode(), is(HTTP_OK));
		
		try (final JsonReader jsonReader = 
				getJsonReaderFactory().createReader(new StringReader(response.asString()))) {
			final JsonObject jsonObject = jsonReader.readObject();
			assertThat(jsonObject.getJsonNumber("id").longValue(), is(produktId.longValue()));
		}

		LOGGER.finer("ENDE");
	}
	
	@Test
	public void findProduktByIdNichtVorhanden() {
		LOGGER.finer("BEGINN");
		
		// Given
		final Long produktId = PRODUKT_ID_NICHT_VORHANDEN;
		
		// When
		final Response response = given().header(ACCEPT, APPLICATION_JSON)
				                         .pathParameter(PRODUKT_ID_PATH_PARAM, produktId)
                                         .get(PRODUKT_ID_PATH);

    	// Then
    	assertThat(response.getStatusCode(), is(HTTP_NOT_FOUND));
		LOGGER.finer("ENDE");
	}
	
	@Test
	public void createProdukt() {
		LOGGER.finer("BEGINN");
		
		// Given
		final String bezeichnung = NEUE_BEZEICHNUNG;
		final Float preis = PRODUKT_PREIS;
		final int vorrat = VORRAT;		
		final String farbe = FARBE;
		final String groesse = GROESSE;
		
		
		final String username = USERNAME;
		final String password = PASSWORD;
		
		final JsonObject jsonObject = getJsonBuilderFactory().createObjectBuilder()
//		             		          .add("type", AbstractKunde.PRIVATKUNDE)
		             		          .add("bezeichnung", bezeichnung)
		             		          .add("preis", preis)
		             		          .add("vorrat", vorrat)
		             		          .add("farbe", farbe)
		             		          .add("groesse", groesse)
		                              .build();

		// When
		final Response response = given().contentType(APPLICATION_JSON)
				                         .body(jsonObject.toString())
                                         .auth()
                                         .basic(username, password)
                                         .post(PRODUKT_PATH);
		
		// Then
		assertThat(response.getStatusCode(), is(HTTP_CREATED));
//		final String location = response.getHeader(LOCATION);
//		final int startPos = location.lastIndexOf('/');
//		final String idStr = location.substring(startPos + 1);
//		final Long id = Long.valueOf(idStr);
//		assertThat(id.longValue() > 0, is(true));

		LOGGER.finer("ENDE");
	}
	
//	@Test
//	public void createProduktFalschesPassword() {
//		LOGGER.finer("BEGINN");
//		
//		// Given
//		final String username = USERNAME;
//		final String password = PASSWORD_FALSCH;
//		final String bezeichnung = NEUE_BEZEICHNUNG;
//		
//		final JsonObject jsonObject = getJsonBuilderFactory().createObjectBuilder()
//            		                  .add("bezeichnung", bezeichnung)
//            		                  .build();
//		
//		// When
//		final Response response = given().contentType(APPLICATION_JSON)
//				                         .body(jsonObject.toString())
//                                         .auth()
//                                         .basic(username, password)
//                                         .post(PRODUKT_PATH);
//		
//		// Then
//		assertThat(response.getStatusCode(), is(HTTP_UNAUTHORIZED));
//		
//		LOGGER.finer("ENDE");
//	}
	
	@Test
	@Log
	public void updateProdukt() {
		LOGGER.finer("BEGINN");
		
		// Given
		final Long produktId = PRODUKT_ID_UPDATE;
		final String neueBezeichnung = NEUE_BEZEICHNUNG;
		final String username = USERNAME;
		final String password = PASSWORD;
		
		// When
		Response response = given().header(ACCEPT, APPLICATION_JSON)
				                   .pathParameter(PRODUKT_ID_PATH_PARAM, produktId)
                                   .get(PRODUKT_ID_PATH);
		
		JsonObject jsonObject;
		try (final JsonReader jsonReader =
				              getJsonReaderFactory().createReader(new StringReader(response.asString()))) {
			jsonObject = jsonReader.readObject();
		}
    	assertThat(jsonObject.getJsonNumber("id").longValue(), is(produktId.longValue()));
    	   	
    	// Aus den gelesenen JSON-Werten ein neues JSON-Objekt mit neuer Bezeichnung
    	final JsonObjectBuilder job = getJsonBuilderFactory().createObjectBuilder();
    	final Set<String> keys = jsonObject.keySet();
    	for (String k : keys) {
    		if ("bezeichnung".equals(k)) {
    			job.add("bezeichnung", neueBezeichnung);
    		}
    		else {
    			job.add(k, jsonObject.get(k));
    		}
    	}
    	jsonObject = job.build();
    	
		response = given().contentType(APPLICATION_JSON)
				          .body(jsonObject.toString())
                          .auth()
                          .basic(username, password)
                          .put(PRODUKT_PATH);
		
		// Then
		assertThat(response.getStatusCode(), is(HTTP_NO_CONTENT));
   	}
	
	
	
}