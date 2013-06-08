package de.shop.ui.bestellung;

import static de.shop.util.Constants.BESTELLUNG_KEY;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import de.shop.R;
import de.shop.data.Bestellposition;
import de.shop.data.Kunde;
import de.shop.data.Bestellung;
import de.shop.service.BestellungService.BestellungServiceBinder;
import de.shop.service.KundeService.KundeServiceBinder;
import de.shop.ui.main.Main;
import de.shop.ui.main.Prefs;
import de.shop.util.WischenListener;

public class BestellungBestellpositionen extends Fragment {
	private static final String LOG_TAG = BestellungBestellpositionen.class.getSimpleName();
	
	private Bestellung bestellung;
	private List<Long> bestellpositionenIds;
	private List<Bestellposition> bestellpositionen;
	private Bestellposition bestellposition;
	private int bestellpositionenListePos;
	
	private TextView txtBestellpositionId;
	private TextView txtBestellpositionDatum;
	
	private KundeServiceBinder kundeServiceBinder;
	private BestellungServiceBinder bestellungServiceBinder;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		bestellung = (Bestellung) getArguments().get(BESTELLUNG_KEY);
        
        setHasOptionsMenu(true);
        
        // attachToRoot = false, weil die Verwaltung des Fragments durch die Activity erfolgt
		return inflater.inflate(R.layout.bestellung_bestellpositionen, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// View-IDs fuer die Textfelder einer Bestellung des aktuellen Kunden
		final TextView bestellungTxt = (TextView) view.findViewById(R.id.bestellpositionen_bestellung_id);
		bestellungTxt.setText(getString(R.string.b_bestellposition_bestellung_id, bestellung.id));
		txtBestellpositionId = (TextView) view.findViewById(R.id.bestellposition_id);
		txtBestellpositionDatum = (TextView) view.findViewById(R.id.bestellzeitpunkt);
		
		final Activity activity = getActivity();
		if (Main.class.equals(activity.getClass())) {		//Falls wir uns in MAIN befinden werden die Services generiert
			Main main = (Main) activity;
			kundeServiceBinder = main.getKundeServiceBinder();
			bestellungServiceBinder = main.getBestellungServiceBinder();
		}
		else {
			Log.e(LOG_TAG, "Activity " + activity.getClass().getSimpleName() + " nicht beruecksichtigt.");
			return;
		}
		
		//TODO entsprechende Methode für bestellungServiceBinder erstellen
		bestellpositionenIds = bestellungServiceBinder.sucheBestellpositionenIdsByBestellungId(bestellung.id, view.getContext());
		if (bestellpositionenIds == null || bestellpositionenIds.isEmpty()) {
			bestellungTxt.setText(getString(R.string.b_keine_bestellpositionen, bestellung.id));
		}
		else {
			// ListView mit den IDs der Bestellungen aufbauen
			final ListView listView = (ListView) view.findViewById(R.id.bestellpositionen_liste);
	        int anzahl = bestellpositionenIds.size();
	        bestellpositionen = new ArrayList<Bestellposition>(anzahl);
			final String[] values = new String[anzahl];
			for (int i = 0; i < anzahl; i++) {
				bestellpositionen.add(null);
	        	values[i] = getString(R.string.b_bestellung_bestellposition_id, bestellpositionenIds.get(anzahl - i - 1));
	        	Log.d(LOG_TAG, values[i]);
	        }
	        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(view.getContext(),
	        		                                                      android.R.layout.simple_list_item_1,
	        		                                                      android.R.id.text1,
	        		                                                      values);
			listView.setAdapter(adapter);
			activateBestellposition(0, view);                  // Die neueste Bestellung visualisieren
			setItemClickListenerBestellposition(listView);   // Bestellungen in der Liste duerfen angeklickt werden
		}
		
		setTouchListenerTab(view);
	}
	
	private void setItemClickListenerBestellposition(ListView listView) {
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int itemPosition, long itemId) {
				// view: TextView innerhalb von ListFragment
				// itemPosition: Textposition innerhalb der Liste mit Zaehlung ab 0
				// itemId = itemPosition bei String-Arrays bzw. = Primaerschluessel bei Listen aus einer DB
				
				// Bestellung ermitteln bzw. per Web Service nachladen
				activateBestellposition(itemPosition, view);
			}
		});
	}

	private void setTouchListenerTab(View view) {
	    final GestureDetector gestureDetector = new GestureDetector(getActivity(), new WischenListener(this));
	    view.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View tab, MotionEvent event) {
				return gestureDetector.onTouchEvent(event);
			}
		});
	}
	
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    	super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main, menu);
    }
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
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

	private void activateBestellposition(int itemPosition, View view) {
		// Bestellposition-ID ermitteln
		bestellpositionenListePos = bestellpositionenIds.size() - itemPosition - 1;
		
		// Bestellposition ermitteln bzw. per Web Service nachladen
		bestellposition = bestellpositionen.get(bestellpositionenListePos);
		if (bestellposition == null) {
			final Long bestellpositionId = bestellpositionenIds.get(bestellpositionenListePos);
			Log.v(LOG_TAG, "Bestellposition nachladen: " + bestellpositionId);
			
			bestellposition = bestellungServiceBinder.getBestellpositionById(bestellpositionId, view.getContext()).resultObject;
		}
		else {
			Log.v(LOG_TAG, "Bereits geladene Bestellposition: " + bestellposition);
			
			//TODO Werte der geladenen Bestellung visualisieren
		}
		
		txtBestellpositionId.setText(String.valueOf(bestellposition.id));
		final String datumStr = bestellposition.aktualisiert == null ? "" : DateFormat.getDateFormat(getActivity()).format(bestellposition.aktualisiert);
    	txtBestellpositionDatum.setText(datumStr);
	}
}
