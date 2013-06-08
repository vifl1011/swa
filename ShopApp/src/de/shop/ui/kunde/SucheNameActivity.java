package de.shop.ui.kunde;

import static de.shop.util.Constants.KUNDEN_KEY;

import java.util.ArrayList;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import de.shop.data.Kunde;
import de.shop.service.KundeService;
import de.shop.service.KundeService.KundeServiceBinder;

public class SucheNameActivity extends Activity {
	private static final String LOG_TAG = SucheNameActivity.class.getSimpleName();
	
	private KundeServiceBinder kundeServiceBinder;
	
	// ServiceConnection ist ein Interface: anonyme Klasse verwenden, um ein Objekt davon zu erzeugen
	private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder serviceBinder) {
			kundeServiceBinder = (KundeServiceBinder) serviceBinder;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			kundeServiceBinder = null;
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.search);

		final Intent intent = getIntent();
		if (!Intent.ACTION_SEARCH.equals(intent.getAction())) {
			return;
		}
		
		final String query = intent.getStringExtra(SearchManager.QUERY);
		Log.d(LOG_TAG, query);
		
		suchen(query);
	}
	
	@Override
	public void onResume() {
		final Intent intent = new Intent(this, KundeService.class);
		bindService(intent, serviceConnection, BIND_AUTO_CREATE);
		super.onResume();
	}

	@Override
	public void onPause() {
		unbindService(serviceConnection);
		super.onPause();
	}
	
	private void suchen(String name) {
		final ArrayList<Kunde> kunden = kundeServiceBinder.sucheKundenByName(name);
		Log.d(LOG_TAG, kunden.toString());
		
		final Intent intent = new Intent(this, KundenListe.class);
		if (kunden != null && !kunden.isEmpty()) {
			intent.putExtra(KUNDEN_KEY, kunden);
		}
		startActivity(intent);
	}
}
