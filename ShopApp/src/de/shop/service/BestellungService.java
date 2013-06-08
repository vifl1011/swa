package de.shop.service;

import static de.shop.ui.main.Prefs.mock;
import static de.shop.ui.main.Prefs.timeout;
import static de.shop.util.Constants.BESTELLUNGEN_PATH;
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
import de.shop.data.Bestellung;
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
		    		final String path = BESTELLUNGEN_PATH + "/" + bestellungId;
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
	}
}
