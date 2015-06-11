package com.kshitij.android.clickme.adapter;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

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
	HashMap<String, WeakReference<Bitmap>> mImageMap = new HashMap<String, WeakReference<Bitmap>>();

	public ListAdapter(Context context, List<ImageDetail> imageDetails) {
		mInflater = LayoutInflater.from(context);
		mImageDetails = imageDetails;
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
			viewHolder.image = (ImageView) convertView.findViewById(R.id.image);
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

			Bitmap bitmap = null;
			if (mImageMap.get(imageDetail.getDiskPath()) != null) {
				bitmap = mImageMap.get(imageDetail.getDiskPath()).get();
			}
			if (bitmap == null) {
				int targetWidth = 400;
				int targetHeight = 300;
				bitmap = getdownSampledImage(imageDetail.getDiskPath(),
						targetWidth, targetHeight);
				mImageMap.put(getFileNameFromPath(imageDetail.getDiskPath()),
						new WeakReference<Bitmap>(bitmap));
			}

			viewHolder.image.setImageBitmap(bitmap);
			if (imageDetail.getLatitude() != null) {
				viewHolder.tvLat.setVisibility(View.VISIBLE);
				viewHolder.tvLat.setText("Lat: " + imageDetail.getLatitude()
						+ ",");
			} else {
				viewHolder.tvLat.setVisibility(View.GONE);
			}
			if (imageDetail.getLongitude() != null) {
				viewHolder.tvLong.setVisibility(View.VISIBLE);
				viewHolder.tvLong.setText("Lon: " + imageDetail.getLongitude());
			} else {
				viewHolder.tvLong.setVisibility(View.GONE);
			}
			if (imageDetail.getAddress() != null
					&& imageDetail.getAddress().length() != 0) {
				viewHolder.tvAddress.setVisibility(View.VISIBLE);
				viewHolder.tvAddress.setText("Address: "
						+ imageDetail.getAddress());
			} else {
				viewHolder.tvAddress.setVisibility(View.GONE);
			}
			if (imageDetail.getDate() > 0) {
				viewHolder.tvUpdated.setText("Updated: "
						+ TimeFormatter.getCustomisedTimeLabel(imageDetail
								.getDate()));
			}
		}

		return convertView;
	}

	private Bitmap getdownSampledImage(String path, int width, int height) {

		/* There isn't enough memory to open up more than a couple camera photos */
		/* So pre-scale the target bitmap into which the file is decoded */

		/* Get the size of the ImageView */
		int targetW = width;
		int targetH = height;

		/* Get the size of the image */
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, bmOptions);
		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;

		/* Figure out which way needs to be reduced less */
		int scaleFactor = 1;
		if ((targetW > 0) || (targetH > 0)) {
			scaleFactor = Math.min(photoW / targetW, photoH / targetH);
		}

		/* Set bitmap options to scale the image decode target */
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inPurgeable = true;

		/* Decode the JPEG file into a Bitmap */
		return BitmapFactory.decodeFile(path, bmOptions);

	}
	
	private String getFileNameFromPath(String path) {
		if (path != null) {
			File file = new File(path);
			if (file.exists()) {
				return file.getName();
			}
		}
		return null;
	}

	static class ViewHolderItem {
		ImageView image;
		TextView tvLat;
		TextView tvLong;
		TextView tvAddress;
		TextView tvUpdated;
	}
}