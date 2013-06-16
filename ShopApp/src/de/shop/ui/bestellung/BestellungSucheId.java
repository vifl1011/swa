package de.shop.ui.bestellung;

import static de.shop.util.Constants.BESTELLUNG_KEY;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;

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
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import de.shop.R;
import de.shop.data.Bestellung;
import de.shop.service.HttpResponse;
import de.shop.ui.main.Main;
import de.shop.ui.main.Prefs;

public class BestellungSucheId extends Fragment {
	
	private AutoCompleteTextView bestellungIdTxt;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		// attachToRoot = false, weil die Verwaltung des Fragments durch die Activity erfolgt
		return inflater.inflate(R.layout.bestellungen_suche_id, container, false);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		bestellungIdTxt = (AutoCompleteTextView) view.findViewById(R.id.bestellung_id_auto);
    	//bestellungIdTxt.setAdapter(new AutoCompleteIdAdapter(bestellungIdTxt.getContext()));
    	
    	// IME
    	bestellungIdTxt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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

		final String bestellungIdStr = bestellungIdTxt.getText().toString();
		if (TextUtils.isEmpty(bestellungIdStr)) {
			bestellungIdTxt.setError(getString(R.string.b_bestellnr_fehlt));
    		return;
    	}
		
		final Long bestellungId = Long.valueOf(bestellungIdStr);
		final Main mainActivity = (Main) getActivity();
		final HttpResponse<? extends Bestellung> result = mainActivity.getBestellungServiceBinder().getBestellungById(bestellungId, ctx);

		if (result.responseCode == HTTP_NOT_FOUND) {
			final String msg = getString(R.string.b_bestellung_not_found, bestellungIdStr);
			bestellungIdTxt.setError(msg);
			return;
		}
		
		final Bestellung bestellung = result.resultObject;
		final Bundle args = new Bundle(1);
		args.putSerializable(BESTELLUNG_KEY, bestellung);
		
		final Fragment neuesFragment = new BestellungDetails();
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
}
