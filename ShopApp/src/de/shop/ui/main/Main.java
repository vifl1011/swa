package de.shop.ui.main;

import static android.widget.Toast.LENGTH_LONG;
import static de.shop.ui.main.Prefs.mock;
import static de.shop.util.Constants.KUNDE_KEY;
import static de.shop.util.Constants.HOST_DEFAULT;
import static de.shop.util.Constants.LOCALHOST_DEVICE;
import static de.shop.util.Constants.LOCALHOST_EMULATOR;
import static de.shop.util.Constants.DEVICE_NAME;
import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import de.shop.R;
import de.shop.data.Kunde;
import de.shop.service.BestellungService;
import de.shop.service.KundeService;
import de.shop.service.BestellungService.BestellungServiceBinder;
import de.shop.service.KundeService.KundeServiceBinder;
import de.shop.service.ProduktService;
import de.shop.service.ProduktService.ProduktServiceBinder;
import de.shop.ui.kunde.KundeDetails;

public class Main extends Activity {
	private static final String LOG_TAG = Main.class.getSimpleName();
	
	private KundeServiceBinder kundeServiceBinder;
	private BestellungServiceBinder bestellungServiceBinder;
	private ProduktServiceBinder produktServiceBinder;
	
	// ServiceConnection ist ein Interface: anonyme Klasse verwenden, um ein Objekt davon zu erzeugen
	private ServiceConnection kundeServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder serviceBinder) {
			Log.v(LOG_TAG, "onServiceConnected() fuer KundeServiceBinder");
			kundeServiceBinder = (KundeServiceBinder) serviceBinder;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			kundeServiceBinder = null;
		}
	};
	
	private ServiceConnection bestellungServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder serviceBinder) {
			Log.v(LOG_TAG, "onServiceConnected() fuer BestellungServiceBinder");
			bestellungServiceBinder = (BestellungServiceBinder) serviceBinder;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			bestellungServiceBinder = null;
		}
	};
	
	private ServiceConnection produktServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder serviceBinder) {
			Log.v(LOG_TAG, "onServiceConnected() fuer ProduktServiceBinder");
			produktServiceBinder = (ProduktServiceBinder) serviceBinder;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			produktServiceBinder = null;
		}
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        
        //Prüfe ob das Gerät innerhalb des Emulators oder des realen Gerätes läuft
        // TODO: Remove before production
		Log.i(LOG_TAG, "Checking the Running Environment...");
		String ANDROID_DEVICE_ID = "";
		ANDROID_DEVICE_ID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
		Log.i(LOG_TAG, "Shop is running on " + ANDROID_DEVICE_ID);
	    if (ANDROID_DEVICE_ID.equals(DEVICE_NAME)) {
	        HOST_DEFAULT = LOCALHOST_DEVICE;
	        Log.i(LOG_TAG, "Environment is the Tablet..");
	    }
	    else {
	    	HOST_DEFAULT = LOCALHOST_EMULATOR;
	    	Log.i(LOG_TAG, "Environment is the Emulator..");
	    }
        
        // Gibt es Suchergebnisse durch SearchView in der ActionBar, z.B. Kunde ?
        
        Fragment detailsFragment = null;
        final Bundle extras = getIntent().getExtras();
        if (extras == null) {
        	// Keine Suchergebnisse o.ae. vorhanden
        	
        	detailsFragment = new Startseite();
        	
          // Preferences laden
          Prefs.init(this);
        }
        else {
	        final Kunde kunde = (Kunde) extras.get(KUNDE_KEY);
	        if (kunde != null) {
	        	Log.d(LOG_TAG, kunde.toString());
	        	
	    		final Bundle args = new Bundle(1);
	    		args.putSerializable(KUNDE_KEY, kunde);
	    		
	        	detailsFragment = new KundeDetails();
	        	detailsFragment.setArguments(args);
	        }
        }
        
        getFragmentManager().beginTransaction()
                            .add(R.id.details, detailsFragment)
                            .commit();
        
    	if (mock) {
    		Toast.makeText(this, R.string.s_mock, LENGTH_LONG).show();
    	}
    }
    
    @Override
	public void onStart() {
		super.onStart();
		
		Intent intent = new Intent(this, KundeService.class);
		bindService(intent, kundeServiceConnection, Context.BIND_AUTO_CREATE);
		
		intent = new Intent(this, BestellungService.class);
		bindService(intent, bestellungServiceConnection, Context.BIND_AUTO_CREATE);
		
		intent = new Intent(this, ProduktService.class);
		bindService(intent, produktServiceConnection, Context.BIND_AUTO_CREATE);
    }
    
	@Override
	public void onStop() {
		super.onStop();
		
		unbindService(kundeServiceConnection);
		unbindService(bestellungServiceConnection);
		unbindService(produktServiceConnection);
	}

	public KundeServiceBinder getKundeServiceBinder() {
		return kundeServiceBinder;
	}
	
	public ProduktServiceBinder getProduktServiceBinder() {
		return produktServiceBinder;
	}

	public BestellungServiceBinder getBestellungServiceBinder() {
		return bestellungServiceBinder;
	}
}
