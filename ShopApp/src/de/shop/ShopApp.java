package de.shop;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonReaderFactory;

import android.app.Application;
import android.util.Log;
import android.content.Context;

import de.shop.util.InternalShopError;

/**
 * Die Klasse ShopApp muss 2x in AndroidManifest.xml eingetragen werden:
 *     beim Wurzelelement manifest und
 *     beim Untertag application
 */
public class ShopApp extends Application {
	private static final String LOG_TAG = ShopApp.class.getSimpleName();

	public static JsonReaderFactory jsonReaderFactory;
	public static JsonBuilderFactory jsonBuilderFactory;

	private static Context ctx;
	
	
	@Override
	public void onCreate() {
		jsonReaderFactory = Json.createReaderFactory(null);
		jsonBuilderFactory = Json.createBuilderFactory(null);

		ctx = this;
	}
	
	public static InputStream open(int dateinameId) {
		if (ctx == null) {
			final String msg = "ShopApp ist nicht initialisiert! Gibt es die 2 Eintraege in AndroidManifest.xml?";
			Log.e(LOG_TAG, msg);
			throw new InternalShopError(msg);
		}
		
		// dateinameId = R.raw.dateiname
		// fuer die Datei res\raw\dateiname.json
		return ctx.getResources().openRawResource(dateinameId);
	}
	
	public static OutputStream open(String dateiname) throws FileNotFoundException {
		if (ctx == null) {
			final String msg = "ShopApp ist nicht initialisiert! Gibt es die 2 Eintraege in AndroidManifest.xml?";
			Log.e(LOG_TAG, msg);
			throw new InternalShopError(msg);
		}
		
		// /data/data/de.shop/files/<dateiname>
		return ctx.openFileOutput(dateiname, MODE_PRIVATE);
	}
}
