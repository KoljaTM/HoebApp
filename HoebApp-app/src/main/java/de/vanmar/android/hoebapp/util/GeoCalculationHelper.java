package de.vanmar.android.hoebapp.util;

/**
 * Place to provide some common geo calculation used within the services or ws
 * layer
 * 
 * @author Kolja Markwardt
 */
public final class GeoCalculationHelper {
	/**
	 * Calculate the direct distance between to coordinates from/to given with
	 * LBS we use wgs84 only
	 * 
	 * @param fromLatitude
	 *            (-90.0=S / +90.0=N )
	 * @param fromLongitude
	 *            (-180=E / +180.0=W)
	 * @param toLatitude
	 *            (-90.0=S / +90.0=N )
	 * @param toLongitude
	 *            (-180=E / +180.0=W)
	 * @return distance in m
	 */
	public static double calculateDirectDistance(double fromLatitude,
			double fromLongitude, double toLatitude, double toLongitude) {

		// calculate geo distance with Haversine formula
		double earthDiameter = 12742001.6; // in meter
		double dLat = Math.toRadians(toLatitude - fromLatitude);
		double dLng = Math.toRadians(toLongitude - fromLongitude);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
				+ Math.cos(Math.toRadians(fromLatitude))
				* Math.cos(Math.toRadians(toLatitude)) * Math.sin(dLng / 2)
				* Math.sin(dLng / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double dist = earthDiameter / 2 * c;
		return dist;
	}

	private GeoCalculationHelper() {
		// no objects needed
	}
}
