package de.shop.data;

import static de.shop.ShopApp.jsonBuilderFactory;

import java.util.Collection;
import java.util.HashSet;

import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;


public class Privatkunde extends AbstractKunde {
	private static final long serialVersionUID = -3018823336715723505L;

	public GeschlechtType geschlecht;
	public FamilienstandType familienstand;
	public Collection<HobbyType> hobbies;

	@Override
	protected JsonObjectBuilder getJsonObjectBuilder() {
		final JsonObjectBuilder jsonObjectBuilder = super.getJsonObjectBuilder()
				                                         .add("geschlecht", geschlecht.toString())
			                                             .add("familienstand", familienstand.toString());
		
		final JsonArrayBuilder jsonArrayBuilder = jsonBuilderFactory.createArrayBuilder();
		for (HobbyType hobby : hobbies) {
			jsonArrayBuilder.add(hobby.toString());
		}
		jsonObjectBuilder.add("hobbies", jsonArrayBuilder);
		
		return jsonObjectBuilder;
	}
	
	@Override
	public void fromJsonObject(JsonObject jsonObject) {
		super.fromJsonObject(jsonObject);
		
		geschlecht = GeschlechtType.valueOf(jsonObject.getString("geschlecht"));
		familienstand = FamilienstandType.valueOf(jsonObject.getString("familienstand"));
		
		final JsonArray jsonArray = jsonObject.getJsonArray("hobbies");
		final int anzahl = jsonArray.size();
		hobbies = new HashSet<HobbyType>(HobbyType.values().length, 1);
		for (int i = 0; i < anzahl; i++) {
			hobbies.add(HobbyType.valueOf(jsonArray.getString(i)));
		}
	}
	
	@Override
	public String toString() {
		return "Privatkunde [" + super.toString() + ", geschlecht=" + geschlecht
				+ ", familienstand=" + familienstand + ", hobbies=" + hobbies + "]";
	}
}
