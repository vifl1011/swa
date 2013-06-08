package de.shop.data;

import java.io.Serializable;
import java.util.Date;

import javax.json.JsonObject;

public class Bestellposition implements JsonMappable, Serializable {
	private static final long serialVersionUID = -2495835532451031403L;
	
	public Long id;
	public int version;
	public Date aktualisiert;
	public float einzelpreis;
	public Date erzeugt;
	public int menge;
	public Produkt produkt;
	public Lieferung lieferung;
	public Bestellung bestellung;
	
	@Override
	public JsonObject toJsonObject() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void fromJsonObject(JsonObject jsonObject) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateVersion() {
		// TODO Auto-generated method stub
		
	}

}
