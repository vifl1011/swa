package de.shop.service;

import static de.shop.ui.main.Prefs.mock;
import static de.shop.ui.main.Prefs.timeout;
import static de.shop.util.Constants.BESTELLUNG_PATH;
import static de.shop.util.Constants.BESTELLPOSITION_PATH;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.List;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import de.shop.R;
import de.shop.data.Bestellposition;
import de.shop.data.Bestellung;
import de.shop.data.Produkt;
import de.shop.util.InternalShopError;

public class BestellungService extends Service {
	private static final String LOG_TAG = BestellungService.class.getSimpleName();

	private final BestellungServiceBinder binder = new BestellungServiceBinder();

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
	
	public class BestellungServiceBinder extends Binder {
		
		// Aufruf in einem eigenen Thread
public HttpResponse<Bestellung> getBestellungById(Long id, final Context ctx) {
			
			// (evtl. mehrere) Parameter vom Typ "Long", Resultat vom Typ "Bestellung"
			final AsyncTask<Long, Void, HttpResponse<Bestellung>> getBestellungByIdTask = new AsyncTask<Long, Void, HttpResponse<Bestellung>>() {

				@Override
	    		protected void onPreExecute() {
					progressDialog = showProgressDialog(ctx);
				}
				
				@Override
				// Neuer Thread, damit der UI-Thread nicht blockiert wird
				protected HttpResponse<Bestellung> doInBackground(Long... ids) {
					final Long bestellungId = ids[0];
		    		final String path = BESTELLUNG_PATH + "/" + bestellungId;
		    		Log.v(LOG_TAG, "path = " + path);

		    		final HttpResponse<Bestellung> result = mock
		    				                                ? Mock.sucheBestellungById(bestellungId)
		    				                                : WebServiceClient.getJsonSingle(path, Bestellung.class);
					Log.d(LOG_TAG + ".AsyncTask", "doInBackground: " + result);
					return result;
				}
				
				@Override
	    		protected void onPostExecute(HttpResponse<Bestellung> unused) {
					progressDialog.dismiss();
	    		}
			};
			
			getBestellungByIdTask.execute(Long.valueOf(id));
			HttpResponse<Bestellung> result = null;
	    	try {
	    		result = getBestellungByIdTask.get(timeout, SECONDS);
			}
	    	catch (Exception e) {
	    		throw new InternalShopError(e.getMessage(), e);
			}
	    	
	    	return result;
		}

		/**
	 	*/
		public HttpResponse<Bestellposition> getBestellpositionById(Long id, final Context ctx) {
			
			// (evtl. mehrere) Parameter vom Typ "Long", Resultat vom Typ "Bestellung"
			final AsyncTask<Long, Void, HttpResponse<Bestellposition>> getBestellpositionByIdTask = new AsyncTask<Long, Void, HttpResponse<Bestellposition>>() {
		
				@Override
				protected void onPreExecute() {
					progressDialog = showProgressDialog(ctx);
				}
				
				@Override
				// Neuer Thread, damit der UI-Thread nicht blockiert wird
				protected HttpResponse<Bestellposition> doInBackground(Long... ids) {
					final Long bestellpositionId = ids[0];
		    		final String path = BESTELLPOSITION_PATH + "/" + bestellpositionId;
		    		Log.v(LOG_TAG, "path = " + path);
		
		    		final HttpResponse<Bestellposition> result = mock
		    				                                ? Mock.sucheBestellpositionById(bestellpositionId)
		    				                                : WebServiceClient.getJsonSingle(path, Bestellposition.class);
					Log.d(LOG_TAG + ".AsyncTask", "doInBackground: " + result);
					return result;
				}
				
				@Override
				protected void onPostExecute(HttpResponse<Bestellposition> unused) {
					progressDialog.dismiss();
				}
			};
			
			getBestellpositionByIdTask.execute(Long.valueOf(id));
			HttpResponse<Bestellposition> result = null;
			try {
				result = getBestellpositionByIdTask.get(timeout, SECONDS);
			}
			catch (Exception e) {
				throw new InternalShopError(e.getMessage(), e);
			}
			
			return result;
		}
		
		
		/**
	 	*/
		public HttpResponse<Produkt> getProduktByBestellposition(Bestellposition bp, final Context ctx) {
			
			// (evtl. mehrere) Parameter vom Typ "Long", Resultat vom Typ "Bestellung"
			final AsyncTask<Long, Void, HttpResponse<Produkt>> getBestellpositionByIdTask = new AsyncTask<Long, Void, HttpResponse<Produkt>>() {
		
				@Override
				protected void onPreExecute() {
					progressDialog = showProgressDialog(ctx);
				}
				
				@Override
				// Neuer Thread, damit der UI-Thread nicht blockiert wird
				protected HttpResponse<Produkt> doInBackground(Long... ids) {
					final Long bestellpositionId = ids[0];
		    		final String path = BESTELLPOSITION_PATH + "/" + bestellpositionId + "/produkt";
		    		Log.v(LOG_TAG, "path = " + path);
		
		    		//	TODO Mock option ist noch nicht angepasst
		    		final HttpResponse<Produkt> result = mock
		    				                                ? Mock.sucheProduktByBestellposition(bestellpositionId)
		    				                                : WebServiceClient.getJsonSingle(path, Produkt.class);
					Log.d(LOG_TAG + ".AsyncTask", "doInBackground: " + result);
					return result;
				}
				
				@Override
				protected void onPostExecute(HttpResponse<Produkt> unused) {
					progressDialog.dismiss();
				}
			};
			
			getBestellpositionByIdTask.execute(Long.valueOf(bp.id));
			HttpResponse<Produkt> result = null;
			try {
				result = getBestellpositionByIdTask.get(timeout, SECONDS);
			}
			catch (Exception e) {
				throw new InternalShopError(e.getMessage(), e);
			}
			
			return result;
		}
		
		/**
		 */
		public List<Long> sucheBestellpositionenIdsByBestellungId(Long id, final Context ctx) {
			// (evtl. mehrere) Parameter vom Typ "Long", Resultat vom Typ "List<Long>"
			final AsyncTask<Long, Void, List<Long>> sucheBestellpositionenIdsByBestellungIdTask = new AsyncTask<Long, Void, List<Long>>() {
				@Override
				// Neuer Thread, damit der UI-Thread nicht blockiert wird
				protected List<Long> doInBackground(Long... ids) {
					final Long id = ids[0];
		    		final String path = BESTELLUNG_PATH + "/" + id + "/bestellpositionen";
		    		Log.v(LOG_TAG, "path = " + path);
		    		final List<Long> result = mock
		    				                  ? Mock.sucheBestellungenIdsByKundeId(id)
		    				                  : WebServiceClient.getJsonLongList(path);
			    	Log.d(LOG_TAG + ".AsyncTask", "doInBackground: " + result);
					return result;
				}
			};
			
			sucheBestellpositionenIdsByBestellungIdTask.execute(id);
			List<Long> bestellpositionenIds = null;
			try {
				bestellpositionenIds = sucheBestellpositionenIdsByBestellungIdTask.get(timeout, SECONDS);
			}
	    	catch (Exception e) {
	    		throw new InternalShopError(e.getMessage(), e);
			}
	
			return bestellpositionenIds;
	    }
	}
}
