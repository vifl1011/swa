package de.shop.ui.produkt;

import static de.shop.util.Constants.BESTELLUNG_KEY;
import static de.shop.util.Constants.PRODUKTE_KEY;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
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
import de.shop.data.Produkt;
import de.shop.service.HttpResponse;
import de.shop.service.BestellungService.BestellungServiceBinder;
import de.shop.service.KundeService.KundeServiceBinder;
import de.shop.service.ProduktService.ProduktServiceBinder;
import de.shop.ui.main.Main;
import de.shop.ui.main.Prefs;
import de.shop.util.WischenListener;

public class ListProdukte extends Fragment {
	private static final String LOG_TAG = ListProdukte.class.getSimpleName();
	
	private Produkt produkt;
	private List<Long> produktIds;
	private List<Produkt> produkte;
	private int produkteListePos;
	
	private TextView txtProduktId;
	private TextView txtProduktAktualisiert;
	private TextView txtProduktBezeichnung;
	private TextView txtProduktFarbe;
	private TextView txtProduktVorrat;
	private TextView txtProduktPreis;
	
	private ProduktServiceBinder produktServiceBinder;
	private BestellungServiceBinder bestellungServiceBinder;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//produkte = (List<Produkt>) getArguments().get(PRODUKTE_KEY);
        
        setHasOptionsMenu(true);
        
        // attachToRoot = false, weil die Verwaltung des Fragments durch die Activity erfolgt
		return inflater.inflate(R.layout.list_produkte, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// View-IDs fuer die Textfelder einer Bestellung der aktuellen Bestellposition
		final TextView produktTxt = (TextView) view.findViewById(R.id.produkte_id);
		produktTxt.setText(getString(R.string.s_nav_produkte));
		
		txtProduktId = (TextView) view.findViewById(R.id.list_product_id);
		txtProduktAktualisiert = (TextView) view.findViewById(R.id.list_produkt_aktualisiert);
		txtProduktBezeichnung = (TextView) view.findViewById(R.id.list_produkt_bez);
		txtProduktFarbe = (TextView) view.findViewById(R.id.list_produkt_farbe);
		txtProduktVorrat = (TextView) view.findViewById(R.id.list_produkt_vorrat);
		txtProduktPreis = (TextView) view.findViewById(R.id.list_produkt_preis);
		
		final Activity activity = getActivity();
		if (Main.class.equals(activity.getClass())) {		//Falls wir uns in MAIN befinden werden die Services generiert
			Main main = (Main) activity;
			produktServiceBinder = main.getProduktServiceBinder();
			bestellungServiceBinder = main.getBestellungServiceBinder();
			Log.e(LOG_TAG, "Activity " + activity.getClass().getSimpleName() + " ==> Service Binder wurden neu generiert.");
		}
		else {
			Log.e(LOG_TAG, "Activity " + activity.getClass().getSimpleName() + " nicht beruecksichtigt.");
			return;
		}
		
		//lade Produkte
				Log.e(LOG_TAG, "suche Produkt...");
				sucheProdukte(view);
		
		Log.d(LOG_TAG, "Anzahl gefundener Produkte: " + produkte.size());
		
		//bestellpositionenIds = bestellungServiceBinder.sucheBestellpositionenIdsByBestellungId(bestellung.id, view.getContext());
		if (produkte == null || produkte.isEmpty()) {
			produktTxt.setText(getString(R.string.b_keine_bestellpositionen)); //	TODO msg anpassen
		}
		else {
			// ListView mit den IDs der Bestellungen aufbauen
			final ListView listView = (ListView) view.findViewById(R.id.produkte_liste);
			Log.d(LOG_TAG, "View: " + listView);
	        int anzahl =  produkte.size();
	        Log.d(LOG_TAG, "Anzahl: " + anzahl);
	        //bestellpositionen = new ArrayList<Bestellposition>(anzahl);
			final String[] values = new String[anzahl];
			for (int i = 0; i < anzahl; i++) {
				//bestellpositionen.add(null);
	        	values[i] = produkte.get(anzahl - i - 1).id.toString() + ": " + produkte.get(anzahl - i - 1).bezeichnung;
	        	Log.d(LOG_TAG, values[i]);
	        }
	        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(view.getContext(),
	        		                                                      android.R.layout.simple_list_item_1,
	        		                                                      android.R.id.text1,
	        		                                                      values);
			listView.setAdapter(adapter);
			activateProdukt(0, view);                  	// Das neuste Produkt visualisieren
			setItemClickListenerProdukt(listView);   	// Produkte in der Liste duerfen angeklickt werden
		}
		
		setTouchListenerTab(view);
	}
	
	private void setItemClickListenerProdukt(ListView listView) {
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int itemPosition, long itemId) {
				// view: TextView innerhalb von ListFragment
				// itemPosition: Textposition innerhalb der Liste mit Zaehlung ab 0
				// itemId = itemPosition bei String-Arrays bzw. = Primaerschluessel bei Listen aus einer DB
				
				// Produkt ermitteln bzw. per Web Service nachladen
				activateProdukt(itemPosition, view);
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

	private void activateProdukt(int itemPosition, View view) {		
		// Bestellposition-ID ermitteln
		produkteListePos = produkte.size() - itemPosition - 1;
		
		//Produkt nachladen
//		try {
//			if (produkte.get(produkteListePos) == null) {
//				Produkt prod = bestellungServiceBinder.getProduktByBestellposition(bestellung.bestellpositionen
//																				.get(bestellpositionenListePos), view.getContext())
//																				.resultObject;
//				bestellung.bestellpositionen.get(bestellpositionenListePos).produkt = prod;
//			}
//		} catch (Exception e) {
//			//	TODO anzeigen das die Bestellposition nicht geladen werden konnte
//		}
		
		// Produkt ermitteln bzw. per Web Service nachladen
		produkt = produkte.get(produkteListePos);
		if (produkt == null) {
			final Long produktId = produktIds.get(produkteListePos);
			Log.v(LOG_TAG, "Bestellposition nachladen: " + produktId);
			
			produkt = produktServiceBinder.getProduktById(produktId, view.getContext()).resultObject;
		}
		else {
			Log.v(LOG_TAG, "Bereits geladenes Produkt: " + produkt);
			
			//TODO Werte der geladenen Bestellung visualisieren
		}
		fillValues(view, produkteListePos);
//		txtBestellpositionId.setText(String.valueOf(bestellposition.id));
//		final String datumStr = bestellposition.aktualisiert == null ? "" : DateFormat.getDateFormat(getActivity()).format(bestellposition.aktualisiert);
//    	txtBestellpositionDatum.setText(datumStr);
	}
	
	private void fillValues(View view, int pos) {
		Log.v(LOG_TAG, "start fillValues...");

		txtProduktId.setText(produkte.get(pos).id.toString());

    	//txtProduktAktualisiert.setText(produkte.get(pos).aktualisiert.toString());

    	txtProduktBezeichnung.setText(produkte.get(pos).bezeichnung);

    	txtProduktFarbe.setText(produkte.get(pos).farbe);

    	txtProduktVorrat.setText(String.valueOf(produkte.get(pos).vorrat));

    	txtProduktPreis.setText(String.valueOf(produkte.get(pos).preis));
    	
    	Log.v(LOG_TAG, "end fillValues");
    }
	
	private void sucheProdukte(View view) {
		final Context ctx = view.getContext();
		
//		final String nachname = nachnameTxt.getText().toString();
//		if (TextUtils.isEmpty(nachname)) {
//			nachnameTxt.setError(getString(R.string.k_nachname_fehlt));
//    		return;
//    	}
		final Main mainActivity = (Main) getActivity();
		final HttpResponse<Produkt> result = mainActivity.getProduktServiceBinder().getProdukte(ctx);

		//TODO Error MSG setzen
//		if (result.responseCode == HTTP_NOT_FOUND) {
//			final String msg = getString(R.string.k_kunden_not_found);
//			nachnameTxt.setError(msg);
//			return;
//		}
		
		Log.d(LOG_TAG, result.toString());
		
		produkte = result.resultList;
	}
}
