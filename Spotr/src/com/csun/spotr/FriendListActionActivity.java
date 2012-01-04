package com.csun.spotr;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;

import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.core.User;
import com.csun.spotr.gui.FriendListMainItemAdapter;
import com.csun.spotr.helper.DownloadImageHelper;
import com.csun.spotr.helper.JsonHelper;

public class FriendListActionActivity extends Activity {
	private static final String TAG = "FriendListActionActivity";
	private static final String SEARCH_FRIENDS_URL = "http://107.22.209.62/android/search_friends.php";
	private static final String SEND_REQUEST_URL = "http://107.22.209.62/android/send_friend_request.php";
	private EditText editTextSearch = null;

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

	private class GetUserTask extends AsyncTask<String, Integer, Boolean> {
		private List<NameValuePair> userData = new ArrayList<NameValuePair>();
		private List<User> userList = new ArrayList<User>();
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
		protected Boolean doInBackground(String... text) {
			userData.add(new BasicNameValuePair("text", text[0].toString()));
			userData.add(new BasicNameValuePair("users_id", Integer.toString(CurrentUser.getCurrentUser().getId())));
			userList = new ArrayList<User>();
			JSONArray array = JsonHelper.getJsonArrayFromUrlWithData(SEARCH_FRIENDS_URL, userData);
			if (array != null) {
				try {
					for (int i = 0; i < array.length(); ++i) {
						userList.add(
								new User.Builder(
									array.getJSONObject(i).getInt("id"),
									array.getJSONObject(i).getString("username"),
									array.getJSONObject(i).getString("password"))
										.imageUrl(array.getJSONObject(i).getString("user_image_url"))
										.imageDrawable(DownloadImageHelper.getImageFromUrl(array.getJSONObject(i).getString("user_image_url")))
											.build());
					}
				}
				catch (JSONException e) {
					Log.e(TAG + "GetUserTask.doInBackGround(Void ...voids) : ", "JSON error parsing data" + e.toString());
				}
				return true;
			}
			else {
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			progressDialog.dismiss();
			if (result == true) {
				ListView listViewUser = (ListView) findViewById(R.id.friend_list_action_xml_listview_search_friends);
				FriendListMainItemAdapter userItemAdapter = new FriendListMainItemAdapter(FriendListActionActivity.this, userList);
				listViewUser.setAdapter(userItemAdapter);
				listViewUser.setOnItemClickListener(new OnItemClickListener() {
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						startDialog(userList.get(position));
					}
				});
			}
			else {
				AlertDialog dialogMessage = new AlertDialog.Builder(FriendListActionActivity.this).create();
				dialogMessage.setTitle("Hello " + CurrentUser.getCurrentUser().getUsername());
				dialogMessage.setMessage("No name match this search criteria. Please try again!");
				dialogMessage.setButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				dialogMessage.show();	
			}
		}
	}

	private void startDialog(final User user) {
		AlertDialog.Builder builder;
		Context c = this;
		final AlertDialog dialog;
		LayoutInflater inflater = (LayoutInflater) c.getSystemService(LAYOUT_INFLATER_SERVICE);
		final View layout = inflater.inflate(R.layout.friend_request_dialog, null);
		builder = new AlertDialog.Builder(c);
		builder.setTitle("Send request");
		builder.setView(layout);
		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				EditText editTextMessage = (EditText) layout.findViewById(R.id.friend_request_dialog_xml_edittext_message);
				String message = " ";
				if (editTextMessage.getText() != null) {
					message = editTextMessage.getText().toString();
				}
				SendRequestTask task = new SendRequestTask();
				task.execute(
					Integer.toString(CurrentUser.getCurrentUser().getId()),
					Integer.toString(user.getId()), 
					message);
			}
		});

		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
			}
		});
		dialog = builder.create();
		dialog.show();
	}
	
	
	private class SendRequestTask extends AsyncTask<String, Integer, Boolean> {
		private List<NameValuePair> userData = new ArrayList<NameValuePair>();
		private ProgressDialog progressDialog = null;

		@Override
		protected void onPreExecute() {
			// display waiting dialog
			progressDialog = new ProgressDialog(FriendListActionActivity.this);
			progressDialog.setMessage("Sending request...");
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(true);
			progressDialog.show();
		}

		@Override
		protected Boolean doInBackground(String... datas) {
			userData.add(new BasicNameValuePair("users_id", datas[0].toString()));
			userData.add(new BasicNameValuePair("friend_id", datas[1].toString()));
			userData.add(new BasicNameValuePair("friend_message", datas[2].toString()));
			JSONObject json = JsonHelper.getJsonObjectFromUrlWithData(SEND_REQUEST_URL, userData);
			try {
				if (json.getString("result").equals("success")) {
					return true;
				}
			} 
			catch (JSONException e) {
				Log.e(TAG + "SnapPictureTask.doInBackGround(Void ...voids) : ", "JSON error parsing data" + e.toString());
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			progressDialog.dismiss();
			if (result == true) {
				AlertDialog dialogMessage = new AlertDialog.Builder(FriendListActionActivity.this).create();
				dialogMessage.setTitle("Hello " + CurrentUser.getCurrentUser().getUsername());
				dialogMessage.setMessage("Request has been sent succesfully");
				dialogMessage.setButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				dialogMessage.show();	
			}
			else {
				AlertDialog dialogMessage = new AlertDialog.Builder(FriendListActionActivity.this).create();
				dialogMessage.setTitle("Hello " + CurrentUser.getCurrentUser().getUsername());
				dialogMessage.setMessage("Fail to send request");
				dialogMessage.setButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				dialogMessage.show();	
			}
		}
	}
}
