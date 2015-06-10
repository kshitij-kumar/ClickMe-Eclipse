package com.kshitij.android.clickme.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.kshitij.android.clickme.model.ImageDetail;
import com.kshitij.android.clickme.util.Constants;
import com.kshitij.android.clickme.util.ContentManager;

/**
 * Created by kshitij.kumar on 09-06-2015.
 */

/**
 * An implementation of SQLiteOpenHelper to maintain details of images
 * 
 */

public class ImageDataBaseHelper extends SQLiteOpenHelper {
	private static final String TAG = ImageDataBaseHelper.class.getSimpleName();
	private static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "click_me";
	public static final String TABLE_IMAGE_DETAIL = "image_detail";
	public static final String COL_ID = "_id";
	public static final String COL_DISK_PATH = "disk_path";
	public static final String COL_LATITUDE = "latitude";
	public static final String COL_LONGITUDE = "longitude";
	public static final String COL_ADDRESS = "address";
	public static final String COL_DATE = "date";

	private static ImageDataBaseHelper instance;

	public static synchronized ImageDataBaseHelper getInstance(Context context) {

		if (instance == null) {
			instance = new ImageDataBaseHelper(context);
		}

		return instance;
	}

	public ImageDataBaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public ImageDataBaseHelper(Context context, String databaseName,
			CursorFactory object, int databaseVersion) {
		super(context, databaseName, object, databaseVersion);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String queryCreateImageDetailTable = "Create virtual table "
				+ TABLE_IMAGE_DETAIL + " USING fts3(" + COL_ID
				+ " INTEGER PRIMARY KEY, " + COL_DISK_PATH + " TEXT, "
				+ COL_LATITUDE + " TEXT, " + COL_LONGITUDE + " TEXT, "
				+ COL_ADDRESS + " TEXT, " + COL_DATE + " INTEGER)";

		db.execSQL(queryCreateImageDetailTable);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		String queryDropImageDetailTable = "Drop table if exists "
				+ TABLE_IMAGE_DETAIL;
		db.execSQL(queryDropImageDetailTable);
		onCreate(db);
	}

	/**
	 * Inserts an {@link com.kshitij.android.ImageDetail.Image ImageDetail} into
	 * database.
	 * 
	 * @param imageDetail
	 *            The {@link com.kshitij.android.ImageDetail.ImageDetail
	 *            ImageDetail} to be saved.
	 * @return 1 if insertion is successful, 0 otherwise.
	 */

	public int saveImageDetailInDB(ImageDetail imageDetail) {
		if (imageDetail == null) {
			return Constants.SAVE_RESULT_FAILURE;
		}
		SQLiteDatabase db = getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put(COL_DISK_PATH, imageDetail.getDiskPath());
		contentValues.put(COL_LATITUDE, imageDetail.getLatitude());
		contentValues.put(COL_LONGITUDE, imageDetail.getLongitude());
		contentValues.put(COL_ADDRESS, imageDetail.getAddress());
		contentValues.put(COL_DATE, imageDetail.getDate());
		long rowId = db.insert(TABLE_IMAGE_DETAIL, null, contentValues);

		if (rowId != -1) {
			ContentManager contentManager = ContentManager.getInstance();
			if (contentManager.getImageDetails() == null
					|| contentManager.getImageDetails().size() == 0) {
				Log.d(TAG, "saveImageDetailInDB(), Updating content manager.");
				contentManager.setImageDetails(getImageDetailsFromDB());
			} else {
				Log.d(TAG, "saveImageDetailInDB(), Updating content manager.");
				contentManager.getImageDetails().add(imageDetail);
			}
			return Constants.SAVE_RESULT_SUCCESS;
		}

		return Constants.SAVE_RESULT_FAILURE;
	}

	public List<ImageDetail> getImageDetailsFromDB() {
		Log.d(TAG, "getImageDetailsFromDB()");
		List<ImageDetail> imageDetails = new ArrayList<ImageDetail>();
		String querySelectImages = "Select * from " + TABLE_IMAGE_DETAIL;
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.rawQuery(querySelectImages, new String[] {});
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			do {
				ImageDetail image = readImageDetailsFromCursor(cursor);
				imageDetails.add(image);
			} while (cursor.moveToNext());
		}
		cursor.close();
		Log.d(TAG, "getImageDetailsFromDB(), Size = " + imageDetails.size());
		return imageDetails;
	}

	private ImageDetail readImageDetailsFromCursor(Cursor cursor) {
		Log.d(TAG, "readImageDetailsFromCursor()");
		ImageDetail imageDetail = new ImageDetail();
		imageDetail.setId(cursor.getInt(cursor.getColumnIndex(COL_ID)));
		imageDetail.setDiskPath(cursor.getString(cursor
				.getColumnIndex(COL_DISK_PATH)));
		imageDetail.setLatitude(cursor.getString(cursor
				.getColumnIndex(COL_LATITUDE)));
		imageDetail.setLongitude(cursor.getString(cursor
				.getColumnIndex(COL_LONGITUDE)));
		imageDetail.setAddress(cursor.getString(cursor
				.getColumnIndex(COL_ADDRESS)));
		imageDetail.setDate(cursor.getLong(cursor.getColumnIndex(COL_DATE)));
		return imageDetail;
	}
}
