package com.kshitij.android.clickme.util;

import android.content.res.Resources;

public class Utility {

	private static final String TAG = Utility.class.getSimpleName();

	public static int dpToPx(int dp) {
		return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
	}

}
