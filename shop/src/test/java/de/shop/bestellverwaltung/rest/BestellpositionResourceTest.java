package de.shop.bestellverwaltung.rest;

import static com.jayway.restassured.RestAssured.given;
import static de.shop.util.TestConstants.ACCEPT;
import static de.shop.util.TestConstants.BESTELLPOSITION_ID_PATH_PARAM;
import static de.shop.util.TestConstants.BESTELLPOSITION_ID_PATH;
import static de.shop.util.TestConstants.BESTELLPOSITION_ID_BESTELLUNG_PATH;
import static de.shop.util.TestConstants.BESTELLPOSITION_ID_LIEFERUNG_PATH;
import static de.shop.util.TestConstants.BESTELLPOSITION_ID_PRODUKT_PATH;
import static java.net.HttpURLConnection.HTTP_OK;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;

import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;

import javax.json.JsonObject;
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
	
	@Test
	public void first() {
		LOGGER.finer("Starting test method \"first\"...");
		
		assertThat(true,is(true));
		
		LOGGER.finer("Finished test method \"first\"...");
	}
	
	@Test
	public void findBestellpositionByID() {
		LOGGER.finer("Starte Testmethode \"findBestellpositionByID\" mit ID: " + BESTELLPOSITION_ID_VORHANDEN.toString() + "...");
		
		final Response response = given().header(ACCEPT, APPLICATION_JSON)
                							.pathParameter(BESTELLPOSITION_ID_PATH_PARAM, BESTELLPOSITION_ID_VORHANDEN)
                							.get(BESTELLPOSITION_ID_PATH);
		
		assertThat(response.getStatusCode(), is(HTTP_OK));
		LOGGER.finer("Response Status Code was 200... OK");
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
		LOGGER.finer("Starting test method \"findBestellungByBestellposition\" with ID: " + BESTELLPOSITION_ID_VORHANDEN.toString() + "...");
		
		final Long bestellungID = Long.valueOf(600);
		
		final Response response = given().header(ACCEPT, APPLICATION_JSON)
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
		LOGGER.finer("Starting test method \"findProduktByBestellposition\" with ID: " + BESTELLPOSITION_ID_VORHANDEN.toString() + "...");
		
		final Long produktID = Long.valueOf(500);
		
		final Response response = given().header(ACCEPT, APPLICATION_JSON)
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
		LOGGER.finer("Starting test method \"findLieferungByBestellposition\" with ID: " + BESTELLPOSITION_ID_VORHANDEN.toString() + "...");
		
		final Long lieferungID = Long.valueOf(300);
		
		final Response response = given().header(ACCEPT, APPLICATION_JSON)
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

}
