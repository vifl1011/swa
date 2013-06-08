package de.shop.service;

import static android.widget.Toast.LENGTH_LONG;

import java.util.concurrent.TimeUnit;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import de.shop.R;

public class NetworkService extends Service {
	private enum ConnectionType { WIFI, MOBILE };
	
	private static final String LOG_TAG = NetworkService.class.getSimpleName();
	
	private final NetworkServiceBinder binder = new NetworkServiceBinder();

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}
	
	public class NetworkServiceBinder extends Binder {
		// Aufruf in einem eigenen Thread
		public void checkNetworkConnection(final Context ctx) {
			final AsyncTask<Void, Void, ConnectionType> checkConnectionTask = new AsyncTask<Void, Void, ConnectionType>() {
				@Override
				// Neuer Thread, damit der UI-Thread nicht blockiert wird
				protected ConnectionType doInBackground(Void... unused) {
					final ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
					
					NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI); 
					if (networkInfo != null && networkInfo.isConnected()) {
						return ConnectionType.WIFI;
					}
					
					networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE); 
					if (networkInfo != null && networkInfo.isConnected()) {
						return ConnectionType.MOBILE;
					}
					
					return null;
				}
				
				@Override
	    		protected void onPostExecute(ConnectionType connectionType) {
					switch (connectionType) {
						case WIFI:
							Toast.makeText(ctx, R.string.s_wifi, LENGTH_LONG).show();
							break;
							
						case MOBILE:
							Toast.makeText(ctx, R.string.s_mobile, LENGTH_LONG).show();
							break;
							
						default:
							Toast.makeText(ctx, R.string.s_not_connected, LENGTH_LONG).show();
							break;
					}
					
	    		}
			};
			
			checkConnectionTask.execute();
	    	try {
	    		checkConnectionTask.get(3L, TimeUnit.SECONDS);
			}
	    	catch (Exception e) {
	    		Log.e(LOG_TAG, e.getMessage(), e);
	    		Toast.makeText(NetworkService.this, R.string.s_mock, LENGTH_LONG).show();
			}
		}
	}
}
