package de.shop.kundenverwaltung.rest;

import static com.jayway.restassured.RestAssured.given;
import static de.shop.util.TestConstants.ACCEPT;
import static de.shop.util.TestConstants.KUNDEN_ID_PATH_PARAM;
import static de.shop.util.TestConstants.KUNDEN_ID_PATH;
import static de.shop.util.TestConstants.KUNDEN_PATH;
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
import java.util.HashSet;
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

import de.shop.auth.service.jboss.AuthService.RolleType;
import de.shop.util.AbstractResourceTest;


@RunWith(Arquillian.class)
@FixMethodOrder(NAME_ASCENDING)
public class KundeResourceTest extends AbstractResourceTest {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	private static final Long KUNDE_ID_VORHANDEN = Long.valueOf(209);
	private static final String NEUER_NACHNAME = "Nachnameneu";
	private static final String NEUER_VORNAME = "Vorname";
	private static final String NEUE_EMAIL = NEUER_NACHNAME + "@test.de";
	private static final float NEUER_RABATT = (float) 0.9;
	private static final String NEUE_PLZ = "76133";
	private static final String NEUER_ORT = "Karlsruhe";
	private static final String NEUE_STRASSE = "Teststrasse";
	private static final String NEUE_HAUSNR = "1";
	private static final String NEUES_GESCHLECHT = "m";
	private static final String NEUER_LOGIN = "NewLogin";
	private static final String NEUES_PASSWORT = "mypw";
	
	private static final String GEANDERTER_NAME = "Maier";
	
	
	
	//private static final String FILENAME = "video.mp4";

	@Test
	public void validate() {
		assertThat(true, is(true));
	}
	
	@Test
	public void findKundeById() {
		LOGGER.finer("BEGINN");
		
		// Given
		final Long kundeId = KUNDE_ID_VORHANDEN;
		
		// When
		final Response response = given().header(ACCEPT, APPLICATION_JSON)
				                         .pathParameter(KUNDEN_ID_PATH_PARAM, kundeId)
				                         .auth()
                                         .basic(USERNAME, PASSWORD)
                                         .get(KUNDEN_ID_PATH);

		// Then
		assertThat(response.getStatusCode(), is(HTTP_OK));
		
		try (final JsonReader jsonReader =
				              getJsonReaderFactory().createReader(new StringReader(response.asString()))) {
			final JsonObject jsonObject = jsonReader.readObject();
			assertThat(jsonObject.getJsonNumber("id").longValue(), is(kundeId.longValue()));
		}
		
		LOGGER.finer("ENDE");
	}

	@Test
	public void createKunde() {
		LOGGER.finer("BEGINN");
		
		// Given
		final String name = NEUER_NACHNAME;
		final String vorname = NEUER_VORNAME;
		final String email = NEUE_EMAIL;
		final float rabatt = NEUER_RABATT;
		final String plz = NEUE_PLZ;
		final String ort = NEUER_ORT;
		final String strasse = NEUE_STRASSE;
		final String hausnummer = NEUE_HAUSNR;
		final String geschlecht = NEUES_GESCHLECHT;
		final String login = NEUER_LOGIN;
		final String passwort = NEUES_PASSWORT;
		final Set<RolleType> rolle = new HashSet<>();
		rolle.add(RolleType.KUNDE);
		//Daten des anlegenden Users
		final String username = USERNAME_ADMIN;
		final String password = PASSWORD_ADMIN;
		
		
		final JsonObject jsonObject = getJsonBuilderFactory().createObjectBuilder()	
		             		          .add("name", name)
		             		          .add("vorname", vorname)
		             		          .add("email", email)
		             		          .add("rabatt", rabatt)
		             		          .add("geschlecht", geschlecht)
		             		          .add("login", login)
		             		          .add("passwort", passwort)
		             		          .add("passwortWdh", passwort)
		             		          .add("agbAkzeptiert",true)
		             		          .add("adresse", getJsonBuilderFactory().createObjectBuilder()
		                    		                  .add("plz", plz)
		                    		                  .add("ort", ort)
		                    		                  .add("strasse", strasse)
		                    		                  .add("hausnummer", hausnummer)
		                    		                  .build())
		                    		  .add("rollen", getJsonBuilderFactory().createArrayBuilder()
		                    				  .add("KUNDE"))
		                              .build();

		// When
		final Response response = given().contentType(APPLICATION_JSON)
				                         .body(jsonObject.toString())
                                         .auth()
                                         .basic(username, password)
                                         .post(KUNDEN_PATH);
		
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
	public void updateKunde() {
		LOGGER.finer("BEGIN");
		// Given
		final Long kundeId = KUNDE_ID_VORHANDEN;
		final String name = GEANDERTER_NAME;
		//Daten des anlegenden Users
		final String username = USERNAME_ADMIN;
		final String password = PASSWORD_ADMIN;
		
		
		// When
		Response response = given().header(ACCEPT, APPLICATION_JSON)
				                         .pathParameter(KUNDEN_ID_PATH_PARAM, kundeId)
				                         .auth()
                                         .basic(USERNAME, PASSWORD)
                                         .get(KUNDEN_ID_PATH);
		JsonObject jOb;
		try (final JsonReader jsonReader = getJsonReaderFactory().createReader(new StringReader(response.asString()))) {
			jOb = jsonReader.readObject();				
				assertThat(jOb.getJsonNumber("id").longValue(), is(kundeId.longValue()));
		}
    	final JsonObjectBuilder job = getJsonBuilderFactory().createObjectBuilder();
    	final Set<String> keys = jOb.keySet();
    	for (String k : keys) {
    		if ("name".equals(k)) {
    			job.add("name", name);
    		}
    		else {
    			job.add(k, jOb.get(k));
    		}
    	}
    	jOb = job.build();
    	
		response = given().contentType(APPLICATION_JSON)
				          .body(jOb.toString())
                          .auth()
                          .basic(username, password)
                          .put(KUNDEN_PATH);
		
		// Then
		assertThat(response.getStatusCode(), is(HTTP_NO_CONTENT));
		LOGGER.finer("ENDE");
	}
	
	
}
