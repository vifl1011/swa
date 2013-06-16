package de.shop.ui.produkt;

import static de.shop.util.Constants.PRODUKT_KEY;
import static de.shop.util.Constants.MAX_KATEGORIE;
import static de.shop.util.Constants.MIN_KATEGORIE;
import static java.net.HttpURLConnection.HTTP_CONFLICT;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;

import java.util.GregorianCalendar;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.ToggleButton;
import de.shop.R;
import de.shop.data.Produkt;
import de.shop.service.HttpResponse;
import de.shop.service.ProduktService.ProduktServiceBinder;
import de.shop.ui.main.Main;
import de.shop.ui.main.Prefs;

public class ProduktCreate extends Fragment {
	private static final String LOG_TAG = ProduktCreate.class.getSimpleName();
	
	private Bundle args;
	
	private Produkt produkt;
	private EditText createBezeichnung;
	private EditText createPreis;
	private EditText createFarbe;
	private EditText createGroesse;
	private EditText createVorrat;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		args = getArguments();
		Log.d(LOG_TAG, "createProduct view...");
		// Voraussetzung fuer onOptionsItemSelected()
		setHasOptionsMenu(true);
		
		// attachToRoot = false, weil die Verwaltung des Fragments durch die Activity erfolgt
		return inflater.inflate(R.layout.produkt_create, container, false);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
    	
		
		final TextView txtId = (TextView) view.findViewById(R.id.produkt_id);
    	
    	createBezeichnung = (EditText) view.findViewById(R.id.bezeichnung_create);
    	
    	createPreis = (EditText) view.findViewById(R.id.preis_create);
    	
    	createFarbe = (EditText) view.findViewById(R.id.farbe_create);
    	
    	createGroesse = (EditText) view.findViewById(R.id.groesse_create);
    	
    	createVorrat = (EditText) view.findViewById(R.id.vorrat_create);
    	
    }
    
	@Override
	// Nur aufgerufen, falls setHasOptionsMenu(true) in onCreateView() aufgerufen wird
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.produkt_edit_options, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.speichern:
				createProdukt();

				final Activity activity = getActivity();
				
				// Das Fragment ProduktEdit kann von Main und von ProduktListe aus aufgerufen werden
				ProduktServiceBinder produktServiceBinder;
				if (Main.class.equals(activity.getClass())) {
					Main main = (Main) activity;
					produktServiceBinder = main.getProduktServiceBinder();
				}
				else if (ProduktActivity.class.equals(activity.getClass())) {
					ProduktActivity produktActivity = (ProduktActivity) activity;
					produktServiceBinder = produktActivity.getProduktServiceBinder();
				}
				else {
					return true;
				}
				
				final HttpResponse<Produkt> result = produktServiceBinder.createProdukt( produkt, activity);
				final int statuscode = result.responseCode;
				if (statuscode != HTTP_NO_CONTENT && statuscode != HTTP_OK) {
					String msg = null;
					switch (statuscode) {
						case HTTP_CONFLICT:
							msg = result.content;
							break;
						case HTTP_UNAUTHORIZED:
							msg = getString(R.string.s_error_prefs_login, produkt.id);
							break;
						case HTTP_FORBIDDEN:
							msg = getString(R.string.s_error_forbidden, produkt.id);
							break;
					}
					
		    		final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		    		final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {}
                    };
		    		builder.setMessage(msg)
		    		       .setNeutralButton(R.string.s_ok, listener)
		    		       .create()
		    		       .show();
		    		return true;
				}
				
				produkt = result.resultObject;  // ggf. erhoehte Versionsnr. bzgl. konkurrierender Updates
				
				final Fragment neuesFragment = new ProduktDetails();
				neuesFragment.setArguments(args);
				
				// Kein Name (null) fuer die Transaktion, da die Klasse BackStageEntry nicht verwendet wird
				getFragmentManager().beginTransaction()
				                    .replace(R.id.details, neuesFragment)
				                    .addToBackStack(null)  
				                    .commit();
				return true;
				
			case R.id.einstellungen:
				getFragmentManager().beginTransaction()
                                    .replace(R.id.details, new Prefs())
                                    .addToBackStack(null)
                                    .commit();
				return true;
				
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	private void createProdukt() {
		produkt = new Produkt();
		produkt.bezeichnung = createBezeichnung.getText().toString();
		produkt.preis = Float.valueOf(createPreis.getText().toString());
		produkt.farbe = createFarbe.getText().toString();
		produkt.groesse = createGroesse.getText().toString();
		produkt.vorrat = Integer.valueOf(createVorrat.getText().toString());

		Log.d(LOG_TAG, produkt.toString());
	}
}
