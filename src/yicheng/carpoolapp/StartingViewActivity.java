package yicheng.carpoolapp;

import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;


public class StartingViewActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		Locale locale = new Locale("en_US"); 
		Locale.setDefault(locale);
		Configuration config = new Configuration();
		config.locale = locale;
		getBaseContext().getApplicationContext().getResources().updateConfiguration(config, null);

		overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
		setContentView(R.layout.starting_view_activity_layout);

		Handler handler = new Handler(); 
		handler.postDelayed(new Runnable() { 
			public void run() { 
				Intent main_view_intent = new Intent("yicheng.carpoolapp.LOGINACTIVITY");
				overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
				startActivity(main_view_intent);

			} 
		}, 1500); 
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		finish();

	}

}
