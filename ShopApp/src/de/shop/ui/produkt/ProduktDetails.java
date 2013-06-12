package de.shop.ui.produkt;

import static android.app.ActionBar.NAVIGATION_MODE_TABS;
import static de.shop.util.Constants.PRODUKT_KEY;
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
import de.shop.data.Produkt;
import de.shop.util.TabListener;

public class ProduktDetails extends Fragment {
	private static final String LOG_TAG = ProduktDetails.class.getSimpleName();
	
	private Bundle args;
	private Produkt produkt;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		args = getArguments();
		produkt = (Produkt) args.get(PRODUKT_KEY);
        Log.d(LOG_TAG, produkt.toString());
        
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
	                       .setText(getString(R.string.k_stammdaten))
	                       .setTabListener(new TabListener<ProduktStammdaten>(activity,
	                    		   ProduktStammdaten.class,
	                    		                                            args));
	    actionBar.addTab(tab);
	    
//	    tab = actionBar.newTab()
//                       .setText(getString(R.string.k_bestellungen))
//                       .setTabListener(new TabListener<KundeBestellungen>(activity,
//                    		                                              KundeBestellungen.class,
//                    		                                              args));
//	    actionBar.addTab(tab);
	}
}
