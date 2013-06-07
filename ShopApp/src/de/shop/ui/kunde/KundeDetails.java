package de.shop.ui.kunde;

import static de.shop.util.Constants.KUNDE_KEY;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;
import de.shop.R;
import de.shop.data.Kunde;

public class KundeDetails extends Activity {
	private static final String LOG_TAG = KundeDetails.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kunde_details);
        
        final Bundle extras = getIntent().getExtras();
        if (extras == null) {
        	return;
        }

        final Kunde kunde = (Kunde) extras.getSerializable(KUNDE_KEY);
        Log.d(LOG_TAG, kunde.toString());
        fillValues(kunde);
        
//      Entfaellt seit Android 4.1 bzw. API 16 durch <activity android:parentActivityName="..."> in AndroidManifest.xml
//		final ActionBar actionBar = getActionBar();
//		actionBar.setHomeButtonEnabled(true);
//		actionBar.setDisplayHomeAsUpEnabled(true);
    }
    
    private void fillValues(Kunde kunde) {
        final TextView txtId = (TextView) findViewById(R.id.kunde_id);
    	txtId.setText(kunde.id.toString());
    	
    	final TextView txtName = (TextView) findViewById(R.id.name);
    	txtName.setText(kunde.name);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
