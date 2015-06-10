package com.kshitij.android.clickme.ui;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.kshitij.android.clickme.R;
import com.kshitij.android.clickme.adapter.ListAdapter;
import com.kshitij.android.clickme.db.ImageDataBaseHelper;
import com.kshitij.android.clickme.model.ImageDetail;
import com.kshitij.android.clickme.util.Constants;
import com.kshitij.android.clickme.util.ContentManager;

/**
 * Created by kshitij.kumar on 09-06-2015.
 */

/**
 * Launcher activity, displays list of photos
 * 
 */

public class PhotoFeedActivity extends AppCompatActivity {
	private static final String TAG = PhotoFeedActivity.class.getSimpleName();

	private String mImageFilePath;
	private ListView mListView;
	private ListAdapter mAdapter;
	private ProgressDialog mProgressDialog;
	private LoadFeedTask mLoadFeedTask;
	private MenuItem mMenuItemCamera;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photo_feed);
		mListView = (ListView) findViewById(R.id.photoList);
		mLoadFeedTask = new LoadFeedTask();
		mLoadFeedTask.execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.photo_feed_menu, menu);
		mMenuItemCamera = menu.findItem(R.id.action_camera);
		if (mListView.getAdapter() == null) {
			mMenuItemCamera.setVisible(false);
		} else {
			mMenuItemCamera.setVisible(true);
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_camera) {
			takePicture();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case Constants.ACTION_CAPTURE_PHOTO: {
			if (resultCode == RESULT_OK) {
				Intent viewPhotoIntent = new Intent(this,
						PhotoViewActivity.class);
				viewPhotoIntent.putExtra(Constants.EXTRA_IMAGE_PATH,
						mImageFilePath);
				startActivityForResult(viewPhotoIntent,
						Constants.ACTION_VIEW_PHOTO);
			}
			break;
		}
		case Constants.ACTION_VIEW_PHOTO: {
			if (resultCode == RESULT_OK) {
				if (mAdapter == null) {
					mAdapter = new ListAdapter(getApplicationContext(),
							ContentManager.getInstance().getImageDetails());
					mListView.setAdapter(mAdapter);
					mAdapter.notifyDataSetChanged();
				} else {
					mAdapter.setData(ContentManager.getInstance()
							.getImageDetails());
					mAdapter.notifyDataSetChanged();
				}
				mImageFilePath = null;
			}
			break;
		}
		}
	}

	/**
	 * AsyncTask to download data off the UI thread
	 * 
	 * @param URL
	 *            of the remote server.
	 */
	private class LoadFeedTask extends AsyncTask<Void, Void, List<ImageDetail>> {
		@Override
		protected void onPreExecute() {
			Log.d(TAG, "LoadFeedTask, onPreExecute()");
			// Show progress indicator
			showLoadingProgress();
			super.onPreExecute();
		}

		@Override
		protected List<ImageDetail> doInBackground(Void... params) {
			ImageDataBaseHelper dbHelper = new ImageDataBaseHelper(
					getApplicationContext());
			return dbHelper.getImageDetailsFromDB();
		}

		@Override
		protected void onPostExecute(List<ImageDetail> imageDetails) {
			Log.d(TAG, "LoadFeedTask, onPostExecute(), " + imageDetails.size());
			dissmissLoadingProgress();
			if (mMenuItemCamera != null) {
				mMenuItemCamera.setVisible(true);
			}
			if (imageDetails != null && imageDetails.size() > 0) {
				mAdapter = new ListAdapter(getApplicationContext(),
						imageDetails);
				mAdapter.setData(imageDetails);
				mListView.setAdapter(mAdapter);
				mAdapter.notifyDataSetChanged();
			} else {
				showErrorMessage();
			}
		}
	}

	private void dissmissLoadingProgress() {
		Log.d(TAG, "dissmissLoadingProgress()");
		if (mProgressDialog != null && mProgressDialog.isShowing())
			mProgressDialog.dismiss();
	}

	private void showLoadingProgress() {
		Log.d(TAG, "showLoadingProgress()");
		dissmissLoadingProgress();
		mProgressDialog = ProgressDialog.show(this, "", "Loading...", true,
				false);
	}

	private void showErrorMessage() {
		Log.d(TAG, "showErrorMessage()");
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getString(R.string.no_photos)).setCancelable(false)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						if (mLoadFeedTask != null) {
							mLoadFeedTask.cancel(true);
						}
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void takePicture() {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		File f = null;
		try {
			f = createImageFile();
			mImageFilePath = f.getAbsolutePath();
			takePictureIntent
					.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
		} catch (IOException e) {
			Log.d(TAG, "takeCapture(), Exception: " + e.toString());
		}
		startActivityForResult(takePictureIntent,
				Constants.ACTION_CAPTURE_PHOTO);
	}

	@SuppressLint("SimpleDateFormat")
	private File createImageFile() throws IOException {
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		String imageFileName = Constants.JPEG_FILE_PREFIX + timeStamp + "_";
		File albumF = getAlbumDir();
		File imageF = File.createTempFile(imageFileName,
				Constants.JPEG_FILE_SUFFIX, albumF);
		return imageF;
	}

	private File getAlbumDir() {
		File storageDir = null;
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {
			storageDir = new File(Environment.getExternalStorageDirectory()
					+ Constants.CAMERA_DIR + Constants.ALBUM_NAME);
			if (storageDir != null) {
				if (!storageDir.mkdirs()) {
					if (!storageDir.exists()) {
						Log.d(TAG, "failed to create directory");
						return null;
					}
				}
			}
		} else {
			Log.v(getString(R.string.app_name),
					"External storage is not mounted READ/WRITE.");
		}

		return storageDir;
	}
}
