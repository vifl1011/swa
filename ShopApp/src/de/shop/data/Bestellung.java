package de.shop.data;

import static de.shop.ShopApp.jsonBuilderFactory;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;


public class Bestellung implements JsonMappable, Serializable {
	private static final long serialVersionUID = -3227854872557641281L;
	
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
		id = Long.valueOf(jsonObject.getJsonNumber("id").longValue());
		version = jsonObject.getInt("version");
		aktualisiert = new Date(jsonObject.getJsonNumber("aktualisiert").longValue());
		bestellstatus = jsonObject.getString("bestellstatus");
		erzeugt = new Date(jsonObject.getJsonNumber("erzeugt").longValue());
		bestellzeitpunkt = new Date(jsonObject.getJsonNumber("bestellzeitpunkt").longValue());
		gesamtpreis = Float.valueOf(jsonObject.getString("gesamtpreis"));
		gezahlt = Byte.valueOf(jsonObject.getString("gezahlt"));
		
		//Kunden ermitteln
		Kunde kunde = new Kunde();
		//	TODO hier kommt noch n bissel zeugs rein =)
		
		//bestellpositionen ermitteln
		//	TODO auch hier kommt noch zeugs rein
		
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
