package yicheng.carpoolapp;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesResult;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class EventInfoActivity extends Activity{
	private Button event_info_back_button, event_info_contact_button;
	private ImageView event_info_imageView;
	private TextView event_info_title_textView, event_info_content_textView, 
	event_info_date_textView, event_info_postUsername_textView, event_info_availableSeat_textView,
	event_info_final_confirm_passenger_textView,event_info_final_confirm_driver_textView,
	event_info_final_confirm_extra_passenger_textView;

	public static AmazonClientManager clientManager;

	public static SharedPreferences local_user_information;
	private SharedPreferences.Editor local_user_editor;
	private String PREFS_NAME = "LocalUserInfo";

	private String PHONE_NUMBER;
	private String ACCOUNT_TYPE;

	private String EVENT_PHONENUMBER;
	private String EVENT_TYPE;
	private String EVENT_TITLE;
	private String EVENT_CONTENT;
	private String EVENT_DATE;
	private String POST_USERNAME;
	private String POST_PHOTO_URL;
	private String AVAILABLE_SEAT_NUMBER;
	private String FINAL_CONFIRM_PASSENGER;
	private String FINAL_CONFIRM_DRIVER;
	private String FINAL_CONFIRM_EXTRA_PASSENGER;


	private Handler handler;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		Bundle bundle = intent.getBundleExtra("eventInfo");
		EVENT_TYPE = bundle.getString("eventType");
		overridePendingTransition(R.anim.activity_left_in, R.anim.activity_left_out);
		if (EVENT_TYPE.equals("driver")){
			setContentView(R.layout.driver_event_info_activity_layout);		
			EVENT_TITLE = bundle.getString("eventTitle");
			EVENT_PHONENUMBER = bundle.getString("eventPhoneNumber");
			EVENT_CONTENT = bundle.getString("eventContent");
			EVENT_DATE = bundle.getString("eventDate");
			POST_USERNAME = bundle.getString("postUsername");
			POST_PHOTO_URL = bundle.getString("postPhotoUrl");
			AVAILABLE_SEAT_NUMBER = bundle.getString("availableSeatNumber");
			FINAL_CONFIRM_PASSENGER = bundle.getString("finalConfirmPassenger");
		}
		else{
			setContentView(R.layout.passenger_event_info_activity_layout);
			EVENT_TITLE = bundle.getString("eventTitle");
			EVENT_PHONENUMBER = bundle.getString("eventPhoneNumber");
			EVENT_CONTENT = bundle.getString("eventContent");
			EVENT_DATE = bundle.getString("eventDate");
			POST_USERNAME = bundle.getString("postUsername");
			POST_PHOTO_URL = bundle.getString("postPhotoUrl");
			AVAILABLE_SEAT_NUMBER = bundle.getString("availableSeatNumber");
			FINAL_CONFIRM_DRIVER = bundle.getString("finalConfirmDriver");
			FINAL_CONFIRM_EXTRA_PASSENGER = bundle.getString("finalConfirmExtraPassenger");
		}

		local_user_information =  this.getSharedPreferences(PREFS_NAME,0);
		PHONE_NUMBER = local_user_information.getString("phoneNumber", "default");
		ACCOUNT_TYPE = local_user_information.getString("accountType", "default");

		initiateComponents();

		setBackButtonControl();
		setEventInfoDisplay();
		setContactButtonControl();

		setHandleControl();


	}

	private void setHandleControl(){
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == 1){
					if (!myContactEventListContainsEventPhoneNumber() && 
							!myTempConfirmEventListContainsEventPhoneNumber()
							&& !myFinalConfirmEventListContainsEventPhoneNumber()){
						contactDatebaseUpdate();
						getInterestedUser();
					}

				}	
				if (msg.what == 2){
					contactEventDatabaseUpdate();
				}
			}
		};
	}

	private boolean myContactEventListContainsEventPhoneNumber(){
		for (int i = 0; i < contactEventPhoneNumberArray.length; i++){
			if (contactEventPhoneNumberArray[i].equals(EVENT_PHONENUMBER)){
				return true;
			}
		}
		return false;
	}

	private boolean myTempConfirmEventListContainsEventPhoneNumber(){
		for (int i = 0; i < tempConfirmEventPhoneNumberArray.length; i++){
			if (tempConfirmEventPhoneNumberArray[i].equals(EVENT_PHONENUMBER)){
				return true;
			}
		}
		return false;
	}

	private boolean myFinalConfirmEventListContainsEventPhoneNumber(){
		for (int i = 0; i < finalConfirmEventPhoneNumberArray.length; i++){
			if (finalConfirmEventPhoneNumberArray[i].equals(EVENT_PHONENUMBER)){
				return true;
			}
		}
		return false;
	}


	private String[] contactEventPhoneNumberArray;
	private String contactEventPhoneNumber;
	private String[] tempConfirmEventPhoneNumberArray;
	private String[] finalConfirmEventPhoneNumberArray;


	private Thread loadMyActiveEventThread;

	private void loadMyContactEvent(){
		clientManager = new AmazonClientManager();
		if (ACCOUNT_TYPE.equals("driver")){
			runOnUiThread(new Runnable(){

				@Override
				public void run() {
					// TODO Auto-generated method stub
					loadMyActiveEventThread = new Thread(new Runnable(){

						@Override
						public void run() {
							// TODO Auto-generated method stub
							Future<GetAttributesResult> future = SimpleDB.getInstance(clientManager).getAttributesAsync(new GetAttributesRequest(SimpleDB.DRIVER_ACCOUNT_TABLE, PHONE_NUMBER));

							try {
								GetAttributesResult result = future.get();
								List<Attribute> list = result.getAttributes();
								ArrayList<Attribute> myActiveEventAttributeOrderedList = new ArrayList<Attribute>(list);

								for (Attribute attribute: list){

									if (attribute.getName().equals("contactEventAttribute")){
										myActiveEventAttributeOrderedList.set(0, attribute);
									}
									else if (attribute.getName().equals("tempConfirmEventAttribute")){
										myActiveEventAttributeOrderedList.set(1, attribute);
									}
									else if (attribute.getName().equals("finalConfirmEventAttribute")){
										myActiveEventAttributeOrderedList.set(2, attribute);
									}

								}

								contactEventPhoneNumber = myActiveEventAttributeOrderedList.get(0).getValue();
								contactEventPhoneNumberArray = contactEventPhoneNumber.split("\n");
								tempConfirmEventPhoneNumberArray = myActiveEventAttributeOrderedList.get(1).getValue().split("\n");
								finalConfirmEventPhoneNumberArray = myActiveEventAttributeOrderedList.get(2).getValue().split("\n");

								Message msg = Message.obtain();
								msg.what = 1;
								handler.sendMessage(msg);

							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (ExecutionException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}

					});

					loadMyActiveEventThread.start();

				}

			});


		}
		else{
			runOnUiThread(new Runnable(){

				@Override
				public void run() {
					// TODO Auto-generated method stub
					loadMyActiveEventThread = new Thread(new Runnable(){

						@Override
						public void run() {
							// TODO Auto-generated method stub
							Future<GetAttributesResult> future = SimpleDB.getInstance(clientManager).getAttributesAsync(new GetAttributesRequest(SimpleDB.PASSENGER_ACCOUNT_TABLE, PHONE_NUMBER));

							try {
								GetAttributesResult result = future.get();
								List<Attribute> list = result.getAttributes();
								ArrayList<Attribute> myActiveEventAttributeOrderedList = new ArrayList<Attribute>(list);

								for (Attribute attribute: list){

									if (attribute.getName().equals("contactEventAttribute")){
										myActiveEventAttributeOrderedList.set(0, attribute);
									}
									else if (attribute.getName().equals("tempConfirmEventAttribute")){
										myActiveEventAttributeOrderedList.set(1, attribute);
									}
									else if (attribute.getName().equals("finalConfirmEventAttribute")){
										myActiveEventAttributeOrderedList.set(2, attribute);
									}

								}



								contactEventPhoneNumber = myActiveEventAttributeOrderedList.get(0).getValue();
								contactEventPhoneNumberArray = contactEventPhoneNumber.split("\n");
								tempConfirmEventPhoneNumberArray = myActiveEventAttributeOrderedList.get(1).getValue().split("\n");
								finalConfirmEventPhoneNumberArray = myActiveEventAttributeOrderedList.get(2).getValue().split("\n");


								Message msg = Message.obtain();
								msg.what = 1;
								handler.sendMessage(msg);

							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (ExecutionException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}

					});
					loadMyActiveEventThread.start();

				}

			});

		}

	}

	private String interestedPassengerArray;
	private String interestedDriverArray;
	private String interestedExtraPassengerArray;

	private void getInterestedUser(){
		if (EVENT_TYPE.equals("driver")){
			runOnUiThread(new Runnable(){

				@Override
				public void run() {
					// TODO Auto-generated method stub
					new Thread(new Runnable(){

						@Override
						public void run() {
							// TODO Auto-generated method stub
							Future<GetAttributesResult> future = SimpleDB.getInstance(clientManager).getAttributesAsync(new GetAttributesRequest(SimpleDB.DRIVER_EVENT_TABLE, EVENT_PHONENUMBER));

							try {
								GetAttributesResult result = future.get();
								List<Attribute> list = result.getAttributes();
								ArrayList<Attribute> orderedList = new ArrayList<Attribute>(list);

								for (Attribute attribute: list){

									if (attribute.getName().equals("interestedPassengerAttribute")){
										orderedList.set(0, attribute);
									}

								}

								interestedPassengerArray = orderedList.get(0).getValue();

								Message msg = Message.obtain();
								msg.what = 2;
								handler.sendMessage(msg);


								/*Synchronized(){
								driverEventList.add(new Event(driverEventPhoneNumberArray[index], orderedList.get(0).getValue(), orderedList.get(1).getValue(), orderedList.get(2).getValue(), orderedList.get(3).getValue(), orderedList.get(4).getValue(), orderedList.get(5).getValue(), ""));
							}*/
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (ExecutionException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}

					}).start();
				}

			});
		}
		else{
			runOnUiThread(new Runnable(){

				@Override
				public void run() {
					// TODO Auto-generated method stub
					new Thread(new Runnable(){

						@Override
						public void run() {
							// TODO Auto-generated method stub
							Future<GetAttributesResult> future = SimpleDB.getInstance(clientManager).getAttributesAsync(new GetAttributesRequest(SimpleDB.PASSENGER_EVENT_TABLE, EVENT_PHONENUMBER));

							try {
								GetAttributesResult result = future.get();
								List<Attribute> list = result.getAttributes();
								ArrayList<Attribute> orderedList = new ArrayList<Attribute>(list);

								for (Attribute attribute: list){

									if (attribute.getName().equals("interestedDriverAttribute")){
										orderedList.set(0, attribute);
									}
									else if (attribute.getName().equals("interestedExtraPassengerAttribute")){
										orderedList.set(1, attribute);
									}

								}

								interestedDriverArray = orderedList.get(0).getValue();
								interestedExtraPassengerArray = orderedList.get(1).getValue();




								Message msg = Message.obtain();
								msg.what = 2;
								handler.sendMessage(msg);

								/*Synchronized(){
									driverEventList.add(new Event(driverEventPhoneNumberArray[index], orderedList.get(0).getValue(), orderedList.get(1).getValue(), orderedList.get(2).getValue(), orderedList.get(3).getValue(), orderedList.get(4).getValue(), orderedList.get(5).getValue(), ""));
								}*/
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (ExecutionException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}

					}).start();
				}

			});
		}
	}

	private void contactEventDatabaseUpdate(){
		runOnUiThread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				new Thread(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (ACCOUNT_TYPE.equals("driver")){
							if (EVENT_TYPE.equals("passenger")){
								String newInterestedDriver = "";
								if (interestedDriverArray.trim().length() < 1){
									newInterestedDriver = PHONE_NUMBER + "\n";
								}
								else{
									newInterestedDriver = interestedDriverArray + PHONE_NUMBER + "\n" ;
								}


								SimpleDB.updateSingleAttribute(clientManager, SimpleDB.PASSENGER_EVENT_TABLE, EVENT_PHONENUMBER, SimpleDB.PASSENGER_EVENT_INTERESTED_DRIVER_ATTRIBUTE, newInterestedDriver);
							}
						}
						else{
							if (EVENT_TYPE.equals("driver")){
								String newInterestedPassenger = "";
								if (interestedPassengerArray.trim().length() < 1){
									newInterestedPassenger = PHONE_NUMBER + "\n";
								}
								else{
									newInterestedPassenger = interestedPassengerArray + PHONE_NUMBER + "\n" ;
								}


								SimpleDB.updateSingleAttribute(clientManager, SimpleDB.DRIVER_EVENT_TABLE, EVENT_PHONENUMBER, SimpleDB.DRIVER_EVENT_INTERESTED_PASSENGER_ATTRIBUTE, newInterestedPassenger);
							}
							else{
								if (!FINAL_CONFIRM_DRIVER.equals("")){
									String newInterestedExtraPassenger = "";
									if (interestedExtraPassengerArray.trim().length() < 1){
										newInterestedExtraPassenger = PHONE_NUMBER + "\n";
									}
									else{
										newInterestedExtraPassenger = interestedExtraPassengerArray + PHONE_NUMBER + "\n" ;
									}


									SimpleDB.updateSingleAttribute(clientManager, SimpleDB.PASSENGER_EVENT_TABLE, EVENT_PHONENUMBER, SimpleDB.PASSENGER_EVENT_INTERESTED_EXTRA_PASSENGER_ATTRIBUTE, newInterestedExtraPassenger);
									
								}
							}



						}
					}

				}).start();
			}

		});
	}



	private void contactDatebaseUpdate(){


		runOnUiThread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				new Thread(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (ACCOUNT_TYPE.equals("driver")){
							
							String newContactEventPhoneNumber;
							if (contactEventPhoneNumber.trim().length() < 1){
								newContactEventPhoneNumber = EVENT_PHONENUMBER + "\n";
							}
							else{
								newContactEventPhoneNumber = contactEventPhoneNumber + EVENT_PHONENUMBER + "\n" ;
							}

							SimpleDB.updateSingleAttribute(clientManager, SimpleDB.DRIVER_ACCOUNT_TABLE, PHONE_NUMBER, SimpleDB.DRIVER_ACCOUNT_CONTACT_EVENT_ATTRIBUTE, newContactEventPhoneNumber);

						}
						else{

							String newContactEventPhoneNumber;
							if (contactEventPhoneNumber.trim().length() < 1){
								newContactEventPhoneNumber = EVENT_PHONENUMBER + "\n";
							}
							else{
								newContactEventPhoneNumber = contactEventPhoneNumber + EVENT_PHONENUMBER + "\n" ;
							}
							SimpleDB.updateSingleAttribute(clientManager, SimpleDB.PASSENGER_ACCOUNT_TABLE, PHONE_NUMBER, SimpleDB.PASSENGER_ACCOUNT_CONTACT_EVENT_ATTRIBUTE, newContactEventPhoneNumber);


						}

					}

				}).start();
			}

		});


	}


	private void setContactButtonControl(){
		event_info_contact_button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				AlertDialog.Builder builder = new AlertDialog.Builder(EventInfoActivity.this);

				builder.setItems(new CharSequence []{"Call", "Text", "Cancel"}, new DialogInterface.OnClickListener(){
					public void onClick(DialogInterface dialog, int which) {
						if (which == 0){
							PhoneCallListener phoneListener = new PhoneCallListener();
							TelephonyManager telephonyManager = (TelephonyManager) EventInfoActivity.this.getSystemService(Context.TELEPHONY_SERVICE);
							telephonyManager.listen(phoneListener,PhoneStateListener.LISTEN_CALL_STATE);
							callEvent();
						}

						if (which == 1){
							textEvent();
							loadMyContactEvent();
						}

						if (which == 2){
							dialog.cancel();
						}

					}
				}).create().show();
			}
		});
	}

	private void textEvent(){
		Log.i("Send SMS", "");

		Intent smsIntent = new Intent(Intent.ACTION_VIEW);
		smsIntent.setData(Uri.parse("smsto:"));
		smsIntent.setType("vnd.android-dir/mms-sms");

		smsIntent.putExtra("address"  , EVENT_PHONENUMBER);
		smsIntent.putExtra("sms_body"  , "");
		try {
			startActivity(smsIntent);

			Log.i("Finished sending SMS...", "");
		} catch (android.content.ActivityNotFoundException ex) {
			Toast.makeText(EventInfoActivity.this, 
					"SMS faild, please try again later.", Toast.LENGTH_SHORT).show();
		}
	}

	private void callEvent(){
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(EventInfoActivity.this);
		dialogBuilder.setTitle("Make a call");

		String message = "Are you sure you want to call " + POST_USERNAME + "("+ EVENT_PHONENUMBER + ")?" ;
		dialogBuilder.setMessage(message);
		dialogBuilder.setCancelable(true);
		dialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {


				Uri number = Uri.parse("tel:" + EVENT_PHONENUMBER);
				Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
				startActivity(callIntent);

				loadMyContactEvent();

				dialog.cancel();



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

	private class PhoneCallListener extends PhoneStateListener {
		private boolean isPhoneCalling = false;
		String LOG_TAG = "LOGGING 123";
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {

			if (TelephonyManager.CALL_STATE_RINGING == state) {
				// phone ringing
				Log.i(LOG_TAG, "RINGING, number: " + incomingNumber);
			}

			if (TelephonyManager.CALL_STATE_OFFHOOK == state) {
				// active
				Log.i(LOG_TAG, "OFFHOOK");

				isPhoneCalling = true;
			}

			if (TelephonyManager.CALL_STATE_IDLE == state) {
				// run when class initial and phone call ended, 
				// need detect flag from CALL_STATE_OFFHOOK
				Log.i(LOG_TAG, "IDLE");

				if (isPhoneCalling) {

					Log.i(LOG_TAG, "restart app");

					// restart app
					Intent i = getBaseContext().getPackageManager()
							.getLaunchIntentForPackage(
									getBaseContext().getPackageName());
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(i);

					isPhoneCalling = false;
				}

			}
		}
	}









	private void initiateComponents(){
		if (EVENT_TYPE.equals("driver")){
			event_info_back_button = (Button) findViewById(R.id.driver_event_info_back_button);
			event_info_contact_button = (Button) findViewById(R.id.driver_event_info_contact_button);
			if (ACCOUNT_TYPE.equals("driver")){
				event_info_contact_button.setVisibility(View.INVISIBLE);	
			}
			event_info_title_textView = (TextView) findViewById(R.id.driver_event_info_title_textView);
			event_info_content_textView = (TextView) findViewById(R.id.driver_event_info_content_textView); 
			event_info_imageView = (ImageView) findViewById(R.id.driver_event_info_imageView);
			event_info_imageView.setImageResource(R.drawable.ic_action_driver);
			
			event_info_date_textView = (TextView) findViewById(R.id.driver_event_info_date_textView);
			event_info_postUsername_textView = (TextView) findViewById(R.id.driver_event_info_postUsername_textView);
			event_info_availableSeat_textView = (TextView) findViewById(R.id.driver_event_info_availableSeat_textView);
			event_info_final_confirm_passenger_textView = (TextView) findViewById(R.id.driver_event_info_final_confirm_passenger_textView);		
		}
		else{
			event_info_back_button = (Button) findViewById(R.id.passenger_event_info_back_button);
			event_info_contact_button = (Button) findViewById(R.id.passenger_event_info_contact_button);
			if (FINAL_CONFIRM_DRIVER.equals("") && ACCOUNT_TYPE.equals("passenger")){
				event_info_contact_button.setVisibility(View.INVISIBLE);
			}
			event_info_title_textView = (TextView) findViewById(R.id.passenger_event_info_title_textView);
			event_info_content_textView = (TextView) findViewById(R.id.passenger_event_info_content_textView); 
			event_info_imageView = (ImageView) findViewById(R.id.passenger_event_info_imageView);
			event_info_imageView.setImageResource(R.drawable.ic_action_passenger);
			
			event_info_date_textView = (TextView) findViewById(R.id.passenger_event_info_date_textView);
			event_info_postUsername_textView = (TextView) findViewById(R.id.passenger_event_info_postUsername_textView);
			event_info_availableSeat_textView = (TextView) findViewById(R.id.passenger_event_info_availableSeat_textView);
			event_info_final_confirm_driver_textView = (TextView) findViewById(R.id.passenger_event_info_final_confirm_driver_textView);
			event_info_final_confirm_extra_passenger_textView = (TextView) findViewById(R.id.passenger_event_info_final_confirm_extra_passenger_textView);
		}
	}

	private void setEventInfoDisplay(){
		if (EVENT_TYPE.equals("driver")){
			event_info_title_textView.setText(EVENT_TITLE);
			event_info_content_textView.setText(EVENT_CONTENT);
			event_info_date_textView.setText(EVENT_DATE);
			event_info_postUsername_textView.setText(POST_USERNAME);
			event_info_availableSeat_textView.setText(AVAILABLE_SEAT_NUMBER);
			event_info_final_confirm_passenger_textView.setText(FINAL_CONFIRM_PASSENGER);
		}
		else{	
			event_info_title_textView.setText(EVENT_TITLE);
			event_info_content_textView.setText(EVENT_CONTENT);
			event_info_date_textView.setText(EVENT_DATE);
			event_info_postUsername_textView.setText(POST_USERNAME);
			if (AVAILABLE_SEAT_NUMBER.equals("Touch to add available seat")){
				event_info_availableSeat_textView.setText("");
			}
			else{
				event_info_availableSeat_textView.setText(AVAILABLE_SEAT_NUMBER);
			}


			event_info_final_confirm_driver_textView.setText(FINAL_CONFIRM_DRIVER);
			event_info_final_confirm_extra_passenger_textView.setText(FINAL_CONFIRM_EXTRA_PASSENGER);
		}
	}

	private void setBackButtonControl(){
		event_info_back_button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
				overridePendingTransition(R.anim.activity_right_in, R.anim.activity_right_out);
			}
		});
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.activity_right_in, R.anim.activity_right_out);
	}

}
