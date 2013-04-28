package de.shop.bestellverwaltung.rest;

import static com.jayway.restassured.RestAssured.given;
import static de.shop.util.TestConstants.ACCEPT;
import static de.shop.util.TestConstants.LIEFERUNG_ID_BESTELLPOSITIONEN_PATH;
import static de.shop.util.TestConstants.LIEFERUNG_ID_PATH;
import static de.shop.util.TestConstants.LIEFERUNG_ID_PATH_PARAM;
import static de.shop.util.TestConstants.LIEFERUNG_PATH;
import static de.shop.util.TestConstants.LOCATION;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_OK;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.CoreMatchers.is;
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
public class LieferungResourceTest extends AbstractResourceTest {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	private static final Long LIEFERUNG_ID_VORHANDEN = Long.valueOf(300);
	
	
	
	//GET-TESTS...
	
	@Test
	public void findLieferungById() {
		LOGGER.finer("Starte Testmethode \"findLieferungById\" mit ID: " + LIEFERUNG_ID_VORHANDEN.toString() + "...");
		
		final int expectedStatusCode = HTTP_OK;
		
		final Response response = given().auth()
										 .basic(USERNAME, PASSWORD)
										 .header(ACCEPT, APPLICATION_JSON)
                					     .pathParameter(LIEFERUNG_ID_PATH_PARAM, LIEFERUNG_ID_VORHANDEN)
                						 .get(LIEFERUNG_ID_PATH);
											
		
		assertThat(response.getStatusCode(), is(expectedStatusCode));
		
		LOGGER.finer("Response Status Code was " + expectedStatusCode + "... OK");
		
		try (final JsonReader jsonReader = 
				getJsonReaderFactory().createReader(new StringReader(response.asString()))) {
			final JsonObject jsonObject = jsonReader.readObject();
			assertThat(jsonObject.getJsonNumber("id").longValue(), is(LIEFERUNG_ID_VORHANDEN.longValue()));
			LOGGER.finer("ID war " + LIEFERUNG_ID_VORHANDEN + "... OK");			
		}
		
		LOGGER.finer("Test erfolgreich!");
	}
	
	@Test
	public void findBestellpositionenByLieferung() {
		LOGGER.finer("Starting test method \"findBestellpositionenByLieferung\" with ID: " 
																	+ LIEFERUNG_ID_VORHANDEN.toString() + "...");
		
		final Response response = given().auth()
											.basic(USERNAME,  PASSWORD)
											.header(ACCEPT, APPLICATION_JSON)
                							.pathParameter(LIEFERUNG_ID_PATH_PARAM, LIEFERUNG_ID_VORHANDEN)
                							.get(LIEFERUNG_ID_BESTELLPOSITIONEN_PATH);
		
		assertThat(response.getStatusCode(), is(HTTP_OK));
		
		LOGGER.finer("Response Status Code was 200... OK");
		
//		try (final JsonReader jsonReader = 
//				getJsonReaderFactory().createReader(new StringReader(response.asString()))) {
//			final JsonObject jsonObject = jsonReader.readObject();
//			assertThat(jsonObject.getJsonNumber("id").longValue(), is(LIEFERUNG_ID_VORHANDEN.longValue()));
//			LOGGER.finer("ID des geladenen Objekts ist " + LIEFERUNG_ID_VORHANDEN + "... OK");
//		}
		
		LOGGER.finer("Test erfolgreich!");
	}
	
	
	//POST - TESTS
	
	@Test
	public void createLieferung() {
		LOGGER.finer("Starte Testmethode \"createLieferung\": ...");
			
		// Neues, client-seitiges Bestellungsobjekt als JSON-Datensatz
		final JsonObject jsonObject = getJsonBuilderFactory().createObjectBuilder()
				                      .add("art", "test")
				                      .add("versanddatumStr", "2006-08-05")
				                      .build();
		// When
		final Response response = given().contentType(APPLICATION_JSON)
				                         .body(jsonObject.toString())
				                         .auth()
				                         .basic(USERNAME_ADMIN, PASSWORD)
				                         .post(LIEFERUNG_PATH);
			
		assertThat(response.getStatusCode(), is(HTTP_CREATED));
		
		final String location = response.getHeader(LOCATION);
		final int startPos = location.lastIndexOf('/');
		final String idStr = location.substring(startPos + 1);
		final Long id = Long.valueOf(idStr);
		assertThat(id.longValue() > 0, is(true));

		LOGGER.finer("Lieferung wurde erfolgreich angelegt mit ID: " + id);
		LOGGER.finer("URI ist: " + location);
		LOGGER.finer("Test erfolgreich!");
	}
	
	//PUT - TESTS
	
	@Test
	public void updateLieferung() {
		LOGGER.finer("Starte Testmethode \"updateLieferung\": ...");
		
		final String neueArt = "ZügigUndFix-Versand";
		
		//	WHEN
		Response response = given().auth()
				.basic(USERNAME_ADMIN,  PASSWORD)
				.header(ACCEPT, APPLICATION_JSON)
                .pathParameter(LIEFERUNG_ID_PATH_PARAM, LIEFERUNG_ID_VORHANDEN)
                .get(LIEFERUNG_ID_PATH);
		
		JsonObject jsonObject;
		try (final JsonReader jsonReader =
									getJsonReaderFactory().createReader(new StringReader(response.asString()))) {
			jsonObject = jsonReader.readObject();
		}
		
		assertThat(jsonObject.getJsonNumber("id").longValue(), is(LIEFERUNG_ID_VORHANDEN));
		
		LOGGER.finer("Das Object welches geupdated werden soll ist vorhanden...");
		
		final JsonObjectBuilder job = getJsonBuilderFactory().createObjectBuilder();
		final Set<String> keys = jsonObject.keySet();
		for (String k : keys) {
			if ("art".equals(k)) {
				job.add("art",  neueArt);
			}
			else {
				job.add(k, jsonObject.get(k));
			}
		}
		
		jsonObject = job.build();
		
		response = given().auth()
							.basic(USERNAME_ADMIN,  PASSWORD)
							.contentType(APPLICATION_JSON)
							.body(jsonObject.toString())
							.put(LIEFERUNG_PATH);
		
		//	THEN
		assertThat(response.getStatusCode(), is(HTTP_NO_CONTENT));
		
		LOGGER.finer("Test erfolgreich!");
	}

}
