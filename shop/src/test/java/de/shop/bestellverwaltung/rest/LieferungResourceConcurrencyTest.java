package de.shop.bestellverwaltung.rest;

import static com.jayway.restassured.RestAssured.given;
import static de.shop.util.TestConstants.ACCEPT;
import static de.shop.util.TestConstants.LIEFERUNG_ID_PATH;
import static de.shop.util.TestConstants.LIEFERUNG_ID_PATH_PARAM;
import static de.shop.util.TestConstants.LIEFERUNG_PATH;
import static java.net.HttpURLConnection.HTTP_CONFLICT;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.CoreMatchers.is;
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
import org.junit.Test;
import org.junit.runner.RunWith;

import com.jayway.restassured.response.Response;

import de.shop.util.AbstractResourceTest;
import de.shop.util.ConcurrentUpdate;

@RunWith(Arquillian.class)
@FixMethodOrder(NAME_ASCENDING)
public class LieferungResourceConcurrencyTest extends AbstractResourceTest {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	private static final Long LIEFERUNG_ID_VORHANDEN = Long.valueOf(300);
	
	@Test
	public void concurrentUpdateLieferung() throws InterruptedException, ExecutionException  {
		LOGGER.finer("Starte Testmethode \"concurrentUpdateBestellposition\": ...");
		
		//	GIVEN
		final String neueArt1 = "FixUndFertig-Versand";
		final String neueArt2 = "DoBischBlaad-Versand";
		
		//	WHEN
		Response response = given().auth()
					.basic(USERNAME_ADMIN,  PASSWORD_ADMIN)
					.header(ACCEPT, APPLICATION_JSON)
	                .pathParameter(LIEFERUNG_ID_PATH_PARAM, LIEFERUNG_ID_VORHANDEN)
	                .get(LIEFERUNG_ID_PATH);
		
		JsonObject jsonObject;
		try (final JsonReader jsonReader =
									getJsonReaderFactory().createReader(new StringReader(response.asString()))) {
			jsonObject = jsonReader.readObject();
		}
		
		//	konkurrierendes Update
		JsonObjectBuilder job = getJsonBuilderFactory().createObjectBuilder();
		Set<String> keys = jsonObject.keySet();
		for (String k : keys) {
			if ("art".equals(k)) {
				job.add("art",  neueArt2);
			}
			else {
				job.add(k, jsonObject.get(k));
			}
		}
			
		final JsonObject jsonObject2 = job.build();
		final ConcurrentUpdate concUpdate = new ConcurrentUpdate(jsonObject2, LIEFERUNG_PATH, USERNAME_ADMIN, PASSWORD);
		
		final ExecutorService executorService = Executors.newSingleThreadExecutor();
		final Future<Response> future = executorService.submit(concUpdate);
		response = future.get();
		assertThat(response.getStatusCode(), is(HTTP_NO_CONTENT));
		
		//	fehlschlagendes eigenes Update
		job = getJsonBuilderFactory().createObjectBuilder();
		keys = jsonObject.keySet();
		for (String k : keys) {
			if ("art".equals(k)) {
				job.add("art",  neueArt1);
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
		assertThat(response.getStatusCode(), is(HTTP_CONFLICT));
	}
}
