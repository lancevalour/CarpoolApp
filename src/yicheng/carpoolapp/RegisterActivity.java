package yicheng.carpoolapp;




import java.io.IOException;
import java.util.ArrayList;








import com.techventus.server.voice.Voice;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


public class RegisterActivity extends Activity{ 

	private EditText register_phoneNumber_editText, register_username_editText, register_password_editText, register_password_again_editText;
	private Button register_confirm_button, register_back_button;
	private RelativeLayout register_activity_layout;
	private ToggleButton register_toggleButton;


	private ArrayList<String> driverAccountTableItems = new ArrayList<String>();
	private ArrayList<String> passengerAccountTableItems = new ArrayList<String>();





	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.activity_left_in, R.anim.activity_left_out);
		setContentView(R.layout.register_activity_layout);

		if (isConnectedToInternet()){
			loadAccountTable();
		}
		else{
			Toast.makeText(getBaseContext(), "Unable to connect to Internet, please check your network.", Toast.LENGTH_LONG).show();
			Handler newHandler = new Handler();
			newHandler.postDelayed(new Runnable(){

				@Override
				public void run() {
					// TODO Auto-generated method stub
					finish();
					overridePendingTransition(R.anim.activity_right_in, R.anim.activity_right_out);
				}

			}, 1500);
		}

		initiateComponents();
		setTouchHideKeyboardControl();

		setBackButtonControl();
		setConfirmButtonControl();

	}

	private void initiateComponents(){
		register_phoneNumber_editText = (EditText) findViewById(R.id.register_phoneNumber_editText);
		register_username_editText = (EditText) findViewById(R.id.register_username_editText);
		register_password_editText = (EditText) findViewById(R.id.register_password_editText);
		register_password_again_editText = (EditText) findViewById(R.id.register_password_again_editText);
		register_confirm_button = (Button) findViewById(R.id.register_confirm_button);
		register_back_button = (Button) findViewById(R.id.register_back_button);
		register_activity_layout = (RelativeLayout) findViewById(R.id.register_activity_layout);
		register_toggleButton = (ToggleButton) findViewById(R.id.register_toggleButton);
		register_toggleButton.setChecked(false);
	}

	private void hideKeyboard(){
		if(getCurrentFocus()!=null) {
			InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
		}
	}

	private void setTouchHideKeyboardControl(){
		register_activity_layout.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				hideKeyboard();
				return false;
			}
		});

		register_back_button.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				hideKeyboard();
				return false;
			}
		});

		register_confirm_button.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				hideKeyboard();
				return false;
			}
		});
	}










	private void setBackButtonControl(){
		register_back_button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(RegisterActivity.this);	
				
				LayoutInflater factory = LayoutInflater.from(RegisterActivity.this);
				View view = factory.inflate(
						R.layout.custom_dialog_layout, null);
				dialogBuilder.setView(view);
				TextView dialog_title = (TextView) view.findViewById(R.id.custom_dialog_title);
				dialog_title.setText("Go Back");
				TextView dialog_content = (TextView) view.findViewById(R.id.custom_dialog_content);
				dialog_content.setText("Are you sure to exit register?");
				
				
				dialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {

						Intent intent = new Intent("yicheng.carpoolapp.LOGINACTIVITY");
						startActivity(intent);

						finish();
						overridePendingTransition(R.anim.activity_right_in, R.anim.activity_right_out);
					}
				});

				dialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});

				AlertDialog dialog = dialogBuilder.create();
				dialog.show();

			}
		});
	}


	private void setConfirmButtonControl(){
		register_confirm_button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub


				if (register_phoneNumber_editText.getText().toString().length() < 1 || 
						register_username_editText.getText().toString().length() < 1 ||
						register_password_editText.getText().toString().length() < 1 ||
						register_password_again_editText.getText().toString().length() < 1){
					Toast.makeText(getBaseContext(), "One of your information is empty, please check.", Toast.LENGTH_LONG).show();

				}
				else{
					if (driverAccountTableItems.contains(register_phoneNumber_editText.getText().toString()) || passengerAccountTableItems.contains(register_phoneNumber_editText.getText().toString())){
						Toast.makeText(getBaseContext(), "The phone numeber is already registered.", Toast.LENGTH_LONG).show();
					}
					else{
						if (!register_password_editText.getText().toString().equals(register_password_again_editText.getText().toString())){
							Toast.makeText(getBaseContext(), "Passwords don't match.", Toast.LENGTH_LONG).show();
						}
						else{

							AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(RegisterActivity.this);	
							
							LayoutInflater factory = LayoutInflater.from(RegisterActivity.this);
							View view = factory.inflate(
									R.layout.custom_dialog_layout, null);
							dialogBuilder.setView(view);
							TextView dialog_title = (TextView) view.findViewById(R.id.custom_dialog_title);
							dialog_title.setText("Confirmation");
							TextView dialog_content = (TextView) view.findViewById(R.id.custom_dialog_content);
							

							if (!register_toggleButton.isChecked()){
								dialog_content.setText("Ready to be a passenger?");
								//dialogBuilder.setMessage("Ready to be a passenger?");	
							}
							else{
								dialog_content.setText("Ready to be a driver?");
							}
							
							dialogBuilder.setCancelable(true);
							dialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									goToConfirmationCodeActivity();	
								}


							});
							dialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									dialog.cancel();
								}


							});
							dialogBuilder.create().show();

						}
					}
				}
			}
		});
	}




	private void goToConfirmationCodeActivity(){
		Toast.makeText(getBaseContext(), "We have sent you a text message with confirmation code.", Toast.LENGTH_LONG).show();
		int randomNum = (int)(Math.random()*10000) + 1000;			
		if (randomNum / 10000 >= 1){
			randomNum = randomNum - 1000;
		}
		if (randomNum == 10000){
			randomNum = 9999;
		}

		String sentConfirmationCode = "" + randomNum;

		//String sentConfirmationCode = "1234";


		sendTextMessageWithCode(sentConfirmationCode);



		Bundle bundle = new Bundle();
		bundle.putBoolean("isDriver", register_toggleButton.isChecked());
		bundle.putString("phoneNumber", register_phoneNumber_editText.getText().toString());
		bundle.putString("password", register_password_editText.getText().toString());
		bundle.putString("username", register_username_editText.getText().toString());
		bundle.putString("confirmationCode", sentConfirmationCode);

		Intent intent = new Intent("yicheng.carpoolapp.CONFIRMATIONCODEACTIVITY");
		intent.putExtras(bundle);
		startActivity(intent);
		finish();
		overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
	}

	private void sendTextMessageWithCode(final String sentConfirmationCode){
		runOnUiThread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				new Thread(new Runnable(){
					@Override
					public void run() {

						try {
							sendMessage(sentConfirmationCode);
						} catch (IOException e) {

							e.printStackTrace();
						}

					}

				}).start();
			}
			
		});
		
	}

	private void sendMessage(String sentConfirmationCode) throws IOException{
		Voice voice = new Voice("zhangyicheng1234@gmail.com", "password");
		voice.login();
		String phoneNumber = register_phoneNumber_editText.getText().toString();
		voice.sendSMS(phoneNumber, "Your Carpooler verification code is:  " + sentConfirmationCode + "  Please confirm.");
	}



	
	
	
	
	
	
	
	
	

	private boolean isConnectedToInternet(){
		ConnectivityManager connectivityManager = 
				(ConnectivityManager)RegisterActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);

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




					}

				}).start();
			}

		});
	}



}
