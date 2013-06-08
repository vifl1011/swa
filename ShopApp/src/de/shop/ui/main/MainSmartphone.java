package de.shop.ui.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import de.shop.R;
import de.shop.ui.bestellung.BestellungenActivity;
import de.shop.ui.kunde.KundenActivity;

public class MainSmartphone extends Activity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        final View.OnClickListener clickListenerKunden = new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				final Intent intent = new Intent(view.getContext(), KundenActivity.class);;
				startActivity(intent);
			}
		}; 
       	findViewById(R.id.btn_kunden).setOnClickListener(clickListenerKunden);
       	
        final View.OnClickListener clickListenerBestellungen = new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				final Intent intent = new Intent(view.getContext(), BestellungenActivity.class);;
				startActivity(intent);
			}
		};
        findViewById(R.id.btn_bestellungen).setOnClickListener(clickListenerBestellungen);
    }

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
