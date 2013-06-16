package de.shop.ui.bestellung;

import static android.app.ActionBar.NAVIGATION_MODE_TABS;
import static de.shop.util.Constants.BESTELLUNG_KEY;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.shop.R;
import de.shop.data.Bestellung;
import de.shop.util.TabListener;

public class BestellungDetails extends Fragment {
	private static final String LOG_TAG = BestellungDetails.class.getSimpleName();
	
	private Bundle args;
	private Bestellung bestellung;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		args = getArguments();
		bestellung = (Bestellung) args.get(BESTELLUNG_KEY);
        Log.d(LOG_TAG, bestellung.toString());
        
		// attachToRoot = false, weil die Verwaltung des Fragments durch die Activity erfolgt
		return inflater.inflate(R.layout.details_tabs, container, false);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		final Activity activity = getActivity();
		final ActionBar actionBar = activity.getActionBar();
		// (horizontale) Tabs; NAVIGATION_MODE_LIST fuer Dropdown Liste
		actionBar.setNavigationMode(NAVIGATION_MODE_TABS);
	    actionBar.setDisplayShowTitleEnabled(false);  // Titel der App ausblenden, um mehr Platz fuer die Tabs zu haben
    	
	    Tab tab = actionBar.newTab()
	                       .setText(getString(R.string.b_stammdaten))
	                       .setTabListener(new TabListener<BestellungStammdaten>(activity,
	                    		                                            BestellungStammdaten.class,
	                    		                                            args));
	    actionBar.addTab(tab);
	    
	    tab = actionBar.newTab()
                       .setText(getString(R.string.b_bestellpositionen))
                       .setTabListener(new TabListener<BestellungBestellpositionen>(activity,
                    		   												BestellungBestellpositionen.class,
                    		   												args));
	    actionBar.addTab(tab);
	}
}
