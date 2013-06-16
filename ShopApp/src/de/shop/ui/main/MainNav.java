package de.shop.ui.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Fragment;
import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.PopupMenu;
import android.widget.SimpleAdapter;
import de.shop.R;
import de.shop.ui.bestellung.BestellungSucheId;
import de.shop.ui.kunde.KundeCreate;
import de.shop.ui.kunde.KundeDelete;
import de.shop.ui.kunde.KundeSucheId;
import de.shop.ui.kunde.KundenSucheNachname;
import de.shop.ui.produkt.ListProdukte;
import de.shop.ui.produkt.ProduktCreate;

public class MainNav extends ListFragment {
	public enum NavType {
		KUNDEN(0),
		BESTELLUNGEN(1),
		PRODUKTE(2);
		
		private int value;
		
		private NavType(int value) {
			this.value = value;
		}
		
		public int getValue() {
			return value;
		}
		
		public static NavType valueOf(int value) {
			switch (value) {
				case 0:	return KUNDEN;
				case 1:	return BESTELLUNGEN;
				case 2:	return PRODUKTE;
				default: return KUNDEN;
			}
		}
	}
	
	private static final String LOG_TAG = Main.class.getSimpleName();
	private static final String ICON = "icon";
	private static final String TEXT = "text";
	private static final String[] FROM = { ICON, TEXT };
	private static final int[] TO = { R.id.nav_icon, R.id.nav_text };
	
	private PopupMenu kundenPopup;
	private PopupMenu bestellungenPopup;
	private PopupMenu produktePopup;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final ListAdapter listAdapter = createListAdapter();        
        setListAdapter(listAdapter);
        
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	private ListAdapter createListAdapter() {
		final NavType[] navValues = NavType.values();
		final List<Map<String, Object>> navList = new ArrayList<Map<String, Object>>(navValues.length);
		
		for (NavType nav : navValues) {
			final Map<String, Object> navItem = new HashMap<String, Object>(FROM.length, 1); // max 2 Eintraege, bis zu 100 % Fuellung
			switch (nav) {
				case KUNDEN:
					navItem.put(ICON, R.drawable.ic_kunden);
					navItem.put(TEXT, getString(R.string.s_nav_kunden));
					break;
				
				case BESTELLUNGEN:
					navItem.put(ICON, R.drawable.ic_bestellungen);
					navItem.put(TEXT, getString(R.string.s_nav_bestellungen));
					break;
					
				case PRODUKTE:
					navItem.put(ICON, R.drawable.ic_artikel);
					navItem.put(TEXT, getString(R.string.s_nav_produkte));
					break;
					
				default:
					Log.e(LOG_TAG, nav.toString() + " wird nicht in die Navigationsleiste eingetragen");
					continue;
			}
			navList.add(navItem);
		}
		
		final ListAdapter listAdapter = new SimpleAdapter(getActivity(), navList, R.layout.nav_item, FROM, TO);
		return listAdapter;
    }
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
        	@Override
        	public void onItemClick(AdapterView<?> adapterView, View view, int itemPosition, long itemId) {
        		// view: TextView innerhalb von ListFragment
        		// itemPosition: Textposition innerhalb der Liste mit Zaehlung ab 0
        		// itemId = itemPosition bei String-Arrays bzw. = Primaerschluessel bei Listen aus einer DB
        		
        		switch (NavType.valueOf(itemPosition)) {
        			case KUNDEN:
        				initKundenPopup(view);
        				kundenPopup.show();
        				return;
        				
        			case BESTELLUNGEN:
        				initBestellungenPopup(view);
        				bestellungenPopup.show();
        				return;
        				
        			case PRODUKTE:
        				initProduktePopup(view);
        				produktePopup.show();
        				return;
        				
        			default:
        				return;
        		}
        	}
        	
        });
	}
	
	private void initKundenPopup(View view) {
		if (kundenPopup != null) {
			return;
		}
		
		kundenPopup = new PopupMenu(getActivity(), view);
		// inflate() ab Android 4.0 bzw. API-Level 14
		kundenPopup.inflate(R.menu.kunden_popup);
			
		kundenPopup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				Fragment neuesFragment;
				switch (item.getItemId()) {
					case R.id.kunden_suche_id:
						neuesFragment = new KundeSucheId();
						break;
						
					case R.id.kunden_suche_nachname:
						neuesFragment = new KundenSucheNachname();
						break;
						
					case R.id.kunde_create:
						neuesFragment = new KundeCreate();
						break;
						
					case R.id.kunden_delete:
						neuesFragment = new KundeDelete();
						break;

					default:
						return false;
				}
				
				// Kein Name (null) fuer die Transaktion, da die Klasse BackStageEntry nicht verwendet wird
				getFragmentManager().beginTransaction()
				                    .replace(R.id.details, neuesFragment)
				                    .addToBackStack(null)  
				                    .commit();
				
				return true;
			}
		});
	}
	
	private void initBestellungenPopup(View view) {
		if (bestellungenPopup != null) {
			return;
		}
		
		bestellungenPopup = new PopupMenu(getActivity(), view);
		// inflate() ab Android 4.0 bzw. API-Level 14
		bestellungenPopup.inflate(R.menu.bestellungen_popup);
		
		bestellungenPopup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				Fragment neuesFragment;
				switch (item.getItemId()) {
					case R.id.bestellungen_suche_id:
						neuesFragment = new BestellungSucheId();
						break;

					default:
						return false;
				}
				
				// Kein Name (null) fuer die Transaktion, da die Klasse BackStageEntry nicht verwendet wird
				getFragmentManager().beginTransaction()
				                    .replace(R.id.details, neuesFragment)
				                    .addToBackStack(null)  
				                    .commit();
				
				return true;
			}
		});
	}
	
	private void initProduktePopup(View view) {
		if (produktePopup != null) {
			return;
		}
		
		produktePopup = new PopupMenu(getActivity(), view);
		// inflate() ab Android 4.0 bzw. API-Level 14
		produktePopup.inflate(R.menu.produkte_popup);
		
		produktePopup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				Fragment neuesFragment;
				switch (item.getItemId()) {
					case R.id.produkte_suche_id:
						neuesFragment = new ListProdukte();
						break;
						
					case R.id.produkt_create:
						neuesFragment = new ProduktCreate();
						break;

					default:
						return false;
				}
				
				// Kein Name (null) fuer die Transaktion, da die Klasse BackStageEntry nicht verwendet wird
				getFragmentManager().beginTransaction()
				                    .replace(R.id.details, neuesFragment)
				                    .addToBackStack(null)  
				                    .commit();
				
				return true;
			}
		});
	}
}
