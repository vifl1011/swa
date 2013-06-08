package de.shop.data;

import java.io.Serializable;
import java.util.Date;

import javax.json.JsonObject;

public class Produkt implements JsonMappable, Serializable  {
	private static final long serialVersionUID = -2691507537306675231L;

	public Long id;
	public int version;
	public Date aktualisiert;
	public Date erzeugt;
	
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
