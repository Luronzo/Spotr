package com.csun.spotr;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.csun.spotr.core.User;
import com.csun.spotr.gui.FriendListMainItemAdapter;
import com.csun.spotr.helper.JsonHelper;

public class FriendListActionActivity extends Activity {
	private static final String TAG = "FriendListActionActivity";
	private static final String SEARCH_FRIENDS_URL = "http://107.22.209.62/android/search_friends.php";
	private ListView listViewUser;
	private FriendListMainItemAdapter userItemAdapter;
	private EditText editTextSearch;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friend_list_action);
		Button buttonSearch = (Button) findViewById(R.id.friend_list_action_xml_button_search);
		editTextSearch = (EditText) findViewById(R.id.friend_list_action_xml_edittext_search);
		// TODO: should we allow user to search on an empty string? which
		// returns the whole list of users in
		// our database
		buttonSearch.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				GetUserTask task = new GetUserTask();
				task.execute(editTextSearch.getText().toString());
			}
		});
	}

	private class GetUserTask extends AsyncTask<String, Integer, List<User>> {
		private List<NameValuePair> userData = new ArrayList<NameValuePair>();
		private ProgressDialog progressDialog = null;

		@Override
		protected void onPreExecute() {
			// display waiting dialog
			progressDialog = new ProgressDialog(FriendListActionActivity.this);
			progressDialog.setMessage("Loading...");
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(true);
			progressDialog.show();
		}

		@Override
		protected List<User> doInBackground(String... text) {
			userData.add(new BasicNameValuePair("text", text[0].toString()));
			List<User> userList = new ArrayList<User>();
			JSONArray array = JsonHelper.getJsonArrayFromUrlWithData(SEARCH_FRIENDS_URL, userData);
			try {
				for (int i = 0; i < array.length(); ++i) {
					userList.add(new User.Builder(array.getJSONObject(i).getInt("id"), array.getJSONObject(i).getString("username"), array.getJSONObject(i).getString("password")).build());
				}
			}
			catch (JSONException e) {
				Log.e(TAG + "GetUserTask.doInBackGround(Void ...voids) : ", "JSON error parsing data" + e.toString());
			}
			return userList;
		}

		@Override
		protected void onPostExecute(List<User> userList) {
			progressDialog.dismiss();
			listViewUser = (ListView) findViewById(R.id.friend_list_action_xml_listview_search_friends);
			userItemAdapter = new FriendListMainItemAdapter(FriendListActionActivity.this, userList, R.drawable.funshine_bear);
			listViewUser.setAdapter(userItemAdapter);
			listViewUser.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					startDialog();
				}
			});
		}
	}

	private void startDialog() {
		AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
		myAlertDialog.setTitle("Friend Dialog");
		myAlertDialog.setMessage("Pick an option");
		myAlertDialog.setPositiveButton("Send a request", new DialogInterface.OnClickListener() {
			// do something when the button is clicked
			public void onClick(DialogInterface arg0, int arg1) {
			}
		});

		myAlertDialog.setNegativeButton("View profile", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
			}
		});
		myAlertDialog.show();
	}

	private void sendRequest(User user) {

	}

	private void viewProfile(User user) {

	}
}