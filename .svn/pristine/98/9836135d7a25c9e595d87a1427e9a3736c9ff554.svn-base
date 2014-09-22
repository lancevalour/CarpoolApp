package yicheng.carpoolapp;


import java.text.SimpleDateFormat;
import java.util.Date;




import java.util.HashMap;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class PostEventActivity extends Activity{
	private Button post_event_back_button, post_event_publish_button;
	private TextView post_event_title_textView, post_event_content_textView, post_event_date_textView, post_event_postUsername_textView, post_event_availableSeat_textView;
	private ImageView post_event_imageView;
	private FrameLayout post_event_layout;
	private RelativeLayout post_event_card_layout;

	public static AmazonClientManager clientManager;

	private SharedPreferences local_user_information;
	private SharedPreferences.Editor local_user_editor;
	private String PREFS_NAME = "LocalUserInfo";


	private String PHONE_NUMBER;
	private String USERNAME;
	private String PASSWORD;
	private String ACCOUNT_TYPE;
	private String PHOTO_URL;




	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		local_user_information =  this.getSharedPreferences(PREFS_NAME,0);
		overridePendingTransition(R.anim.activity_left_in, R.anim.activity_left_out);
		setContentView(R.layout.post_event_activity);

		clientManager = new AmazonClientManager();

		PHONE_NUMBER = local_user_information.getString("phoneNumber", "default");
		USERNAME = local_user_information.getString("username", "default");
		PASSWORD = local_user_information.getString("password", "default");
		ACCOUNT_TYPE = local_user_information.getString("accountType", "default");
		PHOTO_URL = local_user_information.getString("photoUrl", "default");

		initiateComponents();

		setAccountTypeDisplay();		
		setBackButtonControl();
		setTextViewControl();

		setPublishButtonControl();



	}

	private void initiateComponents(){
		post_event_back_button = (Button) findViewById(R.id.post_event_back_button);
		post_event_publish_button = (Button) findViewById(R.id.post_event_publish_button);
		post_event_imageView = (ImageView) findViewById(R.id.post_event_imageView);
		if (ACCOUNT_TYPE.equals("driver")){
			post_event_imageView.setImageResource(R.drawable.ic_action_driver);
		}
		else{
			post_event_imageView.setImageResource(R.drawable.ic_action_passenger);
		}
		post_event_title_textView = (TextView) findViewById(R.id.post_event_title_textView);
		post_event_content_textView = (TextView) findViewById(R.id.post_event_content_textView);
		post_event_date_textView = (TextView) findViewById(R.id.post_event_date_textView);
		post_event_postUsername_textView = (TextView) findViewById(R.id.post_event_postUsername_textView);
		post_event_availableSeat_textView = (TextView) findViewById(R.id.post_event_availableSeat_textView);
		post_event_layout = (FrameLayout) findViewById(R.id.post_event_layout);
		post_event_card_layout = (RelativeLayout) findViewById(R.id.post_event_card_layout);

	}

	private void setAccountTypeDisplay(){
		if (ACCOUNT_TYPE.equals("passenger")){
			post_event_availableSeat_textView.setVisibility(View.INVISIBLE);
			post_event_card_layout.setBackground(getResources().getDrawable(R.drawable.passenger_event_card_selector));
			float scale = getResources().getDisplayMetrics().density;
			int padding_15dp = (int) (15 * scale + 0.5f);	
			post_event_card_layout.setPadding(padding_15dp, padding_15dp, padding_15dp, padding_15dp);
		}

		post_event_postUsername_textView.setText(USERNAME);

	}

	private void setBackButtonControl(){
		post_event_back_button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
				overridePendingTransition(R.anim.activity_right_in, R.anim.activity_right_out);
			}
		});
	}

	private void setTextViewControl(){
		setTitleTextViewControl();
		setContentTextViewControl();
		setDateTextViewControl();

		if (ACCOUNT_TYPE.equals("driver")){
			setAvailableSeatNumberTextViewControl();
		}
	}

	private void setContentTextViewControl(){
		post_event_content_textView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(PostEventActivity.this);	
				LayoutInflater factory = LayoutInflater.from(PostEventActivity.this);
				View view = factory.inflate(
						R.layout.post_event_content_dialog_layout, null);
				dialogBuilder.setView(view);

				final EditText post_event_content_dialog_editText = (EditText) view.findViewById(R.id.post_event_content_dialog_editText);
				final TextView post_event_content_dialog_textView = (TextView) view.findViewById(R.id.post_event_content_dialog_textView);

				if (!post_event_content_textView.getText().toString().equals("Touch to add content")){
					post_event_content_dialog_editText.setText(post_event_content_textView.getText().toString());
					int textLength = post_event_content_dialog_editText.getText().length();
					post_event_content_dialog_editText.setSelection(textLength, textLength);
					post_event_content_dialog_textView.setText(String.valueOf(50 - post_event_content_textView.getText().length()));

				}
				else{
					post_event_content_dialog_editText.setText("");
				}



				TextWatcher textWatcher = new TextWatcher(){
					private CharSequence temp;
					private int selectionStart;
					private int selectionEnd;
					@Override
					public void beforeTextChanged(CharSequence s, int start, int count,
							int after) {
						// TODO Auto-generated method stub
						temp = s;

					}

					@Override
					public void onTextChanged(CharSequence s, int start, int before,
							int count) {
						// TODO Auto-generated method stub
						post_event_content_dialog_textView.setText(String.valueOf(200 - s.length()));



					}

					@Override
					public void afterTextChanged(Editable s) {
						// TODO Auto-generated method stub
						selectionStart = post_event_content_dialog_editText.getSelectionStart();                    
						selectionEnd = post_event_content_dialog_editText.getSelectionEnd();    
						if (temp.length() > 200) {                        

							s.delete(selectionStart-1, selectionEnd);                        
							int tempSelection = selectionStart;                        
							post_event_content_dialog_editText.setText(s);                        
							post_event_content_dialog_editText.setSelection(tempSelection);                    
						}   
					}

				};

				post_event_content_dialog_editText.addTextChangedListener(textWatcher);



				dialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub

						if (post_event_content_dialog_editText.getText().toString().trim().length() < 1){
							post_event_content_textView.setText("Touch to add content");
						}
						else{
							post_event_content_textView.setText(post_event_content_dialog_editText.getText().toString());
							ViewGroup.LayoutParams params = post_event_content_textView.getLayoutParams();
							params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
							post_event_content_textView.setLayoutParams(params);
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


	private void setDateTextViewControl(){
		post_event_date_textView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(PostEventActivity.this);				
				LayoutInflater factory = LayoutInflater.from(PostEventActivity.this);
				View view = factory.inflate(
						R.layout.post_event_date_picker_layout, null);

				final DatePicker date_picker = (DatePicker) view.findViewById(R.id.date_picker);
				final TimePicker time_picker = (TimePicker) view.findViewById(R.id.time_picker);


				dialogBuilder.setView(view);
				dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {

						SimpleDateFormat dateFormat = new SimpleDateFormat("yy/MM/dd h:mm a");

						SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat("EE");
						Date dayOfWeekDate = new Date(date_picker.getYear(), date_picker.getMonth(), date_picker.getDayOfMonth() - 1);

						String dayOfWeekText = dayOfWeekFormat.format(dayOfWeekDate);


						Date date = new Date(date_picker.getYear(), date_picker.getMonth(), date_picker.getDayOfMonth(),time_picker.getCurrentHour(), time_picker.getCurrentMinute());

						if (date.before(new Date())){
							Toast.makeText(getApplicationContext(), "Please choose a time after now.", Toast.LENGTH_LONG).show();
							dialog.cancel();
						}
						else{

							if (isConnectedToInternet()){



								String event_date = dateFormat.format(date);
								event_date = dayOfWeekText + ", " + event_date;

								post_event_date_textView.setText(event_date);

								
								ViewGroup.LayoutParams params = post_event_postUsername_textView.getLayoutParams();
								params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
								post_event_postUsername_textView.setLayoutParams(params);
								

								ViewGroup.LayoutParams params1 = post_event_date_textView.getLayoutParams();
								params1.height = ViewGroup.LayoutParams.WRAP_CONTENT;
								post_event_date_textView.setLayoutParams(params1);





							}
							else{
								Toast.makeText(getApplicationContext(), "Unable to connect to Internet, please check your network.", Toast.LENGTH_SHORT).show();
							}


						}




					}
				});

				dialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {

						dialog.cancel();

					}
				});

				dialogBuilder.create().show();
			}
		});
	}

	private void setAvailableSeatNumberTextViewControl(){
		post_event_availableSeat_textView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(PostEventActivity.this);	
				LayoutInflater factory = LayoutInflater.from(PostEventActivity.this);
				View view = factory.inflate(
						R.layout.post_event_seat_number_dialog_layout, null);
				dialogBuilder.setView(view);

				final NumberPicker seat_numberPicker = (NumberPicker) view.findViewById(R.id.seat_numberPicker);
				seat_numberPicker.setMaxValue(10);
				seat_numberPicker.setMinValue(0);
				seat_numberPicker.setWrapSelectorWheel(true);
				seat_numberPicker.setValue(4);

				dialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub

						post_event_availableSeat_textView.setText(String.valueOf(seat_numberPicker.getValue()));
						ViewGroup.LayoutParams params = post_event_availableSeat_textView.getLayoutParams();
						params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
						post_event_availableSeat_textView.setLayoutParams(params);

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

	private void setTitleTextViewControl(){
		post_event_title_textView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(PostEventActivity.this);	
				LayoutInflater factory = LayoutInflater.from(PostEventActivity.this);
				View view = factory.inflate(
						R.layout.post_event_title_dialog_layout, null);
				dialogBuilder.setView(view);

				final EditText post_event_title_dialog_editText = (EditText) view.findViewById(R.id.post_event_title_dialog_editText);
				final TextView post_event_title_dialog_textView = (TextView) view.findViewById(R.id.post_event_title_dialog_textView);

				if (!post_event_title_textView.getText().toString().equals("Touch to add title")){
					post_event_title_dialog_editText.setText(post_event_title_textView.getText().toString());
					int textLength = post_event_title_dialog_editText.getText().length();
					post_event_title_dialog_editText.setSelection(textLength, textLength);
					post_event_title_dialog_textView.setText(String.valueOf(50 - post_event_title_textView.getText().length()));

				}
				else{
					post_event_title_dialog_editText.setText("");
				}



				TextWatcher preference_editText_watcher = new TextWatcher(){
					private CharSequence temp;
					private int selectionStart;
					private int selectionEnd;
					@Override
					public void beforeTextChanged(CharSequence s, int start, int count,
							int after) {
						// TODO Auto-generated method stub
						temp = s;

					}

					@Override
					public void onTextChanged(CharSequence s, int start, int before,
							int count) {
						// TODO Auto-generated method stub
						post_event_title_dialog_textView.setText(String.valueOf(50 - s.length()));



					}

					@Override
					public void afterTextChanged(Editable s) {
						// TODO Auto-generated method stub
						selectionStart = post_event_title_dialog_editText.getSelectionStart();                    
						selectionEnd = post_event_title_dialog_editText.getSelectionEnd();    
						if (temp.length() > 50) {                        

							s.delete(selectionStart-1, selectionEnd);                        
							int tempSelection = selectionStart;                        
							post_event_title_dialog_editText.setText(s);                        
							post_event_title_dialog_editText.setSelection(tempSelection);                    
						}   
					}

				};

				post_event_title_dialog_editText.addTextChangedListener(preference_editText_watcher);



				dialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub

						if (post_event_title_dialog_editText.getText().toString().trim().length() < 1){
							post_event_title_textView.setText("Touch to add title");
						}
						else{
							post_event_title_textView.setText(post_event_title_dialog_editText.getText().toString());
							ViewGroup.LayoutParams params = post_event_title_textView.getLayoutParams();
							params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
							post_event_title_textView.setLayoutParams(params);
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

	private void setPublishButtonControl(){
		post_event_publish_button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				if (post_event_title_textView.getText().toString().equals("Touch to add title") ||
						post_event_content_textView.getText().toString().equals("Touch to add content") ||
						post_event_date_textView.toString().equals("Touch to add date") || 
						(ACCOUNT_TYPE.equals("driver") && 
								post_event_availableSeat_textView.toString().equals("Touch to add available seat"))){
					Toast.makeText(getBaseContext(), "One of your event information is empty, please check.", Toast.LENGTH_LONG).show();
				}
				else{
					addEventToEventTable();
					finish();
				}


			}
		});
	}

	private void addEventToEventTable(){
		runOnUiThread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				new Thread(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (ACCOUNT_TYPE.equals("driver")){

							SimpleDB.createItem(clientManager, SimpleDB.DRIVER_EVENT_TABLE, PHONE_NUMBER);

							HashMap<String, String> map = new HashMap<String, String>();
							map.put(SimpleDB.DRIVER_EVENT_EVENT_TITLE_ATTRIBUTE, post_event_title_textView.getText().toString());
							map.put(SimpleDB.DRIVER_EVENT_EVENT_CONTENT_ATTRIBUTE, post_event_content_textView.getText().toString());
							map.put(SimpleDB.DRIVER_EVENT_EVENT_DATE_ATTRIBUTE, post_event_date_textView.getText().toString());
							map.put(SimpleDB.DRIVER_EVENT_POST_USERNAME_ATTRIBUTE, post_event_postUsername_textView.getText().toString());
							map.put(SimpleDB.DRIVER_EVENT_POST_PHOTOURL_ATTRIBUTE, PHOTO_URL);
							map.put(SimpleDB.DRIVER_EVENT_AVAILABLE_SEAT_NUMBER_ATTRIBUTE, post_event_availableSeat_textView.getText().toString());
							map.put(SimpleDB.DRIVER_EVENT_FINAL_CONFIRM_PASSENGER_ATTRIBUTE, "");
							map.put(SimpleDB.DRIVER_EVENT_INTERESTED_PASSENGER_ATTRIBUTE, "");
							map.put(SimpleDB.DRIVER_EVENT_TEMP_CONFIRM_PASSENGER_ATTRIBUTE, "");

							SimpleDB.updateAttributesForItem(clientManager, SimpleDB.DRIVER_EVENT_TABLE, PHONE_NUMBER, map);
						}
						else{

							SimpleDB.createItem(clientManager, SimpleDB.PASSENGER_EVENT_TABLE, PHONE_NUMBER);

							HashMap<String, String> map = new HashMap<String, String>();
							map.put(SimpleDB.PASSENGER_EVENT_EVENT_TITLE_ATTRIBUTE, post_event_title_textView.getText().toString());
							map.put(SimpleDB.PASSENGER_EVENT_EVENT_CONTENT_ATTRIBUTE, post_event_content_textView.getText().toString());
							map.put(SimpleDB.PASSENGER_EVENT_EVENT_DATE_ATTRIBUTE, post_event_date_textView.getText().toString());
							map.put(SimpleDB.PASSENGER_EVENT_POST_USERNAME_ATTRIBUTE, post_event_postUsername_textView.getText().toString());
							map.put(SimpleDB.PASSENGER_EVENT_POST_PHOTOURL_ATTRIBUTE, PHOTO_URL);
							map.put(SimpleDB.PASSENGER_EVENT_AVAILABLE_SEAT_NUMBER_ATTRIBUTE, post_event_availableSeat_textView.getText().toString());
							map.put(SimpleDB.PASSENGER_EVENT_FINAL_CONFIRM_DRIVER_ATTRIBUTE, "");
							map.put(SimpleDB.PASSENGER_EVENT_INTERESTED_DRIVER_ATTRIBUTE, "");
							map.put(SimpleDB.PASSENGER_EVENT_TEMP_CONFIRM_DRIVER_ATTRIBUTE, "");
							map.put(SimpleDB.PASSENGER_EVENT_INTERESTED_EXTRA_PASSENGER_ATTRIBUTE, "");
							map.put(SimpleDB.PASSENGER_EVENT_TEMP_CONFIRM_EXTRA_PASSENGER_ATTRIBUTE, "");
							map.put(SimpleDB.PASSENGER_EVENT_FINAL_CONFIRM_EXTRA_PASSENGER_ATTRIBUTE, "");

							
							
							SimpleDB.updateAttributesForItem(clientManager, SimpleDB.PASSENGER_EVENT_TABLE, PHONE_NUMBER, map);
						}
					}

				}).start();
			}

		});
	}


	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.activity_right_in, R.anim.activity_right_out);
	}

	private void hideKeyboard(){
		if(getCurrentFocus()!=null) {
			InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
		}
	}

	private boolean isConnectedToInternet(){
		ConnectivityManager connectivityManager = 
				(ConnectivityManager)PostEventActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo wifi_network = 
				connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo mobile_network = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);


		return wifi_network.isConnectedOrConnecting() || mobile_network.isConnectedOrConnecting();

		/*return mobile_network.isConnectedOrConnecting();*/
	}

}
