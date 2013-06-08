package de.shop.data;

import javax.json.JsonObject;


public interface JsonMappable {
	JsonObject toJsonObject();
	void fromJsonObject(JsonObject jsonObject);
	void updateVersion();  // fuer PUT bei konkurrierenden Updates
}
