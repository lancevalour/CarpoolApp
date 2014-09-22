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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ActiveEventActivity extends Activity{
	private LinearLayout active_event_loading_layout;
	private ListView active_event_listView;
	private TextView active_event_empty_active_event_textView;

	private SwipeRefreshLayout active_event_refreshLayout;


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
		setContentView(R.layout.active_event_activity_layout);


		clientManager = new AmazonClientManager();

		PHONE_NUMBER = local_user_information.getString("phoneNumber", "default");
		ACCOUNT_TYPE = local_user_information.getString("accountType", "default");


		initiateComponents();
		setHandlerControl();



		if (isConnectedToInternet()){
			loadAllEvents();

			active_event_loading_layout.setVisibility(View.VISIBLE);

			active_event_empty_active_event_textView.setVisibility(View.INVISIBLE);
		}
		else{
			Toast.makeText(getBaseContext(), "Unable to connect to Internet, please check your network.", Toast.LENGTH_LONG).show();
		}

		setRefreshLayoutControl();
	}

	private void initiateComponents(){
		active_event_loading_layout = (LinearLayout) findViewById(R.id.active_event_loading_layout);
		active_event_listView = (ListView) findViewById(R.id.active_event_listView);
		active_event_empty_active_event_textView = (TextView) findViewById(R.id.active_event_empty_active_event_textView);
		active_event_refreshLayout = (SwipeRefreshLayout) findViewById(R.id.active_event_refreshLayout);
	}

	private void setRefreshLayoutControl(){
		active_event_refreshLayout.setColorScheme(android.R.color.darker_gray, 
				android.R.color.holo_orange_light, 
				android.R.color.darker_gray, 
				android.R.color.holo_green_dark);
		active_event_refreshLayout.setOnRefreshListener(new OnRefreshListener(){

			@Override
			public void onRefresh() {
				// TODO Auto-generated method stub

				active_event_refreshLayout.setEnabled(false);
				active_event_refreshLayout.setRefreshing(true);
				if (isConnectedToInternet()){
					loadAllEvents();
					active_event_loading_layout.setVisibility(View.VISIBLE);
					active_event_empty_active_event_textView.setVisibility(View.INVISIBLE);
				}
				else{
					Toast.makeText(getBaseContext(), "Unable to connect to Internet, please check your network.", Toast.LENGTH_LONG).show();
				}
			}

		});
	}



	private void setHandlerControl(){
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == 1){
					setMyActiveEventDisplay();

				}

				if (msg.what == 2){
					loadMyActiveEvent();
				}

				if (msg.what == 4){
					if (active_event_loading_layout.getVisibility() == View.VISIBLE){
						active_event_loading_layout.setVisibility(View.INVISIBLE);
					}

					if (active_event_refreshLayout.isRefreshing()){
						active_event_refreshLayout.setRefreshing(false);
						active_event_refreshLayout.setEnabled(true);
					}

				}
				if (msg.what == 5){
					addItemToList();
					if (active_event_loading_layout.getVisibility() == View.VISIBLE){
						active_event_loading_layout.setVisibility(View.INVISIBLE);
					}

					if (active_event_refreshLayout.isRefreshing()){
						active_event_refreshLayout.setRefreshing(false);
						active_event_refreshLayout.setEnabled(true);
					}
				}
				if (msg.what == 6){
					if (ACCOUNT_TYPE.equals("driver")){
						deleteDriverPhoneNumberFromInterestedDriverPhoneNumber(deletedEventPhoneNumber);
					}
					else{
						if (driverEventPhoneNumberList.contains(deletedEventPhoneNumber)){
							deletePassengerPhoneNumberFromInterestedPassengerPhoneNumber(deletedEventPhoneNumber);
						}
						else{
							deleteExtraPassengerPhoneNumberFromInterestedExtraPasssengerPhoneNumber(deletedEventPhoneNumber);
						}
					}
				}

				if (msg.what == 7){
					System.out.println("tempComfirmDriverPhoneNumber:  " + tempComfirmDriverPhoneNumber);
					System.out.println("tempComfirmPassengerPhoneNumber:  " + tempComfirmPassengerPhoneNumber);
					System.out.println("tempComfirmExtraPassengerPhoneNumber:  " + tempComfirmExtraPassengerPhoneNumber);
					System.out.println("interestedDriverPhoneNumber:  " + interestedDriverPhoneNumber);
					System.out.println("interestedPassengerPhoneNumber:  " + interestedPassengerPhoneNumber);
					System.out.println("interestedExtraPassengerPhoneNumber:  " + interestedExtraPassengerPhoneNumber);

					tempConfirmEventDatabaseUpdate();
				}



			}
		};
	}


	private void setMyActiveEventDisplay(){



		if (contactEventPhoneNumberArray[0].length() < 1 
				&& tempConfirmEventPhoneNumberArray[0].length() < 1 
				&& finalConfirmEventPhoneNumberArray[0].length() < 1){


			active_event_empty_active_event_textView.setVisibility(View.VISIBLE);

			Message msg = Message.obtain();
			msg.what = 4;
			handler.sendMessage(msg);

		}
		else{



			loadActiveEvents();


		}



	}

	private void loadActiveEvents(){
		if (ACCOUNT_TYPE.equals("driver")){
			loadDriverActiveEvents();
		}
		else{
			loadPassengerActiveEvents();	
		}
	}



	private void loadPassengerActiveEvents(){
		activeEventList = new ArrayList<Event>();

		ArrayList<ReadDriverEventThread> driverEventThreadList = new ArrayList<ReadDriverEventThread>();
		ArrayList<ReadDriverEventThread> finalDriverEventThreadList = new ArrayList<ReadDriverEventThread>();
		ArrayList<ReadDriverEventThread> tempDriverEventThreadList = new ArrayList<ReadDriverEventThread>();
		ArrayList<ReadDriverEventThread> contactDriverEventThreadList = new ArrayList<ReadDriverEventThread>();

		ArrayList<ReadPassengerEventThread> passengerEventThreadList = new ArrayList<ReadPassengerEventThread>();
		ArrayList<ReadPassengerEventThread> finalPassengerEventThreadList = new ArrayList<ReadPassengerEventThread>();
		ArrayList<ReadPassengerEventThread> tempPassengerEventThreadList = new ArrayList<ReadPassengerEventThread>();
		ArrayList<ReadPassengerEventThread> contactPassengerEventThreadList = new ArrayList<ReadPassengerEventThread>();

		if (finalConfirmEventPhoneNumberArray[0].length() > 1){
			for (int i = 0; i < finalConfirmEventPhoneNumberArray.length; i++){
				if (driverEventPhoneNumberList.contains(finalConfirmEventPhoneNumberArray[i])){
					ReadDriverEventThread thread = new ReadDriverEventThread(clientManager, finalConfirmEventPhoneNumberArray[i]);
					finalDriverEventThreadList.add(thread);

				}
				else{
					ReadPassengerEventThread thread = new ReadPassengerEventThread(clientManager, finalConfirmEventPhoneNumberArray[i]);
					finalPassengerEventThreadList.add(thread);

				}
			}
		}



		if ( tempConfirmEventPhoneNumberArray[0].length() > 1){
			for (int i = 0; i < tempConfirmEventPhoneNumberArray.length; i++){
				if (driverEventPhoneNumberList.contains(tempConfirmEventPhoneNumberArray[i])){
					ReadDriverEventThread thread = new ReadDriverEventThread(clientManager, tempConfirmEventPhoneNumberArray[i]);
					tempDriverEventThreadList.add(thread);

				}
				else{
					ReadPassengerEventThread thread = new ReadPassengerEventThread(clientManager, tempConfirmEventPhoneNumberArray[i]);
					tempPassengerEventThreadList.add(thread);

				}
			}
		}

		if (contactEventPhoneNumberArray[0].length() > 1){
			for (int i = 0; i < contactEventPhoneNumberArray.length; i++){
				if (driverEventPhoneNumberList.contains(contactEventPhoneNumberArray[i])){
					ReadDriverEventThread thread = new ReadDriverEventThread(clientManager, contactEventPhoneNumberArray[i]);
					contactDriverEventThreadList.add(thread);

				}
				else{
					ReadPassengerEventThread thread = new ReadPassengerEventThread(clientManager, contactEventPhoneNumberArray[i]);
					contactPassengerEventThreadList.add(thread);

				}
			}
		}

		driverEventThreadList.addAll(finalDriverEventThreadList);
		driverEventThreadList.addAll(tempDriverEventThreadList);
		driverEventThreadList.addAll(contactDriverEventThreadList);

		passengerEventThreadList.addAll(finalPassengerEventThreadList);
		passengerEventThreadList.addAll(tempPassengerEventThreadList);
		passengerEventThreadList.addAll(contactPassengerEventThreadList);

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

		ArrayList<Event> finalEvent = new ArrayList<Event>();
		ArrayList<Event> tempEvent = new ArrayList<Event>();
		ArrayList<Event> contactEvent = new ArrayList<Event>();

		for (int i = 0; i < finalDriverEventThreadList.size(); i++){
			finalEvent.add(new Event(
					finalDriverEventThreadList.get(i).getEventPhoneNumber(), 
					finalDriverEventThreadList.get(i).getOrderedList().get(0).getValue(),
					finalDriverEventThreadList.get(i).getOrderedList().get(1).getValue(), 
					finalDriverEventThreadList.get(i).getOrderedList().get(2).getValue(), 
					finalDriverEventThreadList.get(i).getOrderedList().get(3).getValue(),
					finalDriverEventThreadList.get(i).getOrderedList().get(4).getValue(),
					finalDriverEventThreadList.get(i).getOrderedList().get(5).getValue(),
					"", "", "", "", "", ""));
		}

		for (int i = 0; i < finalPassengerEventThreadList.size(); i++){
			finalEvent.add(new Event(
					finalPassengerEventThreadList.get(i).getEventPhoneNumber(), 
					finalPassengerEventThreadList.get(i).getOrderedList().get(0).getValue(),
					finalPassengerEventThreadList.get(i).getOrderedList().get(1).getValue(), 
					finalPassengerEventThreadList.get(i).getOrderedList().get(2).getValue(), 
					finalPassengerEventThreadList.get(i).getOrderedList().get(3).getValue(),
					finalPassengerEventThreadList.get(i).getOrderedList().get(4).getValue(),
					finalPassengerEventThreadList.get(i).getOrderedList().get(5).getValue(),
					finalPassengerEventThreadList.get(i).getOrderedList().get(6).getValue(), 
					"", "", "", "", ""));
		}


		for (int i =0; i < passengerEventThreadList.size(); i++){
			System.out.println(passengerEventThreadList.
					get(i).
					getOrderedList().
					get(0));
		}

		for (int i = 0; i < tempDriverEventThreadList.size(); i++){

			tempEvent.add(new Event(
					tempDriverEventThreadList.get(i).getEventPhoneNumber(), 
					tempDriverEventThreadList.get(i).getOrderedList().get(0).getValue(),
					tempDriverEventThreadList.get(i).getOrderedList().get(1).getValue(), 
					tempDriverEventThreadList.get(i).getOrderedList().get(2).getValue(), 
					tempDriverEventThreadList.get(i).getOrderedList().get(3).getValue(),
					tempDriverEventThreadList.get(i).getOrderedList().get(4).getValue(),
					tempDriverEventThreadList.get(i).getOrderedList().get(5).getValue(),
					"", "", "", "", "", ""));



		}

		for (int i = 0; i < tempPassengerEventThreadList.size(); i++){
			tempEvent.add(new Event(
					tempPassengerEventThreadList.get(i).getEventPhoneNumber(), 
					tempPassengerEventThreadList.get(i).getOrderedList().get(0).getValue(),
					tempPassengerEventThreadList.get(i).getOrderedList().get(1).getValue(), 
					tempPassengerEventThreadList.get(i).getOrderedList().get(2).getValue(), 
					tempPassengerEventThreadList.get(i).getOrderedList().get(3).getValue(),
					tempPassengerEventThreadList.get(i).getOrderedList().get(4).getValue(),
					tempPassengerEventThreadList.get(i).getOrderedList().get(5).getValue(),
					tempPassengerEventThreadList.get(i).getOrderedList().get(6).getValue(), 
					"", "", "", "", ""));
		}

		for (int i = 0; i < contactDriverEventThreadList.size(); i++){
			contactEvent.add(new Event(
					contactDriverEventThreadList.get(i).getEventPhoneNumber(), 
					contactDriverEventThreadList.get(i).getOrderedList().get(0).getValue(),
					contactDriverEventThreadList.get(i).getOrderedList().get(1).getValue(), 
					contactDriverEventThreadList.get(i).getOrderedList().get(2).getValue(), 
					contactDriverEventThreadList.get(i).getOrderedList().get(3).getValue(),
					contactDriverEventThreadList.get(i).getOrderedList().get(4).getValue(),
					contactDriverEventThreadList.get(i).getOrderedList().get(5).getValue(),
					"", "", "", "", "", ""));
		}

		for (int i = 0; i < contactPassengerEventThreadList.size(); i++){
			contactEvent.add(new Event(
					contactPassengerEventThreadList.get(i).getEventPhoneNumber(), 
					contactPassengerEventThreadList.get(i).getOrderedList().get(0).getValue(),
					contactPassengerEventThreadList.get(i).getOrderedList().get(1).getValue(), 
					contactPassengerEventThreadList.get(i).getOrderedList().get(2).getValue(), 
					contactPassengerEventThreadList.get(i).getOrderedList().get(3).getValue(),
					contactPassengerEventThreadList.get(i).getOrderedList().get(4).getValue(),
					contactPassengerEventThreadList.get(i).getOrderedList().get(5).getValue(),
					contactPassengerEventThreadList.get(i).getOrderedList().get(6).getValue(), 
					"", "", "", "", ""));
		}

		activeEventList.addAll(finalEvent);
		activeEventList.addAll(tempEvent);
		activeEventList.addAll(contactEvent);


		Message msg = Message.obtain();
		msg.what = 5;
		handler.sendMessage(msg);
	}

	private void loadDriverActiveEvents(){
		activeEventList = new ArrayList<Event>();

		ArrayList<ReadPassengerEventThread> threadList = new ArrayList<ReadPassengerEventThread>();
		ArrayList<ReadPassengerEventThread> finalThreadList = new ArrayList<ReadPassengerEventThread>();
		ArrayList<ReadPassengerEventThread> tempThreadList = new ArrayList<ReadPassengerEventThread>();
		ArrayList<ReadPassengerEventThread> contactThreadList = new ArrayList<ReadPassengerEventThread>();


		if (finalConfirmEventPhoneNumberArray[0].length() > 1){
			for (int i = 0; i <finalConfirmEventPhoneNumberArray.length; i++){
				ReadPassengerEventThread readFinalConfrimEventThread = new ReadPassengerEventThread(clientManager, finalConfirmEventPhoneNumberArray[i]);
				finalThreadList.add(readFinalConfrimEventThread);
			}
		}

		if ( tempConfirmEventPhoneNumberArray[0].length() > 1){
			for (int i = 0; i < tempConfirmEventPhoneNumberArray.length; i++){
				ReadPassengerEventThread thread = new ReadPassengerEventThread(clientManager, tempConfirmEventPhoneNumberArray[i]);
				tempThreadList.add(thread);
			}
		}

		if (contactEventPhoneNumberArray[0].length() > 1){
			for (int i = 0; i < contactEventPhoneNumberArray.length; i++){
				ReadPassengerEventThread thread = new ReadPassengerEventThread(clientManager, contactEventPhoneNumberArray[i]);
				contactThreadList.add(thread);
			}
		}

		threadList.addAll(finalThreadList);
		threadList.addAll(tempThreadList);
		threadList.addAll(contactThreadList);

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

		ArrayList<Event> finalEvent = new ArrayList<Event>();
		ArrayList<Event> tempEvent = new ArrayList<Event>();
		ArrayList<Event> contactEvent = new ArrayList<Event>();


		for (int i = 0; i < finalThreadList.size(); i++){
			finalEvent.add(new Event(
					finalThreadList.get(i).getEventPhoneNumber(), 
					finalThreadList.get(i).getOrderedList().get(0).getValue(),
					finalThreadList.get(i).getOrderedList().get(1).getValue(), 
					finalThreadList.get(i).getOrderedList().get(2).getValue(), 
					finalThreadList.get(i).getOrderedList().get(3).getValue(),
					finalThreadList.get(i).getOrderedList().get(4).getValue(),
					finalThreadList.get(i).getOrderedList().get(5).getValue(),
					finalThreadList.get(i).getOrderedList().get(6).getValue(),
					"", "", "", "", ""));
		}

		for (int i = 0; i < tempThreadList.size(); i++){
			tempEvent.add(new Event(
					tempThreadList.get(i).getEventPhoneNumber(), 
					tempThreadList.get(i).getOrderedList().get(0).getValue(),
					tempThreadList.get(i).getOrderedList().get(1).getValue(), 
					tempThreadList.get(i).getOrderedList().get(2).getValue(), 
					tempThreadList.get(i).getOrderedList().get(3).getValue(),
					tempThreadList.get(i).getOrderedList().get(4).getValue(),
					tempThreadList.get(i).getOrderedList().get(5).getValue(),
					tempThreadList.get(i).getOrderedList().get(6).getValue(),
					"", "", "", "", ""));
		}

		for (int i = 0; i < contactThreadList.size(); i++){
			contactEvent.add(new Event(
					contactThreadList.get(i).getEventPhoneNumber(), 
					contactThreadList.get(i).getOrderedList().get(0).getValue(),
					contactThreadList.get(i).getOrderedList().get(1).getValue(), 
					contactThreadList.get(i).getOrderedList().get(2).getValue(), 
					contactThreadList.get(i).getOrderedList().get(3).getValue(),
					contactThreadList.get(i).getOrderedList().get(4).getValue(),
					contactThreadList.get(i).getOrderedList().get(5).getValue(),
					contactThreadList.get(i).getOrderedList().get(6).getValue(),
					"", "", "", "", ""));
		}


		activeEventList.addAll(finalEvent);
		activeEventList.addAll(tempEvent);
		activeEventList.addAll(contactEvent);


		Message msg = Message.obtain();
		msg.what = 5;
		handler.sendMessage(msg);



	}

	private void addItemToList(){

		activeEventTypeList = new ArrayList<String>();

		if (finalConfirmEventPhoneNumberArray[0].length() > 1){
			for (int i = 0; i < finalConfirmEventPhoneNumberArray.length; i++){
				activeEventTypeList.add("finalConfirmEvent");
			}
		}
		if ( tempConfirmEventPhoneNumberArray[0].length() > 1){
			for (int i = 0; i < tempConfirmEventPhoneNumberArray.length; i++){
				activeEventTypeList.add("tempConfrimEvent");
			}
		}
		if (contactEventPhoneNumberArray[0].length() > 1){
			for (int i = 0; i < contactEventPhoneNumberArray.length; i++){
				activeEventTypeList.add("contactEvent");
			}
		}

		for (Event e : activeEventList){
			System.out.println("active event list: " + e.getPhoneNumber());
		}



		ActiveEventArrayAdapter adapter = new ActiveEventArrayAdapter(getBaseContext(), activeEventList, activeEventTypeList);

		active_event_listView.setAdapter(adapter);

		setListViewControl();
	}

	private String deletedEventType;
	private String deletedEventPhoneNumber;
	private Event deletedEvent;

	private String tempConfirmEventPhoneNumber;
	private Event tempConfirmEvent;

	private void setListViewControl(){

		SwipeDismissListViewTouchListener touchListener =
				new SwipeDismissListViewTouchListener(
						active_event_listView,
						new SwipeDismissListViewTouchListener.DismissCallbacks() {
							@Override
							public boolean canDismiss(int position) {
								return true;
							}

							@Override
							public void onDismiss(ListView listView, int[] reverseSortedPositions) {

								final int position = reverseSortedPositions[0];
								AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ActiveEventActivity.this);	

								LayoutInflater factory = LayoutInflater.from(ActiveEventActivity.this);
								View view = factory.inflate(
										R.layout.custom_dialog_layout, null);
								dialogBuilder.setView(view);
								TextView dialog_title = (TextView) view.findViewById(R.id.custom_dialog_title);
								dialog_title.setText("Delete Event");
								TextView dialog_content = (TextView) view.findViewById(R.id.custom_dialog_content);


								deletedEvent = activeEventList.get(position);
								deletedEventType = activeEventTypeList.get(position);
								deletedEventPhoneNumber = activeEventList.get(position).getPhoneNumber();
								activeEventList.remove(position);
								activeEventTypeList.remove(position);

								ActiveEventArrayAdapter adapter = new ActiveEventArrayAdapter(getBaseContext(), activeEventList, activeEventTypeList);

								active_event_listView.setAdapter(adapter);

								if (activeEventList.size() < 1){
									active_event_empty_active_event_textView.setVisibility(View.VISIBLE);
								}



								if (deletedEventType.equals("finalConfirmEvent")){
									dialog_content.setText("Do you want to delete this final confirm event?");

									dialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener(){

										@Override
										public void onClick(DialogInterface dialog, int which) {
											// TODO Auto-generated method stub


										}

									});

								}
								else if (deletedEventType.equals("tempConfrimEvent")){

									dialog_content.setText("Do you want to delete this temp confirm event?");
									//dialogBuilder.setMessage("Do you want to delete this temp confirm event?");
									dialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener(){

										@Override
										public void onClick(DialogInterface dialog, int which) {
											// TODO Auto-generated method stub




										}

									});

								}
								else{
									dialog_content.setText("Do you want to delete this contact event?");
									//dialogBuilder.setMessage("Do you want to delete this contact event?");
									dialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener(){

										@Override
										public void onClick(DialogInterface dialog, int which) {
											// TODO Auto-generated method stub


											deleteContactEvent(deletedEventPhoneNumber);
										}

									});

								}
								// TODO Auto-generated method stub


								dialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener(){

									@Override
									public void onClick(DialogInterface dialog, int which) {
										// TODO Auto-generated method stub

										activeEventList.add(position, deletedEvent);
										activeEventTypeList.add(position, deletedEventType);

										ActiveEventArrayAdapter adapter = new ActiveEventArrayAdapter(getBaseContext(), activeEventList, activeEventTypeList);

										active_event_listView.setAdapter(adapter);

										if (activeEventList.size() >= 1){
											active_event_empty_active_event_textView.setVisibility(View.INVISIBLE);
										}


										deletedEventPhoneNumber = "";
										deletedEventType = "";

										dialog.cancel();

									}

								});


								dialogBuilder.create().show();


							}
						});
		active_event_listView.setOnTouchListener(touchListener);

		active_event_listView.setOnItemLongClickListener(new OnItemLongClickListener(){

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					final int position, long id) {
				if (activeEventTypeList.get(position).equals("contactEvent")){
					AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ActiveEventActivity.this);	

					/*LayoutInflater factory = LayoutInflater.from(ActiveEventActivity.this);
					View view1 = factory.inflate(
							R.layout.custom_dialog_layout, null);
					dialogBuilder.setView(view);
					TextView dialog_title = (TextView) view1.findViewById(R.id.custom_dialog_title);
					dialog_title.setText("Confirm Event");
					TextView dialog_content = (TextView) view1.findViewById(R.id.custom_dialog_content);	
					dialog_content.setText("Do you want to confirm this contact event?");*/
					LayoutInflater factory = LayoutInflater.from(ActiveEventActivity.this);
					view = factory.inflate(
							R.layout.custom_dialog_layout, null);
					dialogBuilder.setView(view);
					TextView dialog_title = (TextView) view.findViewById(R.id.custom_dialog_title);
					dialog_title.setText("Confirm Event");
					TextView dialog_content = (TextView) view.findViewById(R.id.custom_dialog_content);
					dialog_content.setText("Do you want to confirm this contact event?");
					//dialogBuilder.setMessage("Do you want to confirm this contact event?");
					dialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub

							tempConfirmEvent = activeEventList.get(position);

							tempConfirmEventPhoneNumber = activeEventList.get(position).getPhoneNumber();
							activeEventList.remove(position);
							activeEventTypeList.remove(position);

							ActiveEventArrayAdapter adapter = new ActiveEventArrayAdapter(getBaseContext(), activeEventList, activeEventTypeList);

							active_event_listView.setAdapter(adapter);

							if (activeEventList.size() < 1){
								active_event_empty_active_event_textView.setVisibility(View.VISIBLE);
							}


							confirmContactEvent(tempConfirmEventPhoneNumber);


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
				// TODO Auto-generated method stub



				





				return false;
			}

		});
	}

	private ArrayList<String> newTempConfirmEventPhoneNumberList;


	private void confirmContactEvent(String tempConfirmEventPhoneNumber){
		newTempConfirmEventPhoneNumberList = new ArrayList<String>();

		for (String s : tempConfirmEventPhoneNumberArray){
			if (!s.trim().equals("")){
				newTempConfirmEventPhoneNumberList.add(s);
			}
		}
		newTempConfirmEventPhoneNumberList.add(tempConfirmEventPhoneNumber);

		String newTempConfirmEventPhoneNumber = "";

		for (String s : newTempConfirmEventPhoneNumberList){
			newTempConfirmEventPhoneNumber += s + "\n";
		}

		newContactEventPhoneNumberList = new ArrayList<String>();
		for (int i = 0; i < contactEventPhoneNumberArray.length; i++){
			if (!contactEventPhoneNumberArray[i].equals(tempConfirmEventPhoneNumber)){
				newContactEventPhoneNumberList.add(contactEventPhoneNumberArray[i]);	
			}	
		}

		String newContactEventPhoneNumber = "";

		for (String s : newContactEventPhoneNumberList){
			newContactEventPhoneNumber += s + "\n";
		}


		updateTempConfirmDatabase(newTempConfirmEventPhoneNumber, newContactEventPhoneNumber);


		getTempConfirmUser(tempConfirmEventPhoneNumber);



	}


	private String tempComfirmDriverPhoneNumber;
	private String tempComfirmPassengerPhoneNumber;
	private String tempComfirmExtraPassengerPhoneNumber;





	private void getTempConfirmUser(final String tempConfirmEventPhoneNumber){
		if (driverEventPhoneNumberList.contains(tempConfirmEventPhoneNumber)){
			runOnUiThread(new Runnable(){

				@Override
				public void run() {
					// TODO Auto-generated method stub
					new Thread(new Runnable(){

						@Override
						public void run() {
							// TODO Auto-generated method stub
							Future<GetAttributesResult> future = SimpleDB.getInstance(clientManager).getAttributesAsync(new GetAttributesRequest(SimpleDB.DRIVER_EVENT_TABLE, tempConfirmEventPhoneNumber));

							try {
								GetAttributesResult result = future.get();
								List<Attribute> list = result.getAttributes();
								ArrayList<Attribute> orderedList = new ArrayList<Attribute>(list);

								for (Attribute attribute: list){

									if (attribute.getName().equals("tempConfirmPassengerAttribute")){
										orderedList.set(0, attribute);
									}
									else if (attribute.getName().equals("interestedPassengerAttribute")){
										orderedList.set(1, attribute);
									}

								}

								tempComfirmPassengerPhoneNumber = orderedList.get(0).getValue();
								interestedPassengerPhoneNumber = orderedList.get(1).getValue();

								Message msg = Message.obtain();
								msg.what = 7;
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
							Future<GetAttributesResult> future = SimpleDB.getInstance(clientManager).getAttributesAsync(new GetAttributesRequest(SimpleDB.PASSENGER_EVENT_TABLE, tempConfirmEventPhoneNumber));

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

									else if (attribute.getName().equals("interestedDriverAttribute")){
										orderedList.set(2, attribute);
									}
									else if (attribute.getName().equals("interestedExtraPassengerAttribute")){
										orderedList.set(3, attribute);
									}

								}

								tempComfirmDriverPhoneNumber = orderedList.get(0).getValue();
								tempComfirmExtraPassengerPhoneNumber = orderedList.get(1).getValue();

								interestedDriverPhoneNumber = orderedList.get(2).getValue();
								interestedExtraPassengerPhoneNumber = orderedList.get(3).getValue();

								Message msg = Message.obtain();
								msg.what = 7;
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


	private void tempConfirmEventDatabaseUpdate(){
		runOnUiThread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				new Thread(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (ACCOUNT_TYPE.equals("driver")){
							if (!driverEventPhoneNumberList.contains(tempConfirmEventPhoneNumber)){

								String [] oldInterestedDriverArray = interestedDriverPhoneNumber.split("\n");

								ArrayList<String> newInterestedDriverList = new ArrayList<String>();
								for (int i = 0; i < oldInterestedDriverArray.length; i++){
									if (!oldInterestedDriverArray[i].equals(PHONE_NUMBER)){
										newInterestedDriverList.add(oldInterestedDriverArray[i]);	
									}	
								}

								String newInterestedDriver = "";

								for (String s : newInterestedDriverList){
									newInterestedDriver += s + "\n";
								}


								String newTempConfirmDriver = "";
								if (tempComfirmDriverPhoneNumber.trim().length() < 1){
									newTempConfirmDriver = PHONE_NUMBER + "\n";
								}
								else{
									newTempConfirmDriver = tempComfirmDriverPhoneNumber + PHONE_NUMBER + "\n" ;
								}





								//SimpleDB.updateSingleAttribute(clientManager, SimpleDB.PASSENGER_EVENT_TABLE, tempConfirmEventPhoneNumber, SimpleDB.PASSENGER_EVENT_TEMP_CONFIRM_DRIVER_ATTRIBUTE, newTempConfirmDriver);

								HashMap<String, String> oldAttribute = SimpleDB.getAttributesForItem(clientManager, SimpleDB.PASSENGER_EVENT_TABLE, tempConfirmEventPhoneNumber);
								oldAttribute.put(SimpleDB.PASSENGER_EVENT_INTERESTED_DRIVER_ATTRIBUTE, newInterestedDriver);
								oldAttribute.put(SimpleDB.PASSENGER_EVENT_TEMP_CONFIRM_DRIVER_ATTRIBUTE, newTempConfirmDriver);

								SimpleDB.updateAttributesForItem(clientManager,SimpleDB.PASSENGER_EVENT_TABLE, tempConfirmEventPhoneNumber, oldAttribute);

							}
						}
						else{
							if (driverEventPhoneNumberList.contains(tempConfirmEventPhoneNumber)){

								String [] oldInterestedPassengerArray = interestedPassengerPhoneNumber.split("\n");

								ArrayList<String> newInterestedPassengerList = new ArrayList<String>();
								for (int i = 0; i < oldInterestedPassengerArray.length; i++){
									if (!oldInterestedPassengerArray[i].equals(PHONE_NUMBER)){
										newInterestedPassengerList.add(oldInterestedPassengerArray[i]);	
									}	
								}

								String newInterestedPassenger = "";

								for (String s : newInterestedPassengerList){
									newInterestedPassenger += s + "\n";
								}



								String newTempConfirmPassenger = "";
								if (tempComfirmPassengerPhoneNumber.trim().length() < 1){
									newTempConfirmPassenger = PHONE_NUMBER + "\n";
								}
								else{
									newTempConfirmPassenger = tempComfirmPassengerPhoneNumber + PHONE_NUMBER + "\n" ;
								}


								//SimpleDB.updateSingleAttribute(clientManager, SimpleDB.DRIVER_EVENT_TABLE, tempConfirmEventPhoneNumber, SimpleDB.DRIVER_EVENT_TEMP_CONFIRM_PASSENGER_ATTRIBUTE, newTempConfirmPassenger);

								HashMap<String, String> oldAttribute = SimpleDB.getAttributesForItem(clientManager, SimpleDB.DRIVER_EVENT_TABLE, tempConfirmEventPhoneNumber);
								oldAttribute.put(SimpleDB.DRIVER_EVENT_INTERESTED_PASSENGER_ATTRIBUTE, newInterestedPassenger);
								oldAttribute.put(SimpleDB.DRIVER_EVENT_TEMP_CONFIRM_PASSENGER_ATTRIBUTE, newTempConfirmPassenger);

								SimpleDB.updateAttributesForItem(clientManager,SimpleDB.DRIVER_EVENT_TABLE, tempConfirmEventPhoneNumber, oldAttribute);


							}
							else{
								System.out.println("haha");
								System.out.println(tempConfirmEvent.getFinalConfirmDriver());
								if (!tempConfirmEvent.getFinalConfirmDriver().trim().equals("")){

									System.out.println("haha1");


									String [] oldInterestedExtraPassengerArray = interestedExtraPassengerPhoneNumber.split("\n");

									ArrayList<String> newInterestedExtraPassengerList = new ArrayList<String>();
									for (int i = 0; i < oldInterestedExtraPassengerArray.length; i++){
										if (!oldInterestedExtraPassengerArray[i].equals(PHONE_NUMBER)){
											newInterestedExtraPassengerList.add(oldInterestedExtraPassengerArray[i]);	
										}	
									}

									String newInterestedExtraPassenger = "";

									for (String s : newInterestedExtraPassengerList){
										newInterestedExtraPassenger += s + "\n";
									}


									String newTempConfirmExtraPassenger = "";
									if (tempComfirmExtraPassengerPhoneNumber.trim().length() < 1){
										newTempConfirmExtraPassenger = PHONE_NUMBER + "\n";
									}
									else{
										newTempConfirmExtraPassenger = tempComfirmExtraPassengerPhoneNumber + PHONE_NUMBER + "\n" ;
									}


									//SimpleDB.updateSingleAttribute(clientManager, SimpleDB.PASSENGER_EVENT_TABLE, tempConfirmEventPhoneNumber, SimpleDB.PASSENGER_EVENT_TEMP_CONFIRM_EXTRA_PASSENGER_ATTRIBUTE, newTempConfirmExtraPassenger);

									HashMap<String, String> oldAttribute = SimpleDB.getAttributesForItem(clientManager, SimpleDB.PASSENGER_EVENT_TABLE, tempConfirmEventPhoneNumber);
									oldAttribute.put(SimpleDB.PASSENGER_EVENT_INTERESTED_EXTRA_PASSENGER_ATTRIBUTE, newInterestedExtraPassenger);
									oldAttribute.put(SimpleDB.PASSENGER_EVENT_TEMP_CONFIRM_EXTRA_PASSENGER_ATTRIBUTE, newTempConfirmExtraPassenger);

									SimpleDB.updateAttributesForItem(clientManager,SimpleDB.PASSENGER_EVENT_TABLE, tempConfirmEventPhoneNumber, oldAttribute);


								}
							}



						}
					}

				}).start();
			}

		});
	}












	private void updateTempConfirmDatabase(final String newTempConfirmEventPhoneNumber, final String newContactEventPhoneNumber){
		runOnUiThread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				new Thread(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (ACCOUNT_TYPE.equals("driver")){


							HashMap<String, String> oldAttribute = SimpleDB.getAttributesForItem(clientManager, SimpleDB.DRIVER_ACCOUNT_TABLE, PHONE_NUMBER);
							oldAttribute.put(SimpleDB.DRIVER_ACCOUNT_CONTACT_EVENT_ATTRIBUTE, newContactEventPhoneNumber);
							oldAttribute.put(SimpleDB.DRIVER_ACCOUNT_TEMP_CONFIRM_EVENT_ATTRIBUTE, newTempConfirmEventPhoneNumber);

							SimpleDB.updateAttributesForItem(clientManager,SimpleDB.DRIVER_ACCOUNT_TABLE, PHONE_NUMBER, oldAttribute);

							/*SimpleDB.updateSingleAttribute(clientManager, SimpleDB.DRIVER_ACCOUNT_TABLE, PHONE_NUMBER, SimpleDB.DRIVER_ACCOUNT_TEMP_CONFIRM_EVENT_ATTRIBUTE, newTempConfirmEventPhoneNumber);*/
						}
						else{
							HashMap<String, String> oldAttribute = SimpleDB.getAttributesForItem(clientManager, SimpleDB.PASSENGER_ACCOUNT_TABLE, PHONE_NUMBER);
							oldAttribute.put(SimpleDB.PASSENGER_ACCOUNT_CONTACT_EVENT_ATTRIBUTE, newContactEventPhoneNumber);
							oldAttribute.put(SimpleDB.PASSENGER_ACCOUNT_TEMP_CONFIRM_EVENT_ATTRIBUTE, newTempConfirmEventPhoneNumber);

							SimpleDB.updateAttributesForItem(clientManager,SimpleDB.PASSENGER_ACCOUNT_TABLE, PHONE_NUMBER, oldAttribute);
						}

					}

				}).start();
			}

		});
	}


	private ArrayList<String> newContactEventPhoneNumberList;



	private void deleteContactEvent(String contactEventPhoneNumber){
		newContactEventPhoneNumberList = new ArrayList<String>();
		for (int i = 0; i < contactEventPhoneNumberArray.length; i++){
			if (!contactEventPhoneNumberArray[i].equals(contactEventPhoneNumber)){
				newContactEventPhoneNumberList.add(contactEventPhoneNumberArray[i]);	
			}	
		}

		String newContactEventPhoneNumber = "";

		for (String s : newContactEventPhoneNumberList){
			newContactEventPhoneNumber += s + "\n";
		}

		updateContactDatabase(newContactEventPhoneNumber);

		getInterestedUser(deletedEventPhoneNumber);




	}

	private void deleteDriverPhoneNumberFromInterestedDriverPhoneNumber(String deletedContactEventPhoneNumber){

		String [] interestedDriverPhoneNumberArray = interestedDriverPhoneNumber.split("\n");
		ArrayList<String> newInterestedDriverPhoneNumberList = new ArrayList<String>();
		for (String s : interestedDriverPhoneNumberArray){
			if (!s.equals(PHONE_NUMBER)){
				newInterestedDriverPhoneNumberList.add(s);
			}
		}

		String newInterestedDriverPhoneNumber = "";

		for (String s : newInterestedDriverPhoneNumberList){
			newInterestedDriverPhoneNumber += s + "\n";
		}

		updateInterestedDriverPhoneNumberDatabase(newInterestedDriverPhoneNumber, deletedContactEventPhoneNumber);

	}


	private void updateInterestedDriverPhoneNumberDatabase(final String newInterestedDriverPhoneNumber, final String deletedContactEventPhoneNumber){
		runOnUiThread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				new Thread(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						SimpleDB.updateSingleAttribute(clientManager, SimpleDB.PASSENGER_EVENT_TABLE, deletedContactEventPhoneNumber, SimpleDB.PASSENGER_EVENT_INTERESTED_DRIVER_ATTRIBUTE, newInterestedDriverPhoneNumber);
					}

				}).start();
			}

		});

	}


	private void deletePassengerPhoneNumberFromInterestedPassengerPhoneNumber(String deletedContactEventPhoneNumber){
		String [] interestedPassengerPhoneNumberArray = interestedPassengerPhoneNumber.split("\n");
		ArrayList<String> newInterestedPassengerPhoneNumberList = new ArrayList<String>();
		for (String s : interestedPassengerPhoneNumberArray){
			if (!s.equals(PHONE_NUMBER)){
				newInterestedPassengerPhoneNumberList.add(s);
			}
		}

		String newInterestedPassengerPhoneNumber = "";

		for (String s : newInterestedPassengerPhoneNumberList){
			newInterestedPassengerPhoneNumber += s + "\n";
		}

		updateInterestedPassengerPhoneNumberDatabase(newInterestedPassengerPhoneNumber, deletedContactEventPhoneNumber);
	}

	private void updateInterestedPassengerPhoneNumberDatabase(final String newInterestedPassengerPhoneNumber, final String deletedContactEventPhoneNumber){
		runOnUiThread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				new Thread(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						SimpleDB.updateSingleAttribute(clientManager, SimpleDB.DRIVER_EVENT_TABLE, deletedContactEventPhoneNumber, SimpleDB.DRIVER_EVENT_INTERESTED_PASSENGER_ATTRIBUTE, newInterestedPassengerPhoneNumber);
					}

				}).start();
			}

		});
	}

	private void deleteExtraPassengerPhoneNumberFromInterestedExtraPasssengerPhoneNumber(String deletedContactEventPhoneNumber){
		String [] interestedExtraPassengerPhoneNumberArray = interestedExtraPassengerPhoneNumber.split("\n");
		ArrayList<String> newInterestedExtraPassengerPhoneNumberList = new ArrayList<String>();
		for (String s : interestedExtraPassengerPhoneNumberArray){
			if (!s.equals(PHONE_NUMBER)){
				newInterestedExtraPassengerPhoneNumberList.add(s);
			}
		}

		String newInterestedExtraPassengerPhoneNumber = "";

		for (String s : newInterestedExtraPassengerPhoneNumberList){
			newInterestedExtraPassengerPhoneNumber += s + "\n";
		}

		updateInterestedExtraPassengerPhoneNumberDatabase(newInterestedExtraPassengerPhoneNumber, deletedContactEventPhoneNumber);
	}

	private void updateInterestedExtraPassengerPhoneNumberDatabase(final String newInterestedExtraPassengerPhoneNumber, final String deletedContactEventPhoneNumber){
		runOnUiThread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				new Thread(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						SimpleDB.updateSingleAttribute(clientManager, SimpleDB.PASSENGER_EVENT_TABLE, deletedContactEventPhoneNumber, SimpleDB.PASSENGER_EVENT_INTERESTED_EXTRA_PASSENGER_ATTRIBUTE, newInterestedExtraPassengerPhoneNumber);
					}

				}).start();
			}

		});
	}




	private String interestedDriverPhoneNumber;
	private String interestedPassengerPhoneNumber;
	private String interestedExtraPassengerPhoneNumber;


	private void getInterestedUser(final String deletedEventPhoneNumber){
		if (ACCOUNT_TYPE.equals("driver")){
			runOnUiThread(new Runnable(){

				@Override
				public void run() {
					// TODO Auto-generated method stub
					new Thread(new Runnable(){

						@Override
						public void run() {
							// TODO Auto-generated method stub
							Future<GetAttributesResult> future = SimpleDB.getInstance(clientManager).getAttributesAsync(new GetAttributesRequest(SimpleDB.PASSENGER_EVENT_TABLE, deletedEventPhoneNumber));

							try {
								GetAttributesResult result = future.get();
								List<Attribute> list = result.getAttributes();
								ArrayList<Attribute> orderedList = new ArrayList<Attribute>(list);

								for (Attribute attribute: list){

									if (attribute.getName().equals("interestedDriverAttribute")){
										orderedList.set(0, attribute);
									}

								}

								interestedDriverPhoneNumber = orderedList.get(0).getValue();

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
			if (driverEventPhoneNumberList.contains(deletedEventPhoneNumber)){
				runOnUiThread(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						new Thread(new Runnable(){

							@Override
							public void run() {
								// TODO Auto-generated method stub
								Future<GetAttributesResult> future = SimpleDB.getInstance(clientManager).getAttributesAsync(new GetAttributesRequest(SimpleDB.DRIVER_EVENT_TABLE, deletedEventPhoneNumber));

								try {
									GetAttributesResult result = future.get();
									List<Attribute> list = result.getAttributes();
									ArrayList<Attribute> orderedList = new ArrayList<Attribute>(list);

									for (Attribute attribute: list){

										if (attribute.getName().equals("interestedPassengerAttribute")){
											orderedList.set(0, attribute);
										}


									}

									interestedPassengerPhoneNumber = orderedList.get(0).getValue();

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
								Future<GetAttributesResult> future = SimpleDB.getInstance(clientManager).getAttributesAsync(new GetAttributesRequest(SimpleDB.PASSENGER_EVENT_TABLE, deletedEventPhoneNumber));

								try {
									GetAttributesResult result = future.get();
									List<Attribute> list = result.getAttributes();
									ArrayList<Attribute> orderedList = new ArrayList<Attribute>(list);

									for (Attribute attribute: list){

										if (attribute.getName().equals("interestedExtraPassengerAttribute")){
											orderedList.set(0, attribute);
										}

									}

									interestedExtraPassengerPhoneNumber = orderedList.get(0).getValue();

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
	}

	private void updateContactDatabase(final String newContactEventPhoneNumber){
		runOnUiThread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				new Thread(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (ACCOUNT_TYPE.equals("driver")){

							SimpleDB.updateSingleAttribute(clientManager, SimpleDB.DRIVER_ACCOUNT_TABLE, PHONE_NUMBER, SimpleDB.DRIVER_ACCOUNT_CONTACT_EVENT_ATTRIBUTE, newContactEventPhoneNumber);

						}
						else{


							SimpleDB.updateSingleAttribute(clientManager, SimpleDB.PASSENGER_ACCOUNT_TABLE, PHONE_NUMBER, SimpleDB.PASSENGER_ACCOUNT_CONTACT_EVENT_ATTRIBUTE, newContactEventPhoneNumber);


						}

					}

				}).start();
			}

		});
	}

	private void deleteFinalConfirmEvent(){

	}

	private void deleteTempConfirmEvent(){

	}


	private ArrayList<Event> activeEventList;
	private ArrayList<String> activeEventTypeList;


	private String[] contactEventPhoneNumberArray;
	private String[] tempConfirmEventPhoneNumberArray;
	private String[] finalConfirmEventPhoneNumberArray;

	private Thread loadMyActiveEventThread;


	private void loadMyActiveEvent(){
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

								contactEventPhoneNumberArray = myActiveEventAttributeOrderedList.get(0).getValue().split("\n");
								tempConfirmEventPhoneNumberArray = myActiveEventAttributeOrderedList.get(1).getValue().split("\n");
								finalConfirmEventPhoneNumberArray = myActiveEventAttributeOrderedList.get(2).getValue().split("\n");

								System.out.println("tempConfirmEventPhoneNumberArray length " + tempConfirmEventPhoneNumberArray.length);
								for (int i = 0; i < tempConfirmEventPhoneNumberArray.length; i++){
									System.out.println(tempConfirmEventPhoneNumberArray[i]);
								}

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

								contactEventPhoneNumberArray = myActiveEventAttributeOrderedList.get(0).getValue().split("\n");
								tempConfirmEventPhoneNumberArray = myActiveEventAttributeOrderedList.get(1).getValue().split("\n");
								finalConfirmEventPhoneNumberArray = myActiveEventAttributeOrderedList.get(2).getValue().split("\n");

								/*for (int i = 0; i < contactEventPhoneNumberArray.length; i++){
									System.out.println(contactEventPhoneNumberArray[i]);
								}*/

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
				(ConnectivityManager)ActiveEventActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo wifi_network = 
				connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo mobile_network = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);


		return wifi_network.isConnectedOrConnecting() || mobile_network.isConnectedOrConnecting();

		/*return mobile_network.isConnectedOrConnecting();*/
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();



	}




}
