package yicheng.carpoolapp;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ActiveEventArrayAdapter extends ArrayAdapter<Event>{

	private final Context context;
	private final ArrayList<Event> eventList;
	private final ArrayList<String> eventTypeList;


	public ActiveEventArrayAdapter(Context context, ArrayList<Event> eventList, ArrayList<String> eventTypeList){
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
		if (this.eventTypeList.get(position).equals("finalConfirmEvent")){

			rowView = inflater.inflate(R.layout.final_confirm_event_list_item_layout, parent, false);

			// 3. Get the two text view from the rowView

			TextView titleView = (TextView) rowView.findViewById(R.id.final_confirm_event_title_textView);
			TextView contentView = (TextView) rowView.findViewById(R.id.final_confirm_event_content_textView);
			TextView dateView = (TextView) rowView.findViewById(R.id.final_confirm_event_date_textView);
			TextView postUsernameView = (TextView) rowView.findViewById(R.id.final_confirm_event_postUsername_textView);
			TextView availableSeatNumberView = (TextView) rowView.findViewById(R.id.final_confirm_event_availableSeat_textView);
			ImageView userPhotoImageView = (ImageView) rowView.findViewById(R.id.final_confirm_event_imageView);
			
			if (!eventList.get(position).getFinalConfirmDriver().equals("")){
				userPhotoImageView.setImageResource(R.drawable.ic_action_passenger);
			}
			else{
				userPhotoImageView.setImageResource(R.drawable.ic_action_driver);	
			}






			// 4. Set the text for textView 
			titleView.setText(eventList.get(position).getEventTitle());
			contentView.setText(eventList.get(position).getEventContent());
			dateView.setText(eventList.get(position).getEventDate());
			postUsernameView.setText(eventList.get(position).getPostUsername());
			if (eventList.get(position).getAvailableSeatNumber().equals("Touch to add available seat")){
				availableSeatNumberView.setVisibility(View.INVISIBLE);
			}
			else{
				availableSeatNumberView.setText(eventList.get(position).getAvailableSeatNumber());
			}

		}
		else if (this.eventTypeList.get(position).equals("tempConfrimEvent")){
			rowView = inflater.inflate(R.layout.temp_confirm_event_list_item_layout, parent, false);

			// 3. Get the two text view from the rowView
			TextView titleView = (TextView) rowView.findViewById(R.id.temp_confirm_event_title_textView);
			TextView contentView = (TextView) rowView.findViewById(R.id.temp_confirm_event_content_textView);
			TextView dateView = (TextView) rowView.findViewById(R.id.temp_confirm_event_date_textView);
			TextView postUsernameView = (TextView) rowView.findViewById(R.id.temp_confirm_event_postUsername_textView);
			TextView availableSeatNumberView = (TextView) rowView.findViewById(R.id.temp_confirm_event_availableSeat_textView);

			ImageView userPhotoImageView = (ImageView) rowView.findViewById(R.id.temp_confirm_event_imageView);

			if (!eventList.get(position).getTempConfirmDriver().equals("")){
				userPhotoImageView.setImageResource(R.drawable.ic_action_passenger);
			}
			else{
				userPhotoImageView.setImageResource(R.drawable.ic_action_driver);	
			}


			// 4. Set the text for textView 
			titleView.setText(eventList.get(position).getEventTitle());
			contentView.setText(eventList.get(position).getEventContent());
			dateView.setText(eventList.get(position).getEventDate());
			postUsernameView.setText(eventList.get(position).getPostUsername());
			if (eventList.get(position).getAvailableSeatNumber().equals("Touch to add available seat")){
				availableSeatNumberView.setVisibility(View.INVISIBLE);
			}
			else{
				availableSeatNumberView.setText(eventList.get(position).getAvailableSeatNumber());
			}
		}
		else{
			rowView = inflater.inflate(R.layout.contact_event_list_item_layout, parent, false);

			// 3. Get the two text view from the rowView
			TextView titleView = (TextView) rowView.findViewById(R.id.contact_event_title_textView);
			TextView contentView = (TextView) rowView.findViewById(R.id.contact_event_content_textView);
			TextView dateView = (TextView) rowView.findViewById(R.id.contact_event_date_textView);
			TextView postUsernameView = (TextView) rowView.findViewById(R.id.contact_event_postUsername_textView);
			TextView availableSeatNumberView = (TextView) rowView.findViewById(R.id.contact_event_availableSeat_textView);
			ImageView userPhotoImageView = (ImageView) rowView.findViewById(R.id.contact_event_imageView);

			if (eventList.get(position).getAvailableSeatNumber().equals("Touch to add available seat")){
				userPhotoImageView.setImageResource(R.drawable.ic_action_passenger);
			}
			else{
				userPhotoImageView.setImageResource(R.drawable.ic_action_driver);	
			}


			// 4. Set the text for textView 
			titleView.setText(eventList.get(position).getEventTitle());
			contentView.setText(eventList.get(position).getEventContent());
			dateView.setText(eventList.get(position).getEventDate());
			postUsernameView.setText(eventList.get(position).getPostUsername());
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
