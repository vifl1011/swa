package de.shop.util;

import static de.shop.util.Constants.WISCHEN_MAX_OFFSET_PATH;
import static de.shop.util.Constants.WISCHEN_MIN_DISTANCE;
import static de.shop.util.Constants.WISCHEN_THRESHOLD_VELOCITY;
import android.app.ActionBar;
import android.app.Fragment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.GestureDetector.SimpleOnGestureListener;

public class WischenListener extends SimpleOnGestureListener {
	private static final String LOG_TAG = WischenListener.class.getSimpleName();
	
	private final ActionBar actionBar;
	private final int tabCount;
	
	public WischenListener(Fragment fragment) {
		super();
		actionBar = fragment.getActivity().getActionBar();
		tabCount = actionBar.getTabCount();
	}

	@Override
	// to fling = schleudern, werfen; velocity = Geschwindigkeit in X- oder Y-Richtung in Pixel pro Sekunde
	public boolean onFling(MotionEvent firstDownEvent, MotionEvent secondUpEvent, float velocityX, float velocityY) {
		// Zu grosse Abweichung entlang der Y-Koordinaten? Falls ja: kein Wischen
		final float yFirstDown = firstDownEvent.getY();
		final float ySecondUp = secondUpEvent.getY();
		if (Math.abs(yFirstDown - ySecondUp) > WISCHEN_MAX_OFFSET_PATH) {
			Log.d(LOG_TAG, "onFling: Kein Wischen wegen Y-Abweichung");
			return false;
		}
		
		if (Math.abs(velocityX) < WISCHEN_THRESHOLD_VELOCITY) {
			Log.d(LOG_TAG, "onFling: Kein Wischen wegen zu langsamer Bewegung entlang der X-Achse");
			return false;
		}

		final float xFirstDown = firstDownEvent.getX();
		final float xSecondUp = secondUpEvent.getX();
		final float distance = xFirstDown - xSecondUp;
		
		final int selectedTabPosition = actionBar.getSelectedTab().getPosition();
		
		if (distance > WISCHEN_MIN_DISTANCE) {
			// Bewegung von rechts nach links, da die 1. X-Koordinate groesser als die 2. ist
			Log.d(LOG_TAG, "von rechts nach links");
			
			final int newPosition = selectedTabPosition < tabCount - 1 ? selectedTabPosition + 1 : 0;
			actionBar.selectTab(actionBar.getTabAt(newPosition));
			return false;
		}
		
		if (-distance > WISCHEN_MIN_DISTANCE) {
			// Bewegung von links nach rechts, da die 2. X-Koordinate groesser als die 1. ist
			Log.d(LOG_TAG, "von links nach rechts");
			final int newPosition = selectedTabPosition > 0 ? selectedTabPosition - 1 : tabCount - 1;
			actionBar.selectTab(actionBar.getTabAt(newPosition));
			return false;
		}
		
		Log.d(LOG_TAG, "onFling: Kein Wischen wegen zu kurzer Bewegung: " + Math.abs(xSecondUp - xFirstDown));
		return false;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return true;
	}
}
