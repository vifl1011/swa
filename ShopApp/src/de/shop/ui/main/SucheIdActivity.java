package de.shop.ui.main;

import static de.shop.util.Constants.KUNDE_KEY;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import de.shop.data.AbstractKunde;
import de.shop.service.KundeService;
import de.shop.service.KundeService.KundeServiceBinder;

public class SucheIdActivity extends Activity {
	private static final String LOG_TAG = SucheIdActivity.class.getSimpleName();
	
	private Long kundeId;
	private KundeServiceBinder kundeServiceBinder;
	
	private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder serviceBinder) {
			kundeServiceBinder = (KundeServiceBinder) serviceBinder;
			Log.d(LOG_TAG, kundeServiceBinder.toString());
			suchen();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			kundeServiceBinder = null;
		}
	};

	private void suchen() {
		Log.v(LOG_TAG, "suchen()");
		final AbstractKunde kunde = kundeServiceBinder.sucheKundeById(kundeId, this).resultObject;
		Log.d(LOG_TAG, kunde.toString());
		
		final Intent intent = new Intent(this, Main.class);
		if (kunde != null) {
			intent.putExtra(KUNDE_KEY, kunde);
		}
		startActivity(intent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(LOG_TAG, "onCreate()");
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.search);

		final Intent intent = getIntent();
		if (!Intent.ACTION_SEARCH.equals(intent.getAction())) {
			return;
		}
	
		final String kundeIdStr = intent.getStringExtra(SearchManager.QUERY);
		kundeId = Long.valueOf(kundeIdStr);
		Log.d(LOG_TAG, kundeIdStr);
	}
	
	@Override
	public void onStart() {		
		Log.v(LOG_TAG, "onStart()");
		final Intent intent = new Intent(this, KundeService.class);
		bindService(intent, serviceConnection, BIND_AUTO_CREATE);    // asynchron!
		if (kundeServiceBinder == null) {
			Log.v(LOG_TAG, "kundeServiceBinder ist noch null");
		}

		super.onStart();
	}

	@Override
	public void onStop() {
		unbindService(serviceConnection);
		super.onStop();
	}
}
