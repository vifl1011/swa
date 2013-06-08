package de.shop.ui.bestellung;

import static de.shop.util.Constants.BESTELLUNG_KEY;
import android.app.Activity;
import android.app.Fragment;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import de.shop.R;
import de.shop.data.Bestellung;
import de.shop.ui.kunde.KundeEdit;
import de.shop.ui.main.Prefs;
import de.shop.util.WischenListener;

public class BestellungStammdaten extends Fragment {
	private static final String LOG_TAG = BestellungStammdaten.class.getSimpleName();
	
	private Bundle args;
	private Bestellung bestellung;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		args = getArguments();
		bestellung = (Bestellung) args.get(BESTELLUNG_KEY);
        Log.d(LOG_TAG, bestellung.toString());

        // Voraussetzung fuer onOptionsItemSelected()
        setHasOptionsMenu(true);
        
		// attachToRoot = false, weil die Verwaltung des Fragments durch die Activity erfolgt
		return inflater.inflate(R.layout.bestellung_stammdaten, container, false);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		fillValues(view);
    	
	    final GestureDetector gestureDetector = new GestureDetector(getActivity(), new WischenListener(this));
	    view.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return gestureDetector.onTouchEvent(event);
			}
		});
    }
	
	private void fillValues(View view) {
		final TextView txtId = (TextView) view.findViewById(R.id.bestellung_id_txt);
    	txtId.setText(bestellung.id.toString());
    	
    	final TextView txtStatus = (TextView) view.findViewById(R.id.bestellstatus_txt);
    	txtStatus.setText(bestellung.bestellstatus);
    	
    	final TextView txtGezahlt = (TextView) view.findViewById(R.id.bezahlt_txt);
    	txtGezahlt.setText(bestellung.gezahlt);
    	
    	final TextView txtPreis = (TextView) view.findViewById(R.id.gesamtpreis_txt);
    	txtPreis.setText(String.valueOf(bestellung.gesamtpreis));
    	
    	final TextView txtBestellzeitpunkt = (TextView) view.findViewById(R.id.ort);
    	txtBestellzeitpunkt.setText(bestellung.bestellzeitpunkt.toString());
    	
    	final TextView txtAktualisiert = (TextView) view.findViewById(R.id.aktualisiert_txt);
    	txtAktualisiert.setText(bestellung.aktualisiert.toString());
    	
    	final TextView txtErzeugt = (TextView) view.findViewById(R.id.erzeugt_txt);
    	txtErzeugt.setText(bestellung.erzeugt.toString());
    }

	@Override
	// http://developer.android.com/guide/topics/ui/actionbar.html#ChoosingActionItems :
	// "As a general rule, all items in the options menu (let alone action items) should have a global impact on the app,
	//  rather than affect only a small portion of the interface."
	// Nur aufgerufen, falls setHasOptionsMenu(true) in onCreateView() aufgerufen wird
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.kunde_stammdaten_options, menu); //TODO entsprechendes Pendant fehlts
		
		// "Searchable Configuration" in res\xml\searchable.xml wird der SearchView zugeordnet
		final Activity activity = getActivity();
	    final SearchManager searchManager = (SearchManager) activity.getSystemService(Context.SEARCH_SERVICE);
	    final SearchView searchView = (SearchView) menu.findItem(R.id.suchen).getActionView();
	    searchView.setSearchableInfo(searchManager.getSearchableInfo(activity.getComponentName()));
	}

	//	TODO auch folgende Methode muss angepasst werden
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.edit:
				// Evtl. vorhandene Tabs der ACTIVITY loeschen
		    	getActivity().getActionBar().removeAllTabs();
		    	
				final Fragment neuesFragment = new KundeEdit();
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
}
