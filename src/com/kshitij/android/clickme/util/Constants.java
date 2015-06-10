package com.kshitij.android.clickme.util;

/**
 * Created by kshitij.kumar on 09-06-2015.
 */

/**
 * Holds application wide constants
 * 
 */
public class Constants {

	public static final String JPEG_FILE_PREFIX = "IMG_";
	public static final String JPEG_FILE_SUFFIX = ".jpg";
	public static final int ACTION_CAPTURE_PHOTO = 1;
	public static final int ACTION_VIEW_PHOTO = 2;
	public static final String EXTRA_IMAGE_PATH = "extra_image_path";
	public static final String CAMERA_DIR = "/ClickMe/";
	public static final String ALBUM_NAME = "Photos";

	public static final int SAVE_RESULT_SUCCESS = 1;
	public static final int SAVE_RESULT_FAILURE = 0;

	public static final int SUCCESS_RESULT = 0;
	public static final int FAILURE_RESULT = 1;
	public static final String PACKAGE_NAME = "com.kshitij.android.clickme";
	public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
	public static final String RESULT_DATA_KEY = PACKAGE_NAME
			+ ".RESULT_DATA_KEY";
	public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME
			+ ".LOCATION_DATA_EXTRA";

}
