package de.shop.data;

import static de.shop.ShopApp.jsonBuilderFactory;

import java.io.Serializable;
import java.util.Date;

import javax.json.JsonObject;

import android.net.Uri;
import android.util.Log;

public class Bestellposition implements JsonMappable, Serializable {
	private static final long serialVersionUID = -2495835532451031403L;
	private static final String LOG_TAG = Bestellposition.class.getSimpleName();
	
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
	
	public Long getProduktId() {
		String produktUriStr = produktUri.toString();
		int pos = produktUriStr.lastIndexOf("/");
		String idString = produktUriStr.substring(pos+1);
		
		Long result;
		
		try {
			result = Long.valueOf(idString);
		} catch (Exception e) {
			Log.e(LOG_TAG, e.getStackTrace().toString());
			return null;
		}
		
		return result;
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
		Log.v(LOG_TAG, "start fromJsonObject() method...");
		
		id = Long.valueOf(jsonObject.getJsonNumber("id").longValue());
		Log.v(LOG_TAG, "	id = " + id);
		version = jsonObject.getInt("version");
		Log.v(LOG_TAG, "	version = " + version);
		aktualisiert = new Date(jsonObject.getJsonNumber("aktualisiert").longValue());
		Log.v(LOG_TAG, "	aktualisiert = " + aktualisiert);
		einzelpreis = Float.valueOf(jsonObject.getJsonNumber("einzelpreis").toString());
		Log.v(LOG_TAG, "	einzelpreis = " + einzelpreis);
		erzeugt = new Date(jsonObject.getJsonNumber("erzeugt").longValue());
		Log.v(LOG_TAG, "	erzeugt = " + erzeugt);
		menge = jsonObject.getInt("menge");	
		Log.v(LOG_TAG, "	menge = " + menge);
		produktUri = Uri.parse(jsonObject.getString("produktUri"));
		Log.v(LOG_TAG, "	produktUri = " + produktUri);
		lieferungUri = Uri.parse(jsonObject.getString("lieferungUri"));
		Log.v(LOG_TAG, "	lieferungUri = " + lieferungUri);
		bestellungUri = Uri.parse(jsonObject.getString("bestellungUri"));
		Log.v(LOG_TAG, "	bestellungUri = " + bestellungUri);
		
		Log.v(LOG_TAG, "end fromJsonObject() method...");
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
