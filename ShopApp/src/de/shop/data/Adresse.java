package de.shop.data;

import static de.shop.ShopApp.jsonBuilderFactory;

import java.io.Serializable;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;


public class Adresse implements JsonMappable, Serializable {
	private static final long serialVersionUID = -4218760204888865981L;
	
	public int version;
	public String plz;
	public String ort;
	public String strasse;
	public String hausnr;

	public Adresse() {
		super();
	}
	
	public Adresse(String plz, String ort, String strasse, String hausnr) {
		super();
		this.plz = plz;
		this.ort = ort;
		this.strasse = strasse;
		this.hausnr = hausnr;
	}
	
	// fuer AbstractKunde.toJsonObject()
	JsonObjectBuilder getJsonBuilderFactory() {
		return jsonBuilderFactory.createObjectBuilder()
		                         .add("version", version)
		                         .add("plz", plz)
		                         .add("ort", ort)
		                         .add("strasse", strasse)
		                         .add("hausnr", hausnr);
	}
	
	@Override
	public JsonObject toJsonObject() {
		return getJsonBuilderFactory().build();
	}
	
	@Override
	public void fromJsonObject(JsonObject jsonObject) {		
		version = jsonObject.getInt("version");
		plz = jsonObject.getString("plz");
		ort = jsonObject.getString("ort");
		strasse = jsonObject.getString("strasse");
		hausnr = jsonObject.getString("hausnr");
	}
	
	@Override
	public void updateVersion() {
		version++;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((hausnr == null) ? 0 : hausnr.hashCode());
		result = prime * result + ((ort == null) ? 0 : ort.hashCode());
		result = prime * result + ((plz == null) ? 0 : plz.hashCode());
		result = prime * result + ((strasse == null) ? 0 : strasse.hashCode());
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
		Adresse other = (Adresse) obj;
		if (hausnr == null) {
			if (other.hausnr != null)
				return false;
		} else if (!hausnr.equals(other.hausnr))
			return false;
		if (ort == null) {
			if (other.ort != null)
				return false;
		} else if (!ort.equals(other.ort))
			return false;
		if (plz == null) {
			if (other.plz != null)
				return false;
		} else if (!plz.equals(other.plz))
			return false;
		if (strasse == null) {
			if (other.strasse != null)
				return false;
		} else if (!strasse.equals(other.strasse))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Adresse [plz=" + plz + ", ort=" + ort + ", strasse=" + strasse
				+ ", hausnr=" + hausnr + "]";
	}
}
