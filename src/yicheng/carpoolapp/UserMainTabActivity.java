package yicheng.carpoolapp;



import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

public class UserMainTabActivity extends TabActivity{
	private TabHost tabHost;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
		setContentView(R.layout.user_main_tab_activity_layout);

		setUpTabLayout();

	}

	private void setUpTabLayout(){
		tabHost = getTabHost();

		Intent eventPoolTabIntent = new Intent(this, EventPoolActivity.class);
		TabSpec eventPoolTabSpec = tabHost.newTabSpec("EventPool")
				.setIndicator("Event Pool")
				.setContent(eventPoolTabIntent);
		tabHost.addTab(eventPoolTabSpec);

		Intent activeEventTabIntent = new Intent(this, ActiveEventActivity.class);
		TabSpec activeEventTabSpec = tabHost.newTabSpec("ActiveEvent")
				.setIndicator("Active Event")
				.setContent(activeEventTabIntent);
		tabHost.addTab(activeEventTabSpec);
		
		Intent myEventTabIntent = new Intent(this, MyEventActivity.class);
		TabSpec myEventTabSpec = tabHost.newTabSpec("MyEvent")
				.setIndicator("My Event")
				.setContent(myEventTabIntent);
		
		tabHost.addTab(myEventTabSpec);

		tabHost.setCurrentTab(2);

		tabHost.getTabWidget().getChildAt(0).setBackgroundColor(Color.rgb(255, 255, 255));
		tabHost.getTabWidget().getChildAt(1).setBackgroundColor(Color.rgb(255, 255, 255));
		tabHost.getTabWidget().getChildAt(2).setBackgroundColor(Color.rgb(255, 255, 255));
		
		
	    for(int i=0; i< tabHost.getTabWidget().getChildCount();i++) 
	    {
	        TextView tv = (TextView) tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
	        tv.setTextColor(Color.rgb(225, 97, 56));
	    } 
		
		
		tabHost.getTabWidget().getChildAt(0).setAlpha((float) 0.4);
		tabHost.getTabWidget().getChildAt(1).setAlpha((float) 0.4);
		tabHost.getTabWidget().getChildAt(2).setAlpha((float) 1);
		




		tabHost.setOnTabChangedListener(new OnTabChangeListener(){

			@Override
			public void onTabChanged(String tabId) {
				// TODO Auto-generated method stub
				if (tabId.equals("EventPool")){
					tabHost.getTabWidget().getChildAt(0).setAlpha((float) 1);
					tabHost.getTabWidget().getChildAt(1).setAlpha((float) 0.4);
					tabHost.getTabWidget().getChildAt(2).setAlpha((float) 0.4);
				}	
				else if (tabId.equals("ActiveEvent")){
					tabHost.getTabWidget().getChildAt(0).setAlpha((float) 0.4);
					tabHost.getTabWidget().getChildAt(1).setAlpha((float) 1);		
					tabHost.getTabWidget().getChildAt(2).setAlpha((float) 0.4);
				}
				else{
					tabHost.getTabWidget().getChildAt(0).setAlpha((float) 0.4);
					tabHost.getTabWidget().getChildAt(1).setAlpha((float) 0.4);
					tabHost.getTabWidget().getChildAt(2).setAlpha((float) 1);
				}
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

}
