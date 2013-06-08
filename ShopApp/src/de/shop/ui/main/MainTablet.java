package de.shop.ui.main;

import static android.os.Build.VERSION_CODES.HONEYCOMB;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import de.shop.R;

@TargetApi(HONEYCOMB)
public class MainTablet extends Activity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        getFragmentManager().beginTransaction()
                            .add(R.id.details, new Startseite())
                            .commit();
    }

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
