package com.csun.spotr;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TabHost;

public class PlaceMainActivity extends TabActivity {
	private static final String TAG = "(PlaceMainActivity)";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.place_main);
		Resources res = getResources(); 
		TabHost tabHost = getTabHost(); 
		TabHost.TabSpec spec; 
		Intent intent; 
		// get place_id extras from PlaceActivity/LocalPlaceActivity
		Bundle extras = getIntent().getExtras();
		// create an Intent to launch an Activity for the tab (to be reused)
		intent = new Intent().setClass(this, PlaceActionActivity.class);
		// pass this extra to PlaceActionActivity
		intent.putExtras(extras);

		// initialize a TabSpec for each tab and add it to the TabHost
		spec = tabHost
				.newTabSpec("missions")
				.setIndicator("Missions")//, res.getDrawable(R.drawable.place_activity_tab))
				.setContent(intent);
		tabHost.addTab(spec);

		// do the same for the other tabs
		intent = new Intent().setClass(this, PlaceActivityActivity.class);
		// pass this Extra to PlaceActivityActivity
		intent.putExtras(extras);
		
		spec = tabHost
				.newTabSpec("news")
				.setIndicator("News")//, res.getDrawable(R.drawable.place_activity_tab))
				.setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, PlaceInfoActivity.class);
		// pass this extra to PlaceInfoActivity
		intent.putExtras(extras);
		
		spec = tabHost
				.newTabSpec("about")
				.setIndicator("About")//, res.getDrawable(R.drawable.place_activity_tab))
				.setContent(intent);
		tabHost.addTab(spec);
		// set current tab to action
		tabHost.setCurrentTab(0);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.all_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = new Intent("com.csun.spotr.MainMenuActivity");
		startActivity(intent);
		finish();
		return true;
	}
}
