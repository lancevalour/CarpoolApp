package yicheng.carpoolapp;

import java.util.ArrayList;


import java.util.HashMap;
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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;


public class MyEventActivity extends Activity{
	private Button my_event_me_button;
	private ListView my_event_listView;
	private LinearLayout my_event_loading_layout;
	private TextView my_event_empty_active_event_textView;

	private SwipeRefreshLayout my_event_refreshLayout;


	public static AmazonClientManager clientManager;

	private SharedPreferences local_user_information;
	private SharedPreferences.Editor local_user_editor;
	private String PREFS_NAME = "LocalUserInfo";


	private Handler handler;


	private String PHONE_NUMBER;
	private String ACCOUNT_TYPE;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		local_user_information =  this.getSharedPreferences(PREFS_NAME,0);
		clientManager = new AmazonClientManager();

		setContentView(R.layout.my_event_activity_layout);

		PHONE_NUMBER = local_user_information.getString("phoneNumber", "default");
		ACCOUNT_TYPE = local_user_information.getString("accountType", "default");

		initiateComponents();
		setHandlerControl();

		if (isConnectedToInternet()){
			loadAllEvents();
			my_event_loading_layout.setVisibility(View.VISIBLE);
			my_event_empty_active_event_textView.setVisibility(View.INVISIBLE);
		}
		else{
			Toast.makeText(getBaseContext(), "Unable to connect to Internet, please check your network.", Toast.LENGTH_LONG).show();
		}

		setMeButtonControl();
		setRefreshLayoutControl();
	}

	private void initiateComponents(){
		my_event_me_button = (Button) findViewById(R.id.my_event_me_button);
		my_event_listView = (ListView) findViewById(R.id.my_event_listView);
		my_event_empty_active_event_textView = (TextView) findViewById(R.id.my_event_empty_active_event_textView);
		my_event_loading_layout = (LinearLayout) findViewById(R.id.my_event_loading_layout);
		my_event_refreshLayout = (SwipeRefreshLayout) findViewById(R.id.my_event_refreshLayout);
	}

	private void setRefreshLayoutControl(){
		my_event_refreshLayout.setColorScheme(android.R.color.darker_gray, 
				android.R.color.holo_red_light, 
				android.R.color.darker_gray, 
				android.R.color.holo_red_light);
		my_event_refreshLayout.setOnRefreshListener(new OnRefreshListener(){

			@Override
			public void onRefresh() {
				// TODO Auto-generated method stub

				my_event_refreshLayout.setEnabled(false);
				my_event_refreshLayout.setRefreshing(true);
				if (isConnectedToInternet()){
					loadAllEvents();
					my_event_loading_layout.setVisibility(View.VISIBLE);
					my_event_empty_active_event_textView.setVisibility(View.INVISIBLE);
				}
				else{
					Toast.makeText(getBaseContext(), "Unable to connect to Internet, please check your network.", Toast.LENGTH_LONG).show();
				}
			}

		});
	}

	private void setMeButtonControl(){
		my_event_me_button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent("yicheng.carpoolapp.MESETTINGACTIVITY");

				startActivity(intent);

			}
		});
	}

	private void setHandlerControl(){
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == 1){
					addItemToList();
					if (my_event_loading_layout.getVisibility() == View.VISIBLE){
						my_event_loading_layout.setVisibility(View.INVISIBLE);
					}
					if (my_event_refreshLayout.isRefreshing()){
						my_event_refreshLayout.setRefreshing(false);
						my_event_refreshLayout.setEnabled(true);
					}

				}
				if (msg.what == 2){
					setMyEventDisplay();
				}
				if (msg.what == 3){
					deleteTempConfirmEventFromUserAccount();
				}
				if (msg.what == 4){
					if (my_event_loading_layout.getVisibility() == View.VISIBLE){
						my_event_loading_layout.setVisibility(View.INVISIBLE);
					}
				}
				if (msg.what == 5){
					deleteSelectedTempConfirmUserFromDatabase();
				}
				if (msg.what == 6){
					moveSelectedTempConfirmUserToFinalConfirm();
				}
				if (msg.what == 7){
					moveTempConfirmEventToFinalConfirmEventInUserAccount();
				}
				if (msg.what == 8){
					//getTempConfirmUserTempFinalConfirmEventAttribute();
				}
				if (msg.what == 9){
					//getTempConfirmUserTempConfirmEventAttribute();
				}
			}
		};
	}

	private void setMyEventDisplay(){
		if (ACCOUNT_TYPE.equals("driver")){
			if (driverEventPhoneNumberList.contains(PHONE_NUMBER)){
				loadDriverMyEvent();
			}
			else{
				my_event_empty_active_event_textView.setVisibility(View.VISIBLE);

				Message msg = Message.obtain();
				msg.what = 4;
				handler.sendMessage(msg);
			}
		}
		else{
			if (passengerEventPhoneNumberList.contains(PHONE_NUMBER)){
				loadPassengerMyEvent();
			}
			else{
				my_event_empty_active_event_textView.setVisibility(View.VISIBLE);

				Message msg = Message.obtain();
				msg.what = 4;
				handler.sendMessage(msg);
			}
		}
	}



	private ArrayList<Event> myEventList;

	private void loadDriverMyEvent(){
		myEventList = new ArrayList<Event>();
		ArrayList<ReadDriverEventThread> driverEventThreadList = new ArrayList<ReadDriverEventThread>();
		ReadDriverEventThread thread0 = new ReadDriverEventThread(clientManager, PHONE_NUMBER);
		driverEventThreadList.add(thread0);

		for (ReadDriverEventThread thread : driverEventThreadList){
			thread.start();
		}

		for (ReadDriverEventThread thread : driverEventThreadList){
			try {
				thread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		for (int i = 0; i < driverEventThreadList.size(); i++){
			myEventList.add(new Event(PHONE_NUMBER, 
					driverEventThreadList.get(i).getOrderedList().get(0).getValue(), 
					driverEventThreadList.get(i).getOrderedList().get(1).getValue(), 
					driverEventThreadList.get(i).getOrderedList().get(2).getValue(), 
					driverEventThreadList.get(i).getOrderedList().get(3).getValue(), 
					driverEventThreadList.get(i).getOrderedList().get(4).getValue(),
					driverEventThreadList.get(i).getOrderedList().get(5).getValue(), "",
					driverEventThreadList.get(i).getOrderedList().get(6).getValue(), 
					"", "", driverEventThreadList.get(i).getOrderedList().get(7).getValue(), ""));


		}

		Message msg = Message.obtain();
		msg.what = 1;
		handler.sendMessage(msg);


	}

	private void loadPassengerMyEvent(){
		myEventList = new ArrayList<Event>();
		ArrayList<ReadPassengerEventThread> passengerEventThreadList = new ArrayList<ReadPassengerEventThread>();
		ReadPassengerEventThread thread0 = new ReadPassengerEventThread(clientManager, PHONE_NUMBER);
		passengerEventThreadList.add(thread0);


		for (ReadPassengerEventThread thread : passengerEventThreadList){
			thread.start();
		}

		for (ReadPassengerEventThread thread : passengerEventThreadList){
			try {
				thread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		for (int i = 0; i < passengerEventThreadList.size(); i++){
			myEventList.add(new Event(
					PHONE_NUMBER, 
					passengerEventThreadList.get(i).getOrderedList().get(0).getValue(),
					passengerEventThreadList.get(i).getOrderedList().get(1).getValue(), 
					passengerEventThreadList.get(i).getOrderedList().get(2).getValue(), 
					passengerEventThreadList.get(i).getOrderedList().get(3).getValue(),
					passengerEventThreadList.get(i).getOrderedList().get(4).getValue(),
					passengerEventThreadList.get(i).getOrderedList().get(5).getValue(),
					passengerEventThreadList.get(i).getOrderedList().get(6).getValue(),
					"", passengerEventThreadList.get(i).getOrderedList().get(7).getValue(),
					passengerEventThreadList.get(i).getOrderedList().get(8).getValue(), "",
					passengerEventThreadList.get(i).getOrderedList().get(9).getValue()));

		}
		Message msg = Message.obtain();
		msg.what = 1;
		handler.sendMessage(msg);

	}

	private ArrayList<String> myEventTypeList;

	private void addItemToList(){
		myEventTypeList = new ArrayList<String>();
		if (ACCOUNT_TYPE.equals("driver")){
			myEventTypeList.add("driver");
			MyEventArrayAdapter adapter = new MyEventArrayAdapter(getBaseContext(), myEventList, myEventTypeList);

			my_event_listView.setAdapter(adapter);
		}
		else{
			myEventTypeList.add("passenger");
			MyEventArrayAdapter adapter = new MyEventArrayAdapter(getBaseContext(), myEventList, myEventTypeList);

			my_event_listView.setAdapter(adapter);
		}

		setListViewControl();
	}




	private String selectedUserPhoneNumber;
	private Event selectedMyEvent;

	private void setListViewControl(){

		my_event_listView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub

			}

		});

		my_event_listView.setOnItemLongClickListener(new OnItemLongClickListener(){

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub

				selectedMyEvent = myEventList.get(position);

				if (selectedMyEvent.getTempConfirmDriver().trim().length() >= 1 || selectedMyEvent.getTempConfirmExtraPassenger().trim().length() >= 1
						|| selectedMyEvent.getTempConfirmPassenger().trim().length() >= 1){



					AlertDialog.Builder builder = new AlertDialog.Builder(MyEventActivity.this);



					final String [] tempConfirmPhoneNumber;
					if (driverEventPhoneNumberList.contains(selectedMyEvent.getPhoneNumber())){
						tempConfirmPhoneNumber = myEventList.get(position).getTempConfirmPassenger().split("\n");
					}
					else{
						if (!selectedMyEvent.getFinalConfirmDriver().equals("")){
							tempConfirmPhoneNumber = selectedMyEvent.getTempConfirmExtraPassenger().split("\n");
						}
						else{
							tempConfirmPhoneNumber = selectedMyEvent.getTempConfirmDriver().split("\n");
						}

					}

					final String [] tempConfirmPhoneNumberArray = new String[tempConfirmPhoneNumber.length + 1]; 
					for (int i = 0; i < tempConfirmPhoneNumber.length; i++){
						tempConfirmPhoneNumberArray[i] = tempConfirmPhoneNumber[i];
					}
					tempConfirmPhoneNumberArray[tempConfirmPhoneNumberArray.length - 1] = "Cancel";
					
					

					builder.setItems(tempConfirmPhoneNumberArray, new DialogInterface.OnClickListener(){
						public void onClick(DialogInterface dialog, int which) {
							if (which == tempConfirmPhoneNumberArray.length - 1){
								dialog.cancel();
							}
							else{
								selectedUserPhoneNumber = tempConfirmPhoneNumberArray[which];


								AlertDialog.Builder builder = new AlertDialog.Builder(MyEventActivity.this);
								
								LayoutInflater factory = LayoutInflater.from(MyEventActivity.this);
								View view = factory.inflate(
										R.layout.custom_dialog_layout, null);
								builder.setView(view);
								TextView dialog_title = (TextView) view.findViewById(R.id.custom_dialog_title);
								dialog_title.setText("Confirm User");
								TextView dialog_content = (TextView) view.findViewById(R.id.custom_dialog_content);
								dialog_content.setText("Confirm or decline this user?");
								
								
								builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub

										getTempConfirmUserTempFinalConfirmEventAttribute();
										getFinalConfirmUser();


									}


								});
								builder.setNegativeButton("Decline", new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub

										getTempConfirmUserTempConfirmEventAttribute();
										getTempConfirmUser();


									}
								});

								builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {
										// TODO Auto-generated method stub
										dialog.cancel();
									}
								});
								builder.create().show();
							}


						}
					}).create().show();


				}

				return false;
			}

		});
	}

	private ArrayList<String> driverAccountTableItems;
	/*	private ArrayList<String> passengerAccountTableItems;*/

	private void loadAccountTableForConfirm(){
		runOnUiThread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				new Thread(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						String [] driverAccountTableItemArray = SimpleDB.getItemNamesForDomain(clientManager, SimpleDB.DRIVER_ACCOUNT_TABLE);

						driverAccountTableItems = new ArrayList<String>();

						for (int i = 0; i < driverAccountTableItemArray.length; i++){
							driverAccountTableItems.add(driverAccountTableItemArray[i]);
						}
						/*	 passengerAccountTableItems = new ArrayList<String>();

						String [] passengerAccountTableItemArray = SimpleDB.getItemNamesForDomain(LoginActivity.clientManager, SimpleDB.PASSENGER_ACCOUNT_TABLE);
						for (int i = 0; i < passengerAccountTableItemArray.length; i++){
							passengerAccountTableItems.add(passengerAccountTableItemArray[i]);
						}*/

						Message msg = Message.obtain();
						msg.what = 8;
						handler.sendMessage(msg);



					}

				}).start();
			}

		});
	}

	private void loadAccountTableForDecline(){
		runOnUiThread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				new Thread(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						String [] driverAccountTableItemArray = SimpleDB.getItemNamesForDomain(clientManager, SimpleDB.DRIVER_ACCOUNT_TABLE);

						driverAccountTableItems = new ArrayList<String>();

						for (int i = 0; i < driverAccountTableItemArray.length; i++){
							driverAccountTableItems.add(driverAccountTableItemArray[i]);
						}
						/* passengerAccountTableItems = new ArrayList<String>();

						String [] passengerAccountTableItemArray = SimpleDB.getItemNamesForDomain(LoginActivity.clientManager, SimpleDB.PASSENGER_ACCOUNT_TABLE);
						for (int i = 0; i < passengerAccountTableItemArray.length; i++){
							passengerAccountTableItems.add(passengerAccountTableItemArray[i]);
						}
						 */
						Message msg = Message.obtain();
						msg.what = 9;
						handler.sendMessage(msg);



					}

				}).start();
			}

		});
	}





	private String[] tempConfirmEventPhoneNumberArray;
	private String finalConfirmEventPhoneNumber;

	private void getTempConfirmUserTempConfirmEventAttribute(){
		if (!driverEventPhoneNumberList.contains(selectedMyEvent.getPhoneNumber())){
			if (selectedMyEvent.getFinalConfirmDriver().equals("")){
				runOnUiThread(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						new Thread(new Runnable(){

							@Override
							public void run() {
								// TODO Auto-generated method stub
								Future<GetAttributesResult> future = SimpleDB.getInstance(clientManager).getAttributesAsync(new GetAttributesRequest(SimpleDB.DRIVER_ACCOUNT_TABLE, selectedUserPhoneNumber));

								try {
									GetAttributesResult result = future.get();
									List<Attribute> list = result.getAttributes();
									ArrayList<Attribute> myActiveEventAttributeOrderedList = new ArrayList<Attribute>(list);

									for (Attribute attribute: list){

										if (attribute.getName().equals("tempConfirmEventAttribute")){
											myActiveEventAttributeOrderedList.set(0, attribute);
										}
										else if (attribute.getName().equals("finalConfirmEventAttribute")){
											myActiveEventAttributeOrderedList.set(1, attribute);
										}
									}


									tempConfirmEventPhoneNumberArray = myActiveEventAttributeOrderedList.get(0).getValue().split("\n");
									finalConfirmEventPhoneNumber = myActiveEventAttributeOrderedList.get(1).getValue();



									Message msg = Message.obtain();
									msg.what = 3;
									handler.sendMessage(msg);

								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (ExecutionException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}

						}).start();;



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
								Future<GetAttributesResult> future = SimpleDB.getInstance(clientManager).getAttributesAsync(new GetAttributesRequest(SimpleDB.PASSENGER_ACCOUNT_TABLE, selectedUserPhoneNumber));

								try {
									GetAttributesResult result = future.get();
									List<Attribute> list = result.getAttributes();
									ArrayList<Attribute> myActiveEventAttributeOrderedList = new ArrayList<Attribute>(list);

									for (Attribute attribute: list){


										if (attribute.getName().equals("tempConfirmEventAttribute")){
											myActiveEventAttributeOrderedList.set(0, attribute);
										}
										else if (attribute.getName().equals("finalConfirmEventAttribute")){
											myActiveEventAttributeOrderedList.set(1, attribute);
										}
									}


									tempConfirmEventPhoneNumberArray = myActiveEventAttributeOrderedList.get(0).getValue().split("\n");
									finalConfirmEventPhoneNumber = myActiveEventAttributeOrderedList.get(1).getValue();

									/*for (int i = 0; i < contactEventPhoneNumberArray.length; i++){
										System.out.println(contactEventPhoneNumberArray[i]);
									}*/

									Message msg = Message.obtain();
									msg.what = 3;
									handler.sendMessage(msg);

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
		else{
			runOnUiThread(new Runnable(){

				@Override
				public void run() {
					// TODO Auto-generated method stub
					new Thread(new Runnable(){

						@Override
						public void run() {
							// TODO Auto-generated method stub
							Future<GetAttributesResult> future = SimpleDB.getInstance(clientManager).getAttributesAsync(new GetAttributesRequest(SimpleDB.PASSENGER_ACCOUNT_TABLE, selectedUserPhoneNumber));

							try {
								GetAttributesResult result = future.get();
								List<Attribute> list = result.getAttributes();
								ArrayList<Attribute> myActiveEventAttributeOrderedList = new ArrayList<Attribute>(list);

								for (Attribute attribute: list){


									if (attribute.getName().equals("tempConfirmEventAttribute")){
										myActiveEventAttributeOrderedList.set(0, attribute);
									}
									else if (attribute.getName().equals("finalConfirmEventAttribute")){
										myActiveEventAttributeOrderedList.set(1, attribute);
									}
								}


								tempConfirmEventPhoneNumberArray = myActiveEventAttributeOrderedList.get(0).getValue().split("\n");
								finalConfirmEventPhoneNumber = myActiveEventAttributeOrderedList.get(1).getValue();

								/*for (int i = 0; i < contactEventPhoneNumberArray.length; i++){
									System.out.println(contactEventPhoneNumberArray[i]);
								}*/

								Message msg = Message.obtain();
								msg.what = 3;
								handler.sendMessage(msg);

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

	private void deleteTempConfirmEventFromUserAccount(){
		ArrayList<String> newTempConfirmEventList = new ArrayList<String>();
		for (int i = 0; i < tempConfirmEventPhoneNumberArray.length; i++){
			if (!tempConfirmEventPhoneNumberArray[i].equals(selectedMyEvent.getPhoneNumber())){
				newTempConfirmEventList.add(tempConfirmEventPhoneNumberArray[i]);	
			}	
		}

		String newTempConfirmEvent = "";

		for (String s : newTempConfirmEventList){
			newTempConfirmEvent += s + "\n";
		}

		deleteTempConfirmEventFromUserAccountDatabaseUpdate(newTempConfirmEvent);
	}

	private void deleteTempConfirmEventFromUserAccountDatabaseUpdate(final String newTempConfirmEvent){
		runOnUiThread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				new Thread(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (!driverEventPhoneNumberList.contains(selectedMyEvent.getPhoneNumber())){
							if (selectedMyEvent.getFinalConfirmDriver().trim().equals("")){
								SimpleDB.updateSingleAttribute(clientManager, SimpleDB.DRIVER_ACCOUNT_TABLE, selectedUserPhoneNumber, SimpleDB.DRIVER_ACCOUNT_TEMP_CONFIRM_EVENT_ATTRIBUTE, newTempConfirmEvent);
							}
							else{
								SimpleDB.updateSingleAttribute(clientManager, SimpleDB.PASSENGER_ACCOUNT_TABLE, selectedUserPhoneNumber, SimpleDB.PASSENGER_ACCOUNT_TEMP_CONFIRM_EVENT_ATTRIBUTE, newTempConfirmEvent);
							}
							
						}
						else{
							SimpleDB.updateSingleAttribute(clientManager, SimpleDB.PASSENGER_ACCOUNT_TABLE, selectedUserPhoneNumber, SimpleDB.PASSENGER_ACCOUNT_TEMP_CONFIRM_EVENT_ATTRIBUTE, newTempConfirmEvent);
						}


					}

				}).start();
			}

		});
	}

	private void getTempConfirmUserTempFinalConfirmEventAttribute(){
		if (!driverEventPhoneNumberList.contains(selectedMyEvent.getPhoneNumber())){
			if (selectedMyEvent.getFinalConfirmDriver().equals("")){
				runOnUiThread(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						new Thread(new Runnable(){

							@Override
							public void run() {
								// TODO Auto-generated method stub
								Future<GetAttributesResult> future = SimpleDB.getInstance(clientManager).getAttributesAsync(new GetAttributesRequest(SimpleDB.DRIVER_ACCOUNT_TABLE, selectedUserPhoneNumber));

								try {
									GetAttributesResult result = future.get();
									List<Attribute> list = result.getAttributes();
									ArrayList<Attribute> myActiveEventAttributeOrderedList = new ArrayList<Attribute>(list);

									for (Attribute attribute: list){

										if (attribute.getName().equals("tempConfirmEventAttribute")){
											myActiveEventAttributeOrderedList.set(0, attribute);
										}
										else if (attribute.getName().equals("finalConfirmEventAttribute")){
											myActiveEventAttributeOrderedList.set(1, attribute);
										}
									}


									tempConfirmEventPhoneNumberArray = myActiveEventAttributeOrderedList.get(0).getValue().split("\n");
									finalConfirmEventPhoneNumber = myActiveEventAttributeOrderedList.get(1).getValue();



									Message msg = Message.obtain();
									msg.what = 7;
									handler.sendMessage(msg);

								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (ExecutionException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}

						}).start();;



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
								Future<GetAttributesResult> future = SimpleDB.getInstance(clientManager).getAttributesAsync(new GetAttributesRequest(SimpleDB.PASSENGER_ACCOUNT_TABLE, selectedUserPhoneNumber));

								try {
									GetAttributesResult result = future.get();
									List<Attribute> list = result.getAttributes();
									ArrayList<Attribute> myActiveEventAttributeOrderedList = new ArrayList<Attribute>(list);

									for (Attribute attribute: list){


										if (attribute.getName().equals("tempConfirmEventAttribute")){
											myActiveEventAttributeOrderedList.set(0, attribute);
										}
										else if (attribute.getName().equals("finalConfirmEventAttribute")){
											myActiveEventAttributeOrderedList.set(1, attribute);
										}
									}


									tempConfirmEventPhoneNumberArray = myActiveEventAttributeOrderedList.get(0).getValue().split("\n");
									finalConfirmEventPhoneNumber = myActiveEventAttributeOrderedList.get(1).getValue();

									/*for (int i = 0; i < contactEventPhoneNumberArray.length; i++){
										System.out.println(contactEventPhoneNumberArray[i]);
									}*/

									Message msg = Message.obtain();
									msg.what = 7;
									handler.sendMessage(msg);

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
		else{
			runOnUiThread(new Runnable(){

				@Override
				public void run() {
					// TODO Auto-generated method stub
					new Thread(new Runnable(){

						@Override
						public void run() {
							// TODO Auto-generated method stub
							Future<GetAttributesResult> future = SimpleDB.getInstance(clientManager).getAttributesAsync(new GetAttributesRequest(SimpleDB.PASSENGER_ACCOUNT_TABLE, selectedUserPhoneNumber));

							try {
								GetAttributesResult result = future.get();
								List<Attribute> list = result.getAttributes();
								ArrayList<Attribute> myActiveEventAttributeOrderedList = new ArrayList<Attribute>(list);

								for (Attribute attribute: list){


									if (attribute.getName().equals("tempConfirmEventAttribute")){
										myActiveEventAttributeOrderedList.set(0, attribute);
									}
									else if (attribute.getName().equals("finalConfirmEventAttribute")){
										myActiveEventAttributeOrderedList.set(1, attribute);
									}
								}


								tempConfirmEventPhoneNumberArray = myActiveEventAttributeOrderedList.get(0).getValue().split("\n");
								finalConfirmEventPhoneNumber = myActiveEventAttributeOrderedList.get(1).getValue();

								/*for (int i = 0; i < contactEventPhoneNumberArray.length; i++){
									System.out.println(contactEventPhoneNumberArray[i]);
								}*/

								Message msg = Message.obtain();
								msg.what = 7;
								handler.sendMessage(msg);

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

	private void moveTempConfirmEventToFinalConfirmEventInUserAccount(){
		ArrayList<String> newTempConfirmEventList = new ArrayList<String>();
		for (int i = 0; i < tempConfirmEventPhoneNumberArray.length; i++){
			if (!tempConfirmEventPhoneNumberArray[i].equals(selectedMyEvent.getPhoneNumber())){
				newTempConfirmEventList.add(tempConfirmEventPhoneNumberArray[i]);	
			}	
		}

		String newTempConfirmEvent = "";

		for (String s : newTempConfirmEventList){
			newTempConfirmEvent += s + "\n";
		}

		System.out.println("haha1");
		
		String newFinalConfirmEvent;
		if (finalConfirmEventPhoneNumber.trim().length() < 1){
			newFinalConfirmEvent = selectedMyEvent.getPhoneNumber() + "\n";
		}
		else{
			newFinalConfirmEvent = finalConfirmEventPhoneNumber + selectedMyEvent.getPhoneNumber() + "\n" ;
		}
		
		System.out.println(newFinalConfirmEvent);
		
		moveTempConfirmEventToFinalConfirmEventInUserAccountDatabaseUpdate(newTempConfirmEvent, newFinalConfirmEvent);
	}

	private void moveTempConfirmEventToFinalConfirmEventInUserAccountDatabaseUpdate(final String newTempConfirmEvent, final String newFinalConfirmEvent){
		runOnUiThread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				new Thread(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (!driverEventPhoneNumberList.contains(selectedMyEvent.getPhoneNumber())){
							if (selectedMyEvent.getFinalConfirmDriver().trim().equals("")){
								System.out.println("haha");
								HashMap<String, String> oldAttribute = SimpleDB.getAttributesForItem(clientManager, SimpleDB.DRIVER_ACCOUNT_TABLE, selectedUserPhoneNumber);
								oldAttribute.put(SimpleDB.DRIVER_ACCOUNT_TEMP_CONFIRM_EVENT_ATTRIBUTE, newTempConfirmEvent);
								oldAttribute.put(SimpleDB.DRIVER_ACCOUNT_FINAL_CONFIRM_EVENT_ATTRIBUTE, newFinalConfirmEvent);

								SimpleDB.updateAttributesForItem(clientManager,SimpleDB.DRIVER_ACCOUNT_TABLE, selectedUserPhoneNumber, oldAttribute);
							}
							else{
								HashMap<String, String> oldAttribute = SimpleDB.getAttributesForItem(clientManager, SimpleDB.PASSENGER_ACCOUNT_TABLE, selectedUserPhoneNumber);
								oldAttribute.put(SimpleDB.PASSENGER_ACCOUNT_TEMP_CONFIRM_EVENT_ATTRIBUTE, newTempConfirmEvent);
								oldAttribute.put(SimpleDB.PASSENGER_ACCOUNT_FINAL_CONFIRM_EVENT_ATTRIBUTE, newFinalConfirmEvent);

								SimpleDB.updateAttributesForItem(clientManager,SimpleDB.PASSENGER_ACCOUNT_TABLE, selectedUserPhoneNumber, oldAttribute);
							}
							
						}
						else{
							HashMap<String, String> oldAttribute = SimpleDB.getAttributesForItem(clientManager, SimpleDB.PASSENGER_ACCOUNT_TABLE, selectedUserPhoneNumber);
							oldAttribute.put(SimpleDB.PASSENGER_ACCOUNT_TEMP_CONFIRM_EVENT_ATTRIBUTE, newTempConfirmEvent);
							oldAttribute.put(SimpleDB.PASSENGER_ACCOUNT_FINAL_CONFIRM_EVENT_ATTRIBUTE, newFinalConfirmEvent);

							SimpleDB.updateAttributesForItem(clientManager,SimpleDB.PASSENGER_ACCOUNT_TABLE, selectedUserPhoneNumber, oldAttribute);
						}
					}

				}).start();
			}

		});
	}






	private String tempConfirmPassengerPhoneNumber;
	private String tempConfirmDriverPhoneNumber;
	private String finalConfirmPassengerPhoneNumber;
	private String finalConfirmDriverPhoneNumber;
	private String finalConfirmExtraPassengerPhoneNumber;


	private String tempConfirmExtraPassengerPhoneNumber;

	private void getTempConfirmUser(){
		if (driverEventPhoneNumberList.contains(selectedMyEvent.getPhoneNumber())){
			runOnUiThread(new Runnable(){

				@Override
				public void run() {
					// TODO Auto-generated method stub
					new Thread(new Runnable(){

						@Override
						public void run() {
							// TODO Auto-generated method stub
							Future<GetAttributesResult> future = SimpleDB.getInstance(clientManager).getAttributesAsync(new GetAttributesRequest(SimpleDB.DRIVER_EVENT_TABLE, PHONE_NUMBER));

							try {
								GetAttributesResult result = future.get();
								List<Attribute> list = result.getAttributes();
								ArrayList<Attribute> orderedList = new ArrayList<Attribute>(list);

								for (Attribute attribute: list){

									if (attribute.getName().equals("tempConfirmPassengerAttribute")){
										orderedList.set(0, attribute);
									}


								}

								tempConfirmPassengerPhoneNumber = orderedList.get(0).getValue();


								Message msg = Message.obtain();
								msg.what = 5;
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
							Future<GetAttributesResult> future = SimpleDB.getInstance(clientManager).getAttributesAsync(new GetAttributesRequest(SimpleDB.PASSENGER_EVENT_TABLE, PHONE_NUMBER));

							try {
								GetAttributesResult result = future.get();
								List<Attribute> list = result.getAttributes();
								ArrayList<Attribute> orderedList = new ArrayList<Attribute>(list);

								for (Attribute attribute: list){

									if (attribute.getName().equals("tempConfirmDriverAttribute")){
										orderedList.set(0, attribute);
									}
									else if (attribute.getName().equals("tempConfirmExtraPassengerAttribute")){
										orderedList.set(1, attribute);
									}



								}

								tempConfirmDriverPhoneNumber = orderedList.get(0).getValue();
								tempConfirmExtraPassengerPhoneNumber = orderedList.get(1).getValue();


								Message msg = Message.obtain();
								msg.what = 5;
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

	private void deleteSelectedTempConfirmUserFromDatabase(){

		ArrayList<String> newTempConfirmUserPhoneNumberList = new ArrayList<String>();

		if (driverEventPhoneNumberList.contains(selectedMyEvent.getPhoneNumber())){
			String[] tempConfirmPassengerArray = tempConfirmPassengerPhoneNumber.split("\n");
			for (int i = 0; i < tempConfirmPassengerArray.length; i++){
				if (!tempConfirmPassengerArray[i].equals(selectedUserPhoneNumber)){
					newTempConfirmUserPhoneNumberList.add(tempConfirmPassengerArray[i]);	
				}	
			}

			String newTempConfirmUser = "";

			for (String s : newTempConfirmUserPhoneNumberList){
				newTempConfirmUser += s + "\n";
			}


			deleteSelectedTempConfirmUserFromDatabaseUpdate(newTempConfirmUser);

		}
		else{
			if (!selectedMyEvent.getFinalConfirmDriver().equals("")){
				String[] tempConfirmExtraPassengerArray = tempConfirmExtraPassengerPhoneNumber.split("\n");
				for (int i = 0; i < tempConfirmExtraPassengerArray.length; i++){
					if (!tempConfirmExtraPassengerArray[i].equals(selectedUserPhoneNumber)){
						newTempConfirmUserPhoneNumberList.add(tempConfirmExtraPassengerArray[i]);	
					}	
				}

				String newTempConfirmUser = "";

				for (String s : newTempConfirmUserPhoneNumberList){
					newTempConfirmUser += s + "\n";
				}


				deleteSelectedTempConfirmUserFromDatabaseUpdate(newTempConfirmUser);
			}
			else{
				String[] tempConfirmDriverArray = tempConfirmDriverPhoneNumber.split("\n");
				for (int i = 0; i < tempConfirmDriverArray.length; i++){
					if (!tempConfirmDriverArray[i].equals(selectedUserPhoneNumber)){
						newTempConfirmUserPhoneNumberList.add(tempConfirmDriverArray[i]);	
					}	
				}

				String newTempConfirmUser = "";

				for (String s : newTempConfirmUserPhoneNumberList){
					newTempConfirmUser += s + "\n";
				}


				deleteSelectedTempConfirmUserFromDatabaseUpdate(newTempConfirmUser);
			}
		}
	}

	private void deleteSelectedTempConfirmUserFromDatabaseUpdate(final String newTempConfirmUser){
		runOnUiThread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				new Thread(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (driverEventPhoneNumberList.contains(selectedMyEvent.getPhoneNumber())){
							SimpleDB.updateSingleAttribute(clientManager, SimpleDB.DRIVER_EVENT_TABLE, selectedMyEvent.getPhoneNumber(), SimpleDB.DRIVER_EVENT_TEMP_CONFIRM_PASSENGER_ATTRIBUTE, newTempConfirmUser);
						}
						else{
							if (!selectedMyEvent.getFinalConfirmDriver().equals("")){
								SimpleDB.updateSingleAttribute(clientManager, SimpleDB.PASSENGER_EVENT_TABLE, selectedMyEvent.getPhoneNumber(), SimpleDB.PASSENGER_EVENT_TEMP_CONFIRM_EXTRA_PASSENGER_ATTRIBUTE, newTempConfirmUser);
							}
							else{
								SimpleDB.updateSingleAttribute(clientManager, SimpleDB.PASSENGER_EVENT_TABLE, selectedMyEvent.getPhoneNumber(), SimpleDB.PASSENGER_EVENT_TEMP_CONFIRM_DRIVER_ATTRIBUTE, newTempConfirmUser);
							}
						}
					}

				}).start();
			}

		});
	}




	private void getFinalConfirmUser(){
		if (driverEventPhoneNumberList.contains(selectedMyEvent.getPhoneNumber())){
			runOnUiThread(new Runnable(){

				@Override
				public void run() {
					// TODO Auto-generated method stub
					new Thread(new Runnable(){

						@Override
						public void run() {
							// TODO Auto-generated method stub
							Future<GetAttributesResult> future = SimpleDB.getInstance(clientManager).getAttributesAsync(new GetAttributesRequest(SimpleDB.DRIVER_EVENT_TABLE, PHONE_NUMBER));

							try {
								GetAttributesResult result = future.get();
								List<Attribute> list = result.getAttributes();
								ArrayList<Attribute> orderedList = new ArrayList<Attribute>(list);

								for (Attribute attribute: list){

									if (attribute.getName().equals("tempConfirmPassengerAttribute")){
										orderedList.set(0, attribute);
									}
									else if (attribute.getName().equals("finalConfirmPassengerAttribute")){
										orderedList.set(1, attribute);
									}


								}

								tempConfirmPassengerPhoneNumber = orderedList.get(0).getValue();
								finalConfirmPassengerPhoneNumber = orderedList.get(1).getValue();

								Message msg = Message.obtain();
								msg.what = 6;
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
							Future<GetAttributesResult> future = SimpleDB.getInstance(clientManager).getAttributesAsync(new GetAttributesRequest(SimpleDB.PASSENGER_EVENT_TABLE, PHONE_NUMBER));

							try {
								GetAttributesResult result = future.get();
								List<Attribute> list = result.getAttributes();
								ArrayList<Attribute> orderedList = new ArrayList<Attribute>(list);

								for (Attribute attribute: list){

									if (attribute.getName().equals("tempConfirmDriverAttribute")){
										orderedList.set(0, attribute);
									}
									else if (attribute.getName().equals("tempConfirmExtraPassengerAttribute")){
										orderedList.set(1, attribute);
									}
									else if (attribute.getName().equals("finalConfirmDriverAttribute")){
										orderedList.set(2, attribute);
									}
									else if (attribute.getName().equals("finalConfirmExtraPassengerAttribute")){
										orderedList.set(3, attribute);
									}



								}

								tempConfirmDriverPhoneNumber = orderedList.get(0).getValue();
								tempConfirmExtraPassengerPhoneNumber = orderedList.get(1).getValue();
								finalConfirmDriverPhoneNumber = orderedList.get(2).getValue();
								finalConfirmExtraPassengerPhoneNumber = orderedList.get(3).getValue();

								Message msg = Message.obtain();
								msg.what = 6;
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

	private void moveSelectedTempConfirmUserToFinalConfirm(){
		ArrayList<String> newTempConfirmUserPhoneNumberList = new ArrayList<String>();

		if (driverEventPhoneNumberList.contains(selectedMyEvent.getPhoneNumber())){
			String[] tempConfirmPassengerArray = tempConfirmPassengerPhoneNumber.split("\n");
			for (int i = 0; i < tempConfirmPassengerArray.length; i++){
				if (!tempConfirmPassengerArray[i].equals(selectedUserPhoneNumber)){
					newTempConfirmUserPhoneNumberList.add(tempConfirmPassengerArray[i]);	
				}	
			}

			String newTempConfirmUser = "";

			for (String s : newTempConfirmUserPhoneNumberList){
				newTempConfirmUser += s + "\n";
			}


			String newFinalConfirmUser;
			if (finalConfirmPassengerPhoneNumber.trim().length() < 1){
				newFinalConfirmUser = selectedUserPhoneNumber + "\n";
			}
			else{
				newFinalConfirmUser = finalConfirmPassengerPhoneNumber + selectedUserPhoneNumber + "\n" ;
			}

			moveSelectedTempConfirmUserToFinalConfirmDatabaseUpadate(newTempConfirmUser, newFinalConfirmUser);

		}
		else{
			if (selectedMyEvent.getFinalConfirmDriver().equals("")){
				String[] tempConfirmDriverArray = tempConfirmDriverPhoneNumber.split("\n");
				for (int i = 0; i < tempConfirmDriverArray.length; i++){
					if (!tempConfirmDriverArray[i].equals(selectedUserPhoneNumber)){
						newTempConfirmUserPhoneNumberList.add(tempConfirmDriverArray[i]);	
					}	
				}

				String newTempConfirmUser = "";

				for (String s : newTempConfirmUserPhoneNumberList){
					newTempConfirmUser += s + "\n";
				}


				String newFinalConfirmUser;
				if (finalConfirmDriverPhoneNumber.trim().length() < 1){
					newFinalConfirmUser = selectedUserPhoneNumber + "\n";
				}
				else{
					newFinalConfirmUser = finalConfirmDriverPhoneNumber + selectedUserPhoneNumber + "\n" ;
				}

				moveSelectedTempConfirmUserToFinalConfirmDatabaseUpadate(newTempConfirmUser, newFinalConfirmUser);
			}
			else{
				String[] tempConfirmExtraPassengerArray = tempConfirmExtraPassengerPhoneNumber.split("\n");
				for (int i = 0; i < tempConfirmExtraPassengerArray.length; i++){
					if (!tempConfirmExtraPassengerArray[i].equals(selectedUserPhoneNumber)){
						newTempConfirmUserPhoneNumberList.add(tempConfirmExtraPassengerArray[i]);	
					}	
				}

				String newTempConfirmUser = "";

				for (String s : newTempConfirmUserPhoneNumberList){
					newTempConfirmUser += s + "\n";
				}


				String newFinalConfirmUser;
				if (finalConfirmExtraPassengerPhoneNumber.trim().length() < 1){
					newFinalConfirmUser = selectedUserPhoneNumber + "\n";
				}
				else{
					newFinalConfirmUser = finalConfirmExtraPassengerPhoneNumber + selectedUserPhoneNumber + "\n" ;
				}

				moveSelectedTempConfirmUserToFinalConfirmDatabaseUpadate(newTempConfirmUser, newFinalConfirmUser);
			}
		}
	}

	private void moveSelectedTempConfirmUserToFinalConfirmDatabaseUpadate(final String newTempConfirmUser, final String newFinalConfirmUser){
		runOnUiThread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				new Thread(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (driverEventPhoneNumberList.contains(selectedMyEvent.getPhoneNumber())){
							HashMap<String, String> oldAttribute = SimpleDB.getAttributesForItem(clientManager, SimpleDB.DRIVER_EVENT_TABLE, selectedMyEvent.getPhoneNumber());
							oldAttribute.put(SimpleDB.DRIVER_EVENT_TEMP_CONFIRM_PASSENGER_ATTRIBUTE, newTempConfirmUser);
							oldAttribute.put(SimpleDB.DRIVER_EVENT_FINAL_CONFIRM_PASSENGER_ATTRIBUTE, newFinalConfirmUser);

							SimpleDB.updateAttributesForItem(clientManager,SimpleDB.DRIVER_EVENT_TABLE, selectedMyEvent.getPhoneNumber(), oldAttribute);
						}
						else{
							if (!selectedMyEvent.getFinalConfirmDriver().equals("")){
								HashMap<String, String> oldAttribute = SimpleDB.getAttributesForItem(clientManager, SimpleDB.PASSENGER_EVENT_TABLE, selectedMyEvent.getPhoneNumber());
								oldAttribute.put(SimpleDB.PASSENGER_EVENT_TEMP_CONFIRM_EXTRA_PASSENGER_ATTRIBUTE, newTempConfirmUser);
								oldAttribute.put(SimpleDB.PASSENGER_EVENT_FINAL_CONFIRM_EXTRA_PASSENGER_ATTRIBUTE, newFinalConfirmUser);

								SimpleDB.updateAttributesForItem(clientManager,SimpleDB.PASSENGER_EVENT_TABLE, selectedMyEvent.getPhoneNumber(), oldAttribute);
							}
							else{
								HashMap<String, String> oldAttribute = SimpleDB.getAttributesForItem(clientManager, SimpleDB.PASSENGER_EVENT_TABLE, selectedMyEvent.getPhoneNumber());
								oldAttribute.put(SimpleDB.PASSENGER_EVENT_TEMP_CONFIRM_DRIVER_ATTRIBUTE, newTempConfirmUser);
								oldAttribute.put(SimpleDB.PASSENGER_EVENT_FINAL_CONFIRM_DRIVER_ATTRIBUTE, newFinalConfirmUser);

								SimpleDB.updateAttributesForItem(clientManager,SimpleDB.PASSENGER_EVENT_TABLE, selectedMyEvent.getPhoneNumber(), oldAttribute);
							}
						}
					}

				}).start();
			}

		});
	}









	private void updateTempConfirmDatabase(){

	}




















	private String[] driverEventPhoneNumberArray ;
	private String[] passengerEventPhoneNumberArray;

	private ArrayList<String> driverEventPhoneNumberList;
	private ArrayList<String> passengerEventPhoneNumberList;

	private Thread loadAllEventsThread;

	private void loadAllEvents(){
		driverEventPhoneNumberList = new ArrayList<String>();
		passengerEventPhoneNumberList = new ArrayList<String>();
		runOnUiThread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				loadAllEventsThread = new Thread(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub

						driverEventPhoneNumberArray = SimpleDB.getItemNamesForDomain(clientManager, SimpleDB.DRIVER_EVENT_TABLE);			
						passengerEventPhoneNumberArray = SimpleDB.getItemNamesForDomain(clientManager, SimpleDB.PASSENGER_EVENT_TABLE);

						for (int i = 0; i < driverEventPhoneNumberArray.length; i++){
							driverEventPhoneNumberList.add(driverEventPhoneNumberArray[i]);
						}
						for (int i = 0; i < passengerEventPhoneNumberArray.length; i++){
							passengerEventPhoneNumberList.add(passengerEventPhoneNumberArray[i]);
						}

						Message msg = Message.obtain();
						msg.what = 2;
						handler.sendMessage(msg);

					}

				});
				loadAllEventsThread.start();
			}

		});
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
				(ConnectivityManager)MyEventActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo wifi_network = 
				connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo mobile_network = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);


		return wifi_network.isConnectedOrConnecting() || mobile_network.isConnectedOrConnecting();

		/*return mobile_network.isConnectedOrConnecting();*/
	}

}
