package de.shop.ui.main;

import static de.shop.util.Constants.KUNDE_KEY;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import de.shop.R;
import de.shop.data.Kunde;
import de.shop.ui.kunde.KundeDetails;

public class Main extends Activity implements OnClickListener {
	private static final String LOG_TAG = Main.class.getSimpleName();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		
		findViewById(R.id.btn_suchen).setOnClickListener(this);
    }
    
	@Override // OnClickListener
	public void onClick(View view) {
		final EditText kundeIdTxt = (EditText) findViewById(R.id.kunde_id);
		final String kundeId = kundeIdTxt.getText().toString();
		
		final Kunde kunde = getKunde(kundeId);
		
		// NICHT: new KundeDetails() !!!
		
		final Intent intent = new Intent(view.getContext(), KundeDetails.class);
		intent.putExtra(KUNDE_KEY, kunde);
		startActivity(intent);
	}
    
    private Kunde getKunde(String kundeIdStr) {
    	final Long kundeId = Long.valueOf(kundeIdStr);
    	final Kunde kunde = new Kunde(kundeId, "Name" + kundeIdStr);
    	Log.v(LOG_TAG, kunde.toString());
    	
    	return kunde;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	// inflate = fuellen
        getMenuInflater().inflate(R.menu.main, menu);
    	return true;
    }
}
