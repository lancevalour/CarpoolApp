package yicheng.carpoolapp;

public class Event {
	private String phoneNumber;
	private String eventTitle;
	private String eventContent;
	private String eventDate;
	private String postUsername;
	private String postPhotoUrl;
	private String availableSeatNumber;
	private String finalConfirmDriver;
	private String finalConfirmPassenger;
	private String finalConfirmExtraPassenger;
	private String tempConfirmDriver;
	private String tempConfirmPassenger;
	private String tempConfirmExtraPassenger;
	
	
	
	public Event(String phoneNumber, String eventTitle, String eventContent, String eventDate, String postUsername,
			String postPhotoUrl, String availableSeatNumber, String finalConfirmDriver, String finalConfirmPassenger,
			String finalConfirmExtraPassenger, String tempConfirmDriver, String tempConfirmPassenger,
			String tempConfirmExtraPassenger){
		this.phoneNumber = phoneNumber;
		this.eventTitle = eventTitle;
		this.eventContent = eventContent;
		this.eventDate = eventDate;
		this.postUsername = postUsername;
		this.postPhotoUrl = postPhotoUrl;
		this.availableSeatNumber = availableSeatNumber;
		this.finalConfirmDriver = finalConfirmDriver;
		this.finalConfirmPassenger = finalConfirmPassenger;
		this.finalConfirmExtraPassenger = finalConfirmExtraPassenger;
		this.tempConfirmDriver = tempConfirmDriver;
		this.tempConfirmPassenger = tempConfirmPassenger;
		this.tempConfirmExtraPassenger = tempConfirmExtraPassenger;
	}
	public Event(){
		
	}
	
	public String getPhoneNumber(){
		return this.phoneNumber;
	}
	
	public String getEventTitle(){
		return this.eventTitle;
	}
	
	public String getEventContent(){
		return this.eventContent;
	}
	
	public String getEventDate(){
		return this.eventDate;
	}
	
	public String getPostUsername(){
		return this.postUsername;
	}
	
	public String getPostPhotoUrl(){
		return this.postPhotoUrl;
	}
	
	public String getAvailableSeatNumber(){
		return this.availableSeatNumber;
	}
	
	public String getFinalConfirmDriver(){
		return this.finalConfirmDriver;
	}
	
	public String getFinalConfirmPasssenger(){
		return this.finalConfirmPassenger;
	}
	
	public String getFinalConfirmExtraPassenger(){
		return this.finalConfirmExtraPassenger;
		
	}
	
	public String getTempConfirmDriver(){
		return this.tempConfirmDriver;
	}
	
	public String getTempConfirmPassenger(){
		return this.tempConfirmPassenger;
	}
	
	public String getTempConfirmExtraPassenger(){
		return this.tempConfirmExtraPassenger;
	}
	
	
	
	

	
	
}
