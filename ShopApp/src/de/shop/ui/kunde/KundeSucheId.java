package de.shop.ui.kunde;

import static de.shop.util.Constants.KUNDE_KEY;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;

import java.util.Collections;
import java.util.List;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.TextView;
import de.shop.R;
import de.shop.data.Kunde;
import de.shop.service.HttpResponse;
import de.shop.ui.main.Main;
import de.shop.ui.main.Prefs;

public class KundeSucheId extends Fragment {
	
	private AutoCompleteTextView kundeIdTxt;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		// attachToRoot = false, weil die Verwaltung des Fragments durch die Activity erfolgt
		return inflater.inflate(R.layout.kunden_suche_id, container, false);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		kundeIdTxt = (AutoCompleteTextView) view.findViewById(R.id.kunde_id_auto);
    	//kundeIdTxt.setAdapter(new AutoCompleteIdAdapter(kundeIdTxt.getContext()));
    	
    	// IME
    	kundeIdTxt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
				suchen(view);
				return true;
			}
		});
    	
    	// Listener fuer den Button
    	view.findViewById(R.id.btn_suchen).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				suchen(view);
			}
		});
    	
	    // Evtl. vorhandene Tabs der ACTIVITY loeschen
    	final ActionBar actionBar = getActivity().getActionBar();
    	actionBar.setDisplayShowTitleEnabled(true);
    	actionBar.removeAllTabs();
    }
	
	private void suchen(View view) {
		final Context ctx = view.getContext();

		final String kundeIdStr = kundeIdTxt.getText().toString();
		if (TextUtils.isEmpty(kundeIdStr)) {
			kundeIdTxt.setError(getString(R.string.k_kundennr_fehlt));
    		return;
    	}
		
		final Long kundeId = Long.valueOf(kundeIdStr);
		final Main mainActivity = (Main) getActivity();
		final HttpResponse<? extends Kunde> result = mainActivity.getKundeServiceBinder().sucheKundeById(kundeId, ctx);

		if (result.responseCode == HTTP_NOT_FOUND) {
			final String msg = getString(R.string.k_kunde_not_found, kundeIdStr);
			kundeIdTxt.setError(msg);
			return;
		}
		
		final Kunde kunde = result.resultObject;
		final Bundle args = new Bundle(1);
		args.putSerializable(KUNDE_KEY, kunde);
		
		final Fragment neuesFragment = new KundeDetails();
		neuesFragment.setArguments(args);
		
		// Kein Name (null) fuer die Transaktion, da die Klasse BackStageEntry nicht verwendet wird
		getFragmentManager().beginTransaction()
		                    .replace(R.id.details, neuesFragment)
		                    .addToBackStack(null)
		                    .commit();
	}
	
	@Override
	// Nur aufgerufen, falls setHasOptionsMenu(true) in onCreateView() aufgerufen wird
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
	/*
    // Fuer die Verwendung von AutoCompleteTextView in der Methode onViewCreated()
    private class AutoCompleteIdAdapter extends ArrayAdapter<Long> {
    	private LayoutInflater inflater;
     
    	public AutoCompleteIdAdapter(Context ctx) {
    		super(ctx, -1);
    		inflater = LayoutInflater.from(ctx);
    	}
     
    	@Override
    	public View getView(int position, View convertView, ViewGroup parent) {
    		// TextView ist die Basisklasse von EditText und wiederum AutoCompleteTextView
    		final TextView tv = convertView == null
    				            ? (TextView) inflater.inflate(android.R.layout.simple_dropdown_item_1line, parent, false)
    				            : (TextView) convertView;
     
    		tv.setText(String.valueOf(getItem(position)));  // Long als String innerhalb der Vorschlagsliste
    		return tv;
    	}
     
    	@Override
    	public Filter getFilter() {
    		// Filter ist eine abstrakte Klasse.
    		// Zu einer davon abgeleiteten ANONYMEN Klasse wird ein Objekt erzeugt
    		// Abstrakte Methoden muessen noch implementiert werden, und zwar HIER
    		// performFiltering() wird durch Android in einem eigenen (Worker-) Thread aufgerufen
    		Filter filter = new Filter() {
    			@Override
    			protected FilterResults performFiltering(CharSequence constraint) {
    				List<Long> idList = null;
    				if (constraint != null) {
    					// Liste der IDs, die den bisher eingegebenen Praefix (= constraint) enthalten
    					idList = sucheIds((String) constraint);
    				}
    				if (idList == null) {
    					// Leere Liste, falls keine IDs zum eingegebenen Praefix gefunden werden
    					idList = Collections.emptyList();
    				}
     
    				final FilterResults filterResults = new FilterResults();
    				filterResults.values = idList;
    				filterResults.count = idList.size();
     
    				return filterResults;
    			}
    			
    	    	private List<Long> sucheIds(String idPrefix) {
    	    		final Main mainActivity = (Main) getActivity();
    				final List<Long> ids = mainActivity.getKundeServiceBinder().sucheIds(idPrefix);
    				return ids;
    	    	}
     
    			@Override
    			protected void publishResults(CharSequence contraint, FilterResults results) {
    				clear();
    				@SuppressWarnings("unchecked")
					final List<Long> idList = (List<Long>) results.values;
    				// Ermittelte IDs in die anzuzeigende Vorschlagsliste uebernehmen
    				if (idList != null && !idList.isEmpty()) {
    					addAll(idList);
    				}

    				if (results.count > 0) {
    					notifyDataSetChanged();
    				}
    				else {
    					notifyDataSetInvalidated();
    				}
    			}
     
    			@Override
    			public CharSequence convertResultToString(Object resultValue) {
    				// Long-Objekt als String
    				return resultValue == null ? "" : String.valueOf(resultValue);
    			}
    		};
    		
    		return filter;
    	}
    } */
}
