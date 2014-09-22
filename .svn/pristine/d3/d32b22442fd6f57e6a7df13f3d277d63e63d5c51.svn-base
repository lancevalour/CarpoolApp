package yicheng.carpoolapp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesResult;

public class ReadDriverEventThread extends Thread{

	private ArrayList<Attribute> orderedList;
	private AmazonClientManager clientManager;
	private String eventPhoneNumber;
	public ReadDriverEventThread(AmazonClientManager clientManager, String phoneNumber){
		this.clientManager = clientManager;
		this.eventPhoneNumber = phoneNumber;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		Future<GetAttributesResult> future = SimpleDB.getInstance(clientManager).getAttributesAsync(new GetAttributesRequest(SimpleDB.DRIVER_EVENT_TABLE, eventPhoneNumber));

		try {
			GetAttributesResult result = future.get();
			List<Attribute> list = result.getAttributes();
		    orderedList = new ArrayList<Attribute>(list);

			for (Attribute attribute: list){

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
				else if (attribute.getName().equals("finalConfirmPassengerAttribute")){
					orderedList.set(6, attribute);
				}
				else if (attribute.getName().equals("tempConfirmPassengerAttribute")){
					orderedList.set(7, attribute);
				}
			}

			System.out.println(eventPhoneNumber);


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
	
	public ArrayList<Attribute> getOrderedList(){
		return this.orderedList;
	}
	
	public String getEventPhoneNumber(){
		return this.eventPhoneNumber;
	}

}
