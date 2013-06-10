package de.shop.service;

import static de.shop.ShopApp.jsonReaderFactory;
import static de.shop.ui.main.Prefs.host;
import static de.shop.ui.main.Prefs.password;
import static de.shop.ui.main.Prefs.path;
import static de.shop.ui.main.Prefs.port;
import static de.shop.ui.main.Prefs.protocol;
import static de.shop.ui.main.Prefs.username;
import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAVAILABLE;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonArray;
import javax.json.JsonReader;
import javax.json.JsonString;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import de.shop.data.JsonMappable;
import de.shop.util.InternalShopError;

final class WebServiceClient {
	private enum AuthType { BASIC, FORM };
	
	private static final String LOG_TAG = WebServiceClient.class.getSimpleName();

	private static final AuthType AUTH_TYPE = AuthType.BASIC;
	
	private static final String APPLICATION_JSON = "application/json";
	
	private static final String ACCEPT = "Accept";
	private static final String ACCEPT_LANGUAGE = "Accept-Language";
	private static final String CONTENT_TYPE = "Content-Type";
	private static final String AUTHORIZATION = "Authorization";
	private static final String LOCATION = "Location";

	private static final String PUT_METHOD = "PUT";
	private static final String DELETE_METHOD = "DELETE";
	
	private static String getBaseUrl() {
		if (TextUtils.isEmpty(port)) {
			return protocol + "://" + host + path;
		}
		return protocol + "://" + host + ":" + port + path;
	}

    private static <T> HttpResponse<T> getJson(String path) {
    	URL url;
    	try {
			url = new URL(getBaseUrl() + path);
		}
    	catch (MalformedURLException e) {
    		Log.e(LOG_TAG, "Interner Fehler beim Erstellen der URL: " + getBaseUrl() + path, e);
    		throw new InternalShopError(e.getMessage(), e);
		}
    	Log.v(LOG_TAG, url.toString());
    	
    	// http://developer.android.com/reference/java/net/HttpURLConnection.html
    	HttpURLConnection httpConnection = null;
    	BufferedReader reader;
		try {
			httpConnection = (HttpURLConnection) url.openConnection();
			httpConnection.setRequestProperty(ACCEPT, APPLICATION_JSON);
			httpConnection.setRequestProperty(ACCEPT_LANGUAGE, Locale.getDefault().getLanguage());
			httpConnection = basicAuth(httpConnection);
			reader =  new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
		}
		catch (IOException e) {
			// Statuscode fuer Fehlerursache ueberpruefen
			int statusCode = 0;
			try {
				statusCode = httpConnection.getResponseCode();
			}
			catch (IOException e2) {
				Log.w(LOG_TAG, e2);
				return new HttpResponse<T>(statusCode, null);
			}
			
			final BufferedReader err = new BufferedReader(new InputStreamReader(httpConnection.getErrorStream()));
			final StringBuilder sb = new StringBuilder();
			try {
				// Fehlermeldung(en) lesen
				for (;;) {
					final String line = err.readLine();
					if (line == null) {
						break;
					}
					sb.append(line);
				}
			}
			catch (IOException e2) {
				Log.w(LOG_TAG, e2);
				String msg = sb.toString();
				if (TextUtils.isEmpty(msg)) {
					msg = e2.getMessage();
				}
				return new HttpResponse<T>(statusCode, msg);
			}
			finally {
				if (err != null) {
					try {
						err.close();
					}
					catch (IOException e2) {
						Log.w(LOG_TAG, e2);
						return new HttpResponse<T>(statusCode, sb.toString());
					}
				}
			}
			return new HttpResponse<T>(statusCode, sb.toString());
		}
		
		int statusCode = HTTP_UNAVAILABLE;
		final StringBuilder sb = new StringBuilder();
		try {
			statusCode = httpConnection.getResponseCode();
			for (;;) {
				final String line = reader.readLine();
				if (line == null) {
					break;
				}
				sb.append(line);
			}
		}
		catch (IOException e) {
			// Fehler beim Lesen aus dem BufferedReader
			final String msg = e.getMessage(); 
			Log.e(LOG_TAG, msg, e);
			return new HttpResponse<T>(statusCode, msg);
		}
		finally {
			try {
				if (reader != null) {
					reader.close();
				}
				if (httpConnection != null) {
					httpConnection.disconnect();
				}
			}
			catch (IOException e) {
				Log.w(LOG_TAG, e);
			}
		}
    	
    	return new HttpResponse<T>(statusCode, sb.toString());
    }
    
    static <T extends JsonMappable> HttpResponse<T> getJsonSingle(String path, Class<T> clazz) {
    	final HttpResponse<T> result = getJson(path);
    	if (result.responseCode != HTTP_OK) {
    		return result;
    	}
    	
    	JsonReader jsonReader = null;
    	JsonObject jsonObject;
    	try {
    		jsonReader = jsonReaderFactory.createReader(new StringReader(result.content));
    		jsonObject = jsonReader.readObject();
    	}
    	finally {
    		if (jsonReader != null) {
    			jsonReader.close();
    		}
    	}
    	Log.v(LOG_TAG, "path = " + path);
		try {
			result.resultObject = clazz.newInstance();
		}
		catch (InstantiationException e) {
			throw new InternalShopError(e.getMessage(), e);
		}
		catch (IllegalAccessException e) {
			throw new InternalShopError(e.getMessage(), e);
		}
		result.resultObject.fromJsonObject(jsonObject);
    	
    	return result;
    }
    
    static <T extends JsonMappable> HttpResponse<T> getJsonSingle(String path,
    		                                                      String diskriminator,
    		                                                      Map<String, Class<? extends T>> classMap) {
    	final HttpResponse<T> result = getJson(path);
    	if (result.responseCode != HTTP_OK) {
    		return result;
    	}
    	
    	JsonReader jsonReader = null;
    	JsonObject jsonObject;
    	try {
    		jsonReader = jsonReaderFactory.createReader(new StringReader(result.content));
    		jsonObject = jsonReader.readObject();
    	}
    	finally {
    		if (jsonReader != null) {
    			jsonReader.close();
    		}
    	}
    	
		final Class<? extends T> clazz = classMap.get(jsonObject.getString(diskriminator));
   		if (clazz == null) {
   			result.responseCode = HTTP_INTERNAL_ERROR;
   			return result;
   		}
   		
		try {
			result.resultObject = clazz.newInstance();
		}
		catch (InstantiationException e) {
			throw new InternalShopError(e.getMessage(), e);
		}
		catch (IllegalAccessException e) {
			throw new InternalShopError(e.getMessage(), e);
		}
		result.resultObject.fromJsonObject(jsonObject);
    	
    	return result;
    }
    
    static <T extends JsonMappable> HttpResponse<T> getJsonList(String path, Class<? extends T> clazz) {
    	final HttpResponse<T> result = getJson(path);
    	if (result.responseCode != HTTP_OK) {
    		return result;
    	}
    	
    	JsonReader jsonReader = null;
    	JsonArray jsonArray;
    	try {
    		jsonReader = jsonReaderFactory.createReader(new StringReader(result.content));
    		jsonArray = jsonReader.readArray();
    	}
    	finally {
    		if (jsonReader != null) {
    			jsonReader.close();
    		}
    	}
    	
    	final ArrayList<T> resultList = new ArrayList<T>(jsonArray.size());
    	final List<JsonObject> jsonObjectList = jsonArray.getValuesAs(JsonObject.class);
    	for (JsonObject jsonObject : jsonObjectList) {			
    		T object;
        	try {
    			object = clazz.newInstance();
    		}
    		catch (InstantiationException e) {
    			throw new InternalShopError(e.getMessage(), e);
    		}
    		catch (IllegalAccessException e) {
    			throw new InternalShopError(e.getMessage(), e);
    		}
        	object.fromJsonObject(jsonObject);
        	
        	resultList.add(object);
    	}
    	
		result.resultList = resultList;
    	return result;
    }

    static <T extends JsonMappable> HttpResponse<T> getJsonList(String path,
    		                                                    String diskriminator,
                                                                Map<String, Class<? extends T>> classMap) {
    	final HttpResponse<T> result = getJson(path);
    	if (result.responseCode != HTTP_OK) {
    		return result;
    	}
    	
    	JsonReader jsonReader = null;
    	JsonArray jsonArray;
    	try {
    		jsonReader = jsonReaderFactory.createReader(new StringReader(result.content));
    		jsonArray = jsonReader.readArray();
    	}
    	finally {
    		if (jsonReader != null) {
    			jsonReader.close();
    		}
    	}
    	
    	final ArrayList<T> resultList = new ArrayList<T>(jsonArray.size());
    	final List<JsonObject> jsonObjectList = jsonArray.getValuesAs(JsonObject.class);
    	for (JsonObject jsonObject : jsonObjectList) {
    		final String disriminatorValue = jsonObject.getString(diskriminator);

	    	final Class<? extends T> clazz = classMap.get(disriminatorValue);
    		T object;
        	try {
    			object = clazz.newInstance();
    		}
    		catch (InstantiationException e) {
    			throw new InternalShopError(e.getMessage(), e);
    		}
    		catch (IllegalAccessException e) {
    			throw new InternalShopError(e.getMessage(), e);
    		}
        	object.fromJsonObject(jsonObject);
        	
        	resultList.add(object);
    	}
    	
		result.resultList = resultList;
    	return result;
    }

    // Fuer AutoCompleteTextView mit Long-Zahlen
    static List<Long> getJsonLongList(String path) {
    	final HttpResponse<Void> response = getJson(path);
    	if (response.responseCode != HTTP_OK) {
    		return Collections.emptyList();
    	}
    	
    	JsonReader jsonReader = null;
    	JsonArray jsonArray;
    	try {
    		jsonReader = jsonReaderFactory.createReader(new StringReader(response.content));
    		jsonArray = jsonReader.readArray();
    	}
    	finally {
    		if (jsonReader != null) {
    			jsonReader.close();
    		}
    	}
    	
		final List<JsonNumber> jsonNumberList = jsonArray.getValuesAs(JsonNumber.class);
		final List<Long> result = new ArrayList<Long>(jsonArray.size());
		for (JsonNumber jsonNumber : jsonNumberList) {
			result.add(Long.valueOf(jsonNumber.longValue()));
		}
		
		return result;
	}
    
    
    // Fuer AutoCompleteTextView mit Strings
    static List<String> getJsonStringList(String urlStr) {
    	final HttpResponse<Void> response = getJson(urlStr);
    	if (response.responseCode != HTTP_OK) {
    		return Collections.emptyList();
    	}
    	
    	JsonReader jsonReader = null;
    	JsonArray jsonArray;
    	try {
    		jsonReader = jsonReaderFactory.createReader(new StringReader(response.content));
    		jsonArray = jsonReader.readArray();
    	}
    	finally {
    		if (jsonReader != null) {
    			jsonReader.close();
    		}
    	}
    	
		final List<JsonString> jsonStringList = jsonArray.getValuesAs(JsonString.class);
		final List<String> result = new ArrayList<String>(jsonArray.size());
		for (JsonString jsonString : jsonStringList) {
			result.add(jsonString.getString());
		}
		
		return result;
	}

    
    static <T extends JsonMappable> HttpResponse<T> postJson(T jsonMappable, String path) {
    	URL url;
    	try {
			url = new URL(getBaseUrl() + path);
		}
    	catch (MalformedURLException e) {
    		Log.e(LOG_TAG, "Interner Fehler beim Erstellen der URL: " + getBaseUrl() + path, e);
    		throw new InternalShopError(e.getMessage(), e);
		}
    	Log.v(LOG_TAG, url.toString());
    	
    	// http://developer.android.com/reference/java/net/HttpURLConnection.html
    	HttpURLConnection httpConnection = null;
    	String location;
    	int statusCode = 0;
		try {
			Writer writer = null;
			try {
				httpConnection = (HttpURLConnection) url.openConnection();
				httpConnection.setDoOutput(true);
				httpConnection.setRequestProperty(CONTENT_TYPE, APPLICATION_JSON);
				httpConnection.setRequestProperty(ACCEPT_LANGUAGE, Locale.getDefault().getLanguage());
				httpConnection = auth(httpConnection);

				writer = new BufferedWriter(new OutputStreamWriter(httpConnection.getOutputStream()));
				writer.write(jsonMappable.toJsonObject().toString());
			}
			finally {
				writer.close();
			}
    		
    		statusCode = httpConnection.getResponseCode();
    		if (statusCode != HTTP_CREATED && statusCode != HTTP_NO_CONTENT && statusCode != HTTP_OK) {
    			final StringBuilder sb = new StringBuilder();
    			final BufferedReader err = new BufferedReader(new InputStreamReader(httpConnection.getErrorStream()));
				try {
					for (;;) {
						final String line = err.readLine();
						if (line == null) {
							break;
						}
						sb.append(line);
					}
				}
				catch (IOException e2) {
					Log.w(LOG_TAG, e2);
					String msg = sb.toString();
					if (TextUtils.isEmpty(msg)) {
						msg = e2.getMessage();
					}
					return new HttpResponse<T>(statusCode, msg);
				}
				finally {
					if (err != null) {
						err.close();
					}
    			}
        		return new HttpResponse<T>(statusCode, sb.toString());
    		}

    		location = httpConnection.getHeaderField(LOCATION);
		}
		catch (IOException e) {
			Log.e(LOG_TAG, e.getMessage(), e);
			return new HttpResponse<T>(HTTP_UNAVAILABLE, e.getMessage());
		}
		finally {
			if (httpConnection != null) {
				httpConnection.disconnect();
			}
		}
		
		// ID aus "location" extrahieren
		final int lastSlash = location.lastIndexOf("/");
		final String idStr = location.substring(lastSlash + 1);
		final HttpResponse<T> result = new HttpResponse<T>(statusCode, idStr);
		return result;
    }
    
    
    static <T extends JsonMappable> HttpResponse<T> putJson(T jsonMappable, String path) {
    	URL url;
    	try {
			url = new URL(getBaseUrl() + path);
		}
    	catch (MalformedURLException e) {
    		Log.e(LOG_TAG, "Interner Fehler beim Erstellen der URL: " + getBaseUrl() + path, e);
    		throw new InternalShopError(e.getMessage(), e);
		}
    	Log.v(LOG_TAG, url.toString());
    	
    	// http://developer.android.com/reference/java/net/HttpURLConnection.html
    	HttpURLConnection httpConnection = null;
    	int statusCode;
    	try {
    		Writer writer = null;
			try {
				httpConnection = (HttpURLConnection) url.openConnection();
				httpConnection.setDoOutput(true);
				httpConnection.setRequestMethod(PUT_METHOD);
				httpConnection.setRequestProperty(CONTENT_TYPE, APPLICATION_JSON);
				httpConnection.setRequestProperty(ACCEPT_LANGUAGE, Locale.getDefault().getLanguage());
				httpConnection = auth(httpConnection);
	
				writer = new BufferedWriter(new OutputStreamWriter(httpConnection.getOutputStream()));
				writer.write(jsonMappable.toJsonObject().toString());
			}
			finally {
				writer.close();
			}
    		
    		statusCode = httpConnection.getResponseCode();
    		if (statusCode != HTTP_NO_CONTENT && statusCode != HTTP_OK) {
    			final StringBuilder sb = new StringBuilder();
	    		final BufferedReader err = new BufferedReader(new InputStreamReader(httpConnection.getErrorStream()));
				try {
					for (;;) {
						final String line = err.readLine();
						if (line == null) {
							break;
						}
						sb.append(line);
					}
				}
				catch (IOException e) {
					Log.w(LOG_TAG, e);
					String msg = sb.toString();
					if (TextUtils.isEmpty(msg)) {
						msg = e.getMessage();
					}
					return new HttpResponse<T>(statusCode, msg);
				}
				finally {
					if (err != null) {
						err.close();
					}
				}
				return new HttpResponse<T>(statusCode, sb.toString());
    		}

		}
		catch (IOException e) {
			Log.e(LOG_TAG, e.getMessage(), e);
			return new HttpResponse<T>(HTTP_UNAVAILABLE, e.getMessage());
		}
		finally {
			if (httpConnection != null) {
				httpConnection.disconnect();
			}
		}
		return new HttpResponse<T>(statusCode, null);
    }

    
    static HttpResponse<Void> delete(String path) {
    	URL url;
    	try {
			url = new URL(getBaseUrl() + path);
		}
    	catch (MalformedURLException e) {
    		Log.e(LOG_TAG, "Interner Fehler beim Erstellen der URL: " + getBaseUrl() + path, e);
    		throw new InternalShopError(e.getMessage(), e);
		}
    	Log.v(LOG_TAG, url.toString());
    	
    	// http://developer.android.com/reference/java/net/HttpURLConnection.html
    	HttpURLConnection httpConnection = null;
    	int statusCode;
		try {
			httpConnection = (HttpURLConnection) url.openConnection();
			httpConnection.setRequestMethod(DELETE_METHOD);
			httpConnection.setRequestProperty(ACCEPT_LANGUAGE, Locale.getDefault().getLanguage());
			httpConnection = auth(httpConnection);
			httpConnection.connect();
			
			statusCode = httpConnection.getResponseCode();
			if (statusCode != HTTP_NO_CONTENT && statusCode != HTTP_OK) {
    			final StringBuilder sb = new StringBuilder();
	    		final BufferedReader err = new BufferedReader(new InputStreamReader(httpConnection.getErrorStream()));
				try {
					for (;;) {
						final String line = err.readLine();
						if (line == null) {
							break;
						}
						sb.append(line);
					}
				}
				catch (IOException e) {
					Log.w(LOG_TAG, e);
					String msg = sb.toString();
					if (TextUtils.isEmpty(msg)) {
						msg = e.getMessage();
					}
					return new HttpResponse<Void>(statusCode, msg);
				}
				finally {
					if (err != null) {
						err.close();
					}
				}
				return new HttpResponse<Void>(statusCode, sb.toString());
    		}
		}
		catch (IOException e) {
			Log.e(LOG_TAG, e.getMessage(), e);
			return new HttpResponse<Void>(HTTP_UNAVAILABLE, e.getMessage());
		}
		finally {
			if (httpConnection != null) {
				httpConnection.disconnect();
			}
		}
		return new HttpResponse<Void>(statusCode, null);
    }
    
    
    private static HttpURLConnection auth(HttpURLConnection httpConnection) {
    	if (AUTH_TYPE == null) {
    		return httpConnection;
    	}
    	
    	switch (AUTH_TYPE) {
    		case BASIC:
    			httpConnection = basicAuth(httpConnection);
    			break;
    			
    		case FORM:
    			httpConnection = formAuth(httpConnection);
    			break;
    			
    		default:
    			break;
    	}
    	
		return httpConnection;
    }
    
    
    private static HttpURLConnection basicAuth(HttpURLConnection httpConnection) {
    	if (TextUtils.isEmpty(username)) {
    		return httpConnection;
    	}
    	
		final String usernamePassword = username + ':' + password;

    	Log.v(LOG_TAG, "Passwort:"+usernamePassword);
    	
		httpConnection.setRequestProperty(AUTHORIZATION, "Basic " + Base64.encodeToString(usernamePassword.getBytes(), Base64.DEFAULT));
		
		return httpConnection;    	
    }
    
    
    private static HttpURLConnection formAuth(HttpURLConnection httpConnection) {
    	// Zitat von Craig McLanahan (Erfinder von Struts und erster Entwickler von Tomcat)
    	// bzgl. Form-based Authentifizierung mit HttpUrlConnection: "It's a pain."
    	throw new IllegalStateException("!!! Form-based Authentifizierung ist nicht implementiert !!!");
    }
    
    
    private WebServiceClient() {}
}
