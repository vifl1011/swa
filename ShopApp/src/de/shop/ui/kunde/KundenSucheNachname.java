package de.shop.ui.kunde;

import static de.shop.util.Constants.KUNDEN_KEY;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import de.shop.util.InternalShopError;

public class KundenSucheNachname extends Fragment {	
	private static final String LOG_TAG = KundenSucheNachname.class.getSimpleName();
	
	private AutoCompleteTextView nachnameTxt;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		// attachToRoot = false, weil die Verwaltung des Fragments durch die Activity erfolgt
		return inflater.inflate(R.layout.kunden_suche_nachname, container, false);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		nachnameTxt = (AutoCompleteTextView) view.findViewById(R.id.nachname_auto);
		final ArrayAdapter<String> adapter = new AutoCompleteNachnameAdapter(nachnameTxt.getContext());
		nachnameTxt.setAdapter(adapter);
		
		// IME
		nachnameTxt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
				suchen(view);
				return true;
			}
		});
    	
		// Listener fuer Button
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
		
		final String nachname = nachnameTxt.getText().toString();
		if (TextUtils.isEmpty(nachname)) {
			nachnameTxt.setError(getString(R.string.k_nachname_fehlt));
    		return;
    	}
		final Main mainActivity = (Main) getActivity();
		final HttpResponse<Kunde> result = mainActivity.getKundeServiceBinder().sucheKundenByNachname(nachname, ctx);

		if (result.responseCode == HTTP_NOT_FOUND) {
			final String msg = getString(R.string.k_kunden_not_found, nachname);
			nachnameTxt.setError(msg);
			return;
		}
		
		Log.d(LOG_TAG, result.toString());

		final Intent intent = new Intent(mainActivity, KundenListe.class);
		intent.putExtra(KUNDEN_KEY, result.resultList);
		startActivity(intent);
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

    // Fuer die Verwendung von AutoCompleteTextView in der Methode onViewCreated()
    private class AutoCompleteNachnameAdapter extends ArrayAdapter<String> {
    	private LayoutInflater inflater;
     
    	public AutoCompleteNachnameAdapter(Context ctx) {
    		super(ctx, -1);
    		inflater = LayoutInflater.from(ctx);
    	}
     
    	@Override
    	public View getView(int position, View convertView, ViewGroup parent) {
    		// TextView ist die Basisklasse von EditText und wiederum AutoCompleteTextView
    		final TextView tv = convertView != null
    				            ?  (TextView) convertView
    		                    : (TextView) inflater.inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
    		tv.setText(String.valueOf(getItem(position)));
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
    				List<String> nachnameList = null;
    				if (constraint != null) {
    					// Liste der IDs, die den bisher eingegebenen Praefix (= constraint) enthalten
    					nachnameList = sucheNachnamen((String) constraint);
    				}
    				if (nachnameList == null) {
    					// Leere Liste, falls keine IDs zum eingegebenen Praefix gefunden werden
    					nachnameList = Collections.emptyList();
    				}
     
    				final FilterResults filterResults = new FilterResults();
    				filterResults.values = nachnameList;
    				filterResults.count = nachnameList.size();
     
    				return filterResults;
    			}
    			
    	    	private List<String> sucheNachnamen(String nachnamePrefix) {
    	    		final Main mainActivity = (Main) getActivity();
    				List<String> nachnamen = null;
    				try {
    					nachnamen = mainActivity.getKundeServiceBinder().sucheNachnamen(nachnamePrefix);
    				}
    				catch (InternalShopError e) {
    					final Throwable t = e.getCause();
    					if (t != null && t instanceof TimeoutException) {
    						nachnamen = Collections.emptyList();
    						Log.e(LOG_TAG, e.getMessage(), t);					
    					}
    					else {
    						Log.e(LOG_TAG, e.getMessage(), e);
    					}
    				}
    				return nachnamen;
    	    	}
     
    			@Override
    			protected void publishResults(CharSequence contraint, FilterResults results) {
    				clear();
    				@SuppressWarnings("unchecked")
					final List<String> nachnameList = (List<String>) results.values;
    				// Ermittelte IDs in die anzuzeigende Vorschlagsliste uebernehmen
    				addAll(nachnameList);

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
    }
}
