package de.shop.service;

import static de.shop.ui.main.Prefs.mock;
import static de.shop.ui.main.Prefs.timeout;
import static de.shop.util.Constants.PRODUKT_PATH;
import static java.util.concurrent.TimeUnit.SECONDS;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import de.shop.R;
import de.shop.data.Produkt;
import de.shop.util.InternalShopError;

public class ProduktService extends Service {
	private static final String LOG_TAG = ProduktService.class.getSimpleName();

	private final ProduktServiceBinder binder = new ProduktServiceBinder();

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}
	
	private ProgressDialog progressDialog;
	private ProgressDialog showProgressDialog(Context ctx) {
		progressDialog = new ProgressDialog(ctx);  // Objekt this der umschliessenden Klasse Startseite
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);  // Kreis (oder horizontale Linie)
		progressDialog.setMessage(getString(R.string.s_bitte_warten));
		progressDialog.setCancelable(true);      // Abbruch durch Zuruecktaste 
		progressDialog.setIndeterminate(true);   // Unbekannte Anzahl an Bytes werden vom Web Service geliefert
		progressDialog.show();
		return progressDialog;
	}
	
		public class ProduktServiceBinder extends Binder {
		
		// Aufruf in einem eigenen Thread
		public HttpResponse<Produkt> getProduktById(Long id, final Context ctx) {
			
			// (evtl. mehrere) Parameter vom Typ "Long"
			final AsyncTask<Long, Void, HttpResponse<Produkt>> getProduktByIdTask = new AsyncTask<Long, Void, HttpResponse<Produkt>>() {

				@Override
	    		protected void onPreExecute() {
					progressDialog = showProgressDialog(ctx);
				}
				
				@Override
				// Neuer Thread, damit der UI-Thread nicht blockiert wird
				protected HttpResponse<Produkt> doInBackground(Long... ids) {
					final Long produktId = ids[0];
		    		final String path = PRODUKT_PATH + "/" + produktId;
		    		Log.v(LOG_TAG, "path = " + path);

		    		final HttpResponse<Produkt> result = WebServiceClient.getJsonSingle(path, Produkt.class);
					Log.d(LOG_TAG + ".AsyncTask", "doInBackground: " + result);
					return result;
				}
				
				@Override
	    		protected void onPostExecute(HttpResponse<Produkt> unused) {
					progressDialog.dismiss();
	    		}
			};
			
			getProduktByIdTask.execute(Long.valueOf(id));
			HttpResponse<Produkt> result = null;
	    	try {
	    		result = getProduktByIdTask.get(timeout, SECONDS);
			}
	    	catch (Exception e) {
	    		throw new InternalShopError(e.getMessage(), e);
			}
	    	
	    	return result;
		}

		
		
		/**
	 	*/
		public HttpResponse<Produkt> getProdukte(final Context ctx) {
			
			// (evtl. mehrere) Parameter vom Typ "Long"
			final AsyncTask<Long, Void, HttpResponse<Produkt>> getProdukte = new AsyncTask<Long, Void, HttpResponse<Produkt>>() {
		
				@Override
				protected void onPreExecute() {
					progressDialog = showProgressDialog(ctx);
				}
				
				@Override
				// Neuer Thread, damit der UI-Thread nicht blockiert wird
				protected HttpResponse<Produkt> doInBackground(Long... ids) {
					//final Long produktId = ids[0];
		    		final String path = PRODUKT_PATH;
		    		Log.v(LOG_TAG, "path = " + path);
		
		    		final HttpResponse<Produkt> result = WebServiceClient.getJsonList(path, Produkt.class);
					Log.d(LOG_TAG + ".AsyncTask", "doInBackground: " + result);
					return result;
				}
				
				@Override
				protected void onPostExecute(HttpResponse<Produkt> unused) {
					progressDialog.dismiss();
				}
			};
			
			getProdukte.execute();
			HttpResponse<Produkt> result = null;
			try {
				result = getProdukte.get(timeout, SECONDS);
			}
			catch (Exception e) {
				throw new InternalShopError(e.getMessage(), e);
			}
			
			return result;
		}
		
		public HttpResponse<Produkt> createProdukt(Produkt produkt, final Context ctx) {
			// (evtl. mehrere) Parameter vom Typ "Produkte", Resultat vom Typ "void"
			final AsyncTask<Produkt, Void, HttpResponse<Produkt>> createProduktTask = new AsyncTask<Produkt, Void, HttpResponse<Produkt>>() {
				@Override
	    		protected void onPreExecute() {
					progressDialog = showProgressDialog(ctx);
				}
				
				@Override
				// Neuer Thread, damit der UI-Thread nicht blockiert wird
				protected HttpResponse<Produkt> doInBackground(Produkt... produkte) {
					final Produkt produkt = produkte[0];
		    		final String path = PRODUKT_PATH;
		    		Log.v(LOG_TAG, "path = " + path);

		    		final HttpResponse<Produkt> result = mock
                                                               ? Mock.createProdukt(produkt)
                                                               : WebServiceClient.postJson(produkt, path);
		    		
					Log.d(LOG_TAG + ".AsyncTask", "doInBackground: " + result);
					return result;
				}
				
				@Override
	    		protected void onPostExecute(HttpResponse<Produkt> unused) {
					progressDialog.dismiss();
	    		}
			};
			
			createProduktTask.execute(produkt);
			HttpResponse<Produkt> response = null; 
			try {
				response = createProduktTask.get(timeout, SECONDS);
			}
	    	catch (Exception e) {
	    		throw new InternalShopError(e.getMessage(), e);
			}
			
			produkt.id = Long.valueOf(response.content);
			final HttpResponse<Produkt> result = new HttpResponse<Produkt>(response.responseCode, response.content, produkt);
			return result;
	    }
		
	}
}
