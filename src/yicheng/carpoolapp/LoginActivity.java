package yicheng.carpoolapp;



import java.io.IOException;
import java.util.ArrayList;







import com.techventus.server.voice.Voice;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class LoginActivity extends Activity{
	private EditText login_phoneNumber_editText, login_password_editText;
	private Button login_button, forget_password_button, register_button, explore_button;
	private RelativeLayout login_activity_layout;
	private LinearLayout login_loading_layout;

	public static AmazonClientManager clientManager;


	private ArrayList<String> driverAccountTableItems = new ArrayList<String>();
	private ArrayList<String> passengerAccountTableItems = new ArrayList<String>();

	private Handler handler;

	private String PHONE_NUMBER;
	private String PASSWORD;
	private String USERNAME;
	private String PHOTO_URL;
	private String CONTACT_EVENT;
	private String TEMP_CONFIRM_EVENT;
	private String FINAL_CONFIRM_EVENT;
	private String ACCOUNT_TYPE;


	private SharedPreferences local_user_information;
	private SharedPreferences.Editor local_user_editor;
	private String PREFS_NAME = "LocalUserInfo";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		local_user_information =  this.getSharedPreferences(PREFS_NAME,0);
		overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
		setContentView(R.layout.login_activity_layout);

		boolean isLoggedIn = local_user_information.getBoolean("isLoggedIn", false);

		if (!isLoggedIn){

			clientManager = new AmazonClientManager(); 




			initiateComponents();
			setHandlerControl();

			if (isConnectedToInternet()){
				loadAccountTable();
				login_loading_layout.setVisibility(View.VISIBLE);
			}
			else{
				Toast.makeText(getBaseContext(), "Unable to connect to Internet, please check your network.", Toast.LENGTH_LONG).show();
				Handler newHandler = new Handler();
				newHandler.postDelayed(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						finish();
						overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
					}

				}, 1500);
			}





			setTouchHideKeyboardControl();

			setLoginButtonControl();
			setForgetPasswordButtonControl();
			setRegisterButtonControl();
			setExploreButtonControl();
		}
		else{
			Intent intent = new Intent("yicheng.carpoolapp.USERMAINTABACTIVITY");
			startActivity(intent);
			finish();
		}

	}

	private void setHandlerControl(){

		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == 1){
					login_loading_layout.setVisibility(View.INVISIBLE);
				}
				if (msg.what == 2){

					if (PASSWORD.equals(login_password_editText.getText().toString())){
						if (isConnectedToInternet()){
							loadUserInfo();
						}
						else{
							login_loading_layout.setVisibility(View.INVISIBLE);
							Toast.makeText(getBaseContext(), "Unable to connect to Internet, please check your network.", Toast.LENGTH_LONG).show();
						}
					}
					else{
						login_loading_layout.setVisibility(View.INVISIBLE);
						Toast.makeText(getBaseContext(), "Incorrect password.", Toast.LENGTH_LONG).show();
					}
				}
				if (msg.what == 3){
					PHONE_NUMBER = login_phoneNumber_editText.getText().toString();

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


					login_loading_layout.setVisibility(View.INVISIBLE);

					Toast.makeText(getBaseContext(), "Login success.", Toast.LENGTH_SHORT).show();
					//set true when database is loaded 
					Intent gotoUserMainTabActivityIntent = new Intent("yicheng.carpoolapp.USERMAINTABACTIVITY");
					startActivity(gotoUserMainTabActivityIntent);
					finish();
				}

				if (msg.what == 4){
					Toast.makeText(getBaseContext(), "We have sent you a message with your password.", Toast.LENGTH_LONG).show();
					sendTextMessageWithPassword();
				}

			}
		};
	}



	private void initiateComponents(){
		login_phoneNumber_editText = (EditText) findViewById(R.id.login_phoneNumber_editText);
		login_password_editText = (EditText) findViewById(R.id.login_password_editText);
		login_button = (Button) findViewById(R.id.login_button);
		forget_password_button = (Button) findViewById(R.id.forget_password_button);
		register_button = (Button) findViewById(R.id.register_button);
		explore_button = (Button) findViewById(R.id.explore_button);
		login_activity_layout = (RelativeLayout) findViewById(R.id.login_activity_layout);
		login_loading_layout = (LinearLayout) findViewById(R.id.login_loading_layout);
		login_loading_layout.setVisibility(View.INVISIBLE);


	}

	private void hideKeyboard(){
		if(getCurrentFocus()!=null) {
			InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
		}
	}

	private void setTouchHideKeyboardControl(){
		login_activity_layout.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				hideKeyboard();
				return false;
			}
		});

		login_button.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				hideKeyboard();
				return false;
			}
		});

		forget_password_button.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				hideKeyboard();
				return false;
			}
		});

		register_button.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				hideKeyboard();
				return false;
			}
		});

		explore_button.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				hideKeyboard();
				return false;
			}
		});
	}











	private void setLoginButtonControl(){
		login_button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (login_phoneNumber_editText.getText().toString().length() < 1 || 
						login_password_editText.getText().toString().length() < 1){
					Toast.makeText(getBaseContext(), "One of your information is empty, please check.", Toast.LENGTH_LONG).show();
				}
				else{
					if (!driverAccountTableItems.contains(login_phoneNumber_editText.getText().toString()) && 
							!passengerAccountTableItems.contains(login_phoneNumber_editText.getText().toString())){
						Toast.makeText(getBaseContext(), "This phone number is not registered.", Toast.LENGTH_LONG).show();
					}
					else{
						if (isConnectedToInternet()){
							login_loading_layout.setVisibility(View.VISIBLE);
							getAccountPassword();
						}
						else{
							Toast.makeText(getBaseContext(), "Unable to connect to Internet, please check your network.", Toast.LENGTH_LONG).show();
						}
					}
				}

			}
		});
	}

	private void getAccountPassword(){
		runOnUiThread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				new Thread(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (driverAccountTableItems.contains(login_phoneNumber_editText.getText().toString())){

							PASSWORD = SimpleDB.getSingleAttributesForItem(
									clientManager, 
									SimpleDB.DRIVER_ACCOUNT_TABLE, 
									login_phoneNumber_editText.getText().toString(), 
									SimpleDB.DRIVER_ACCOUNT_PASSWORD_ATTRIBUTE);

						}
						else{

							PASSWORD = SimpleDB.getSingleAttributesForItem(
									clientManager, 
									SimpleDB.PASSENGER_ACCOUNT_TABLE, 
									login_phoneNumber_editText.getText().toString(), 
									SimpleDB.PASSENGER_ACCOUNT_PASSWORD_ATTRIBUTE);
						}


						Message msg = Message.obtain();
						msg.what = 2;
						handler.sendMessage(msg);

					}

				}).start();
			}

		});
	}


	private void loadUserInfo(){
		runOnUiThread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				new Thread(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub

						if (driverAccountTableItems.contains(login_phoneNumber_editText.getText().toString())){
							ACCOUNT_TYPE = "driver";
							USERNAME = SimpleDB.getSingleAttributesForItem(clientManager, SimpleDB.DRIVER_ACCOUNT_TABLE, login_phoneNumber_editText.getText().toString(), SimpleDB.DRIVER_ACCOUNT_USERNAME_ATTRIBUTE);
							PHOTO_URL = SimpleDB.getSingleAttributesForItem(clientManager, SimpleDB.DRIVER_ACCOUNT_TABLE, login_phoneNumber_editText.getText().toString(), SimpleDB.DRIVER_ACCOUNT_PHOTOURL_ATTRIBUTE);
							CONTACT_EVENT = SimpleDB.getSingleAttributesForItem(clientManager, SimpleDB.DRIVER_ACCOUNT_TABLE, login_phoneNumber_editText.getText().toString(), SimpleDB.DRIVER_ACCOUNT_CONTACT_EVENT_ATTRIBUTE);
							TEMP_CONFIRM_EVENT = SimpleDB.getSingleAttributesForItem(clientManager, SimpleDB.DRIVER_ACCOUNT_TABLE, login_phoneNumber_editText.getText().toString(), SimpleDB.DRIVER_ACCOUNT_TEMP_CONFIRM_EVENT_ATTRIBUTE);
							FINAL_CONFIRM_EVENT = SimpleDB.getSingleAttributesForItem(clientManager, SimpleDB.DRIVER_ACCOUNT_TABLE, login_phoneNumber_editText.getText().toString(), SimpleDB.DRIVER_ACCOUNT_FINAL_CONFIRM_EVENT_ATTRIBUTE);
						}
						else{
							ACCOUNT_TYPE = "passenger";
							USERNAME = SimpleDB.getSingleAttributesForItem(clientManager, SimpleDB.PASSENGER_ACCOUNT_TABLE, login_phoneNumber_editText.getText().toString(), SimpleDB.PASSENGER_ACCOUNT_USERNAME_ATTRIBUTE);
							PHOTO_URL = SimpleDB.getSingleAttributesForItem(clientManager, SimpleDB.PASSENGER_ACCOUNT_TABLE, login_phoneNumber_editText.getText().toString(), SimpleDB.PASSENGER_ACCOUNT_PHOTOURL_ATTRIBUTE);
							CONTACT_EVENT = SimpleDB.getSingleAttributesForItem(clientManager, SimpleDB.PASSENGER_ACCOUNT_TABLE, login_phoneNumber_editText.getText().toString(), SimpleDB.PASSENGER_ACCOUNT_CONTACT_EVENT_ATTRIBUTE);
							TEMP_CONFIRM_EVENT = SimpleDB.getSingleAttributesForItem(clientManager, SimpleDB.PASSENGER_ACCOUNT_TABLE, login_phoneNumber_editText.getText().toString(), SimpleDB.PASSENGER_ACCOUNT_TEMP_CONFIRM_EVENT_ATTRIBUTE);
							FINAL_CONFIRM_EVENT = SimpleDB.getSingleAttributesForItem(clientManager, SimpleDB.PASSENGER_ACCOUNT_TABLE, login_phoneNumber_editText.getText().toString(), SimpleDB.PASSENGER_ACCOUNT_FINAL_CONFIRM_EVENT_ATTRIBUTE);
						}

						Message msg = Message.obtain();
						msg.what = 3;
						handler.sendMessage(msg);


					}

				}).start();
			}

		});
	}


	private String forgotten_password;

	private void getForgottenPassword(){
		runOnUiThread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				new Thread(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (driverAccountTableItems.contains(forget_password_dialog_editText.getText().toString())){
							forgotten_password = SimpleDB.getSingleAttributesForItem(clientManager, SimpleDB.DRIVER_ACCOUNT_TABLE, forget_password_dialog_editText.getText().toString(), SimpleDB.DRIVER_ACCOUNT_PASSWORD_ATTRIBUTE);
						}
						else{
							forgotten_password = SimpleDB.getSingleAttributesForItem(clientManager, SimpleDB.PASSENGER_ACCOUNT_TABLE, forget_password_dialog_editText.getText().toString(), SimpleDB.PASSENGER_ACCOUNT_PASSWORD_ATTRIBUTE);
						}

						Message msg = Message.obtain();
						msg.what = 4;
						handler.sendMessage(msg);
					}

				}).start();
			}

		});
	}

	private void sendTextMessageWithPassword(){
		new Thread(new Runnable(){
			@Override
			public void run() {

				try {
					sendMessage();
				} catch (IOException e) {

					e.printStackTrace();
				}

			}

		}).start();
	}

	private void sendMessage() throws IOException{
		Voice voice = new Voice("zhangyicheng1234@gmail.com","password");
		voice.login();
		String phoneNumber = forget_password_dialog_editText.getText().toString();
		voice.sendSMS(phoneNumber, "Your Carpooler password is:  " + forgotten_password + "  \nWelcome back.");
	}




	private EditText forget_password_dialog_editText;

	private void setForgetPasswordButtonControl(){
		forget_password_button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(LoginActivity.this);				
				LayoutInflater factory = LayoutInflater.from(LoginActivity.this);
				View deleteDialogView = factory.inflate(
						R.layout.forget_password_dialog_layout, null);
				dialogBuilder.setView(deleteDialogView);

				forget_password_dialog_editText = (EditText) deleteDialogView.findViewById(R.id.forget_password_dialog_editText);


				dialogBuilder.setPositiveButton("Send password", new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						if (!driverAccountTableItems.contains(forget_password_dialog_editText.getText().toString()) && 
								!passengerAccountTableItems.contains(forget_password_dialog_editText.getText().toString())){
							Toast.makeText(getBaseContext(), "The phone number is not registered.", Toast.LENGTH_LONG).show();
						}
						else{
							if (isConnectedToInternet()){
								getForgottenPassword();		
								dialog.cancel();
							}
							else{
								Toast.makeText(getBaseContext(), "Unable to connect to Internet, please check your network.", Toast.LENGTH_LONG).show();
							}

						}
						hideKeyboard();
					}

				});

				dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.cancel();
						hideKeyboard();
					}

				});

				dialogBuilder.create().show();


			}
		});
	}









	private void setExploreButtonControl(){
		explore_button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

			}
		});
	}


	private void setRegisterButtonControl(){
		register_button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				Intent intent = new Intent("yicheng.carpoolapp.REGISTERACTIVITY");
				startActivity(intent);
				finish();

			}
		});
	}







	private boolean isConnectedToInternet(){
		ConnectivityManager connectivityManager = 
				(ConnectivityManager)LoginActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo wifi_network = 
				connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo mobile_network = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);


		return wifi_network.isConnectedOrConnecting() || mobile_network.isConnectedOrConnecting();

		/*return mobile_network.isConnectedOrConnecting();*/
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


	private void loadAccountTable(){
		runOnUiThread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				new Thread(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						String [] driverAccountTableItemArray = SimpleDB.getItemNamesForDomain(LoginActivity.clientManager, SimpleDB.DRIVER_ACCOUNT_TABLE);
						for (int i = 0; i < driverAccountTableItemArray.length; i++){
							driverAccountTableItems.add(driverAccountTableItemArray[i]);
						}

						String [] passengerAccountTableItemArray = SimpleDB.getItemNamesForDomain(LoginActivity.clientManager, SimpleDB.PASSENGER_ACCOUNT_TABLE);
						for (int i = 0; i < passengerAccountTableItemArray.length; i++){
							passengerAccountTableItems.add(passengerAccountTableItemArray[i]);
						}

						Message msg = Message.obtain();
						msg.what = 1;
						handler.sendMessage(msg);



					}

				}).start();
			}

		});
	}





}
