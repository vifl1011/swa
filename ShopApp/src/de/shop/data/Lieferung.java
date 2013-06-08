package de.shop.data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.json.JsonObject;

public class Lieferung implements JsonMappable, Serializable  {
	private static final long serialVersionUID = 5818905940511009499L;
	
	public Long id;
	public int version;
	public Date aktualisiert;
	public String art;
	public Date erzeugt;
	public Date versanddatum;
	public List<Bestellposition> bestellpositionenFL;
	
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
