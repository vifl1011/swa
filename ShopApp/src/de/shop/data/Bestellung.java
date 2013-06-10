package de.shop.data;

import static de.shop.ShopApp.jsonBuilderFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

import de.shop.service.BestellungService;
import android.net.Uri;
import android.util.Log;


public class Bestellung implements JsonMappable, Serializable {
	private static final long serialVersionUID = -3227854872557641281L;
	private static final String LOG_TAG = Bestellung.class.getSimpleName();
	
	public Long id;
	public int version;
	public Date aktualisiert;
	public Date erzeugt;
	public Date bestellzeitpunkt;
	public String bestellstatus;
	public float gesamtpreis;
	public byte gezahlt;
	public Kunde kunde;
	public List<Bestellposition> bestellpositionen;
	
	public Uri kundeUri;

	public Bestellung() {
		super();
	}

	public Bestellung(long id, Kunde kunde) {
		super();
		this.id = id;
		this.kunde = kunde;
	}

	@Override
	public JsonObject toJsonObject() {
		JsonArrayBuilder arrayBuilder = jsonBuilderFactory.createArrayBuilder();
		for (int i = 0; i < bestellpositionen.size(); ++i) {
						arrayBuilder.add(jsonBuilderFactory.createObjectBuilder()
															.add("bestellposition", bestellpositionen.get(i).toJsonObject()));
		}
		return jsonBuilderFactory.createObjectBuilder()
		                         .add("id", id)
		                         .add("version", version)
		                         .add("aktualisiert", aktualisiert.getTime())
		                         .add("bestellstatus", bestellstatus)
		                         .add("bestellzeitpunkt", bestellzeitpunkt.getTime())
		                         .add("erzeugt", erzeugt.getTime())
		                         .add("gesamtpreis", gesamtpreis)
		                         .add("gezahlt", gezahlt)
		                         .add("bestellpositionen", arrayBuilder)
		                         //.add("kunde", kunde.toJsonObject())
		                         //	TODO comments entfernen sobald die Kunde Klasse fertig ist
		                        
		                         .build();
	}
	
	@Override
	public void fromJsonObject(JsonObject jsonObject) {
		Log.v(LOG_TAG, "start fromJsonObject() method...");
		id = Long.valueOf(jsonObject.getJsonNumber("id").longValue());
		Log.v(LOG_TAG, "id = " + id);
		version = jsonObject.getInt("version");
		Log.v(LOG_TAG, "version = " + version);
		aktualisiert = new Date(jsonObject.getJsonNumber("aktualisiert").longValue());
		Log.v(LOG_TAG, "aktualisiert = " + aktualisiert);
		bestellstatus = jsonObject.getString("bestellstatus");
		Log.v(LOG_TAG, "bestellstatus = " + bestellstatus);
		erzeugt = new Date(jsonObject.getJsonNumber("erzeugt").longValue());
		Log.v(LOG_TAG, "erzeugt = " + erzeugt);
		bestellzeitpunkt = new Date(jsonObject.getJsonNumber("bestellzeitpunkt").longValue());
		Log.v(LOG_TAG, "bestellzeitpunkt = " + bestellzeitpunkt);
		gesamtpreis = Float.valueOf(jsonObject.getJsonNumber("gesamtpreis").toString());
		Log.v(LOG_TAG, "gesamtpreis = " + gesamtpreis);
		gezahlt = Byte.valueOf(jsonObject.getJsonNumber("gezahlt").toString());
		Log.v(LOG_TAG, "gezahlt = " + gezahlt);
		
		//Kunden ermitteln --> muss erst ermittelt werden da im Objekt nur die Uri ist
		kundeUri = Uri.parse(jsonObject.getString("kundeUri"));
		Log.v(LOG_TAG, "kundeUri = " + kundeUri);
		//	TODO hier kommt noch n bissel zeugs rein sonst ist der Kunde null =)
		
		//bestellpositionen ermitteln
		bestellpositionen = new ArrayList<Bestellposition>();
		Bestellposition pos;
		JsonArray jayArray = jsonObject.getJsonArray("bestellpositionen");
		for (int i = 0; i < jayArray.size(); ++i) {
			pos = new Bestellposition();
			pos.fromJsonObject(jayArray.getJsonObject(i));
			bestellpositionen.add(pos);
		}
		
		Log.v(LOG_TAG, "end fromJsonObject() method...");
	}
	
	@Override
	public void updateVersion() {
		version++;
	}

	@Override
	public String toString() {
		return "Bestellung [id=" + id + ", aktualisiert=" + aktualisiert + "]";
	}
}
