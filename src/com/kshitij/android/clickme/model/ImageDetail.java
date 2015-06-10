package com.kshitij.android.clickme.model;

/**
 * Created by kshitij.kumar on 09-06-2015.
 */

/**
 * Model class to hold image information
 * 
 */

public class ImageDetail {
	private Integer id;
	private String diskPath;
	private String latitude;
	private String longitude;
	private String address;
	private long date;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getDiskPath() {
		return diskPath;
	}

	public void setDiskPath(String diskPath) {
		this.diskPath = diskPath;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

}
