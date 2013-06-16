package de.shop.ui.produkt;

import static de.shop.util.Constants.PRODUKTE_KEY;
import static de.shop.util.Constants.PRODUKT_KEY;

import java.util.List;

import de.shop.R;
import de.shop.data.Produkt;
import de.shop.service.BestellungService;
import de.shop.service.BestellungService.BestellungServiceBinder;
import de.shop.service.ProduktService;
import de.shop.service.ProduktService.ProduktServiceBinder;
import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

public class ProduktActivity extends Activity {
	private ProduktServiceBinder produktServiceBinder;
	private BestellungServiceBinder bestellungServiceBinder;
	
	// ServiceConnection ist ein Interface: anonyme Klasse verwenden, um ein Objekt davon zu erzeugen
	private ServiceConnection produktServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder serviceBinder) {
			produktServiceBinder = (ProduktServiceBinder) serviceBinder;
		}
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			produktServiceBinder = null;
		}
	};
	
	// ServiceConnection ist ein Interface: anonyme Klasse verwenden, um ein Objekt davon zu erzeugen
	private ServiceConnection bestellungServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder serviceBinder) {
			bestellungServiceBinder = (BestellungServiceBinder) serviceBinder;
		}
			
		@Override
		public void onServiceDisconnected(ComponentName name) {
			bestellungServiceBinder = null;
		}
	};
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.produkte);
        
        final Fragment details = new ProduktDetails();
		final Bundle extras = getIntent().getExtras();
		
		if (extras != null) {
        	@SuppressWarnings("unchecked")
			final List<Produkt> produkte = (List<Produkt>) extras.get(PRODUKTE_KEY);
        	if (produkte != null && !produkte.isEmpty()) {
        		final Bundle args = new Bundle(1);
        		args.putSerializable(PRODUKT_KEY, produkte.get(0));
        		details.setArguments(args);
        	}
        }
		
        getFragmentManager().beginTransaction()
                            .add(R.id.details, details)
                            .commit();
    }
	
	@Override
	public void onStart() {
		super.onStart();
		
		Intent intent = new Intent(this, ProduktService.class);
		bindService(intent, produktServiceConnection, Context.BIND_AUTO_CREATE);
		
		intent = new Intent(this, BestellungService.class);
		bindService(intent, bestellungServiceConnection, Context.BIND_AUTO_CREATE);
    }
	
	@Override
	public void onStop() {
		super.onStop();
		
		unbindService(produktServiceConnection);
		unbindService(bestellungServiceConnection);
	}
	
	public ProduktServiceBinder getProduktServiceBinder() {
		return produktServiceBinder;
	}

	public BestellungServiceBinder getBestellungServiceBinder() {
		return bestellungServiceBinder;
	}
}
