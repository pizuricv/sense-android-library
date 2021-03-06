/**************************************************************************************************
 * Copyright (C) 2010 Sense Observation Systems, Rotterdam, the Netherlands. All rights reserved. *
 *************************************************************************************************/
package nl.sense_os.service.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import nl.sense_os.service.constants.SensorData.DataPoint;

public class ParserUtils {

    @SuppressWarnings("unused")
    private static final String TAG = "ParserUtils";

    private static String fixCompareSigns(String s) {
		String result = s.replaceAll(" = ", "=");
		result = result.replaceAll("= ", "=");
		result = result.replaceAll(" =", "=");
		result = result.replaceAll(" > ", ">");
		result = result.replaceAll("> ", ">");
		result = result.replaceAll(" >", ">");
		result = result.replaceAll(" < ", "<");
		result = result.replaceAll("< ", "<");
		result = result.replaceAll(" <", "<");
		result = result.replaceAll(" != ", "!=");
		result = result.replaceAll("!= ", "!=");
		result = result.replaceAll(" !=", "!=");
		return result;
    }

    public static String getSelectedDeviceUuid(String selection, String[] selectionArgs) {
	String deviceUuid = null;
	if (selection != null && selection.contains(DataPoint.DEVICE_UUID)) {

	    // preprocess the selection string a bit
	    selection = fixCompareSigns(selection);

	    int eqKeyStart = selection.indexOf(DataPoint.DEVICE_UUID + "='");

	    if (-1 != eqKeyStart) {
			// selection contains "device_uuid='"
			int uuidStart = eqKeyStart + (DataPoint.DEVICE_UUID + "='").length();
			int uuidEnd = selection.indexOf("'", uuidStart);
			uuidEnd = uuidEnd == -1 ? selection.length() - 1 : uuidEnd;
			deviceUuid = selection.substring(uuidStart, uuidEnd);
	    }
	}
	return deviceUuid;
    }

    /**
     * Tries to parse the selection String to see which data has to be returned for the query. Looks
     * for occurrences of "sensor_name" in the selection String.
     * 
     * @param allSensors
     *            Set of all possible sensors, used to form the selection from.
     * @param selection
     *            Selection string from the query.
     * @param selectionArgs
     *            Selection arguments. Not used yet.
     * @return List of sensor names that are included in the query.
     */
    public static List<String> getSelectedSensors(Set<String> allSensors, String selection,
	    String[] selectionArgs) {

	List<String> names = new ArrayList<String>();

	if (selection != null && selection.contains(DataPoint.SENSOR_NAME)) {

	    // preprocess the selection string a bit
	    selection = selection.replaceAll(" = ", "=");
	    selection = selection.replaceAll("= ", "=");
	    selection = selection.replaceAll(" =", "=");
	    selection = selection.replaceAll(" != ", "!=");
	    selection = selection.replaceAll("!= ", "!=");
	    selection = selection.replaceAll(" !=", "!=");

	    int eqKeyStart = selection.indexOf(DataPoint.SENSOR_NAME + "='");
	    int neqKeyStart = selection.indexOf(DataPoint.SENSOR_NAME + "!='")
		    + (DataPoint.SENSOR_NAME + "!='").length();

	    if (-1 != eqKeyStart) {
		// selection contains "sensor_name='"
		int sensorNameStart = eqKeyStart + (DataPoint.SENSOR_NAME + "='").length();
		int sensorNameEnd = selection.indexOf("'", sensorNameStart);
		sensorNameEnd = sensorNameEnd == -1 ? selection.length() - 1 : sensorNameEnd;
		String sensorName = selection.substring(sensorNameStart, sensorNameEnd);
		if (sensorName.equals("?")) {
		    throw new IllegalArgumentException(
			    "LocalStorage cannot handle queries with arguments array, sorry...");
		}
		// Log.v(TAG, "Query contains: " + DataPoint.SENSOR_NAME + " = '" + sensorName +
		// "'");

		boolean inStorage = false;
		for (String key : allSensors) {
		    if (key.startsWith(sensorName)) {
			names.add(key);
			inStorage = true;
		    }
		}
		// sometimes we want to select sensors that are not currently in the storage
		if (!inStorage) {
		    names.add(sensorName);
		}

	    } else if (-1 != neqKeyStart) {
		// selection contains "sensor_name!='"
		int sensorNameStart = neqKeyStart + (DataPoint.SENSOR_NAME + "!='").length();
		int sensorNameEnd = selection.indexOf("'", sensorNameStart);
		sensorNameEnd = sensorNameEnd == -1 ? selection.length() - 1 : sensorNameEnd;
		String notSensorName = selection.substring(sensorNameStart, sensorNameEnd);
		if (notSensorName.equals("?")) {
		    throw new IllegalArgumentException(
			    "LocalStorage cannot handle queries with arguments array, sorry...");
		}
		// Log.v(TAG, "Query contains: " + DataPoint.SENSOR_NAME + " != '" + notSensorName +
		// "'");

		for (String key : allSensors) {
		    if (!key.startsWith(notSensorName)) {
			names.add(key);
		    }
		}

	    } else {
		throw new IllegalArgumentException("Parser cannot handle selection query: "
			+ selection);
	    }

	} else {
	    // no selection: return all sensor names
	    names.addAll(allSensors);
	}

	// return a copy of the list of names
	return new ArrayList<String>(names);
    }

    /**
     * Tries to parse the selection String to see which data has to be returned for the query. Looks
     * for occurrences of "timestamp" in the selection String.
     * 
     * @param selection
     *            Selection string from the query.
     * @param selectionArgs
     *            Selection arguments. Not used yet.
     * @return Array with minimum and maximum time stamp for the query result.
     */
    public static long[] getSelectedTimeRange(String selection, String[] selectionArgs) {

	long minTimestamp = Long.MIN_VALUE;
	long maxTimestamp = Long.MAX_VALUE;

	if (selection != null && selection.contains(DataPoint.TIMESTAMP)) {

	    // preprocess the selection string a bit
	    selection = fixCompareSigns(selection);

	    int eqKeyStart = selection.indexOf(DataPoint.TIMESTAMP + "=");
	    int neqKeyStart = selection.indexOf(DataPoint.TIMESTAMP + "!=");
	    int leqKeyStart = selection.indexOf(DataPoint.TIMESTAMP + "<=");
	    int ltKeyStart = selection.indexOf(DataPoint.TIMESTAMP + "<");
	    int geqKeyStart = selection.indexOf(DataPoint.TIMESTAMP + ">=");
	    int gtKeyStart = selection.indexOf(DataPoint.TIMESTAMP + ">");

	    if (-1 != eqKeyStart) {
		// selection contains "timestamp='"
		int timestampStart = eqKeyStart + (DataPoint.TIMESTAMP + "=").length();
		int timestampEnd = selection.indexOf(" ", timestampStart);
		timestampEnd = timestampEnd == -1 ? selection.length() : timestampEnd;
		String timestamp = selection.substring(timestampStart, timestampEnd);
		if (timestamp.equals("?")) {
		    throw new IllegalArgumentException(
			    "LocalStorage cannot handle queries with arguments array, sorry...");
		}
		// Log.v(TAG, "Query contains: " + DataPoint.TIMESTAMP + " = " + timestamp);

		minTimestamp = maxTimestamp = Long.parseLong(timestamp);

	    } else if (-1 != neqKeyStart) {
		// selection contains "timestamp!='"
		int timestampStart = neqKeyStart + (DataPoint.TIMESTAMP + "!=").length();
		int timestampEnd = selection.indexOf(" ", timestampStart);
		timestampEnd = timestampEnd == -1 ? selection.length() : timestampEnd;
		String timestamp = selection.substring(timestampStart, timestampEnd);
		if (timestamp.equals("?")) {
		    throw new IllegalArgumentException(
			    "LocalStorage cannot handle queries with arguments array, sorry...");
		}
		// Log.v(TAG, "Query contains: " + DataPoint.TIMESTAMP + " != " + timestamp);

		// use default timestamps
	    }

	    if (-1 != geqKeyStart) {
		// selection contains "timestamp>='"
		int timestampStart = geqKeyStart + (DataPoint.TIMESTAMP + ">=").length();
		int timestampEnd = selection.indexOf(" ", timestampStart);
		timestampEnd = timestampEnd == -1 ? selection.length() : timestampEnd;
		String timestamp = selection.substring(timestampStart, timestampEnd);
		if (timestamp.equals("?")) {
		    throw new IllegalArgumentException(
			    "LocalStorage cannot handle queries with arguments array, sorry...");
		}
		// Log.v(TAG, "Query contains: " + DataPoint.TIMESTAMP + " >= " + timestamp);

		minTimestamp = Long.parseLong(timestamp);

	    } else if (-1 != gtKeyStart) {
		// selection contains "timestamp>'"
		int timestampStart = gtKeyStart + (DataPoint.TIMESTAMP + ">").length();
		int timestampEnd = selection.indexOf(" ", timestampStart);
		timestampEnd = timestampEnd == -1 ? selection.length() : timestampEnd;
		String timestamp = selection.substring(timestampStart, timestampEnd);
		if (timestamp.equals("?")) {
		    throw new IllegalArgumentException(
			    "LocalStorage cannot handle queries with arguments array, sorry...");
		}
		// Log.v(TAG, "Query contains: " + DataPoint.TIMESTAMP + " > " + timestamp);

		minTimestamp = Long.parseLong(timestamp) - 1;

	    }

	    if (-1 != leqKeyStart) {
		// selection contains "timestamp<='"
		int timestampStart = leqKeyStart + (DataPoint.TIMESTAMP + "<=").length();
		int timestampEnd = selection.indexOf(" ", timestampStart);
		timestampEnd = timestampEnd == -1 ? selection.length() : timestampEnd;
		String timestamp = selection.substring(timestampStart, timestampEnd);
		if (timestamp.equals("?")) {
		    throw new IllegalArgumentException(
			    "LocalStorage cannot handle queries with arguments array, sorry...");
		}
		// Log.v(TAG, "Query contains: " + DataPoint.TIMESTAMP + " <= " + timestamp);

		maxTimestamp = Long.parseLong(timestamp);

	    } else if (-1 != ltKeyStart) {
		// selection contains "timestamp<'"
		int timestampStart = ltKeyStart + (DataPoint.TIMESTAMP + "<").length();
		int timestampEnd = selection.indexOf(" ", timestampStart);
		timestampEnd = timestampEnd == -1 ? selection.length() : timestampEnd;
		String timestamp = selection.substring(timestampStart, timestampEnd);
		if (timestamp.equals("?")) {
		    throw new IllegalArgumentException(
			    "LocalStorage cannot handle queries with arguments array, sorry...");
		}
		// Log.v(TAG, "Query contains: " + DataPoint.TIMESTAMP + " < " + timestamp);

		maxTimestamp = Long.parseLong(timestamp) - 1;

	    }

	} else {
	    // no selection: return all times
	    return new long[] { Long.MIN_VALUE, Long.MAX_VALUE };
	}

	return new long[] { minTimestamp, maxTimestamp };
    }

    public static int getSelectedTransmitState(String selection, String[] selectionArgs) {

	int result = -1;
	if (selection != null && selection.contains(DataPoint.TRANSMIT_STATE)) {

	    // preprocess the selection a bit
	    selection = selection.replaceAll(" = ", "=");
	    selection = selection.replaceAll("= ", "=");
	    selection = selection.replaceAll(" =", "=");
	    selection = selection.replaceAll(" != ", "!=");
	    selection = selection.replaceAll("!= ", "!=");
	    selection = selection.replaceAll(" !=", "!=");

	    int eqKeyStart = selection.indexOf(DataPoint.TRANSMIT_STATE + "=");
	    int neqKeyStart = selection.indexOf(DataPoint.TRANSMIT_STATE + "!=")
		    + (DataPoint.TRANSMIT_STATE + "!=").length();

	    if (-1 != eqKeyStart) {
		// selection contains "sensor_name='"
		int stateStart = eqKeyStart + (DataPoint.TRANSMIT_STATE + "=").length();
		int stateEnd = selection.indexOf(" ", stateStart);
		stateEnd = stateEnd == -1 ? selection.length() - 1 : stateEnd;
		String state = selection.substring(stateStart, stateEnd);
		if (state.equals("?")) {
		    throw new IllegalArgumentException(
			    "LocalStorage cannot handle queries with arguments array, sorry...");
		}
		// Log.v(TAG, "Query contains: " + DataPoint.TRANSMIT_STATE + " = " + state + "");

		result = state.equals("1") ? 1 : 0;

	    } else if (-1 != neqKeyStart) {
		// selection contains "sensor_name!='"
		int stateStart = neqKeyStart + (DataPoint.TRANSMIT_STATE + "!=").length();
		int stateEnd = selection.indexOf(" ", stateStart);
		stateEnd = stateEnd == -1 ? selection.length() - 1 : stateEnd;
		String notState = selection.substring(stateStart, stateEnd);
		if (notState.equals("?")) {
		    throw new IllegalArgumentException(
			    "LocalStorage cannot handle queries with arguments array, sorry...");
		}
		// Log.v(TAG, "Query contains: " + DataPoint.TRANSMIT_STATE + " != " + notState +
		// "");

		result = notState.equals("1") ? 0 : 1;

	    } else {
		throw new IllegalArgumentException("Parser cannot handle selection query: "
			+ selection);
	    }
	}

	return result;
    }

    private ParserUtils() {
	// class should not be instantiated
    }
}
