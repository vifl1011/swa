package de.shop.util;

import static com.jayway.restassured.RestAssured.given;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import javax.json.JsonObject;

import com.jayway.restassured.response.Response;

public class ConcurrentUpdate implements Callable<Response> {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

	private final JsonObject jsonObject;
	private final String url;
	private final String username;
	private final String password;
	
	public ConcurrentUpdate(JsonObject jsonObject, String url, String username, String password) {
		super();
		this.jsonObject = jsonObject;
		this.url = url;
		this.username = username;
		this.password = password;
	}

	@Override
	public Response call() {
		LOGGER.finer("BEGINN");
		
		final Response response = given().contentType(APPLICATION_JSON)
				                         .body(jsonObject.toString())
				                         .auth()
				                         .basic(username, password)
				                         .put(url);

		LOGGER.finer("ENDE");
		return response;
	}	
}
