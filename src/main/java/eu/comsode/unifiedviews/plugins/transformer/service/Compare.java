package eu.comsode.unifiedviews.plugins.transformer.service;

public class Compare {
	public static boolean isZIP(String s) {
		return (s.length() == 6 && s.charAt(3) == ' '
				&& s.substring(0, 2).matches("[0-9]+") && s.substring(4)
				.matches("[0-9]+"));
	}

	public static boolean isDouble(String s) {
		try {
			Double.parseDouble(s.replace(',', '.'));
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public static boolean isInteger(String s) {
		try {
			Integer.parseInt(s.replace(" ", ""));
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
}