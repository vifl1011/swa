package de.shop.data;

import static de.shop.ShopApp.jsonBuilderFactory;

import java.io.Serializable;
import java.util.Date;

import javax.json.JsonObject;

public class Produkt implements JsonMappable, Serializable  {
	private static final long serialVersionUID = -2691507537306675231L;

	public Long id;
	public int version;
	public String bezeichnung;
	public float preis;
	public Date aktualisiert;
	public Date erzeugt;
	public String farbe;
	public String groesse;
	public int vorrat;
	
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
		id = Long.valueOf(jsonObject.getJsonNumber("id").longValue());
		version = jsonObject.getInt("version");
		aktualisiert = new Date(jsonObject.getJsonNumber("aktualisiert").longValue());
		bezeichnung = jsonObject.getString("bezeichnung");
		preis = Float.valueOf(jsonObject.getString("preis"));
		erzeugt = new Date(jsonObject.getJsonNumber("erzeugt").longValue());
		farbe = jsonObject.getString("farbe");
		groesse = jsonObject.getString("groesse");
		vorrat = jsonObject.getInt("vorrat");
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
