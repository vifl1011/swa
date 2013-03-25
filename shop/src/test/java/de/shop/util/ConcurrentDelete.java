package de.shop.util;

import static com.jayway.restassured.RestAssured.given;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import com.jayway.restassured.response.Response;

public class ConcurrentDelete implements Callable<Response> {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

	private final String url;
	private final String username;
	private final String password;
	
	public ConcurrentDelete(String url, String username, String password) {
		super();
		this.url = url;
		this.username = username;
		this.password = password;
	}

	@Override
	public Response call() {
		LOGGER.finer("BEGINN");
		
		final Response response = given().auth()
                                         .basic(username, password)
                                         .delete(url);

		LOGGER.finer("ENDE");
		return response;
	}	
}
