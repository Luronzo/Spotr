package com.csun.spotr;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.csun.spotr.adapter.PlaceActionItemAdapter;
import com.csun.spotr.core.Challenge;
import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.skeleton.IActivityProgressUpdate;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.JsonHelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

/**
 * Description:
 * 		The Missions tab content in Spots.
 */
public class PlaceActionActivity 
	extends Activity 
		implements IActivityProgressUpdate<Challenge> {
	
	private static final 	String 					TAG = "(PlaceActionActivity)";
	private static final 	String 					GET_CHALLENGES_URL = "http://107.22.209.62/android/get_challenges_from_place.php";
	private static final	String 					DO_CHECK_IN_URL = "http://107.22.209.62/android/do_check_in.php";
	
	public 					int 					currentPlaceId;
	public 					int 					currentChosenItem;
	public 					ListView 				list = null;
	private					PlaceActionItemAdapter	adapter = null;
	private 				List<Challenge> 		challengeList = new ArrayList<Challenge>();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// set layout
		super.onCreate(savedInstanceState);
		setContentView(R.layout.place_action);
	
		// get place id
		Bundle extrasBundle = getIntent().getExtras();
		currentPlaceId = extrasBundle.getInt("place_id");

		// initialize list view of challenges
		list = (ListView) findViewById(R.id.place_action_xml_listview_actions);
		adapter = new PlaceActionItemAdapter(this, challengeList);
		
		// add top padding to first item and add bottom padding to last item
		TextView padding = new TextView(getApplicationContext());
		padding.setHeight(0);
		
		Button buttonTreasure = (Button) findViewById(R.id.place_action_xml_button_treasure);
		buttonTreasure.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), TreasureActivity.class);
				startActivity(intent);
			}
		});
		
		
		list.addHeaderView(padding, null, false);
		list.addFooterView(padding, null, false);
		list.setAdapter(adapter);
				
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Challenge c = (Challenge) list.getAdapter().getItem(position);//challengeList.get(position);
				// set current item chosen so that later we can make some side effects
				currentChosenItem = position;
				
				if (c.getType() == Challenge.Type.CHECK_IN) {
					CheckInTask task = new CheckInTask(PlaceActionActivity.this);
					task.execute(
						Integer.toString(CurrentUser.getCurrentUser().getId()),
						Integer.toString(currentPlaceId),
						Integer.toString(c.getId())
					);
				}
				else if (c.getType() == Challenge.Type.WRITE_ON_WALL) {
					Intent intent = new Intent("com.csun.spotr.WriteOnWallActivity");
					Bundle extras = new Bundle();
					extras.putString("users_id", Integer.toString(CurrentUser.getCurrentUser().getId()));
					extras.putString("spots_id", Integer.toString(currentPlaceId));
					extras.putString("challenges_id", Integer.toString(c.getId()));
					intent.putExtras(extras);
					startActivity(intent);
				}
				else if (c.getType() == Challenge.Type.SNAP_PICTURE) {
					Intent intent = new Intent("com.csun.spotr.SnapPictureActivity");
					Bundle extras = new Bundle();
					extras.putString("users_id", Integer.toString(CurrentUser.getCurrentUser().getId()));
					extras.putString("spots_id", Integer.toString(currentPlaceId));
					extras.putString("challenges_id", Integer.toString(c.getId()));
					intent.putExtras(extras);
					startActivity(intent);
				}
				else if (c.getType() == Challenge.Type.QUESTION_ANSWER) {
					Intent intent = new Intent("com.csun.spotr.QuestionAnswerActivity");
					Bundle extras = new Bundle();
					extras.putString("users_id", Integer.toString(CurrentUser.getCurrentUser().getId()));
					extras.putString("spots_id", Integer.toString(currentPlaceId));
					extras.putString("challenges_id", Integer.toString(c.getId()));
					extras.putString("question_description", c.getDescription());
					intent.putExtras(extras);
					startActivity(intent);
				}
				else { // c.getType == Challenge.Type.OTHER 
					
				}
			}
		});
		
		// run GetChallengeTask
		new GetChallengesTask(PlaceActionActivity.this).execute();
	}
	
	private static class GetChallengesTask 
		extends AsyncTask<String, Challenge, Boolean> 
			implements IAsyncTask<PlaceActionActivity> {
		
		private List<NameValuePair> challengeData = new ArrayList<NameValuePair>();
		private WeakReference<PlaceActionActivity> ref;
		
		public GetChallengesTask(PlaceActionActivity a) {
			attach(a);
		}
		
		@Override
		protected void onPreExecute() {
			
		}
		
		@Override
	    protected void onProgressUpdate(Challenge... c) {
			ref.get().updateAsyncTaskProgress(c[0]);
	    }

		@Override
		protected Boolean doInBackground(String... text) {
			challengeData.add(new BasicNameValuePair("place_id", Integer.toString(ref.get().currentPlaceId)));
			JSONArray array = JsonHelper.getJsonArrayFromUrlWithData(GET_CHALLENGES_URL, challengeData);
			if (array != null) { 
				try {
					for (int i = 0; i < array.length(); ++i) {
						if (!array.getJSONObject(i).getString("challenges_tbl_type").equals("OTHER")) {
							publishProgress(								
							new Challenge.Builder(
									// required parameters
									array.getJSONObject(i).getInt("challenges_tbl_id"),
									Challenge.returnType(array.getJSONObject(i).getString("challenges_tbl_type")),
									array.getJSONObject(i).getInt("challenges_tbl_points")) 
										// optional parameters
										.name(array.getJSONObject(i).getString("challenges_tbl_name"))
										.description(array.getJSONObject(i).getString("challenges_tbl_description"))
											.build());
						}
					}
				}
				catch (JSONException e) {
					Log.e(TAG + "GetChallengesTask.doInBackGround(Void ...voids) : ", "JSON error parsing data" + e.toString());
				}
				return true;
			}
			else {
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			detach();
		}

		public void attach(PlaceActionActivity a) {
			ref = new WeakReference<PlaceActionActivity>(a);
		}

		public void detach() {
			ref.clear();
		}
	}
	
	private static class CheckInTask 
		extends AsyncTask<String, Integer, String> 
			implements IAsyncTask<PlaceActionActivity> {
		
		private List<NameValuePair> checkInData = new ArrayList<NameValuePair>();
		private WeakReference<PlaceActionActivity> ref;
		
		public CheckInTask(PlaceActionActivity a) {
			attach(a);
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected String doInBackground(String... ids) {
			/*
			 * 1. Retrieve data from [activity] table where $users_id and $places_id
			 * 2. Check the result of this query:
			 * 		a. If the result is null, then user hasn't visited this place yet which also implies that he has not done any challenges.
			 * 		   Thus we can update the current user:
			 * 		   i.  Update [activity] table with $users_id, $places_id, $challenges_id   
			 * 		   ii. Update [users] table with 
			 * 			   + $challenges_done = $challenges_done + 1
			 * 			   + $points += challenges.points
			 * 			   + $places_visited = $places_visited + 1
			 * 		b. If the result is not null, update [activity] table with $users_id, $places_id, $challenges_id with CURRENT_TIMESTAMP, but
			 * 		   don't run the statement:
			 * 		       + $places_visited = $places_visited + 1
			 * 3. All these complexity is done at server side, i.e. php script, so we only need to post to the server three parameters:
			 * 	    a. users_id
			 * 		b. places_id
			 * 		c. challenges_id
			 * 4. The return of this query is the number points is added the points added to the user account.
			 */
			checkInData.add(new BasicNameValuePair("users_id", ids[0]));
			checkInData.add(new BasicNameValuePair("spots_id", ids[1]));
			checkInData.add(new BasicNameValuePair("challenges_id", ids[2]));
			JSONObject json = JsonHelper.getJsonObjectFromUrlWithData(DO_CHECK_IN_URL, checkInData);
			String result = "";
			
			try {
				result = json.getString("result");
			} 
			catch (JSONException e) {
				Log.e(TAG + "CheckInTask.doInBackGround(Void ...voids) : ", "JSON error parsing data" + e.toString());
			}
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			if (result.equals("success")) {
				ref.get().list.getChildAt(ref.get().currentChosenItem).setBackgroundColor(Color.GRAY);
			}
			else {
				ref.get().showDialog(0);
			}	
			
			detach();
		}

		public void attach(PlaceActionActivity a) {
			ref = new WeakReference<PlaceActionActivity>(a);
		}

		public void detach() {
			ref.clear();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.all_menu, menu);
		return true;
	}
	
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
			case R.id.options_menu_xml_item_setting_icon:
				intent = new Intent("com.csun.spotr.SettingsActivity");
				startActivity(intent);
				finish();
				break;
			case R.id.options_menu_xml_item_logout_icon:
				SharedPreferences.Editor editor = getSharedPreferences("Spotr", MODE_PRIVATE).edit();
				editor.clear();
				editor.commit();
				intent = new Intent("com.csun.spotr.LoginActivity");
				startActivity(intent);
				finish();
				break;
			case R.id.options_menu_xml_item_mainmenu_icon:
				intent = new Intent("com.csun.spotr.MainMenuActivity");
				startActivity(intent);
				finish();
				break;
		}
		return true;
	}
    
    @Override
    public void onPause() {
		Log.v(TAG, "I'm paused!");
		super.onPause();
	}
	
	@Override
    public void onDestroy() {
		Log.v(TAG, "I'm destroyed!");
        super.onDestroy();
	}

	public void updateAsyncTaskProgress(Challenge c) {
		challengeList.add(c);
		adapter.notifyDataSetChanged();
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case 0:
			return new 
				AlertDialog.Builder(this)
					.setIcon(R.drawable.error_circle)
					.setTitle("Warning!")
					.setMessage("You checked in recently. You can only check in once every 24 hours. :(!")
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							
						}
					}).create();
		
		case 1: 
			return new 
					AlertDialog.Builder(this)
						.setIcon(R.drawable.error_circle)
						.setTitle("Error Message")
						.setMessage("<undefined>")
						.setPositiveButton("OK", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								
							}
						}).create();
		}
		return null;
	}
}
