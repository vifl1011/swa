package de.shop.ui.kunde;

import static de.shop.util.Constants.KUNDE_KEY;
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
import de.shop.data.Adresse;
import de.shop.data.GeschlechtType;
import de.shop.data.Kunde;
import de.shop.service.HttpResponse;
import de.shop.service.KundeService.KundeServiceBinder;
import de.shop.ui.main.Main;
import de.shop.ui.main.Prefs;

public class KundeCreate extends Fragment {
	private static final String LOG_TAG = KundeCreate.class.getSimpleName();
	
	private Bundle args;
	
	private Kunde kunde;
	private EditText createNachname;
	private EditText createVorname;
	private EditText createEmail;
	private EditText createPlz;
	private EditText createOrt;
	private EditText createStrasse;
	private EditText createHausnr;
	private EditText createLogin;
	private EditText createPasswort;

	private RadioButton rbMaennlich;
	private RadioButton rbWeiblich;
	
	private ToggleButton tglAGBs;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		args = getArguments();
		//kunde = (Kunde) args.get(KUNDE_KEY);
/*		
		kunde = new Kunde();
		Log.d(LOG_TAG, kunde.toString());
 */       
		// Voraussetzung fuer onOptionsItemSelected()
		setHasOptionsMenu(true);
		
		// attachToRoot = false, weil die Verwaltung des Fragments durch die Activity erfolgt
		return inflater.inflate(R.layout.kunde_create, container, false);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
    	
		
		final TextView txtId = (TextView) view.findViewById(R.id.kunde_id);
    	//txtId.setText(String.valueOf(kunde.id));
    	
		createPasswort = (EditText) view.findViewById(R.id.passwort_create);
    	//createLogin.setText(kunde.login);
    	
    	
    	createLogin = (EditText) view.findViewById(R.id.login_create);
    	//createLogin.setText(kunde.login);
    	
    	createNachname = (EditText) view.findViewById(R.id.nachname_create);
    	//createNachname.setText(kunde.nachname);
    	
    	createVorname = (EditText) view.findViewById(R.id.vorname_create);
    	//createVorname.setText(kunde.vorname);
    	
    	createEmail = (EditText) view.findViewById(R.id.email_create);
    	//createEmail.setText(kunde.email);
    	
    	createPlz = (EditText) view.findViewById(R.id.plz_create);
    	//createPlz.setText(kunde.adresse.plz);
    	
    	createOrt = (EditText) view.findViewById(R.id.ort_create);
    	//createOrt.setText(kunde.adresse.ort);
    	
    	createStrasse = (EditText) view.findViewById(R.id.strasse_create);
    	//createStrasse.setText(kunde.adresse.strasse);
    	
    	createHausnr = (EditText) view.findViewById(R.id.hausnr_create);
    	//createHausnr.setText(kunde.adresse.hausnr);
    	
    	
    	rbMaennlich = (RadioButton) view.findViewById(R.id.maennlich);
    	rbWeiblich = (RadioButton) view.findViewById(R.id.weiblich);
    	
    	tglAGBs =(ToggleButton) view.findViewById(R.id.agb_tgl);
    	/*
    	if (kunde.geschlecht != null) {
	    	switch (kunde.geschlecht) {
		    	case MAENNLICH:
		        	rbMaennlich.setChecked(true);
			    	break;
			    	
		    	case WEIBLICH:
		        	rbWeiblich.setChecked(true);
			    	break;
			    	
			    default:
	    	}
	    	
    	}
    	*/
    }
    
	@Override
	// Nur aufgerufen, falls setHasOptionsMenu(true) in onCreateView() aufgerufen wird
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.kunde_edit_options, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.speichern:
				createKunde();

				final Activity activity = getActivity();
				
				// Das Fragment KundeEdit kann von Main und von KundeListe aus aufgerufen werden
				KundeServiceBinder kundeServiceBinder;
				if (Main.class.equals(activity.getClass())) {
					Main main = (Main) activity;
					kundeServiceBinder = main.getKundeServiceBinder();
				}
				else if (KundenListe.class.equals(activity.getClass())) {
					KundenListe kundenListe = (KundenListe) activity;
					kundeServiceBinder = kundenListe.getKundeServiceBinder();
				}
				else {
					return true;
				}
				
				final HttpResponse<Kunde> result = kundeServiceBinder.createKunde( kunde, activity);
				final int statuscode = result.responseCode;
				if (statuscode != HTTP_NO_CONTENT && statuscode != HTTP_OK) {
					String msg = null;
					switch (statuscode) {
						case HTTP_CONFLICT:
							msg = result.content;
							break;
						case HTTP_UNAUTHORIZED:
							msg = getString(R.string.s_error_prefs_login, kunde.id);
							break;
						case HTTP_FORBIDDEN:
							msg = getString(R.string.s_error_forbidden, kunde.id);
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
				
				kunde = result.resultObject;  // ggf. erhoehte Versionsnr. bzgl. konkurrierender Updates
				
				// Gibt es in der Navigationsleiste eine KundenListe? Wenn ja: Refresh mit geaendertem Kunde-Objekt
				final Fragment fragment = getFragmentManager().findFragmentById(R.id.kunden_liste_nav);
				if (fragment != null) {
					final KundenListeNav kundenListeFragment = (KundenListeNav) fragment;
					kundenListeFragment.refresh(kunde);
				}
				
				final Fragment neuesFragment = new KundeDetails();
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
	
	private void createKunde() {
		kunde=new Kunde();
		kunde.adresse=new Adresse();
		kunde.passwort = createPasswort.getText().toString();
		kunde.login = createLogin.getText().toString();
		kunde.nachname = createNachname.getText().toString();
		kunde.vorname = createVorname.getText().toString();
		kunde.email = createEmail.getText().toString();
		kunde.adresse.plz = createPlz.getText().toString();
		kunde.adresse.ort = createOrt.getText().toString();
		kunde.adresse.strasse = createStrasse.getText().toString();
		kunde.adresse.hausnr = createHausnr.getText().toString();
		if (rbMaennlich.isChecked()) {
			kunde.geschlecht = GeschlechtType.MAENNLICH;
		} else if (rbWeiblich.isChecked()) {
			kunde.geschlecht = GeschlechtType.WEIBLICH;
		}
		kunde.agbAkzeptiert = tglAGBs.isChecked();
/*
		final GregorianCalendar cal = new GregorianCalendar(Locale.getDefault());
		cal.set(dpSeit.getYear(), dpSeit.getMonth(), dpSeit.getDayOfMonth());
		kunde.seit = cal.getTime();
		
		kunde.kategorie = (short) npKategorie.getValue();
		
		kunde.newsletter = tglNewsletter.isChecked();	
*/
		Log.d(LOG_TAG, kunde.toString());
	}
}
