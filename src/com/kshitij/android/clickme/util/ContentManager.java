package com.kshitij.android.clickme.util;

import java.util.List;

import com.kshitij.android.clickme.model.ImageDetail;

/**
 * Created by kshitij.kumar on 09-06-2015.
 */

/**
 * A helper class to maintain data during app session, reduces potential disk reads
 * 
 */

public class ContentManager {
	public static final String TAG = ContentManager.class.getSimpleName();
	static ContentManager mContentManager;
	private List<ImageDetail> mImageDetails;

	public synchronized static ContentManager getInstance() {
		if (mContentManager == null) {
			mContentManager = new ContentManager();
		}
		return mContentManager;
	}

	public List<ImageDetail> getImageDetails() {
		return mImageDetails;
	}

	public void setImageDetails(List<ImageDetail> imageDetails) {
		this.mImageDetails = imageDetails;
	}
}
