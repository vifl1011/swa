package de.shop.ui.kunde;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import de.shop.R;
import de.shop.ui.main.Prefs;
import de.shop.util.WischenListener;

public class KundeKarte extends Fragment implements OnTouchListener {
	private GestureDetector gestureDetector;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final ActionBar actionBar = getActivity().getActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
    	actionBar.removeAllTabs();
    	
    	// Voraussetzung fuer onOptionsItemSelected()
        setHasOptionsMenu(true);
    	
		// attachToRoot = false, weil die Verwaltung des Fragments durch die Activity erfolgt
		return inflater.inflate(R.layout.kunde_karte, container, false);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		final Activity activity = getActivity();
		final OnGestureListener onGestureListener = new WischenListener(activity);
	    gestureDetector = new GestureDetector(activity, onGestureListener);
	    view.setOnTouchListener(this);
	}
	
	@Override   // OnTouchListener
	public boolean onTouch(View view, MotionEvent event) {
		return gestureDetector.onTouchEvent(event);
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
}
