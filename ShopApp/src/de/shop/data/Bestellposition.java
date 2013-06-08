package de.shop.data;

import static de.shop.ShopApp.jsonBuilderFactory;

import java.io.Serializable;
import java.util.Date;

import javax.json.JsonObject;
import android.net.Uri;

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
	
	public Uri produktUri;
	public Uri lieferungUri;
	public Uri bestellungUri;
	
	
	public Bestellposition() {
		super();
	}
	
	public Bestellposition(Long id, Bestellung best, Produkt prod) {
		super();
		this.id = id;
		this.bestellung = best;
		this.produkt = prod;
	}
	
	@Override
	public JsonObject toJsonObject() {
		return jsonBuilderFactory.createObjectBuilder()
                .add("id", id)
                .add("version", version)
                .add("aktualisiert", aktualisiert.getTime())
                .add("einzelpreis", einzelpreis)
                .add("erzeugt", erzeugt.getTime())
                .add("menge", menge)
                .add("produktUri", produktUri.toString())
                .add("lieferungUri", lieferungUri.toString())
                .add("bestellungUri", bestellungUri.toString())
               
                .build();
	}

	@Override
	public void fromJsonObject(JsonObject jsonObject) {
		id = Long.valueOf(jsonObject.getJsonNumber("id").longValue());
		version = jsonObject.getInt("version");
		aktualisiert = new Date(jsonObject.getJsonNumber("aktualisiert").longValue());
		einzelpreis = Float.valueOf(jsonObject.getString("einzelpreis"));
		erzeugt = new Date(jsonObject.getJsonNumber("erzeugt").longValue());
		menge = jsonObject.getInt("menge");	
		produktUri = Uri.parse(jsonObject.getString("produktUri"));
		lieferungUri = Uri.parse(jsonObject.getString("lieferungUri"));
		bestellungUri = Uri.parse(jsonObject.getString("bestellungUri"));
	}

	@Override
	public void updateVersion() {
		version++;		
	}

	@Override
	public String toString() {
		return "Bestellposition [id=" + id 
				+ ", Aktualisiert=" + aktualisiert 
				+ ", Erzeugt=" + erzeugt
				+ ", Einzelpreis=" + einzelpreis
				+ ", Menge=" + menge
				+ ", Produkt Uri=" + produktUri
				+ ", Lieferung Uri=" + lieferungUri
				+ ", Bestellung Uri=" + bestellungUri
				+ "]";
	}
}
