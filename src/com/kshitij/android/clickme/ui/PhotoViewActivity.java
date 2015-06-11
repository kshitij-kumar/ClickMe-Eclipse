package com.kshitij.android.clickme.ui;

import java.io.File;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kshitij.android.clickme.R;
import com.kshitij.android.clickme.db.ImageDataBaseHelper;
import com.kshitij.android.clickme.model.ImageDetail;
import com.kshitij.android.clickme.service.FetchAddressIntentService;
import com.kshitij.android.clickme.util.Constants;

/**
 * Created by kshitij.kumar on 10-06-2015.
 */

/**
 * Displays captured image
 * 
 */

public class PhotoViewActivity extends AppCompatActivity implements
		LocationListener {
	private static final String TAG = PhotoViewActivity.class.getSimpleName();
	private static final int TWO_MINUTES = 1000 * 60 * 2;

	private String mImagePath;
	private ImageView mImageView;
	private TextView mTxtLat;
	private TextView mTxtLong;
	private TextView mTxtAddress;
	private Button mBtnUpdateRetry;
	private Button mBtnCancel;
	private TextView mTxtLoading;

	private ProgressDialog mProgressDialog;
	private SaveImageDetailsTask mSaveImageDetailsTask;

	private LocationManager mLocationManager;
	protected Location mCurrentBestLocation;
	protected boolean mLocationRequested;
	protected String mAddressOutput;
	private AddressResultReceiver mResultReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate()");
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_photo_view);

		mImageView = (ImageView) findViewById(R.id.ivCaptured);
		mTxtLat = (TextView) findViewById(R.id.tvCapLat);
		mTxtLong = (TextView) findViewById(R.id.tvCapLong);
		mTxtAddress = (TextView) findViewById(R.id.tvCapAddress);
		mBtnUpdateRetry = (Button) findViewById(R.id.btnUpdateRetry);
		mBtnCancel = (Button) findViewById(R.id.btnCancel);
		mTxtLoading = (TextView) findViewById(R.id.tvLoading);

		mResultReceiver = new AddressResultReceiver(new Handler());

		mLocationRequested = false;
		mAddressOutput = "";

		getCurrentLocation();

		if (getIntent() != null && getIntent().getExtras() != null) {
			mImagePath = getIntent().getExtras().getString(
					Constants.EXTRA_IMAGE_PATH);
			displayCapturedImage();
			mBtnUpdateRetry.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mSaveImageDetailsTask = new SaveImageDetailsTask();
					mSaveImageDetailsTask.execute();
				}
			});
		} else {
			showErrorMessage(getString(R.string.image_not_found));
		}

		mBtnCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mSaveImageDetailsTask != null) {
					mSaveImageDetailsTask.cancel(true);
				}
				deleteTempFile();
				finish();

			}
		});

	}

	private void getCurrentLocation() {
		Log.d(TAG, "getCurrentLocation()");
		mLocationRequested = true;
		updateLocationUI();
		mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		String provider = mLocationManager.getBestProvider(criteria, true);
		Location location = mLocationManager.getLastKnownLocation(provider);
		if (location != null) {
			onLocationChanged(location);
		}
		mLocationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 0, 0, this);
	}

	@Override
	public void onLocationChanged(Location location) {
		Log.d(TAG, "onLocationChanged(), Lat:" + location.getLatitude()
				+ "Long:" + location.getLongitude());
		/*
		 * if (location != null && !isBetterLocation(location)) {
		 * mCurrentBestLocation = location; stopLocationUpdates();
		 * fetchAddress(); }
		 */
		mCurrentBestLocation = location;
		stopLocationUpdates();
		updateLocationUI();
		fetchAddress();
	}

	@Override
	public void onProviderDisabled(String provider) {
		Log.d(TAG, "onProviderDisabled()");
	}

	@Override
	public void onProviderEnabled(String provider) {
		Log.d(TAG, "onProviderEnabled()");
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.d(TAG, "onStatusChanged()");
	}

	protected boolean isBetterLocation(Location location) {
		Log.d(TAG, "isBetterLocation()");
		if (mCurrentBestLocation == null) {
			return true;
		}

		long timeDelta = location.getTime() - mCurrentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
		boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
		boolean isNewer = timeDelta > 0;

		if (isSignificantlyNewer) {
			return true;
		} else if (isSignificantlyOlder) {
			return false;
		}

		int accuracyDelta = (int) (location.getAccuracy() - mCurrentBestLocation
				.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		boolean isFromSameProvider = isSameProvider(location.getProvider(),
				mCurrentBestLocation.getProvider());

		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate
				&& isFromSameProvider) {
			return true;
		}
		return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}

	private void stopLocationUpdates() {
		Log.d(TAG, "stopLocationUpdates()");
		mLocationManager.removeUpdates(this);
	}

	public void fetchAddress() {
		Log.d(TAG, "fetchAddress()");
		if (mCurrentBestLocation != null) {
			Intent intent = new Intent(this, FetchAddressIntentService.class);
			intent.putExtra(Constants.RECEIVER, mResultReceiver);
			intent.putExtra(Constants.LOCATION_DATA_EXTRA, mCurrentBestLocation);
			startService(intent);
		}
	}

	private void updateLocationUI() {
		Log.d(TAG, "updateLocationUI(), mLocationRequested: "
				+ mLocationRequested);
		if (mLocationRequested) {
			mTxtLoading.setText(getString(R.string.loading_location_details));
			if (mCurrentBestLocation != null) {
				mTxtLat.setText("Lat:" + mCurrentBestLocation.getLatitude()
						+ ",");
				mTxtLong.setText("Lon:" + mCurrentBestLocation.getLongitude());
			}
			mBtnUpdateRetry.setVisibility(View.GONE);
			mTxtLat.setVisibility(View.GONE);
			mTxtLong.setVisibility(View.GONE);
			mTxtAddress.setVisibility(View.GONE);
		} else {
			mBtnUpdateRetry.setVisibility(View.VISIBLE);
			mTxtLoading.setVisibility(View.GONE);
			mTxtLat.setVisibility(View.VISIBLE);
			mTxtLong.setVisibility(View.VISIBLE);
			mTxtAddress.setVisibility(View.VISIBLE);
			mTxtLat.setText("Lat:" + mCurrentBestLocation.getLatitude() + ",");
			mTxtLong.setText("Lon:" + mCurrentBestLocation.getLongitude());
			mTxtAddress.setText("Address: " + mAddressOutput);
		}
	}

	protected void showToast(String text) {
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}

	class AddressResultReceiver extends ResultReceiver {
		public AddressResultReceiver(Handler handler) {
			super(handler);
		}

		@Override
		protected void onReceiveResult(int resultCode, Bundle resultData) {
			Log.d(TAG, "AddressResultReceiver, onReceiveResult()");
			mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
			mLocationRequested = false;
			updateLocationUI();
		}
	}

	private void displayCapturedImage() {
		if (mImagePath != null) {
			setPic();
		}

	}

	private void setPic() {

		int targetW = mImageView.getWidth();
		int targetH = mImageView.getHeight();

		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(mImagePath, bmOptions);
		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;

		int scaleFactor = 1;
		if ((targetW > 0) || (targetH > 0)) {
			scaleFactor = Math.min(photoW / targetW, photoH / targetH);
		}

		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inPurgeable = true;

		Bitmap bitmap = BitmapFactory.decodeFile(mImagePath, bmOptions);

		mImageView.setImageBitmap(bitmap);
	}

	private class SaveImageDetailsTask extends AsyncTask<Void, Void, Integer> {

		@Override
		protected void onPreExecute() {
			Log.d(TAG, "SaveImageDetailsTask, onPreExecute()");
			showLoadingProgress();
			super.onPreExecute();
		}

		@Override
		protected Integer doInBackground(Void... params) {
			Log.d(TAG, "SaveImageDetailsTask, doInBackground()");
			ImageDetail imageDetail = new ImageDetail();
			imageDetail.setDiskPath(mImagePath);
			if (mCurrentBestLocation != null) {
				imageDetail.setLatitude(String.valueOf(mCurrentBestLocation
						.getLatitude()));
				imageDetail.setLongitude(String.valueOf(mCurrentBestLocation
						.getLongitude()));
			}
			if (mAddressOutput != null && mAddressOutput.length() != 0) {
				imageDetail.setAddress(mAddressOutput);
			}
			imageDetail.setDate(System.currentTimeMillis());
			return ImageDataBaseHelper.getInstance(getApplicationContext())
					.saveImageDetailInDB(imageDetail);
		}

		@Override
		protected void onPostExecute(Integer result) {
			Log.d(TAG, "SaveImageDetailsTask, onPostExecute()");
			dismissLoadingProgress();
			if (result == Constants.SAVE_RESULT_SUCCESS) {
				setResult(RESULT_OK);
				finish();
			} else {
				showToast(getString(R.string.update_failed));
				mBtnUpdateRetry.setText(getString(R.string.retry));
			}
		}
	}

	private void dismissLoadingProgress() {
		Log.d(TAG, "dismissLoadingProgress()");
		if (mProgressDialog != null && mProgressDialog.isShowing())
			mProgressDialog.dismiss();
	}

	private void showLoadingProgress() {
		Log.d(TAG, "showLoadingProgress()");
		dismissLoadingProgress();
		mProgressDialog = ProgressDialog.show(this, "", "Saving...", true,
				false);
	}

	private void showErrorMessage(String message) {
		Log.d(TAG, "showErrorMessage()");
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message).setCancelable(false)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						if (mSaveImageDetailsTask != null) {
							mSaveImageDetailsTask.cancel(true);
							finish();
						}
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	@Override
	public void onBackPressed() {
		if (mSaveImageDetailsTask != null) {
			mSaveImageDetailsTask.cancel(true);
		}
		deleteTempFile();
		super.onBackPressed();
	}

	@Override
	protected void onStop() {
		stopLocationUpdates();
		super.onStop();
	}

	private void deleteTempFile() {
		if (mImagePath != null) {
			File file = new File(mImagePath);
			if (file.exists()) {
				file.delete();
			}
		}
	}
}
