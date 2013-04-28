package de.shop.bestellverwaltung.rest;

import static com.jayway.restassured.RestAssured.given;
import static de.shop.util.TestConstants.ACCEPT;
import static de.shop.util.TestConstants.ARTIKEL_URI;
import static de.shop.util.TestConstants.BESTELLUNGEN_URI;
import static de.shop.util.TestConstants.LIEFERUNG_URI;
import static de.shop.util.TestConstants.BESTELLPOSITION_PATH;
import static de.shop.util.TestConstants.BESTELLPOSITION_ID_PATH_PARAM;
import static de.shop.util.TestConstants.BESTELLPOSITION_ID_PATH;
import static de.shop.util.TestConstants.BESTELLPOSITION_ID_BESTELLUNG_PATH;
import static de.shop.util.TestConstants.BESTELLPOSITION_ID_LIEFERUNG_PATH;
import static de.shop.util.TestConstants.BESTELLPOSITION_ID_PRODUKT_PATH;
import static de.shop.util.TestConstants.LOCATION;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_OK;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;

import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import java.util.Set;
import java.util.logging.Logger;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.jayway.restassured.response.Response;

import de.shop.util.AbstractResourceTest;


@RunWith(Arquillian.class)
@FixMethodOrder(NAME_ASCENDING)
public class BestellpositionResourceTest extends AbstractResourceTest {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	private static final Long BESTELLPOSITION_ID_VORHANDEN = Long.valueOf(700);
	private static final Long ARTIKEL_ID_VORHANDEN = Long.valueOf(500);
	private static final Long LIEFERUNG_ID_VORHANDEN = Long.valueOf(300);
	private static final Long BESTELLUNG_ID_VORHANDEN = Long.valueOf(600);
	
	@Test
	public void first() {
		LOGGER.finer("Starting test method \"first\"...");
		
		assertThat(true, is(true));
		
		LOGGER.finer("Finished test method \"first\"...");
	}
	
	//GET-TESTS...
	
	@Test
	public void findBestellpositionById() {
		LOGGER.finer("Starte Testmethode \"findBestellpositionById\" mit ID: " 
														+ BESTELLPOSITION_ID_VORHANDEN.toString() + "...");
		
		final int expectedStatusCode = HTTP_OK;
		
		final Response response = given().auth()
										 .basic(USERNAME_ADMIN, PASSWORD_ADMIN)
										 .header(ACCEPT, APPLICATION_JSON)
                					     .pathParameter(BESTELLPOSITION_ID_PATH_PARAM, BESTELLPOSITION_ID_VORHANDEN)
                						 .get(BESTELLPOSITION_ID_PATH);
											
		
		assertThat(response.getStatusCode(), is(expectedStatusCode));
		
		LOGGER.finer("Response Status Code was " + expectedStatusCode + "... OK");
		
		try (final JsonReader jsonReader = 
				getJsonReaderFactory().createReader(new StringReader(response.asString()))) {
			final JsonObject jsonObject = jsonReader.readObject();
			assertThat(jsonObject.getJsonNumber("id").longValue(), is(BESTELLPOSITION_ID_VORHANDEN.longValue()));
			assertThat(jsonObject.getString("produktUri"), is(notNullValue()));
			assertThat(jsonObject.getString("lieferungUri"), is(notNullValue()));
			assertThat(jsonObject.getString("bestellungUri"), is(notNullValue()));
			LOGGER.finer("ID war " + BESTELLPOSITION_ID_VORHANDEN + "... OK");
			LOGGER.finer("produktUri + lieferungURI + bestellungURI sind nicht null... OK");			
		}
		
		LOGGER.finer("Test erfolgreich!");
	}
	
	@Test
	public void findBestellungByBestellposition() {
		LOGGER.finer("Starting test method \"findBestellungByBestellposition\" with ID: " 
																+ BESTELLPOSITION_ID_VORHANDEN.toString() + "...");
		
		final Long bestellungID = Long.valueOf(600);
		
		final Response response = given().auth()
											.basic(USERNAME_ADMIN,  PASSWORD_ADMIN)
											.header(ACCEPT, APPLICATION_JSON)
                							.pathParameter(BESTELLPOSITION_ID_PATH_PARAM, BESTELLPOSITION_ID_VORHANDEN)
                							.get(BESTELLPOSITION_ID_BESTELLUNG_PATH);
		
		assertThat(response.getStatusCode(), is(HTTP_OK));
		
		LOGGER.finer("Response Status Code was 200... OK");
		
		try (final JsonReader jsonReader = 
				getJsonReaderFactory().createReader(new StringReader(response.asString()))) {
			final JsonObject jsonObject = jsonReader.readObject();
			assertThat(jsonObject.getJsonNumber("id").longValue(), is(bestellungID.longValue()));
			LOGGER.finer("ID des geladenen Objekts ist " + bestellungID + "... OK");
		}
		
		LOGGER.finer("Test erfolgreich!");
	}
	
	@Test
	public void findProduktByBestellposition() {
		LOGGER.finer("Starting test method \"findProduktByBestellposition\" with ID: "
																+ BESTELLPOSITION_ID_VORHANDEN.toString() + "...");
		
		final Long produktID = Long.valueOf(500);
		
		final Response response = given().auth()
											.basic(USERNAME_ADMIN, PASSWORD_ADMIN)
											.header(ACCEPT, APPLICATION_JSON)
                							.pathParameter(BESTELLPOSITION_ID_PATH_PARAM, BESTELLPOSITION_ID_VORHANDEN)
                							.get(BESTELLPOSITION_ID_PRODUKT_PATH);
		
		assertThat(response.getStatusCode(), is(HTTP_OK));
		
		LOGGER.finer("Response Status Code was 200... OK");
		
		try (final JsonReader jsonReader = 
				getJsonReaderFactory().createReader(new StringReader(response.asString()))) {
			final JsonObject jsonObject = jsonReader.readObject();
			assertThat(jsonObject.getJsonNumber("id").longValue(), is(produktID.longValue()));
			LOGGER.finer("ID des geladenen Objekts ist " + produktID + "... OK");
		}
		
		LOGGER.finer("Test erfolgreich!");
	}
	
	@Test
	public void findLieferungByBestellposition() {
		LOGGER.finer("Starting test method \"findLieferungByBestellposition\" with ID: " 
																	+ BESTELLPOSITION_ID_VORHANDEN.toString() + "...");
		
		final Long lieferungID = Long.valueOf(300);
		
		final Response response = given().auth()
											.basic(USERNAME_ADMIN,  PASSWORD_ADMIN)
											.header(ACCEPT, APPLICATION_JSON)
                							.pathParameter(BESTELLPOSITION_ID_PATH_PARAM, BESTELLPOSITION_ID_VORHANDEN)
                							.get(BESTELLPOSITION_ID_LIEFERUNG_PATH);
		
		assertThat(response.getStatusCode(), is(HTTP_OK));
		
		LOGGER.finer("Response Status Code was 200... OK");
		
		try (final JsonReader jsonReader = 
				getJsonReaderFactory().createReader(new StringReader(response.asString()))) {
			final JsonObject jsonObject = jsonReader.readObject();
			assertThat(jsonObject.getJsonNumber("id").longValue(), is(lieferungID.longValue()));
			LOGGER.finer("ID des geladenen Objekts ist " + lieferungID + "... OK");
		}
		
		LOGGER.finer("Test erfolgreich!");
	}
	
	//POST - TESTS
	
	@Test
	public void createBestellposition() {
		LOGGER.finer("Starte Testmethode \"createBestellposition\": ...");
			
		// Given
		final Long artikelId = ARTIKEL_ID_VORHANDEN;
		final Long lieferungId = LIEFERUNG_ID_VORHANDEN;
		final Long bestellungId = BESTELLUNG_ID_VORHANDEN;
		
		// Neues, client-seitiges Bestellungsobjekt als JSON-Datensatz
		final JsonObject jsonObject = getJsonBuilderFactory().createObjectBuilder()
				                      .add("produktUri", ARTIKEL_URI + "/" + artikelId)
				                      .add("lieferungUri", LIEFERUNG_URI + "/" + lieferungId)
				                      .add("bestellungUri", BESTELLUNGEN_URI + "/" + bestellungId)
				                      .add("menge", 1)
				                      .build();
		// When
		final Response response = given().contentType(APPLICATION_JSON)
				                         .body(jsonObject.toString())
				                         .auth()
				                         .basic(USERNAME_ADMIN, PASSWORD_ADMIN)
				                         .post(BESTELLPOSITION_PATH);
			
		assertThat(response.getStatusCode(), is(HTTP_CREATED));
		
		final String location = response.getHeader(LOCATION);
		final int startPos = location.lastIndexOf('/');
		final String idStr = location.substring(startPos + 1);
		final Long id = Long.valueOf(idStr);
		assertThat(id.longValue() > 0, is(true));

		LOGGER.finer("Bestellposition wurde erfolgreich angelegt mit ID: " + id);
		LOGGER.finer("Test erfolgreich!");
	}
	
	//PUT - TESTS
	
	@Test
	public void updateBestellposition() {
		LOGGER.finer("Starte Testmethode \"updateBestellposition\": ...");
		
		final int neueMenge = 2000;
		
		//	WHEN
		Response response = given().auth()
				.basic(USERNAME_ADMIN,  PASSWORD_ADMIN)
				.header(ACCEPT, APPLICATION_JSON)
                .pathParameter(BESTELLPOSITION_ID_PATH_PARAM, BESTELLPOSITION_ID_VORHANDEN)
                .get(BESTELLPOSITION_ID_PATH);
		
		JsonObject jsonObject;
		try (final JsonReader jsonReader =
									getJsonReaderFactory().createReader(new StringReader(response.asString()))) {
			jsonObject = jsonReader.readObject();
		}
		
		assertThat(jsonObject.getJsonNumber("id").longValue(), is(BESTELLPOSITION_ID_VORHANDEN));
		
		LOGGER.finer("Das Object welches geupdated werden soll ist vorhanden...");
		
		final JsonObjectBuilder job = getJsonBuilderFactory().createObjectBuilder();
		final Set<String> keys = jsonObject.keySet();
		for (String k : keys) {
			if ("menge".equals(k)) {
				job.add("menge",  neueMenge);
			}
			else if ("produktUri".equals(k)) {
				job.add("produktUri", ARTIKEL_URI + "/" + ARTIKEL_ID_VORHANDEN);
			}
			else if ("lieferungUri".equals(k)) {
				job.add("lieferungUri", LIEFERUNG_URI + "/" + LIEFERUNG_ID_VORHANDEN);
			}
			else if ("bestellungUri".equals(k)) {
				job.add("bestellungUri", BESTELLUNGEN_URI + "/" + BESTELLUNG_ID_VORHANDEN);
			}
			else {
				job.add(k, jsonObject.get(k));
			}
		}
		
		jsonObject = job.build();
		
		response = given().auth()
							.basic(USERNAME_ADMIN,  PASSWORD_ADMIN)
							.contentType(APPLICATION_JSON)
							.body(jsonObject.toString())
							.put(BESTELLPOSITION_PATH);
		
		//	THEN
		assertThat(response.getStatusCode(), is(HTTP_NO_CONTENT));
		
		LOGGER.finer("Test erfolgreich!");
	}

}
