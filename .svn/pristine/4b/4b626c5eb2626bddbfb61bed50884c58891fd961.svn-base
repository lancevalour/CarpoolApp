package yicheng.carpoolapp;










import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MeSettingActivity extends Activity{

	private Button me_setting_logout_button, me_setting_my_photo_button, me_setting_nickname_button, me_setting_password_button, me_setting_back_button;
	private ImageButton me_setting_my_photo_imageButton;
	private TextView me_setting_nickname_textView;


	public static AmazonClientManager clientManager;

	public static SharedPreferences local_user_information;
	private SharedPreferences.Editor local_user_editor;
	private String PREFS_NAME = "LocalUserInfo";



	private String PHONE_NUMBER;
	private String USERNAME;
	private String PASSWORD;
	private String ACCOUNT_TYPE;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.activity_left_in, R.anim.activity_left_out);
		setContentView(R.layout.me_setting_activity_layout);

		clientManager = new AmazonClientManager();

		local_user_information =  this.getSharedPreferences(PREFS_NAME,0);
		PHONE_NUMBER = local_user_information.getString("phoneNumber", "default");
		USERNAME = local_user_information.getString("username", "default");
		PASSWORD = local_user_information.getString("password", "default");
		ACCOUNT_TYPE = local_user_information.getString("accountType", "default");

		initiateComponents();
		setLogoutButtonControl();
		setChangePasswordButtonControl();
		setChangeUsernameButtonControl();
		setBackButtonControl();
		

	}

	private void initiateComponents(){
		me_setting_logout_button = (Button) findViewById(R.id.me_setting_logout_button);
		me_setting_my_photo_button = (Button) findViewById(R.id.me_setting_my_photo_button);
		me_setting_nickname_button = (Button) findViewById(R.id.me_setting_nickname_button);
		me_setting_password_button = (Button) findViewById(R.id.me_setting_password_button);
		me_setting_back_button = (Button) findViewById(R.id.me_setting_back_button);
		me_setting_my_photo_imageButton = (ImageButton) findViewById(R.id.me_setting_my_photo_imageButton);
		
		
		if (ACCOUNT_TYPE.equals("driver")){
			me_setting_my_photo_imageButton.setImageResource(R.drawable.ic_action_driver);
		}
		else{
			me_setting_my_photo_imageButton.setImageResource(R.drawable.ic_action_passenger);
		}
		
		me_setting_nickname_textView = (TextView) findViewById(R.id.me_setting_nickname_textView);

		me_setting_nickname_textView.setText(USERNAME);

	}
	
	private void setBackButtonControl(){
		me_setting_back_button.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
				overridePendingTransition(R.anim.activity_right_in, R.anim.activity_right_out);
			}
		});
	}
	
	

	private void setLogoutButtonControl(){
		me_setting_logout_button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				local_user_editor = local_user_information.edit();
				local_user_editor.remove("phoneNumber");
				local_user_editor.remove("password");
				local_user_editor.remove("accountType");
				local_user_editor.remove("photoUrl");
				local_user_editor.remove("username");
				local_user_editor.remove("contactEvent");
				local_user_editor.remove("tempConfirmEvent");
				local_user_editor.remove("finalConfirmEvent");
				local_user_editor.remove("isLoggedIn");
				local_user_editor.commit();


				Intent intent = new Intent("yicheng.carpoolapp.LOGINACTIVITY");
				startActivity(intent);
				finish();

			}
		});
	}

	private void setChangePasswordButtonControl(){
		me_setting_password_button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MeSettingActivity.this);	
				LayoutInflater factory = LayoutInflater.from(MeSettingActivity.this);
				View view = factory.inflate(
						R.layout.change_password_dialog_layout, null);
				dialogBuilder.setView(view);

				final EditText change_password_dialog_editText = (EditText) view.findViewById(R.id.change_password_dialog_editText);
				final EditText change_password_second_dialog_editText = (EditText) view.findViewById(R.id.change_password_second_dialog_editText);



				dialogBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub


						if (change_password_dialog_editText.getText().toString().length() < 1 ||
								change_password_second_dialog_editText.getText().toString().length() < 1){
							Toast.makeText(getBaseContext(), "One of your information is empty, please check.", Toast.LENGTH_LONG).show();
						}
						else{
							if (!change_password_dialog_editText.getText().toString().equals(change_password_second_dialog_editText.getText().toString())){
								Toast.makeText(getBaseContext(), "Passwords don't match.", Toast.LENGTH_LONG).show();
							}
							else{
								if (PASSWORD.equals(change_password_dialog_editText.getText().toString())){
									Toast.makeText(getBaseContext(), "New password should be different.", Toast.LENGTH_LONG).show();
								}
								else{
									if (isConnectedToInternet()){
										local_user_editor = local_user_information.edit();
										local_user_editor.putString("password", change_password_dialog_editText.getText().toString());
										local_user_editor.commit();
										changeUserPassword(change_password_dialog_editText.getText().toString());
									}
									else{
										Toast.makeText(getBaseContext(), "Unable to connect to Internet, please check your network.", Toast.LENGTH_LONG).show();
									}
								}
							}
						}



					}

				});

				dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.cancel();
					}

				});

				dialogBuilder.create().show();
			}
		});
	}

	private void changeUserPassword(final String newPassword){
		runOnUiThread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				new Thread(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (ACCOUNT_TYPE.equals("driver")){
							SimpleDB.updateSingleAttribute(clientManager, SimpleDB.DRIVER_ACCOUNT_TABLE, PHONE_NUMBER, SimpleDB.DRIVER_ACCOUNT_PASSWORD_ATTRIBUTE, newPassword);
						}
						else{
							SimpleDB.updateSingleAttribute(clientManager, SimpleDB.PASSENGER_ACCOUNT_TABLE, PHONE_NUMBER, SimpleDB.PASSENGER_ACCOUNT_PASSWORD_ATTRIBUTE, newPassword);
						}
					}

				}).start();
			}

		});
	}

	private void setChangeUsernameButtonControl(){
		me_setting_nickname_button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MeSettingActivity.this);	
				LayoutInflater factory = LayoutInflater.from(MeSettingActivity.this);
				View view = factory.inflate(
						R.layout.change_username_dialog_layout, null);
				dialogBuilder.setView(view);

				final EditText change_username_dialog_editText = (EditText) view.findViewById(R.id.change_username_dialog_editText);

				change_username_dialog_editText.setText(USERNAME);
				int textLength = change_username_dialog_editText.getText().length();
				change_username_dialog_editText.setSelection(textLength, textLength);

				dialogBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						if (!change_username_dialog_editText.getText().toString().equals(USERNAME)){
							if (isConnectedToInternet()){
								changeUserUsername(change_username_dialog_editText.getText().toString());

								local_user_editor = local_user_information.edit();
								local_user_editor.putString("username", change_username_dialog_editText.getText().toString());
								local_user_editor.commit();

								USERNAME = local_user_information.getString("username", "default");
								me_setting_nickname_textView.setText(USERNAME);

							}
							else{
								Toast.makeText(getBaseContext(), "Unable to connect to Internet, please check your network.", Toast.LENGTH_LONG).show();
							}
						}
						else{
							Toast.makeText(getBaseContext(), "New username should be different.", Toast.LENGTH_LONG).show();

						}




					}

				});

				dialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.cancel();
					}

				});

				dialogBuilder.create().show();

			}
		});
	}

	private void changeUserUsername(final String newUsername){
		runOnUiThread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				new Thread(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (ACCOUNT_TYPE.equals("driver")){
							SimpleDB.updateSingleAttribute(clientManager, SimpleDB.DRIVER_ACCOUNT_TABLE, PHONE_NUMBER, SimpleDB.DRIVER_ACCOUNT_USERNAME_ATTRIBUTE, newUsername);
						}
						else{
							SimpleDB.updateSingleAttribute(clientManager, SimpleDB.PASSENGER_ACCOUNT_TABLE, PHONE_NUMBER, SimpleDB.PASSENGER_ACCOUNT_USERNAME_ATTRIBUTE, newUsername);
						}
					}

				}).start();
			}

		});
	}


	
	
	
	
	
	private boolean isConnectedToInternet(){
		ConnectivityManager connectivityManager = 
				(ConnectivityManager)MeSettingActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo wifi_network = 
				connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo mobile_network = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		//Only for testing
		return wifi_network.isConnectedOrConnecting() || mobile_network.isConnectedOrConnecting();
		/*return  mobile_network.isConnectedOrConnecting();*/
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.activity_right_in, R.anim.activity_right_out);
	}


}
