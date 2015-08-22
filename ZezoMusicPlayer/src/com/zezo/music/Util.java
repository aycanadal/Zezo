package com.zezo.music;

public class Util {

	public static String getNumberWithLeadingZero(int _number) {
		if (_number < 10) {
			return "0" + String.valueOf(_number);
		} else {
			return String.valueOf(_number);
		}
	}

	public static String getTimeStringFromMs(long _ms) {
		int totalSeconds = (int) _ms / 1000;
		int seconds = totalSeconds % 60;
		int minutes = totalSeconds / 60;

		return getNumberWithLeadingZero(minutes) + ":"
				+ getNumberWithLeadingZero(seconds);
	}

}
