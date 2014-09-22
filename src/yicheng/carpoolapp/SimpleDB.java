package yicheng.carpoolapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.amazonaws.services.simpledb.AmazonSimpleDBAsyncClient;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.DeleteAttributesRequest;
import com.amazonaws.services.simpledb.model.DeleteDomainRequest;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesResult;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.ListDomainsRequest;
import com.amazonaws.services.simpledb.model.ListDomainsResult;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.SelectRequest;



public class SimpleDB {

	//Define constant domain name
	public static final String DRIVER_ACCOUNT_TABLE = "driverAccountTable";
	public static final String PASSENGER_ACCOUNT_TABLE = "passengerAccountTable";
	public static final String DRIVER_EVENT_TABLE = "driverEventTable";
	public static final String PASSENGER_EVENT_TABLE = "passengerEventTable";
	
	//Define constant attribute name
	//Driver account table attribute name
	public static final String DRIVER_ACCOUNT_PASSWORD_ATTRIBUTE = "passwordAttribute";
	public static final String DRIVER_ACCOUNT_USERNAME_ATTRIBUTE = "usernameAttribute";
	public static final String DRIVER_ACCOUNT_PHOTOURL_ATTRIBUTE = "photoUrlAttribute";
	public static final String DRIVER_ACCOUNT_CONTACT_EVENT_ATTRIBUTE = "contactEventAttribute";
	public static final String DRIVER_ACCOUNT_TEMP_CONFIRM_EVENT_ATTRIBUTE = "tempConfirmEventAttribute";
	public static final String DRIVER_ACCOUNT_FINAL_CONFIRM_EVENT_ATTRIBUTE = "finalConfirmEventAttribute";

	//Passenger account table attribute name
	public static final String PASSENGER_ACCOUNT_PASSWORD_ATTRIBUTE = "passwordAttribute";
	public static final String PASSENGER_ACCOUNT_USERNAME_ATTRIBUTE = "usernameAttribute";
	public static final String PASSENGER_ACCOUNT_PHOTOURL_ATTRIBUTE = "photoUrlAttribute";
	public static final String PASSENGER_ACCOUNT_CONTACT_EVENT_ATTRIBUTE = "contactEventAttribute";
	public static final String PASSENGER_ACCOUNT_TEMP_CONFIRM_EVENT_ATTRIBUTE = "tempConfirmEventAttribute";
	public static final String PASSENGER_ACCOUNT_FINAL_CONFIRM_EVENT_ATTRIBUTE = "finalConfirmEventAttribute";

	//Driver event table attribute name
	public static final String DRIVER_EVENT_EVENT_TITLE_ATTRIBUTE = "eventTitleAttribute";
	public static final String DRIVER_EVENT_EVENT_CONTENT_ATTRIBUTE = "eventContentAttribute";
	public static final String DRIVER_EVENT_EVENT_DATE_ATTRIBUTE = "eventDateAttribute";
	public static final String DRIVER_EVENT_POST_USERNAME_ATTRIBUTE = "postUsernameAttribute";
	public static final String DRIVER_EVENT_POST_PHOTOURL_ATTRIBUTE = "postPhotoUrlAttribute";
	public static final String DRIVER_EVENT_AVAILABLE_SEAT_NUMBER_ATTRIBUTE = "availableSeatNumberAttribute";
	public static final String DRIVER_EVENT_TEMP_CONFIRM_PASSENGER_ATTRIBUTE = "tempConfirmPassengerAttribute";
	public static final String DRIVER_EVENT_INTERESTED_PASSENGER_ATTRIBUTE = "interestedPassengerAttribute";
	public static final String DRIVER_EVENT_FINAL_CONFIRM_PASSENGER_ATTRIBUTE = "finalConfirmPassengerAttribute";

	//Driver event table attribute name
	public static final String PASSENGER_EVENT_EVENT_TITLE_ATTRIBUTE = "eventTitleAttribute";
	public static final String PASSENGER_EVENT_EVENT_CONTENT_ATTRIBUTE = "eventContentAttribute";
	public static final String PASSENGER_EVENT_EVENT_DATE_ATTRIBUTE = "eventDateAttribute";
	public static final String PASSENGER_EVENT_POST_USERNAME_ATTRIBUTE = "postUsernameAttribute";
	public static final String PASSENGER_EVENT_POST_PHOTOURL_ATTRIBUTE = "postPhotoUrlAttribute";
	public static final String PASSENGER_EVENT_AVAILABLE_SEAT_NUMBER_ATTRIBUTE = "availableSeatNumberAttribute";
	public static final String PASSENGER_EVENT_TEMP_CONFIRM_EXTRA_PASSENGER_ATTRIBUTE = "tempConfirmExtraPassengerAttribute";
	public static final String PASSENGER_EVENT_TEMP_CONFIRM_DRIVER_ATTRIBUTE = "tempConfirmDriverAttribute";
	public static final String PASSENGER_EVENT_INTERESTED_DRIVER_ATTRIBUTE = "interestedDriverAttribute";
	public static final String PASSENGER_EVENT_INTERESTED_EXTRA_PASSENGER_ATTRIBUTE = "interestedExtraPassengerAttribute";
	public static final String PASSENGER_EVENT_FINAL_CONFIRM_DRIVER_ATTRIBUTE = "finalConfirmDriverAttribute";
	public static final String PASSENGER_EVENT_FINAL_CONFIRM_EXTRA_PASSENGER_ATTRIBUTE = "finalConfirmExtraPassengerAttribute";
			
			








	private static String nextToken = null;
	private static int prevNumDomains = 0;
/*	public static final String DOMAIN_NAME = "_domain_name";*/

	public static AmazonSimpleDBAsyncClient getInstance(AmazonClientManager clientManager) {
		return clientManager.sdb();
	}

	public static List<String> getDomainNames(AmazonClientManager clientManager) {
		return getInstance(clientManager).listDomains().getDomainNames();
	}

	public static List<String> getDomainNames(AmazonClientManager clientManager, int numDomains) {
		prevNumDomains = numDomains;
		return getDomainNames(clientManager, numDomains, null);
	}

	private static List<String> getDomainNames(AmazonClientManager clientManager, int numDomains, String nextToken) {
		ListDomainsRequest req = new ListDomainsRequest();
		req.setMaxNumberOfDomains(numDomains);
		if(nextToken != null)
			req.setNextToken(nextToken);
		ListDomainsResult result = getInstance(clientManager).listDomains(req);
		List<String> domains = result.getDomainNames();
		SimpleDB.nextToken = result.getNextToken(); 
		return domains;
	}

	public static List<String> getMoreDomainNames(AmazonClientManager clientManager) {
		if(nextToken == null) {
			return new ArrayList<String>();
		} else {
			return getDomainNames(clientManager,prevNumDomains, nextToken);
		}

	}

	public static void createDomain( AmazonClientManager clientManager, String domainName ) {
		getInstance(clientManager).createDomain( new CreateDomainRequest( domainName ) );
	}

	public static void deleteDomain(AmazonClientManager clientManager, String domainName ) {
		getInstance(clientManager).deleteDomain( new DeleteDomainRequest( domainName ) );
	}

	public static void createItem(AmazonClientManager clientManager, String domainName, String itemName ) {
		List<ReplaceableAttribute> attributes = new ArrayList<ReplaceableAttribute>(1);
		attributes.add( new ReplaceableAttribute().withName( "Name").withValue( "Value") );
		getInstance(clientManager).putAttributes( new PutAttributesRequest( domainName, itemName, attributes ) );
	}

	public static void createAttributeForItem(AmazonClientManager clientManager, String domainName, String itemName, String attributeName, String attributeValue ) {
		List<ReplaceableAttribute> attributes = new ArrayList<ReplaceableAttribute>(1);
		attributes.add( new ReplaceableAttribute().withName( attributeName ).withValue( attributeValue ).withReplace( true ) );
		getInstance(clientManager).putAttributes( new PutAttributesRequest( domainName, itemName, attributes ) );
	}

	public static String[] getItemNamesForDomain( AmazonClientManager clientManager,String domainName ) {
		SelectRequest selectRequest = new SelectRequest( "select itemName() from `" + domainName + "`" ).withConsistentRead( true );
		List<Item> items = getInstance(clientManager).select( selectRequest ).getItems();	

		String[] itemNames = new String[ items.size() ];
		for ( int i = 0; i < items.size(); i++ ) {
			itemNames[ i ] = ((Item)items.get( i )).getName();
		}

		return itemNames;
	}

	public static HashMap<String,String> getAttributesForItem(AmazonClientManager clientManager, String domainName, String itemName ) {
		GetAttributesRequest getRequest = new GetAttributesRequest( domainName, itemName ).withConsistentRead( true );
		GetAttributesResult getResult = getInstance(clientManager).getAttributes( getRequest );	

		HashMap<String,String> attributes = new HashMap<String,String>(30);
		for ( Object attribute : getResult.getAttributes() ) {
			String name = ((Attribute)attribute).getName();
			String value = ((Attribute)attribute).getValue();

			attributes.put(  name, value );
		}

		return attributes;
	}

	public static String getSingleAttributesForItem(AmazonClientManager clientManager, String domainName, String itemName, String key ){
		HashMap<String, String> oldAttribute = getAttributesForItem(clientManager, domainName, itemName);

		System.out.println(oldAttribute.toString());
		return oldAttribute.get(key);
	}

	public static void updateAttributesForItem(AmazonClientManager clientManager, String domainName, String itemName, HashMap<String,String> attributes ) {
		List<ReplaceableAttribute> replaceableAttributes = new ArrayList<ReplaceableAttribute>( attributes.size() ); 

		for ( String attributeName : attributes.keySet() ) {
			replaceableAttributes.add( new ReplaceableAttribute().withName( attributeName ).withValue( attributes.get( attributeName ) ).withReplace( true ) );
		}

		getInstance(clientManager).putAttributesAsync( new PutAttributesRequest( domainName, itemName, replaceableAttributes ) );
	}

	public static void updateSingleAttribute(AmazonClientManager clientManager, String domainName, String itemName, String attributeName, String newValue){
		HashMap<String, String> oldAttribute = getAttributesForItem(clientManager,domainName, itemName);
		oldAttribute.put(attributeName, newValue);
		updateAttributesForItem(clientManager,domainName, itemName, oldAttribute);

	}
	public static void deleteItem(AmazonClientManager clientManager, String domainName, String itemName ) {
		getInstance(clientManager).deleteAttributes( new DeleteAttributesRequest( domainName, itemName ) );
	}

	public static void deleteAllItem(AmazonClientManager clientManager, String domainName){
		for ( String name : SimpleDB.getItemNamesForDomain (clientManager,domainName)) {
			deleteItem(clientManager, domainName, name ) ;
		}
	}

	public static void deleteItemAttribute(AmazonClientManager clientManager, String domainName, String itemName, String attributeName ) {
		getInstance(clientManager).deleteAttributes(  new DeleteAttributesRequest( domainName, itemName ).withAttributes( new Attribute[] { new Attribute().withName( attributeName ) } ) );
	}

}



