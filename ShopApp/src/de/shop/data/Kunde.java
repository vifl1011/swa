package de.shop.data;

import static de.shop.ShopApp.jsonBuilderFactory;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import de.shop.util.InternalShopError;

public class Kunde implements JsonMappable, Serializable {
	private static final long serialVersionUID = -7505776004556360014L;
	private static final String DATE_FORMAT = "yyyy-MM-dd";

	public Long id;
	public int version;
	public String nachname;
	public String vorname;
	public BigDecimal rabatt;
	public String email;
	public Adresse adresse;
	public boolean agbAkzeptiert = true;
	public GeschlechtType geschlecht;
	public String bestellungenUri;
	
	protected JsonObjectBuilder getJsonObjectBuilder() {
		String ges="";
		if(geschlecht!=null){
			if(geschlecht.equals(GeschlechtType.WEIBLICH))
				ges="w";
			else
				ges="m";
		}
		return jsonBuilderFactory.createObjectBuilder()
				                 .add("id", id)
			                     .add("version", version)
			                     .add("name", nachname)
			                     .add("vorname", vorname)
			                     .add("rabatt", rabatt)
			                     .add("email", email)
			                     .add("adresse", adresse.getJsonBuilderFactory())
			                     .add("agbAkzeptiert", agbAkzeptiert)
			                     .add("bestellungenUri", bestellungenUri)
			                     .add("geschlecht", ges)
			                     ;
	}
	
	@Override
	public JsonObject toJsonObject() {
		return getJsonObjectBuilder().build();
	}

	public void fromJsonObject(JsonObject jsonObject) {

		id = Long.valueOf(jsonObject.getJsonNumber("id").longValue());
	    version = jsonObject.getInt("version");
		nachname = jsonObject.getString("name");
		vorname = jsonObject.getString("vorname");
		rabatt = jsonObject.getJsonNumber("rabatt").bigDecimalValue();
		email = jsonObject.getString("email");
		adresse = new Adresse();
		adresse.fromJsonObject(jsonObject.getJsonObject("adresse"));
		agbAkzeptiert = jsonObject.getBoolean("agbAkzeptiert");
		bestellungenUri = jsonObject.getString("bestellungenUri");
		String ges=jsonObject.getString("geschlecht");
		if(ges!=null){
			if(ges.equals("w"))
				geschlecht=GeschlechtType.WEIBLICH;
			else
				geschlecht=GeschlechtType.MAENNLICH;
		}
	}
	
	@Override
	public void updateVersion() {
		version++;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Kunde other = (Kunde) obj;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Kunde [id=" + id + ", nachname=" + nachname + ", vorname="
				+ vorname  + ", rabatt=" + rabatt
				+ ", geschlecht=" + geschlecht  
				+ ", email=" + email + ", adresse=" + adresse
				+ ", bestellungenUri=" + bestellungenUri
				+"]";
	}
}
