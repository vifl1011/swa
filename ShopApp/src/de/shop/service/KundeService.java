package de.shop.service;

import static android.app.ProgressDialog.STYLE_SPINNER;
import static de.shop.ui.main.Prefs.mock;
import static de.shop.ui.main.Prefs.timeout;
import static de.shop.util.Constants.KUNDEN_ID_PREFIX_PATH;
import static de.shop.util.Constants.KUNDEN_PATH;
import static de.shop.util.Constants.LOCALHOST;
import static de.shop.util.Constants.LOCALHOST_EMULATOR;
import static de.shop.util.Constants.NACHNAME_PATH;
import static de.shop.util.Constants.NACHNAME_PREFIX_PATH;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import de.shop.R;
import de.shop.data.AbstractKunde;
import de.shop.data.Firmenkunde;
import de.shop.data.Privatkunde;
import de.shop.util.InternalShopError;

public class KundeService extends Service {
	private static final String LOG_TAG = KundeService.class.getSimpleName();
	private static final String TYPE = "type";
	private static final Map<String, Class<? extends AbstractKunde>> CLASS_MAP;
	
	private KundeServiceBinder binder = new KundeServiceBinder();
	
	static {
		// 2 Eintraege in die HashMap mit 100% = 1.0 Fuellgrad
		CLASS_MAP = new HashMap<String, Class<? extends AbstractKunde>>(2, 1);
		CLASS_MAP.put("P", Privatkunde.class);
		CLASS_MAP.put("F", Firmenkunde.class);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}
	
	public class KundeServiceBinder extends Binder {
		
		public KundeService getService() {
			return KundeService.this;
		}
		
		private ProgressDialog progressDialog;
		private ProgressDialog showProgressDialog(Context ctx) {
			progressDialog = new ProgressDialog(ctx);
			progressDialog.setProgressStyle(STYLE_SPINNER);  // Kreis (oder horizontale Linie)
			progressDialog.setMessage(getString(R.string.s_bitte_warten));
			progressDialog.setCancelable(true);      // Abbruch durch Zuruecktaste
			progressDialog.setIndeterminate(true);   // Unbekannte Anzahl an Bytes werden vom Web Service geliefert
			progressDialog.show();
			return progressDialog;
		}
		
		/**
		 */
		public HttpResponse<AbstractKunde> sucheKundeById(Long id, final Context ctx) {
			
			// (evtl. mehrere) Parameter vom Typ "Long", Resultat vom Typ "AbstractKunde"
			final AsyncTask<Long, Void, HttpResponse<AbstractKunde>> sucheKundeByIdTask = new AsyncTask<Long, Void, HttpResponse<AbstractKunde>>() {
				@Override
	    		protected void onPreExecute() {
					progressDialog = showProgressDialog(ctx);
				}
				
				@Override
				// Neuer Thread, damit der UI-Thread nicht blockiert wird
				protected HttpResponse<AbstractKunde> doInBackground(Long... ids) {
					final Long id = ids[0];
		    		final String path = KUNDEN_PATH + "/" + id;
		    		Log.v(LOG_TAG, "path = " + path);
		    		final HttpResponse<AbstractKunde> result = mock
		    				                                   ? Mock.sucheKundeById(id)
		    				                                   : WebServiceClient.getJsonSingle(path, TYPE, CLASS_MAP);

					Log.d(LOG_TAG + ".AsyncTask", "doInBackground: " + result);
					return result;
				}
				
				@Override
	    		protected void onPostExecute(HttpResponse<AbstractKunde> unused) {
					progressDialog.dismiss();
	    		}
			};

    		sucheKundeByIdTask.execute(id);
    		HttpResponse<AbstractKunde> result = null;
	    	try {
	    		result = sucheKundeByIdTask.get(timeout, SECONDS);
			}
	    	catch (Exception e) {
	    		throw new InternalShopError(e.getMessage(), e);
			}
	    	
    		if (result.responseCode != HTTP_OK) {
	    		return result;
		    }
    		
    		setBestellungenUri(result.resultObject);
		    return result;
		}
		
		private void setBestellungenUri(AbstractKunde kunde) {
	    	// URLs der Bestellungen fuer Emulator anpassen
	    	final String bestellungenUri = kunde.bestellungenUri;
	    	if (!TextUtils.isEmpty(bestellungenUri)) {
			    kunde.bestellungenUri = bestellungenUri.replace(LOCALHOST, LOCALHOST_EMULATOR);
	    	}
		}
		
		/**
		 */
		public HttpResponse<AbstractKunde> sucheKundenByNachname(String nachname, final Context ctx) {
			// (evtl. mehrere) Parameter vom Typ "String", Resultat vom Typ "List<AbstractKunde>"
			final AsyncTask<String, Void, HttpResponse<AbstractKunde>> sucheKundenByNameTask = new AsyncTask<String, Void, HttpResponse<AbstractKunde>>() {
				@Override
	    		protected void onPreExecute() {
					progressDialog = showProgressDialog(ctx);
				}
				
				@Override
				// Neuer Thread, damit der UI-Thread nicht blockiert wird
				protected HttpResponse<AbstractKunde> doInBackground(String... nachnamen) {
					final String nachname = nachnamen[0];
					final String path = NACHNAME_PATH + nachname;
					Log.v(LOG_TAG, "path = " + path);
		    		final HttpResponse<AbstractKunde> result = mock
		    				                                   ? Mock.sucheKundenByNachname(nachname)
		    				                                   : WebServiceClient.getJsonList(path, TYPE, CLASS_MAP);
					Log.d(LOG_TAG + ".AsyncTask", "doInBackground: " + result);
					return result;
				}
				
				@Override
	    		protected void onPostExecute(HttpResponse<AbstractKunde> unused) {
					progressDialog.dismiss();
	    		}
			};
			
			sucheKundenByNameTask.execute(nachname);
			HttpResponse<AbstractKunde> result = null;
			try {
				result = sucheKundenByNameTask.get(timeout, SECONDS);
			}
	    	catch (Exception e) {
	    		throw new InternalShopError(e.getMessage(), e);
			}

	    	if (result.responseCode != HTTP_OK) {
	    		return result;
	    	}
	    	
	    	final ArrayList<AbstractKunde> kunden = result.resultList;
	    	// URLs fuer Emulator anpassen
	    	for (AbstractKunde k : kunden) {
	    		setBestellungenUri(k);
	    	}
			return result;
	    }
	

		/**
		 */
		public List<Long> sucheBestellungenIdsByKundeId(Long id, final Context ctx) {
			// (evtl. mehrere) Parameter vom Typ "Long", Resultat vom Typ "List<Long>"
			final AsyncTask<Long, Void, List<Long>> sucheBestellungenIdsByKundeIdTask = new AsyncTask<Long, Void, List<Long>>() {
				@Override
				// Neuer Thread, damit der UI-Thread nicht blockiert wird
				protected List<Long> doInBackground(Long... ids) {
					final Long id = ids[0];
		    		final String path = KUNDEN_PATH + "/" + id + "/bestellungenIds";
		    		Log.v(LOG_TAG, "path = " + path);
		    		final List<Long> result = mock
		    				                  ? Mock.sucheBestellungenIdsByKundeId(id)
		    				                  : WebServiceClient.getJsonLongList(path);
			    	Log.d(LOG_TAG + ".AsyncTask", "doInBackground: " + result);
					return result;
				}
			};
			
			sucheBestellungenIdsByKundeIdTask.execute(id);
			List<Long> bestellungIds = null;
			try {
				bestellungIds = sucheBestellungenIdsByKundeIdTask.get(timeout, SECONDS);
			}
	    	catch (Exception e) {
	    		throw new InternalShopError(e.getMessage(), e);
			}
	
			return bestellungIds;
	    }
		
		/**
		 * Annahme: wird ueber AutoCompleteTextView aufgerufen, wobei die dortige Methode
		 * performFiltering() schon einen neuen Worker-Thread startet, so dass AsyncTask hier
		 * ueberfluessig ist.
		 */
		public List<Long> sucheIds(String prefix) {
			final String path = KUNDEN_ID_PREFIX_PATH + "/" + prefix;
		    Log.v(LOG_TAG, "sucheIds: path = " + path);

    		final List<Long> ids = mock
   				                   ? Mock.sucheKundeIdsByPrefix(prefix)
   				                   : WebServiceClient.getJsonLongList(path);

			Log.d(LOG_TAG, "sucheIds: " + ids.toString());
			return ids;
		}
		
		/**
		 * Annahme: wird ueber AutoCompleteTextView aufgerufen, wobei die dortige Methode
		 * performFiltering() schon einen neuen Worker-Thread startet, so dass AsyncTask hier
		 * ueberfluessig ist.
		 */
		public List<String> sucheNachnamen(String prefix) {
			final String path = NACHNAME_PREFIX_PATH +  "/" + prefix;
		    Log.v(LOG_TAG, "sucheNachnamen: path = " + path);

    		final List<String> nachnamen = mock
    				                       ? Mock.sucheNachnamenByPrefix(prefix)
    				                       : WebServiceClient.getJsonStringList(path);
			Log.d(LOG_TAG, "sucheNachnamen: " + nachnamen);

			return nachnamen;
		}

		/**
		 */
		public HttpResponse<AbstractKunde> createKunde(AbstractKunde kunde, final Context ctx) {
			// (evtl. mehrere) Parameter vom Typ "AbstractKunde", Resultat vom Typ "void"
			final AsyncTask<AbstractKunde, Void, HttpResponse<AbstractKunde>> createKundeTask = new AsyncTask<AbstractKunde, Void, HttpResponse<AbstractKunde>>() {
				@Override
	    		protected void onPreExecute() {
					progressDialog = showProgressDialog(ctx);
				}
				
				@Override
				// Neuer Thread, damit der UI-Thread nicht blockiert wird
				protected HttpResponse<AbstractKunde> doInBackground(AbstractKunde... kunden) {
					final AbstractKunde kunde = kunden[0];
		    		final String path = KUNDEN_PATH;
		    		Log.v(LOG_TAG, "path = " + path);

		    		final HttpResponse<AbstractKunde> result = mock
                                                               ? Mock.createKunde(kunde)
                                                               : WebServiceClient.postJson(kunde, path);
		    		
					Log.d(LOG_TAG + ".AsyncTask", "doInBackground: " + result);
					return result;
				}
				
				@Override
	    		protected void onPostExecute(HttpResponse<AbstractKunde> unused) {
					progressDialog.dismiss();
	    		}
			};
			
			createKundeTask.execute(kunde);
			HttpResponse<AbstractKunde> response = null; 
			try {
				response = createKundeTask.get(timeout, SECONDS);
			}
	    	catch (Exception e) {
	    		throw new InternalShopError(e.getMessage(), e);
			}
			
			kunde.id = Long.valueOf(response.content);
			final HttpResponse<AbstractKunde> result = new HttpResponse<AbstractKunde>(response.responseCode, response.content, kunde);
			return result;
	    }
		
		/**
		 */
		public HttpResponse<AbstractKunde> updateKunde(AbstractKunde kunde, final Context ctx) {
			// (evtl. mehrere) Parameter vom Typ "AbstractKunde", Resultat vom Typ "void"
			final AsyncTask<AbstractKunde, Void, HttpResponse<AbstractKunde>> updateKundeTask = new AsyncTask<AbstractKunde, Void, HttpResponse<AbstractKunde>>() {
				@Override
	    		protected void onPreExecute() {
					progressDialog = showProgressDialog(ctx);
				}
				
				@Override
				// Neuer Thread, damit der UI-Thread nicht blockiert wird
				protected HttpResponse<AbstractKunde> doInBackground(AbstractKunde... kunden) {
					final AbstractKunde kunde = kunden[0];
		    		final String path = KUNDEN_PATH;
		    		Log.v(LOG_TAG, "path = " + path);

		    		final HttpResponse<AbstractKunde> result = mock
		    				                          ? Mock.updateKunde(kunde)
		    		                                  : WebServiceClient.putJson(kunde, path);
					Log.d(LOG_TAG + ".AsyncTask", "doInBackground: " + result);
					return result;
				}
				
				@Override
	    		protected void onPostExecute(HttpResponse<AbstractKunde> unused) {
					progressDialog.dismiss();
	    		}
			};
			
			updateKundeTask.execute(kunde);
			final HttpResponse<AbstractKunde> result;
			try {
				result = updateKundeTask.get(timeout, SECONDS);
			}
	    	catch (Exception e) {
	    		throw new InternalShopError(e.getMessage(), e);
			}
			
			if (result.responseCode == HTTP_NO_CONTENT || result.responseCode == HTTP_OK) {
				kunde.updateVersion();  // kein konkurrierendes Update auf Serverseite
				result.resultObject = kunde;
			}
			
			return result;
	    }
		
		/**
		 */
		public HttpResponse<Void> deleteKunde(Long id, final Context ctx) {
			
			// (evtl. mehrere) Parameter vom Typ "Long", Resultat vom Typ "AbstractKunde"
			final AsyncTask<Long, Void, HttpResponse<Void>> deleteKundeTask = new AsyncTask<Long, Void, HttpResponse<Void>>() {
				@Override
	    		protected void onPreExecute() {
					progressDialog = showProgressDialog(ctx);
				}
				
				@Override
				// Neuer Thread, damit der UI-Thread nicht blockiert wird
				protected HttpResponse<Void> doInBackground(Long... ids) {
					final Long kundeId = ids[0];
		    		final String path = KUNDEN_PATH + "/" + kundeId;
		    		Log.v(LOG_TAG, "path = " + path);

		    		final HttpResponse<Void> result = mock ? Mock.deleteKunde(kundeId) : WebServiceClient.delete(path);
			    	return result;
				}
				
				@Override
	    		protected void onPostExecute(HttpResponse<Void> unused) {
					progressDialog.dismiss();
	    		}
			};
			
			deleteKundeTask.execute(id);
			final HttpResponse<Void> result;
	    	try {
	    		result = deleteKundeTask.get(timeout, SECONDS);
			}
	    	catch (Exception e) {
	    		throw new InternalShopError(e.getMessage(), e);
			}
			
			return result;
		}
	}
}
