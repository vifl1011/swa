package de.shop.ui.kunde;

import static de.shop.util.Constants.KUNDEN_KEY;
import static de.shop.util.Constants.KUNDE_KEY;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Fragment;
import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import de.shop.R;
import de.shop.data.Kunde;

public class KundenListeNav extends ListFragment {
	private static final String LOG_TAG = KundenListeNav.class.getSimpleName();
	
	private static final String ID = "id";
	private static final String NACHNAME = "nachname";
	private static final String[] FROM = { ID, NACHNAME};
	private static final int[] TO = { R.id.kunde_id, R.id.nachname_txt };
	
	private List<Kunde> kunden;
	private List<Map<String, Object>> kundenItems;
	private int position = 0;
	
	@Override
	@SuppressWarnings("unchecked")
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        kunden = (List<Kunde>) getActivity().getIntent().getExtras().get(KUNDEN_KEY);
        Log.d(LOG_TAG, kunden.toString());
        
		final ListAdapter listAdapter = createListAdapter();
        setListAdapter(listAdapter);
        
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	private ListAdapter createListAdapter() {
		kundenItems = new ArrayList<Map<String, Object>>(kunden.size());
		for (Kunde k : kunden) {
    		final Map<String, Object> kundeItem = new HashMap<String, Object>(2, 1); // max 2 Eintraege, bis zu 100 % Fuellung
    		kundeItem.put(ID, k.id);
    		kundeItem.put(NACHNAME, k.nachname);
    		kundenItems.add(kundeItem);        	
        }
		
		final ListAdapter listAdapter = new SimpleAdapter(getActivity(), kundenItems, R.layout.kunden_liste_item, FROM, TO);
		return listAdapter;
    }
	
	public void refresh(Kunde kunde) {
		kunden.set(position, kunde);
		final ListAdapter listAdapter = createListAdapter();
        setListAdapter(listAdapter);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
        	@Override
        	public void onItemClick(AdapterView<?> adapterView, View view, int itemPosition, long itemId) {
        		// view: TextView innerhalb von ListFragment
        		// itemPosition: Textposition innerhalb der Liste mit Zaehlung ab 0
        		// itemId = itemPosition bei String-Arrays bzw. = Primaerschluessel bei Listen aus einer DB
        		
        		Log.d(LOG_TAG, kunden.get(itemPosition).toString());
        		
        		// Evtl. vorhandene Tabs der ACTIVITY loeschen
            	getActivity().getActionBar().removeAllTabs();
        		
            	// angeklickte Position fuer evtl. spaeteres Refresh merken, falls der angeklickte Kunde noch aktualisiert wird
            	position = itemPosition;
            	
        		final Kunde kunde = kunden.get(itemPosition);
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
		});
	}
}
