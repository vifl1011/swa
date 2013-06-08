package de.shop.service;

import java.util.ArrayList;

public class HttpResponse<T> {
	public int responseCode;
	public String content;   // Inhalt vom Response-Body (JSON-String oder Fehlermeldung) oder ID aus location
	
	public T resultObject;
	public ArrayList<T> resultList; // zur Weitergabe der gefundenen Objekte an einen Intent wird eine serialisierbare Liste benoetigt
	
	public HttpResponse(int responseCode, String content) {
		super();
		this.responseCode = responseCode;
		this.content = content;
	}

	// nur relevant fuer Mock
	public HttpResponse(int responseCode, String content, T resultObject) {
		super();
		this.responseCode = responseCode;
		this.content = content;
		this.resultObject = resultObject;
	}

	// nur relevant fuer Mock
	public HttpResponse(int responseCode, String content, ArrayList<T> resultList) {
		super();
		this.responseCode = responseCode;
		this.content = content;
		this.resultList = resultList;
	}

	@Override
	public String toString() {
		return "HttpResponse [responseCode=" + responseCode + ", content=" + content + "]";
	}
}
