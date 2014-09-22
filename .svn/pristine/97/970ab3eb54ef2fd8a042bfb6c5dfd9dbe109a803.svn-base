package yicheng.carpoolapp;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MyEventArrayAdapter extends ArrayAdapter<Event>{

	private final Context context;
	private final ArrayList<Event> eventList;
	private final ArrayList<String> eventTypeList;

	public MyEventArrayAdapter(Context context, ArrayList<Event> eventList, ArrayList<String> eventTypeList){
		super(context, R.layout.driver_event_list_item_layout, eventList);

		this.context = context;
		this.eventList = eventList;
		this.eventTypeList = eventTypeList;

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = null;
		if (this.eventTypeList.get(position).equals("driver")){

			rowView = inflater.inflate(R.layout.driver_my_event_layout, parent, false);

			// 3. Get the two text view from the rowView

			TextView titleView = (TextView) rowView.findViewById(R.id.driver_my_event_title_textView);
			TextView contentView = (TextView) rowView.findViewById(R.id.driver_my_event_content_textView);
			TextView dateView = (TextView) rowView.findViewById(R.id.driver_my_event_date_textView);
			TextView postUsernameView = (TextView) rowView.findViewById(R.id.driver_my_event_postUsername_textView);
			TextView availableSeatNumberView = (TextView) rowView.findViewById(R.id.driver_my_event_availableSeat_textView);
			TextView passengerNameView = (TextView) rowView.findViewById(R.id.driver_my_event_final_confirm_passenger_textView);
			ImageView newConfirmRequestImageView = (ImageView) rowView.findViewById(R.id.driver_my_event_new_confirm_request_imageView);
			ImageView userPhotoImageView = (ImageView) rowView.findViewById(R.id.driver_my_event_imageView);
			userPhotoImageView.setImageResource(R.drawable.ic_action_driver);
			
			if (eventList.get(position).getTempConfirmPassenger().trim().length() < 1){
				newConfirmRequestImageView.setVisibility(View.INVISIBLE);
			}
			
			// 4. Set the text for textView 
			titleView.setText(eventList.get(position).getEventTitle());
			contentView.setText(eventList.get(position).getEventContent());
			dateView.setText(eventList.get(position).getEventDate());
			postUsernameView.setText(eventList.get(position).getPostUsername());
			availableSeatNumberView.setText(eventList.get(position).getAvailableSeatNumber());
			passengerNameView.setText(eventList.get(position).getFinalConfirmPasssenger());
		}	
		else{
			rowView = inflater.inflate(R.layout.passenger_my_event_layout, parent, false);

			// 3. Get the two text view from the rowView
			TextView titleView = (TextView) rowView.findViewById(R.id.passenger_my_event_title_textView);
			TextView contentView = (TextView) rowView.findViewById(R.id.passenger_my_event_content_textView);
			TextView dateView = (TextView) rowView.findViewById(R.id.passenger_my_event_date_textView);
			TextView postUsernameView = (TextView) rowView.findViewById(R.id.passenger_my_event_postUsername_textView);
			TextView availableSeatNumberView = (TextView) rowView.findViewById(R.id.passenger_my_event_availableSeat_textView);
			TextView driverNameView = (TextView) rowView.findViewById(R.id.passenger_my_event_final_confirm_driver_textView);
			TextView extraPassengerNameView = (TextView) rowView.findViewById(R.id.passenger_my_event_final_confirm_extra_passenger_textView);
			ImageView userPhotoImageView = (ImageView) rowView.findViewById(R.id.passenger_my_event_imageView);
			userPhotoImageView.setImageResource(R.drawable.ic_action_passenger);
			
			ImageView newConfirmRequestImageView = (ImageView) rowView.findViewById(R.id.passenger_my_event_new_confirm_request_imageView);
			
			if (eventList.get(position).getTempConfirmDriver().trim().length() < 1 && 
					eventList.get(position).getTempConfirmExtraPassenger().trim().length() < 1){
				newConfirmRequestImageView.setVisibility(View.INVISIBLE);
			}
			
			
			// 4. Set the text for textView 
			titleView.setText(eventList.get(position).getEventTitle());
			contentView.setText(eventList.get(position).getEventContent());
			dateView.setText(eventList.get(position).getEventDate());
			postUsernameView.setText(eventList.get(position).getPostUsername());
			driverNameView.setText(eventList.get(position).getFinalConfirmDriver());
			extraPassengerNameView.setText(eventList.get(position).getFinalConfirmExtraPassenger());
			
			if (eventList.get(position).getAvailableSeatNumber().equals("Touch to add available seat")){
				availableSeatNumberView.setVisibility(View.INVISIBLE);
			}
			else{
				availableSeatNumberView.setText(eventList.get(position).getAvailableSeatNumber());
			}
		}
		// 5. retrn rowView
		return rowView;

	}

}
