package yicheng.carpoolapp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;




import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.amazonaws.services.simpledb.AmazonSimpleDBAsyncClient;
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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class EventPoolActivity extends Activity{
	private Button event_pool_post_event_button;
	private ListView event_pool_listView;
	private LinearLayout event_pool_loading_layout;
	private SwipeRefreshLayout event_pool_refreshLayout;

	private AmazonClientManager clientManager;

	private SharedPreferences local_user_information;
	private SharedPreferences.Editor local_user_editor;
	private String PREFS_NAME = "LocalUserInfo";

	private Handler handler;

	private ArrayList<Event> unsortedEventList;
	private ArrayList<Event> sortedEventList;
	private ArrayList<Event> driverEventList;
	private ArrayList<Event> passengerEventList;

	private ArrayList<String> unsortedDateList;
	private ArrayList<String> driverEventDateList;
	private ArrayList<String> passengerEventDateList;

	private ArrayList<Date> sortedDateList;
	private HashMap<Date, Integer> map;

	private ArrayList<String> unsortedEventTypeList;
	private ArrayList<String> sortedEventTypeList;




	private String PHONE_NUMBER;
	private String ACCOUNT_TYPE;







	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		local_user_information =  this.getSharedPreferences(PREFS_NAME,0);
		clientManager = new AmazonClientManager();

		setContentView(R.layout.event_pool_activity_layout);
		PHONE_NUMBER = local_user_information.getString("phoneNumber", "default");
		ACCOUNT_TYPE = local_user_information.getString("accountType", "default");

		initiateComponents();
		setHandlerControl();

		if (isConnectedToInternet()){
			loadAllEvents();
			event_pool_loading_layout.setVisibility(View.VISIBLE);
		}
		else{
			Toast.makeText(getBaseContext(), "Unable to connect to Internet, please check your network.", Toast.LENGTH_LONG).show();
		}

		setPostEventButtonControl();

		setRefreshLayoutControl();


		//addItemToListView();


	}

	private void initiateComponents(){
		event_pool_post_event_button = (Button) findViewById(R.id.event_pool_post_event_button);
		event_pool_post_event_button.setEnabled(false);
		event_pool_listView = (ListView) findViewById(R.id.event_pool_listView);
		event_pool_loading_layout = (LinearLayout) findViewById(R.id.event_pool_loading_layout);
		event_pool_refreshLayout = (SwipeRefreshLayout) findViewById(R.id.event_pool_refreshLayout);

	}

	private void setRefreshLayoutControl(){
		event_pool_refreshLayout.setColorScheme(android.R.color.darker_gray, 
				android.R.color.holo_red_light, 
				android.R.color.darker_gray, 
				android.R.color.holo_red_light);
		event_pool_refreshLayout.setOnRefreshListener(new OnRefreshListener(){

			@Override
			public void onRefresh() {
				// TODO Auto-generated method stub

				event_pool_refreshLayout.setEnabled(false);
				event_pool_refreshLayout.setRefreshing(true);
				if (isConnectedToInternet()){
					loadAllEvents();
					event_pool_loading_layout.setVisibility(View.VISIBLE);
				}
				else{
					Toast.makeText(getBaseContext(), "Unable to connect to Internet, please check your network.", Toast.LENGTH_LONG).show();
				}
			}

		});
	}

	private boolean hasPostedEvent = false;

	private void setPostEventButtonControl(){
		event_pool_post_event_button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if (ACCOUNT_TYPE.equals("driver")){
					for (int i = 0; i < driverEventPhoneNumberArray.length; i++){
						if (PHONE_NUMBER.equals(driverEventPhoneNumberArray[i])){
							hasPostedEvent = true;
							break;
						}
					}
				}
				else{
					for (int i = 0; i < passengerEventPhoneNumberArray.length; i++){
						if (PHONE_NUMBER.equals(passengerEventPhoneNumberArray[i])){
							hasPostedEvent = true;
							break;
						}
					}
				}

				if (hasPostedEvent){
					AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(EventPoolActivity.this);	
					
					LayoutInflater factory = LayoutInflater.from(EventPoolActivity.this);
					View view = factory.inflate(
							R.layout.custom_dialog_layout, null);
					dialogBuilder.setView(view);
					TextView dialog_title = (TextView) view.findViewById(R.id.custom_dialog_title);
					dialog_title.setText("Replace Event");
					TextView dialog_content = (TextView) view.findViewById(R.id.custom_dialog_content);
					dialog_content.setText("You have already posted an event, do you want to replace it?");
					

					dialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							Intent intent = new Intent("yicheng.carpoolapp.POSTEVENTACTIVITY");
							startActivity(intent);		
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
				else{
					Intent intent = new Intent("yicheng.carpoolapp.POSTEVENTACTIVITY");
					startActivity(intent);				
				}


			}
		});
	}



	private void setHandlerControl(){
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == 1){

					addItemToListView();

					event_pool_loading_layout.setVisibility(View.INVISIBLE);
					if (event_pool_refreshLayout.isRefreshing()){
						event_pool_refreshLayout.setRefreshing(false);
						event_pool_refreshLayout.setEnabled(true);
					}
				}
				if (msg.what == 2){
					event_pool_post_event_button.setEnabled(true);

					loadAllDriverEvents();
				}
				if (msg.what == 3){
					loadAllPassengerEvents();
				}
			}
		};
	}





	private void loadAllDriverEvents(){

		System.out.println("loading attributes");

		ArrayList<ReadDriverEventThread> threadList = new ArrayList<ReadDriverEventThread>();

		for (int i = 0; i < driverEventPhoneNumberArray.length; i++){
			ReadDriverEventThread thread = new ReadDriverEventThread(clientManager, driverEventPhoneNumberArray[i]);
			threadList.add(thread);
		}

		for (ReadDriverEventThread thread : threadList){
			thread.start();
		}

		for (ReadDriverEventThread thread : threadList){
			try {
				thread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		for (int i = 0; i < threadList.size(); i++){
			driverEventList.add(new Event(driverEventPhoneNumberArray[i], 
					threadList.get(i).getOrderedList().get(0).getValue(), 
					threadList.get(i).getOrderedList().get(1).getValue(), 
					threadList.get(i).getOrderedList().get(2).getValue(), 
					threadList.get(i).getOrderedList().get(3).getValue(), 
					threadList.get(i).getOrderedList().get(4).getValue(),
					threadList.get(i).getOrderedList().get(5).getValue(), "",
					"", "", "", "", ""));

			driverEventDateList.add(threadList.get(i).getOrderedList().get(2).getValue());
		}



		Message msg = Message.obtain();
		msg.what = 3;
		handler.sendMessage(msg);


	}

	private void loadAllPassengerEvents(){

		ArrayList<ReadPassengerEventThread> threadList = new ArrayList<ReadPassengerEventThread>();

		for (int i = 0; i < passengerEventPhoneNumberArray.length; i++){
			ReadPassengerEventThread thread = new ReadPassengerEventThread(clientManager, passengerEventPhoneNumberArray[i]);
			threadList.add(thread);
		}

		for (ReadPassengerEventThread thread : threadList){
			thread.start();
		}

		for (ReadPassengerEventThread thread : threadList){
			try {
				thread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		for (int i = 0; i < threadList.size(); i++){
			passengerEventList.add(new Event(
					passengerEventPhoneNumberArray[i], 
					threadList.get(i).getOrderedList().get(0).getValue(),
					threadList.get(i).getOrderedList().get(1).getValue(), 
					threadList.get(i).getOrderedList().get(2).getValue(), 
					threadList.get(i).getOrderedList().get(3).getValue(),
					threadList.get(i).getOrderedList().get(4).getValue(),
					threadList.get(i).getOrderedList().get(5).getValue(),
					threadList.get(i).getOrderedList().get(6).getValue(),
					"", "", "", "", ""));
			passengerEventDateList.add(threadList.get(i).getOrderedList().get(2).getValue());
		}




		Message msg = Message.obtain();
		msg.what = 1;
		handler.sendMessage(msg);


	}





	private String[] driverEventPhoneNumberArray ;
	private String[] passengerEventPhoneNumberArray;



	private void loadAllEvents(){

		unsortedEventList = new ArrayList<Event>(); 

		driverEventList = new ArrayList<Event>();
		passengerEventList = new ArrayList<Event>();

		unsortedDateList =new ArrayList<String>();
		driverEventDateList = new ArrayList<String>();
		passengerEventDateList = new ArrayList<String>();


		map = new HashMap<Date, Integer>();

		unsortedEventTypeList = new ArrayList<String>();












		runOnUiThread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				new Thread(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						driverEventPhoneNumberArray = SimpleDB.getItemNamesForDomain(clientManager, SimpleDB.DRIVER_EVENT_TABLE);
						passengerEventPhoneNumberArray = SimpleDB.getItemNamesForDomain(clientManager, SimpleDB.PASSENGER_EVENT_TABLE);




						/*	for (int i = 0; i < driverEventPhoneNumberArray.length; i++){
							System.out.println(driverEventPhoneNumberArray[i]);
						}
						 */


						/*


							for (int i = 0; i < driverEventPhoneNumberArray.length; i++){
								Future<GetAttributesResult> future = SimpleDB.getInstance(clientManager).getAttributesAsync(new GetAttributesRequest(SimpleDB.DRIVER_EVENT_TABLE, driverEventPhoneNumberArray[i]));

								try {
									GetAttributesResult result = future.get();
									List<Attribute> list = result.getAttributes();
									ArrayList<Attribute> orderedList = new ArrayList<Attribute>(list);

									for (Attribute attribute: list){
										String name = attribute.getName().toString();

										switch (attributeType.toAttribute(name)){
										case eventTitleAttribute: orderedList.set(0, attribute); break;
										case eventContentAttribute: orderedList.set(1, attribute); break;
										case eventDateAttribute: orderedList.set(2, attribute); break; 
										case postUsernameAttribute: orderedList.set(3, attribute); break;
										case postPhotoUrlAttribute: orderedList.set(4, attribute); break; 
										case availableSeatNumberAttribute: orderedList.set(5, attribute); break;
										default:
											break;
										}
											if (attribute.getName().equals("eventTitleAttribute")){
										orderedList.set(0, attribute);
									}
									else if (attribute.getName().equals("eventContentAttribute")){
										orderedList.set(1, attribute);
									}
									else if (attribute.getName().equals("eventDateAttribute")){
										orderedList.set(2, attribute);

									}
									else if (attribute.getName().equals("postUsernameAttribute")){
										orderedList.set(3, attribute);

									}
									else if (attribute.getName().equals("postPhotoUrlAttribute")){
										orderedList.set(4, attribute);

									}
									else if (attribute.getName().equals("availableSeatNumberAttribute")){
										orderedList.set(5, attribute);

									}
									}


									driverEventList.add(new Event(driverEventPhoneNumberArray[index], orderedList.get(0).getValue(), orderedList.get(1).getValue(), orderedList.get(2).getValue(), orderedList.get(3).getValue(), orderedList.get(4).getValue(), orderedList.get(5).getValue(), ""));
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (ExecutionException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}


							}


							String[] passengerEventPhoneNumberArray = SimpleDB.getItemNamesForDomain(clientManager, SimpleDB.PASSENGER_EVENT_TABLE);


							for (int i = 0; i < passengerEventPhoneNumberArray.length; i++){

								Future<GetAttributesResult> future = SimpleDB.getInstance(clientManager).getAttributesAsync(new GetAttributesRequest(SimpleDB.PASSENGER_EVENT_TABLE, passengerEventPhoneNumberArray[i]));

								try {
									GetAttributesResult result = future.get();
									List<Attribute> list = result.getAttributes();
									ArrayList<Attribute> orderedList = new ArrayList<Attribute>(list);


									for (Attribute attribute: list){
										String name = attribute.getName().toString();
										switch (attributeType.toAttribute(name)){
										case eventTitleAttribute: orderedList.set(0, attribute); break;
										case eventContentAttribute: orderedList.set(1, attribute); break;
										case eventDateAttribute: orderedList.set(2, attribute); break; 
										case postUsernameAttribute: orderedList.set(3, attribute); break;
										case postPhotoUrlAttribute: orderedList.set(4, attribute); break;
										case availableSeatNumberAttribute: orderedList.set(5, attribute); break;
										case finalConfirmDriverAttribute: orderedList.set(6, attribute); break;
										}
										if (attribute.getName().equals("eventTitleAttribute")){
										orderedList.set(0, attribute);
									}
									else if (attribute.getName().equals("eventContentAttribute")){
										orderedList.set(1, attribute);
									}
									else if (attribute.getName().equals("eventDateAttribute")){
										orderedList.set(2, attribute);

									}
									else if (attribute.getName().equals("postUsernameAttribute")){
										orderedList.set(3, attribute);

									}
									else if (attribute.getName().equals("postPhotoUrlAttribute")){
										orderedList.set(4, attribute);

									}
									else if (attribute.getName().equals("availableSeatNumberAttribute")){
										orderedList.set(5, attribute);

									}
									else if (attribute.getName().equals("finalConfirmDriverAttribute")){
										orderedList.set(6, attribute);

									}

									}
									passengerEventList.add(new Event(driverEventPhoneNumberArray[index], orderedList.get(0).getValue(), orderedList.get(1).getValue(), orderedList.get(2).getValue(), orderedList.get(3).getValue(), orderedList.get(4).getValue(), orderedList.get(5).getValue(), orderedList.get(6).getValue()));
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (ExecutionException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

								//String eventTitle = SimpleDB.getSingleAttributesForItem(clientManager, SimpleDB.PASSENGER_EVENT_TABLE, passengerEventPhoneNumberArray[i], SimpleDB.PASSENGER_EVENT_EVENT_TITLE_ATTRIBUTE);
								//String eventContent = SimpleDB.getSingleAttributesForItem(clientManager, SimpleDB.PASSENGER_EVENT_TABLE, passengerEventPhoneNumberArray[i], SimpleDB.PASSENGER_EVENT_EVENT_CONTENT_ATTRIBUTE);
								//String eventDate = SimpleDB.getSingleAttributesForItem(clientManager, SimpleDB.PASSENGER_EVENT_TABLE, passengerEventPhoneNumberArray[i], SimpleDB.PASSENGER_EVENT_EVENT_DATE_ATTRIBUTE);
								//String postUsername = SimpleDB.getSingleAttributesForItem(clientManager, SimpleDB.PASSENGER_EVENT_TABLE, passengerEventPhoneNumberArray[i], SimpleDB.PASSENGER_EVENT_POST_USERNAME_ATTRIBUTE);
								//String postPhotoUrl = SimpleDB.getSingleAttributesForItem(clientManager, SimpleDB.PASSENGER_EVENT_TABLE, passengerEventPhoneNumberArray[i], SimpleDB.PASSENGER_EVENT_POST_PHOTOURL_ATTRIBUTE);
								//String availableSeatNumber = SimpleDB.getSingleAttributesForItem(clientManager, SimpleDB.PASSENGER_EVENT_TABLE, passengerEventPhoneNumberArray[i], SimpleDB.PASSENGER_EVENT_AVAILABLE_SEAT_NUMBER_ATTRIBUTE);

								//passengerEventList.add(new Event(driverEventPhoneNumberArray[i], eventTitle, "", "", "", "", ""));
								//passengerEventList.add(new Event(driverEventPhoneNumberArray[i], eventTitle, eventContent, eventDate, postUsername, postPhotoUrl, availableSeatNumber));
								//passengerEventList.add(new Event());

								//passengerEventDateList.add(eventDate);

							}*/

						/*	unsortedDateList.addAll(driverEventDateList);
							unsortedDateList.addAll(passengerEventDateList);


							unsortedEventList.addAll(driverEventList);
							unsortedEventList.addAll(passengerEventList);

							ArrayList<String> driverEventTypeList = new ArrayList<String>();
							for (Event event : driverEventList){
								driverEventTypeList.add("driver");	
							}
							ArrayList<String> passengerEventTypeList = new ArrayList<String>();

							for (Event event : passengerEventList){
								passengerEventTypeList.add("passenger");
							}

							unsortedEventTypeList.addAll(driverEventTypeList);
							unsortedEventTypeList.addAll(passengerEventTypeList);*/

						//sortEventByDate();

						Message msg = Message.obtain();
						msg.what = 2;
						handler.sendMessage(msg);



					}

				}).start();
			}

		});
	}


	private void sortEventByDate(){
		
		System.out.println("sort by date");
		sortedDateList = new ArrayList<Date>();
		for (int i = 0; i < unsortedDateList.size(); i++){
			Date start_date = new Date();
			try {
				start_date = new SimpleDateFormat("EE, yy/MM/dd h:mm a", Locale.US).parse(unsortedDateList.get(i));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			map.put(start_date, i);

			sortedDateList.add(start_date);
		}

		Collections.sort(sortedDateList);
		/*Collections.sort(sortedDateList, new Comparator<Date>() {
			@Override
			public int compare(Date lhs, Date rhs) {	
			
				return lhs.compareTo(rhs);
				if (lhs.after(rhs))
					return -1;
				else if (lhs.equals(rhs))
					return 0;
				else
					return 1;
			}
		});
		*/
		//Collections.reverse(sortedDateList);
		

		sortedEventList = new ArrayList<Event>(unsortedEventList);
		sortedEventTypeList = new ArrayList<String>(unsortedEventTypeList);

		for (int i = 0; i < unsortedEventList.size(); i++){
			sortedEventList.set(i, unsortedEventList.get(map.get(sortedDateList.get(i))));
			sortedEventTypeList.set(i, unsortedEventTypeList.get(map.get(sortedDateList.get(i))));
		}

		//Collections.reverse(sortedEventList);
		//Collections.reverse(sortedEventTypeList);

	}



	private void addItemToListView(){

		unsortedDateList.addAll(driverEventDateList);
		unsortedDateList.addAll(passengerEventDateList);

		unsortedEventList.addAll(driverEventList);
		unsortedEventList.addAll(passengerEventList);

		ArrayList<String> driverEventTypeList = new ArrayList<String>();
		for (Event event : driverEventList){
			driverEventTypeList.add("driver");	
		}
		ArrayList<String> passengerEventTypeList = new ArrayList<String>();

		for (Event event : passengerEventList){
			passengerEventTypeList.add("passenger");
		}

		unsortedEventTypeList.addAll(driverEventTypeList);
		unsortedEventTypeList.addAll(passengerEventTypeList);

		
		
		sortEventByDate();


		EventArrayAdapter adapter = new EventArrayAdapter(getBaseContext(), sortedEventList, sortedEventTypeList);

		event_pool_listView.setAdapter(adapter);


		setListViewControl();

	}


	private void setListViewControl(){
		event_pool_listView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				
				Bundle bundle = new Bundle();
				
				if (sortedEventTypeList.get(position).equals("driver")){
					bundle.putString("eventType", sortedEventTypeList.get(position));
					bundle.putString("eventPhoneNumber", sortedEventList.get(position).getPhoneNumber());
					bundle.putString("eventTitle", sortedEventList.get(position).getEventTitle());
					bundle.putString("eventContent", sortedEventList.get(position).getEventContent());
					bundle.putString("eventDate", sortedEventList.get(position).getEventDate());
					bundle.putString("postUsername", sortedEventList.get(position).getPostUsername());
					bundle.putString("postPhotoUrl", sortedEventList.get(position).getPostPhotoUrl());
					bundle.putString("availableSeatNumber", sortedEventList.get(position).getAvailableSeatNumber());
					bundle.putString("finalConfirmPassenger", sortedEventList.get(position).getFinalConfirmPasssenger());
				}
				else{
					bundle.putString("eventType", sortedEventTypeList.get(position));
					bundle.putString("eventPhoneNumber", sortedEventList.get(position).getPhoneNumber());
					bundle.putString("eventTitle", sortedEventList.get(position).getEventTitle());
					bundle.putString("eventContent", sortedEventList.get(position).getEventContent());
					bundle.putString("eventDate", sortedEventList.get(position).getEventDate());
					bundle.putString("postUsername", sortedEventList.get(position).getPostUsername());
					bundle.putString("postPhotoUrl", sortedEventList.get(position).getPostPhotoUrl());
					bundle.putString("availableSeatNumber", sortedEventList.get(position).getAvailableSeatNumber());
					bundle.putString("finalConfirmDriver", sortedEventList.get(position).getFinalConfirmDriver());
					bundle.putString("finalConfirmExtraPassenger", sortedEventList.get(position).getFinalConfirmExtraPassenger());
				}
				
		
				Intent intent = new Intent("yicheng.carpoolapp.EVENTINFOACTIVITY");
				intent.putExtra("eventInfo", bundle);
				startActivity(intent);

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
				(ConnectivityManager)EventPoolActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo wifi_network = 
				connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo mobile_network = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);


		return wifi_network.isConnectedOrConnecting() || mobile_network.isConnectedOrConnecting();

		/*return mobile_network.isConnectedOrConnecting();*/
	}


}
