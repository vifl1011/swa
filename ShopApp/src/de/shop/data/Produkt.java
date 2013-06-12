package de.shop.data;

import static de.shop.ShopApp.jsonBuilderFactory;

import java.io.Serializable;
import java.util.Date;

import javax.json.JsonObject;

import android.util.Log;

public class Produkt implements JsonMappable, Serializable  {
	private static final long serialVersionUID = -2691507537306675231L;
	private static final String LOG_TAG = Produkt.class.getSimpleName();

	public Long id;
	public int version;
	public String bezeichnung;
	public float preis;
	public Date aktualisiert;
	public Date erzeugt;
	public String farbe;
	public String groesse;
	public int vorrat;
	
	public Produkt() {
		super();
	}
	
	public Produkt(Long id) {
		super();
		this.id = id;
	}
	
	@Override
	public JsonObject toJsonObject() {
		return jsonBuilderFactory.createObjectBuilder()
                .add("id", id)
                .add("version", version)
                .add("aktualisiert", aktualisiert.getTime())
                .add("bezeichnung", bezeichnung)
                .add("erzeugt", erzeugt.getTime())
                .add("preis", preis)
                .add("farbe", farbe)
                .add("groesse", groesse)
                .add("vorrat", vorrat)
               
                .build();
	}

	@Override
	public void fromJsonObject(JsonObject jsonObject) {
		Log.v(LOG_TAG, "start fromJsonObject() method...");
		
		id = Long.valueOf(jsonObject.getJsonNumber("id").longValue());
		Log.v(LOG_TAG, "...");
		version = jsonObject.getInt("version");
		Log.v(LOG_TAG, "...");
		bezeichnung = jsonObject.getString("bezeichnung");
		Log.v(LOG_TAG, "...");
		preis = Float.valueOf(jsonObject.getJsonNumber("preis").toString());
		Log.v(LOG_TAG, "...");
		farbe = jsonObject.getString("farbe");
		Log.v(LOG_TAG, "...");
		groesse = jsonObject.getString("groesse");
		Log.v(LOG_TAG, "...");
		vorrat = jsonObject.getInt("vorrat");
		Log.v(LOG_TAG, "...");
		
		Log.v(LOG_TAG, "start fromJsonObject() method...");
	}

	@Override
	public void updateVersion() {
		version++;		
	}

	@Override
	public String toString() {
		return "Produkt [id=" + id 
				+ ", Aktualisiert=" + aktualisiert 
				+ ", Erzeugt=" + erzeugt
				+ ", Bezeichnung=" + bezeichnung
				+ ", Preis=" + preis
				+ ", Farbe=" + farbe
				+ ", Groesse=" + groesse
				+ ", Vorrat=" + vorrat
				+ "]";
	}
}
