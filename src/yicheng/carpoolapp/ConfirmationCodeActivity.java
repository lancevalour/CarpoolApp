package yicheng.carpoolapp;



import java.util.HashMap;



import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class ConfirmationCodeActivity extends Activity{
	private RelativeLayout confirmation_code_layout;
	private EditText confirmation_code_editText;
	private Button confirmation_code_button;
	private LinearLayout confirmation_code_loading_layout;

	private boolean isDriver;
	private String PHONE_NUMBER;
	private String PASSWORD;
	private String USERNAME;
	private String PHOTO_URL;
	private String CONTACT_EVENT;
	private String TEMP_CONFIRM_EVENT;
	private String FINAL_CONFIRM_EVENT;
	private String ACCOUNT_TYPE;

	private String confirmationCode;


	private Handler handler;


	private SharedPreferences local_user_information;
	private SharedPreferences.Editor local_user_editor;
	private String PREFS_NAME = "LocalUserInfo";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		local_user_information =  this.getSharedPreferences(PREFS_NAME,0);
		overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
		setContentView(R.layout.confirmation_code_activity_layout);

		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();

		isDriver = bundle.getBoolean("isDriver");
		PHONE_NUMBER = bundle.getString("phoneNumber");
		PASSWORD  = bundle.getString("password");
		USERNAME = bundle.getString("username");
		confirmationCode = bundle.getString("confirmationCode");
		if (isDriver){
			ACCOUNT_TYPE = "driver";
		}
		else{
			ACCOUNT_TYPE = "passenger";
		}
		

		initiateComponents();
		setHandlerControl();
		setTouchHideKeyboardControl();
		setConfirmButtonControl();

	}

	private void setHandlerControl(){

		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == 1){

					Toast.makeText(getBaseContext(), "Account created.", Toast.LENGTH_SHORT).show();
					//set true when database is loaded 
					Intent intent = new Intent("yicheng.carpoolapp.USERMAINTABACTIVITY");
					startActivity(intent);
					finish();

				}
			}
		};
	}



	private void initiateComponents(){
		confirmation_code_layout = (RelativeLayout) findViewById(R.id.confirmation_code_layout);
		confirmation_code_editText = (EditText) findViewById(R.id.confirmation_code_editText);
		confirmation_code_button = (Button) findViewById(R.id.confirmation_code_button);
		confirmation_code_loading_layout = (LinearLayout) findViewById(R.id.confirmation_code_loading_layout);
		confirmation_code_loading_layout.setVisibility(View.INVISIBLE);

	}

	private void hideKeyboard(){
		if(getCurrentFocus()!=null) {
			InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
		}
	}

	private void setTouchHideKeyboardControl(){
		confirmation_code_layout.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				hideKeyboard();
				return false;
			}
		});

		confirmation_code_button.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				hideKeyboard();
				return false;
			}
		});
	}

	private void setConfirmButtonControl(){
		confirmation_code_button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if (confirmationCode.equals(confirmation_code_editText.getText().toString())){

					if (isConnectedToInternet()){
						confirmation_code_editText.setEnabled(false);
						confirmation_code_button.setEnabled(false);
						Animation animationFadeIn = AnimationUtils.loadAnimation(getBaseContext(), R.anim.fade_in);
						confirmation_code_loading_layout.startAnimation(animationFadeIn);
						confirmation_code_loading_layout.setVisibility(View.VISIBLE);

						setUpDataBase();

					}
					else{
						Toast.makeText(getBaseContext(), "Unable to connect to Internet, please check your network.", Toast.LENGTH_LONG).show();
					}

				}
				else{
					Toast.makeText(getBaseContext(), "Your confirmation code is not correct.", Toast.LENGTH_LONG).show();
				}

			}
		});
	}


	private void setUpDataBase(){
		runOnUiThread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				new Thread(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub

						if (isDriver){
							setUpDriverAccountDataBase();
						}
						else{
							setUpPassengerAccountDataBase();
						}

						local_user_editor = local_user_information.edit();
						local_user_editor.putString("phoneNumber", PHONE_NUMBER);
						local_user_editor.putString("password", PASSWORD);
						local_user_editor.putString("username", USERNAME);
						local_user_editor.putString("photoUrl", PHOTO_URL);
						local_user_editor.putString("accountType", ACCOUNT_TYPE);
						local_user_editor.putString("contactEvent", CONTACT_EVENT);
						local_user_editor.putString("tempConfirmEvent", TEMP_CONFIRM_EVENT);
						local_user_editor.putString("finalConfirmEvent", FINAL_CONFIRM_EVENT);
						local_user_editor.putBoolean("isLoggedIn", true);
						local_user_editor.commit();

						Message msg = Message.obtain();
						msg.what = 1;
						handler.sendMessage(msg);


					}

				}).start();

			}

		});
	}

	private void setUpDriverAccountDataBase(){
		SimpleDB.createItem(LoginActivity.clientManager, SimpleDB.DRIVER_ACCOUNT_TABLE, PHONE_NUMBER);
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(SimpleDB.DRIVER_ACCOUNT_PASSWORD_ATTRIBUTE, PASSWORD);
		map.put(SimpleDB.DRIVER_ACCOUNT_USERNAME_ATTRIBUTE, USERNAME);
		map.put(SimpleDB.DRIVER_ACCOUNT_PHOTOURL_ATTRIBUTE, "");
		map.put(SimpleDB.DRIVER_ACCOUNT_CONTACT_EVENT_ATTRIBUTE, "");
		map.put(SimpleDB.DRIVER_ACCOUNT_TEMP_CONFIRM_EVENT_ATTRIBUTE, "");
		map.put(SimpleDB.DRIVER_ACCOUNT_FINAL_CONFIRM_EVENT_ATTRIBUTE, "");


		SimpleDB.updateAttributesForItem(LoginActivity.clientManager, SimpleDB.DRIVER_ACCOUNT_TABLE, PHONE_NUMBER, map);
	}

	private void setUpPassengerAccountDataBase(){
		SimpleDB.createItem(LoginActivity.clientManager, SimpleDB.PASSENGER_ACCOUNT_TABLE, PHONE_NUMBER);
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(SimpleDB.PASSENGER_ACCOUNT_PASSWORD_ATTRIBUTE, PASSWORD);
		map.put(SimpleDB.PASSENGER_ACCOUNT_USERNAME_ATTRIBUTE, USERNAME);
		map.put(SimpleDB.PASSENGER_ACCOUNT_PHOTOURL_ATTRIBUTE, "");
		map.put(SimpleDB.PASSENGER_ACCOUNT_CONTACT_EVENT_ATTRIBUTE, "");
		map.put(SimpleDB.PASSENGER_ACCOUNT_TEMP_CONFIRM_EVENT_ATTRIBUTE, "");
		map.put(SimpleDB.PASSENGER_ACCOUNT_FINAL_CONFIRM_EVENT_ATTRIBUTE, "");

		SimpleDB.updateAttributesForItem(LoginActivity.clientManager, SimpleDB.PASSENGER_ACCOUNT_TABLE, PHONE_NUMBER, map);
	}


	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		moveTaskToBack(true);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK ) {
			//preventing default implementation previous to 
			//android.os.Build.VERSION_CODES.ECLAIR
			moveTaskToBack(true);
			return false;
		}     
		return super.onKeyDown(keyCode, event);    
	}


	private boolean isConnectedToInternet(){
		ConnectivityManager connectivityManager = 
				(ConnectivityManager)ConfirmationCodeActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo wifi_network = 
				connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo mobile_network = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);


		return wifi_network.isConnectedOrConnecting() || mobile_network.isConnectedOrConnecting();

		/*return mobile_network.isConnectedOrConnecting();*/
	}


}
