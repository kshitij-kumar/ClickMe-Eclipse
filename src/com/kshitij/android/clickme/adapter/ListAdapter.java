package com.kshitij.android.clickme.adapter;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.android.displayingbitmaps.ui.RecyclingImageView;
import com.example.android.displayingbitmaps.util.ImageFetcher;
import com.kshitij.android.clickme.R;
import com.kshitij.android.clickme.model.ImageDetail;
import com.kshitij.android.clickme.util.TimeFormatter;

/**
 * Created by kshitij.kumar on 10-06-2015.
 */

/**
 * Adapter for content photo feed.
 * 
 */
public class ListAdapter extends BaseAdapter {

	private static final String TAG = ListAdapter.class.getSimpleName();
	private LayoutInflater mInflater;
	private List<ImageDetail> mImageDetails;
	private ImageFetcher mImageFetcher;

	public ListAdapter(Context context, List<ImageDetail> imageDetails, ImageFetcher imageFetcher) {
		mInflater = LayoutInflater.from(context);
		mImageDetails = imageDetails;
		mImageFetcher = imageFetcher;
	}

	public void setData(List<ImageDetail> imageDetails) {
		this.mImageDetails = imageDetails;
	}

	@Override
	public int getCount() {
		if (mImageDetails != null && mImageDetails.size() > 0) {
			return mImageDetails.size();
		} else {
			return 0;
		}
	}

	@Override
	public Object getItem(int position) {
		if (mImageDetails != null && mImageDetails.size() > 0) {
			return mImageDetails.get(position);
		} else {
			return null;
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolderItem viewHolder;

		if (convertView == null) {

			// inflate the layout
			convertView = mInflater.inflate(R.layout.list_item, parent, false);

			// set up the ViewHolder
			viewHolder = new ViewHolderItem();
			viewHolder.image = (RecyclingImageView) convertView.findViewById(R.id.image);
			viewHolder.tvLat = (TextView) convertView.findViewById(R.id.tvLat);
			viewHolder.tvLong = (TextView) convertView
					.findViewById(R.id.tvLong);
			viewHolder.tvAddress = (TextView) convertView
					.findViewById(R.id.tvAddress);
			viewHolder.tvUpdated = (TextView) convertView
					.findViewById(R.id.tvUpdated);
			// store the holder with the view.
			convertView.setTag(viewHolder);

		} else {
			// just use the viewHolder
			viewHolder = (ViewHolderItem) convertView.getTag();
		}

		// Post item based on the position
		ImageDetail imageDetail = mImageDetails.get(mImageDetails.size()
				- position - 1);

		// assign values if the object is not null
		if (imageDetail != null) {
			mImageFetcher.loadImage(imageDetail.getDiskPath(), viewHolder.image);
			viewHolder.tvLat.setText("Lat: " + imageDetail.getLatitude() + ",");
			viewHolder.tvLong.setText("Lon: " + imageDetail.getLongitude());
			viewHolder.tvAddress.setText("Address: " + imageDetail.getAddress());
			viewHolder.tvUpdated.setText("Updated: " + TimeFormatter.getCustomisedTimeLabel(imageDetail.getDate()));
		}

		return convertView;
	}

	static class ViewHolderItem {
		RecyclingImageView image;
		TextView tvLat;
		TextView tvLong;
		TextView tvAddress;
		TextView tvUpdated;
	}
}