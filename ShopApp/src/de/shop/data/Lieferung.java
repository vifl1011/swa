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
		id = Long.valueOf(jsonObject.getJsonNumber("id").longValue());
		version = jsonObject.getInt("version");
		aktualisiert = new Date(jsonObject.getJsonNumber("aktualisiert").longValue());
		art = jsonObject.getString("art");
		versanddatum = new Date(jsonObject.getJsonNumber("versanddatum").longValue());
		erzeugt = new Date(jsonObject.getJsonNumber("erzeugt").longValue());
	}

	@Override
	public void updateVersion() {
		version++;		
	}

	@Override
	public String toString() {
		return "Lieferung [id=" + id 
				+ ", Aktualisiert="	+ aktualisiert 
				+ ", Erzeugt=" + erzeugt 
				+ ", Art=" + art 
				+ ", Versanddatum=" + versanddatum 
				+ "]";
	}
}
