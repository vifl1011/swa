package de.shop.util;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import de.shop.R;

// http://developer.android.com/guide/topics/ui/actionbar.html#Tabs
public class TabListener<T extends Fragment> implements ActionBar.TabListener {
    private final Activity activity;
    private final String classnameFragment;
    private final Bundle args;

	private Fragment fragment;

    public TabListener(Activity activity, Class<T> clazz, Bundle args) {
    	this.activity = activity;
    	this.classnameFragment = clazz.getName();
    	this.args = args;
    }
    
	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		// Ist das Fragment bereits initialisiert ?
        if (fragment == null) {
            // Das Fragment wird instanziiert und zur Activity als Detail-Fragment hinzugefuegt
        	fragment = Fragment.instantiate(activity, classnameFragment);
        	if (args != null) {
        		fragment.setArguments(args);
        	}
            ft.replace(R.id.details, fragment); // siehe layout-swXXXdp\main.xml und layout-swXXXdp\kunden_liste.xml 
            // WICHTIG: Nicht commit() aufrufen und auch nicht auf dem "Back Stack" ablegen
        }
        else {
            // Das Fragment existiert bereits und wird "attach"ed, um es anzeigen zu koennen
        	// attach() ab Android 3.2 bzw. API-Level 13
            ft.attach(fragment);
        }
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		if (fragment != null) {
            // "detach" fuer das Fragment, weil ein anderes "attached" wird
			// detach() ab Android 3.2 bzw. API-Level 13
            ft.detach(fragment);
        }
	}
	
	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// Erneutes Antippen des aktuell angetippten Fragments: keine weitere Aktivitaet noetig		
	}
}
