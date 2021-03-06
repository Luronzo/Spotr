package com.csun.spotr.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.csun.spotr.R;
import com.csun.spotr.core.Challenge;

public class QuestActionItemAdapter extends BaseAdapter {
	private List<Challenge> items;
	private Context context;
	private static LayoutInflater inflater;
	private	ItemViewHolder holder;
	
	public QuestActionItemAdapter(Context context, List<Challenge> items) {
		this.context = context.getApplicationContext();
		this.items = items;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public int getCount() {
		return items.size();
	}

	public Object getItem(int position) {
		return items.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public static class ItemViewHolder {
		TextView titleTextView;
		TextView descriptionTextView;
		TextView pointTextView;
		ImageView iconImageView;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.place_action_item, null);
			holder = new ItemViewHolder();
			holder.titleTextView = (TextView) convertView.findViewById(R.id.place_action_item_xml_textview_title);
			holder.descriptionTextView = (TextView) convertView.findViewById(R.id.place_action_item_xml_textview_subtitle);
			holder.pointTextView = (TextView) convertView.findViewById(R.id.place_action_item_xml_textview_point);
			holder.iconImageView = (ImageView) convertView.findViewById(R.id.place_action_item_xml_imageview_action_icon);

			convertView.setTag(holder);
		}
		else {
			holder = (ItemViewHolder) convertView.getTag();
		}

		holder.titleTextView.setText(items.get(position).getName());
		holder.descriptionTextView.setText(items.get(position).getDescription());
		holder.pointTextView.setText(Integer.toString(items.get(position).getPoints()));

		if (items.get(position).getType() == Challenge.Type.CHECK_IN)
			holder.iconImageView.setImageResource(R.drawable.ic_launcher);
		else if (items.get(position).getType() == Challenge.Type.SNAP_PICTURE)
			holder.iconImageView.setImageResource(R.drawable.ic_launcher);
		else if (items.get(position).getType() == Challenge.Type.WRITE_ON_WALL)
			holder.iconImageView.setImageResource(R.drawable.ic_launcher);
		else if (items.get(position).getType() == Challenge.Type.WRITE_ON_WALL)
			holder.iconImageView.setImageResource(R.drawable.ic_launcher);
		else
			holder.iconImageView.setImageResource(R.drawable.ic_launcher);

		return convertView;
	}
}