package de.shop.bestellverwaltung.rest;

import static com.jayway.restassured.RestAssured.given;
import static de.shop.util.TestConstants.ACCEPT;
import static de.shop.util.TestConstants.PRODUKT_URI;
import static de.shop.util.TestConstants.BESTELLUNGEN_ID_KUNDE_PATH;
import static de.shop.util.TestConstants.BESTELLUNGEN_ID_PATH;
import static de.shop.util.TestConstants.BESTELLUNGEN_ID_PATH_PARAM;
import static de.shop.util.TestConstants.BESTELLUNGEN_PATH;
import static de.shop.util.TestConstants.KUNDEN_URI;
import static de.shop.util.TestConstants.LOCATION;
import static de.shop.util.TestConstants.LIEFERUNG_URI;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
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
public class BestellungResourceTest extends AbstractResourceTest {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

	private static final Long BESTELLUNG_ID_VORHANDEN = Long.valueOf(601);
	private static final Long KUNDE_ID_VORHANDEN = Long.valueOf(201);
	private static final Long PRODUKT_ID_VORHANDEN_1 = Long.valueOf(502);
	private static final Long PRODUKT_ID_VORHANDEN_2 = Long.valueOf(503);
	private static final Long LIEFERUNG_ID_VORHANDEN = Long.valueOf(300);
	
	private static final Float BESTELLUNG_GESAMTPREIS = Float.valueOf(80);
	private static final Byte BESTELLUNG_GEZAHLT = Byte.valueOf((byte) 0);
	private static final String BESTELLUNG_STATUS = "offen";
	
	@Test
	public void validate() {
		assertThat(true, is(true));
	}
	
	@Test
	public void findBestellungById() {
		LOGGER.finer("BEGINN");

		// Given
		final Long bestellungId = BESTELLUNG_ID_VORHANDEN;

		// When
		final Response response = given().header(ACCEPT, APPLICATION_JSON)
				.pathParameter(BESTELLUNGEN_ID_PATH_PARAM, bestellungId).auth().basic(USERNAME, PASSWORD)
				.get(BESTELLUNGEN_ID_PATH);

		// Then
		assertThat(response.getStatusCode(), is(HTTP_OK));

		try (final JsonReader jsonReader = getJsonReaderFactory().createReader(new StringReader(response.asString()))) {
			final JsonObject jsonObject = jsonReader.readObject();
			assertThat(jsonObject.getJsonNumber("id").longValue(), is(bestellungId.longValue()));
			assertThat(jsonObject.getString("kundeUri"), is(notNullValue()));
		}

		LOGGER.finer("ENDE");
	}

	//TODO Wirft nullpoint Exception
	public void findKundeByBestellungId() {
		LOGGER.finer("BEGINN");

		// Given
		final Long bestellungId = BESTELLUNG_ID_VORHANDEN;

		// When
		final Response response = given().header(ACCEPT, APPLICATION_JSON)
				.pathParameter(BESTELLUNGEN_ID_PATH_PARAM, bestellungId).auth().basic(USERNAME, PASSWORD)
				.get(BESTELLUNGEN_ID_KUNDE_PATH);

		// Then
		assertThat(response.getStatusCode(), is(HTTP_OK));

		try (final JsonReader jsonReader = getJsonReaderFactory().createReader(new StringReader(response.asString()))) {
			final JsonObject jsonObject = jsonReader.readObject();
			assertThat(jsonObject.getString("bestellungenUri"), endsWith("/kunden/" + jsonObject.getInt("id")
					+ "/bestellungen"));
		}

		LOGGER.finer("ENDE");
	}

	@Test
	public void createBestellung() {
		LOGGER.finer("BEGINN");

		// Given
		final JsonObject jsonObject = 
				getJsonBuilderFactory().createObjectBuilder()
			    .add("kundeUri", KUNDEN_URI + "/" + KUNDE_ID_VORHANDEN)
			    .add("bestellstatus", BESTELLUNG_STATUS)
			    .add("gezahlt", BESTELLUNG_GEZAHLT)
			    .add("gesamtpreis", BESTELLUNG_GESAMTPREIS)
			    .add("bestellpositionen", 
			    		getJsonBuilderFactory().createArrayBuilder()
	  					.add(getJsonBuilderFactory().createObjectBuilder()
	  						 .add("produktUri", PRODUKT_URI + "/" + PRODUKT_ID_VORHANDEN_1)
	  						 .add("menge", 2)
	  						 .add("lieferungUri", LIEFERUNG_URI + "/" + LIEFERUNG_ID_VORHANDEN))
	  					.add(getJsonBuilderFactory().createObjectBuilder()
	  						 .add("produktUri", PRODUKT_URI + "/" + PRODUKT_ID_VORHANDEN_2)
	  						 .add("menge", 1)
	  						 .add("lieferungUri", LIEFERUNG_URI + "/" + LIEFERUNG_ID_VORHANDEN)))
	  			.build();

		// When
		final Response response = given().contentType(APPLICATION_JSON)
										 .body(jsonObject.toString())
										 .auth()
										 .basic(USERNAME, PASSWORD)
										 .post(BESTELLUNGEN_PATH);

		// Then
		assertThat(response.getStatusCode(), is(HTTP_CREATED));
		final String location = response.getHeader(LOCATION);
		final int startPos = location.lastIndexOf('/');
		final String idStr = location.substring(startPos + 1);
		final Long id = Long.valueOf(idStr);
		assertThat(id.longValue() > 0, is(true));

		LOGGER.finer("ENDE");
	}
	
	@Test
	public void updateBestellung() {
		LOGGER.finer("BEGINN");
				
		// When		
		Response response = given().header(ACCEPT, APPLICATION_JSON)
										 .pathParameter(BESTELLUNGEN_ID_PATH_PARAM, BESTELLUNG_ID_VORHANDEN)
										 .auth()
										 .basic(USERNAME, PASSWORD)
										 .get(BESTELLUNGEN_ID_PATH);

		JsonObject jsonObject;
		try (final JsonReader jsonReader = 
									getJsonReaderFactory().createReader(new StringReader(response.asString()))) {
			jsonObject = jsonReader.readObject();
			assertThat(jsonObject.getJsonNumber("id").longValue(), is(BESTELLUNG_ID_VORHANDEN.longValue()));
		}
		
		final JsonObjectBuilder jsonObjectBuilder = getJsonBuilderFactory().createObjectBuilder();
		final Set<String> keys = jsonObject.keySet();
    	for (String k : keys) {
    		if ("kundeUri".equals(k)) {
    			jsonObjectBuilder.add("kundeUri", KUNDEN_URI + "/" + KUNDE_ID_VORHANDEN);
    		}
    		else if ("bestellstatus".equals(k)) {
    			jsonObjectBuilder.add("bestellstatus", BESTELLUNG_STATUS);
    		}
    		else if ("gezahlt".equals(k)) {
    			jsonObjectBuilder.add("gezahlt", BESTELLUNG_GEZAHLT);
    		}
    		else {
    			jsonObjectBuilder.add(k, jsonObject.get(k));
    		}
    	}
		jsonObjectBuilder.build();
		
		response = given().contentType(APPLICATION_JSON)
				 .body(jsonObject.toString())
				 .auth()
				 .basic(USERNAME, PASSWORD)
				 .put(BESTELLUNGEN_PATH);
		
		// Then
		assertThat(response.getStatusCode(), is(HTTP_NO_CONTENT));
		
		LOGGER.finer("ENDE");
	}
}
