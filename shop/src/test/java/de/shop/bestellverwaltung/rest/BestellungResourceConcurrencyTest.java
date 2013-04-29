package de.shop.bestellverwaltung.rest;

import static com.jayway.restassured.RestAssured.given;
import static de.shop.util.TestConstants.ACCEPT;
import static de.shop.util.TestConstants.KUNDEN_PATH;
import static de.shop.util.TestConstants.PRODUKT_URI;
import static de.shop.util.TestConstants.BESTELLUNGEN_ID_KUNDE_PATH;
import static de.shop.util.TestConstants.BESTELLUNGEN_ID_PATH;
import static de.shop.util.TestConstants.BESTELLUNGEN_ID_PATH_PARAM;
import static de.shop.util.TestConstants.BESTELLUNGEN_PATH;
import static de.shop.util.TestConstants.KUNDEN_URI;
import static de.shop.util.TestConstants.LOCATION;
import static de.shop.util.TestConstants.LIEFERUNG_URI;
import static java.net.HttpURLConnection.HTTP_CONFLICT;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

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
import de.shop.util.ConcurrentUpdate;

@RunWith(Arquillian.class)
@FixMethodOrder(NAME_ASCENDING)
public class BestellungResourceConcurrencyTest extends AbstractResourceTest {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

	private static final Long BESTELLUNG_ID_UPDATE = Long.valueOf(601);
	private static final Long KUNDE_ID_VORHANDEN = Long.valueOf(201);
	
	private static final Byte BESTELLUNG_UNGEZAHLT = Byte.valueOf((byte) 0);
	private static final String BESTELLUNG_STATUS = "offen";
	
	private static final Byte BESTELLUNG_GEZAHLT = Byte.valueOf((byte) 1);
	private static final String BESTELLUNG_STATUS_NEU = "bezahlt";
	
	@Test
	public void concurrentUpdateBestellung() throws InterruptedException, ExecutionException {
		LOGGER.finer("BEGINN");
				
		// When		
		Response response = given().header(ACCEPT, APPLICATION_JSON)
										 .pathParameter(BESTELLUNGEN_ID_PATH_PARAM, BESTELLUNG_ID_UPDATE)
										 .auth()
										 .basic(USERNAME, PASSWORD)
										 .get(BESTELLUNGEN_ID_PATH);

		JsonObject jsonObject;
		try (final JsonReader jsonReader = 
									getJsonReaderFactory().createReader(new StringReader(response.asString()))) {
			jsonObject = jsonReader.readObject();
			assertThat(jsonObject.getJsonNumber("id").longValue(), is(BESTELLUNG_ID_UPDATE.longValue()));
		}
		
		// Bestellung wird als "offen" markiert, da Scheck nicht gedeckt war
		JsonObjectBuilder jsonObjectBuilder = getJsonBuilderFactory().createObjectBuilder();
		Set<String> keys = jsonObject.keySet();
    	for (String k : keys) {
    		if ("kundeUri".equals(k)) {
    			jsonObjectBuilder.add("kundeUri", KUNDEN_URI + "/" + KUNDE_ID_VORHANDEN);
    		}
    		else if ("bestellstatus".equals(k)){
    			jsonObjectBuilder.add("bestellstatus", BESTELLUNG_STATUS);
    		}
    		else if ("gezahlt".equals(k)){
    			jsonObjectBuilder.add("gezahlt", BESTELLUNG_UNGEZAHLT);
    		}
    		else {
    			jsonObjectBuilder.add(k, jsonObject.get(k));
    		}
    	}
    	final JsonObject jsonObject2 = jsonObjectBuilder.build();
    	final ConcurrentUpdate concurrentUpdate = new ConcurrentUpdate(jsonObject2, BESTELLUNGEN_PATH,
                																			USERNAME, PASSWORD);
		final ExecutorService executorService = Executors.newSingleThreadExecutor();
		final Future<Response> future = executorService.submit(concurrentUpdate);
		response = future.get();
		assertThat(response.getStatusCode(), is(HTTP_NO_CONTENT));
    	
		// Zahlung ist mittlerweile per Bankeinzug eingegangen und wird versucht zu bestätigen
		jsonObjectBuilder = getJsonBuilderFactory().createObjectBuilder();
		keys = jsonObject.keySet();
    	for (String k : keys) {
    		if ("kundeUri".equals(k)) {
    			jsonObjectBuilder.add("kundeUri", KUNDEN_URI + "/" + KUNDE_ID_VORHANDEN);
    		}
    		else if ("bestellstatus".equals(k)){
    			jsonObjectBuilder.add("bestellstatus", BESTELLUNG_STATUS_NEU);
    		}
    		else if ("gezahlt".equals(k)){
    			jsonObjectBuilder.add("gezahlt", BESTELLUNG_GEZAHLT);
    		}
    		else {
    			jsonObjectBuilder.add(k, jsonObject.get(k));
    		}
    	}
		
		response = given().contentType(APPLICATION_JSON)
				 .body(jsonObject.toString())
				 .auth()
				 .basic(USERNAME, PASSWORD)
				 .put(BESTELLUNGEN_PATH);
		
		// Then
		assertThat(response.getStatusCode(), is(HTTP_CONFLICT));
		
		LOGGER.finer("ENDE");
	}
}
