package nl.sense_os.service.commonsense;

import java.util.HashMap;

import nl.sense_os.service.constants.SenseDataTypes;
import nl.sense_os.service.constants.SensePrefs;
import nl.sense_os.service.constants.SensePrefs.Main;
import nl.sense_os.service.constants.SensorData.SensorNames;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.nfc.NfcManager;
import android.os.Build;

/**
 * Class that verifies that all the phone's sensors are known at CommonSense.
 */
public class DefaultSensorRegistrator extends SensorRegistrator {

	public DefaultSensorRegistrator(Context context) {
		super(context);
	}

	// private static final String TAG = "Sensor Registration";

	/**
	 * Checks the IDs for light, camera light, noise, pressure sensors.
	 * 
	 * @param deviceUuid
	 * @param deviceType
	 * 
	 * @return true if all sensor IDs are found or created
	 */
	@TargetApi(9)
	private boolean checkAmbienceSensors(String deviceType, String deviceUuid) {

		// preallocate objects
		SensorManager sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		Sensor sensor;
		String name, displayName, description, dataType, value;
		HashMap<String, Object> dataFields = new HashMap<String, Object>();
		boolean success = true;

		// match light sensor
		sensor = sm.getDefaultSensor(Sensor.TYPE_LIGHT);
		if (null != sensor) {
			name = SensorNames.LIGHT;
			displayName = SensorNames.LIGHT;
			description = sensor.getName();
			dataType = SenseDataTypes.JSON;
			dataFields.clear();
			dataFields.put("lux", 0);
			value = new JSONObject(dataFields).toString();
			success &= checkSensor(name, displayName, dataType, description, value, deviceType,
					deviceUuid);
		} else {
			// Log.v(TAG, "No light sensor present!");
		}

		// match camera light sensor (only for Android < 4.0)
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			// multiple camera support starting from Android 2.3
			if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD) {
				for (int camera_id = 0; camera_id < Camera.getNumberOfCameras(); ++camera_id) {
					name = SensorNames.CAMERA_LIGHT;
					displayName = "Camera Light";
					description = "camera " + camera_id + " average luminance";
					dataType = SenseDataTypes.JSON;
					dataFields.clear();
					dataFields.put("lux", 0);
					value = new JSONObject(dataFields).toString();
					success &= checkSensor(name, displayName, dataType, description, value,
							deviceType, deviceUuid);
				}
			}
		}

		// match noise sensor
		name = SensorNames.NOISE;
		displayName = "noise";
		description = SensorNames.NOISE;
		dataType = SenseDataTypes.FLOAT;
		value = "0.0";
		success &= checkSensor(name, displayName, dataType, description, value, deviceType,
				deviceUuid);

		// match noise spectrum
		name = SensorNames.AUDIO_SPECTRUM;
		displayName = "audio spectrum";
		description = "audio spectrum (dB)";
		dataType = SenseDataTypes.JSON;
		dataFields.clear();
		for (int i = 1; i < 23; i++) {
			dataFields.put(i + " kHz", 0);
		}
		value = new JSONObject(dataFields).toString();
		success &= checkSensor(name, displayName, dataType, description, value, deviceType,
				deviceUuid);

		// match pressure sensor
		sensor = sm.getDefaultSensor(Sensor.TYPE_PRESSURE);
		if (null != sensor) {
			name = SensorNames.PRESSURE;
			displayName = "";
			description = sensor.getName();
			dataType = SenseDataTypes.JSON;
			dataFields.clear();
			dataFields.put("millibar", 0);
			value = new JSONObject(dataFields).toString();
			success &= checkSensor(name, displayName, dataType, description, value, deviceType,
					deviceUuid);
		} else {
			// Log.v(TAG, "No pressure sensor present!");
		}

		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			sensor = sm.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
			if (null != sensor) {
				name = SensorNames.AMBIENT_TEMPERATURE;
				displayName = "ambient temperature";
				description = sensor.getName();
				dataType = SenseDataTypes.JSON;
				dataFields.clear();
				dataFields.put("celsius", 0);
				value = new JSONObject(dataFields).toString();
				success &= checkSensor(name, displayName, dataType, description, value, deviceType,
						deviceUuid);
			} else {
				// Log.v(TAG, "No ambient temperature sensor present!");
			}
		}

		// match loudness sensor
		name = SensorNames.LOUDNESS;
		displayName = name;
		description = SensorNames.LOUDNESS;
		dataType = SenseDataTypes.FLOAT;
		value = "0.0";
		success &= checkSensor(name, displayName, dataType, description, value, deviceType,
				deviceUuid);

		return success;
	}

	/**
	 * Checks the IDs for Bluetooth and Wi-Fi scan sensors.
	 * 
	 * @param deviceUuid
	 * @param deviceType
	 * 
	 * @return true if all sensor IDs are found or created
	 */
	@TargetApi(10)
	private boolean checkDeviceScanSensors(String deviceType, String deviceUuid) {

		// preallocate objects
		String name, displayName, description, dataType, value;
		HashMap<String, Object> dataFields = new HashMap<String, Object>();
		boolean success = true;

		// match Bluetooth scan
		name = SensorNames.BLUETOOTH_DISCOVERY;
		displayName = "bluetooth scan";
		description = SensorNames.BLUETOOTH_DISCOVERY;
		dataType = SenseDataTypes.JSON;
		dataFields.clear();
		dataFields.put("name", "string");
		dataFields.put("address", "string");
		dataFields.put("rssi", 0);
		value = new JSONObject(dataFields).toString();
		success &= checkSensor(name, displayName, dataType, description, value, deviceType,
				deviceUuid);

		// match Bluetooth neighbours count
		name = SensorNames.BLUETOOTH_NEIGHBOURS_COUNT;
		displayName = "bluetooth neighbours count";
		description = SensorNames.BLUETOOTH_NEIGHBOURS_COUNT;
		dataType = SenseDataTypes.INT;
		value = "0";
		success &= checkSensor(name, displayName, dataType, description, value, deviceType,
				deviceUuid);

		// match Wi-Fi scan
		name = SensorNames.WIFI_SCAN;
		displayName = "wi-fi scan";
		description = SensorNames.WIFI_SCAN;
		dataType = SenseDataTypes.JSON;
		dataFields.clear();
		dataFields.put("ssid", "string");
		dataFields.put("bssid", "string");
		dataFields.put("frequency", 0);
		dataFields.put("rssi", 0);
		dataFields.put("capabilities", "string");
		value = new JSONObject(dataFields).toString();
		success &= checkSensor(name, displayName, dataType, description, value, deviceType,
				deviceUuid);

		// match NFC scan
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
			NfcManager nm = (NfcManager) context.getSystemService(Context.NFC_SERVICE);
			if (null != nm.getDefaultAdapter()) {
				name = SensorNames.NFC_SCAN;
				displayName = "nfc scan";
				description = SensorNames.NFC_SCAN;
				dataType = SenseDataTypes.JSON;
				dataFields.clear();
				dataFields.put("id", "string");
				dataFields.put("technology", "string");
				dataFields.put("message", "string");
				value = new JSONObject(dataFields).toString();
				success &= checkSensor(name, displayName, dataType, description, value, deviceType,
						deviceUuid);
			}
		}

		return success;
	}

	/**
	 * Checks the ID for the location sensor
	 * 
	 * @param deviceUuid
	 * @param deviceType
	 * 
	 * @return true if the sensor ID is found or created
	 */
	private boolean checkLocationSensors(String deviceType, String deviceUuid) {
		boolean succes = true;
		// match location sensor
		String name = SensorNames.LOCATION;
		String displayName = SensorNames.LOCATION;
		String description = SensorNames.LOCATION;
		String dataType = SenseDataTypes.JSON;
		HashMap<String, Object> dataFields = new HashMap<String, Object>();
		dataFields.put("longitude", 1.0);
		dataFields.put("latitude", 1.0);
		dataFields.put("altitude", 1.0);
		dataFields.put("accuracy", 1.0);
		dataFields.put("speed", 1.0);
		dataFields.put("bearing", 1.0f);
		dataFields.put("provider", "string");
		String value = new JSONObject(dataFields).toString();
		succes &= checkSensor(name, displayName, dataType, description, value, deviceType,
				deviceUuid);

		name = SensorNames.TRAVELED_DISTANCE_1H;
		displayName = SensorNames.TRAVELED_DISTANCE_1H;
		description = SensorNames.TRAVELED_DISTANCE_1H;
		dataType = SenseDataTypes.FLOAT;
		value = "0.0";
		succes &= checkSensor(name, displayName, dataType, description, value, deviceType,
				deviceUuid);

		name = SensorNames.TRAVELED_DISTANCE_24H;
		displayName = SensorNames.TRAVELED_DISTANCE_24H;
		description = SensorNames.TRAVELED_DISTANCE_24H;
		dataType = SenseDataTypes.FLOAT;
		value = "0.0";
		succes &= checkSensor(name, displayName, dataType, description, value, deviceType,
				deviceUuid);

		return succes;
	}

	/**
	 * Checks the IDs for the accelerometer, orientation, fall detector, motion energy sensors.
	 * 
	 * @param deviceUuid
	 * @param deviceType
	 * 
	 * @return true if the sensor ID is found or created
	 */
	@SuppressWarnings("deprecation")
	private boolean checkMotionSensors(String deviceType, String deviceUuid) {

		// preallocate objects
		SensorManager sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		Sensor sensor;
		String name, displayName, description, dataType, value;
		HashMap<String, Object> jsonFields = new HashMap<String, Object>();
		boolean success = true;

		sensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		if (null != sensor) {

			// match accelerometer
			name = SensorNames.ACCELEROMETER;
			displayName = "acceleration";
			description = sensor.getName();
			dataType = SenseDataTypes.JSON;
			jsonFields.clear();
			jsonFields.put("x-axis", 1.0);
			jsonFields.put("y-axis", 1.0);
			jsonFields.put("z-axis", 1.0);
			value = new JSONObject(jsonFields).toString();
			success &= checkSensor(name, displayName, dataType, description, value, deviceType,
					deviceUuid);

			// match accelerometer (epi)
			SharedPreferences mainPrefs = context.getSharedPreferences(SensePrefs.MAIN_PREFS,
					Context.MODE_PRIVATE);
			if (mainPrefs.getBoolean(Main.Motion.EPIMODE, false)) {
				name = SensorNames.ACCELEROMETER_EPI;
				displayName = "acceleration (epi-mode)";
				description = sensor.getName();
				dataType = SenseDataTypes.JSON;
				jsonFields.clear();
				jsonFields.put("interval", 0);
				jsonFields.put("data", new JSONArray());
				value = new JSONObject(jsonFields).toString();
				success &= checkSensor(name, displayName, dataType, description, value, deviceType,
						deviceUuid);
			}

			// match motion energy
			if (mainPrefs.getBoolean(Main.Motion.MOTION_ENERGY, false)) {
				name = SensorNames.MOTION_ENERGY;
				displayName = SensorNames.MOTION_ENERGY;
				description = SensorNames.MOTION_ENERGY;
				dataType = SenseDataTypes.FLOAT;
				value = "1.0";
				success &= checkSensor(name, displayName, dataType, description, value, deviceType,
						deviceUuid);
			}

			// match fall detector
			if (mainPrefs.getBoolean(Main.Motion.FALL_DETECT, false)) {
				name = SensorNames.FALL_DETECTOR;
				displayName = "fall";
				description = "human fall";
				dataType = SenseDataTypes.BOOL;
				value = "true";
				success &= checkSensor(name, displayName, dataType, description, value, deviceType,
						deviceUuid);
			}

			// match fall detector
			if (mainPrefs.getBoolean(Main.Motion.FALL_DETECT_DEMO, false)) {
				name = SensorNames.FALL_DETECTOR;
				displayName = "fall (demo)";
				description = "demo fall";
				dataType = SenseDataTypes.BOOL;
				value = "true";
				success &= checkSensor(name, displayName, dataType, description, value, deviceType,
						deviceUuid);
			}

		} else {
			// Log.v(TAG, "No accelerometer present!");
		}

		// match linear acceleration sensor
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {

			sensor = sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
			if (null != sensor) {
				name = SensorNames.LIN_ACCELERATION;
				displayName = SensorNames.LIN_ACCELERATION;
				description = sensor.getName();
				dataType = SenseDataTypes.JSON;
				jsonFields.clear();
				jsonFields.put("x-axis", 1.0);
				jsonFields.put("y-axis", 1.0);
				jsonFields.put("z-axis", 1.0);
				value = new JSONObject(jsonFields).toString();
				success &= checkSensor(name, displayName, dataType, description, value, deviceType,
						deviceUuid);

			} else {
				// Log.v(TAG, "No linear acceleration sensor present!");
			}
		}

		// match orientation
		sensor = sm.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		if (null != sensor) {
			name = SensorNames.ORIENT;
			displayName = SensorNames.ORIENT;
			description = sensor.getName();
			dataType = SenseDataTypes.JSON;
			jsonFields.clear();
			jsonFields.put("azimuth", 1.0);
			jsonFields.put("pitch", 1.0);
			jsonFields.put("roll", 1.0);
			value = new JSONObject(jsonFields).toString();
			success &= checkSensor(name, displayName, dataType, description, value, deviceType,
					deviceUuid);

		} else {
			// Log.v(TAG, "No orientation sensor present!");
		}

		// match gyroscope
		sensor = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		if (null != sensor) {
			name = SensorNames.GYRO;
			displayName = "rotation rate";
			description = sensor.getName();
			dataType = SenseDataTypes.JSON;
			jsonFields.clear();
			jsonFields.put("azimuth rate", 1.0);
			jsonFields.put("pitch rate", 1.0);
			jsonFields.put("roll rate", 1.0);
			value = new JSONObject(jsonFields).toString();
			success &= checkSensor(name, displayName, dataType, description, value, deviceType,
					deviceUuid);

		} else {
			// Log.v(TAG, "No gyroscope present!");
		}

		return success;
	}

	/**
	 * Checks IDs for the battery, screen activity, proximity, cal state, connection type, service
	 * state, signal strength sensors.
	 * 
	 * @param deviceUuid
	 * @param deviceType
	 * 
	 * @return true if all of the sensor IDs were found or created
	 */
	private boolean checkPhoneStateSensors(String deviceType, String deviceUuid) {

		// preallocate objects
		SensorManager sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		Sensor sensor;
		String name, displayName, description, dataType, value;
		HashMap<String, Object> dataFields = new HashMap<String, Object>();
		boolean success = true;

		// match battery sensor
		name = SensorNames.BATTERY_SENSOR;
		displayName = "battery state";
		description = SensorNames.BATTERY_SENSOR;
		dataType = SenseDataTypes.JSON;
		dataFields.clear();
		dataFields.put("status", "string");
		dataFields.put("level", "string");
		value = new JSONObject(dataFields).toString();
		success &= checkSensor(name, displayName, dataType, description, value, deviceType,
				deviceUuid);

		// match screen activity
		name = SensorNames.SCREEN_ACTIVITY;
		displayName = SensorNames.SCREEN_ACTIVITY;
		description = SensorNames.SCREEN_ACTIVITY;
		dataType = SenseDataTypes.JSON;
		dataFields.clear();
		dataFields.put("screen", "string");
		value = new JSONObject(dataFields).toString();
		success &= checkSensor(name, displayName, dataType, description, value, deviceType,
				deviceUuid);

		// match proximity
		sensor = sm.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		if (null != sensor) {
			name = SensorNames.PROXIMITY;
			displayName = SensorNames.PROXIMITY;
			description = sensor.getName();
			dataType = SenseDataTypes.JSON;
			dataFields.clear();
			dataFields.put("distance", 1.0);
			value = new JSONObject(dataFields).toString();
			success &= checkSensor(name, displayName, dataType, description, value, deviceType,
					deviceUuid);
		} else {
			// Log.v(TAG, "No proximity sensor present!");
		}

		// match call state
		name = SensorNames.CALL_STATE;
		displayName = SensorNames.CALL_STATE;
		description = SensorNames.CALL_STATE;
		dataType = SenseDataTypes.JSON;
		dataFields.clear();
		dataFields.put("state", "string");
		dataFields.put("incomingNumber", "string");
		value = new JSONObject(dataFields).toString();
		success &= checkSensor(name, displayName, dataType, description, value, deviceType,
				deviceUuid);

		// match connection type
		name = SensorNames.CONN_TYPE;
		displayName = "network type";
		description = SensorNames.CONN_TYPE;
		dataType = SenseDataTypes.STRING;
		value = "string";
		success &= checkSensor(name, displayName, dataType, description, value, deviceType,
				deviceUuid);

		// match ip address
		name = SensorNames.IP_ADDRESS;
		displayName = SensorNames.IP_ADDRESS;
		description = SensorNames.IP_ADDRESS;
		dataType = SenseDataTypes.STRING;
		value = "string";
		success &= checkSensor(name, displayName, dataType, description, value, deviceType,
				deviceUuid);

		// match messages waiting sensor
		name = SensorNames.UNREAD_MSG;
		displayName = "message waiting";
		description = SensorNames.UNREAD_MSG;
		dataType = SenseDataTypes.BOOL;
		value = "true";
		success &= checkSensor(name, displayName, dataType, description, value, deviceType,
				deviceUuid);

		// match data connection
		name = SensorNames.DATA_CONN;
		displayName = SensorNames.DATA_CONN;
		description = SensorNames.DATA_CONN;
		dataType = SenseDataTypes.STRING;
		value = "string";
		success &= checkSensor(name, displayName, dataType, description, value, deviceType,
				deviceUuid);

		// match service state
		name = SensorNames.SERVICE_STATE;
		displayName = SensorNames.SERVICE_STATE;
		description = SensorNames.SERVICE_STATE;
		dataType = SenseDataTypes.JSON;
		dataFields.clear();
		dataFields.put("state", "string");
		dataFields.put("phone number", "string");
		dataFields.put("manualSet", true);
		value = new JSONObject(dataFields).toString();
		success &= checkSensor(name, displayName, dataType, description, value, deviceType,
				deviceUuid);

		// match signal strength
		name = SensorNames.SIGNAL_STRENGTH;
		displayName = SensorNames.SIGNAL_STRENGTH;
		description = SensorNames.SIGNAL_STRENGTH;
		dataType = SenseDataTypes.JSON;
		dataFields.clear();
		dataFields.put("GSM signal strength", 1);
		dataFields.put("GSM bit error rate", 1);
		value = new JSONObject(dataFields).toString();
		success &= checkSensor(name, displayName, dataType, description, value, deviceType,
				deviceUuid);

		return success;
	}

	private boolean checkDebugSensors(String deviceType, String deviceUuid) {
		String name, displayName, description, dataType;

		boolean success = true;

		SharedPreferences mainPrefs = context.getSharedPreferences(SensePrefs.MAIN_PREFS,
				Context.MODE_PRIVATE);
		if (mainPrefs.getBoolean(Main.Advanced.LOCATION_FEEDBACK, false)) {
			// match myrianode sensor
			name = SensorNames.ATTACHED_TO_MYRIANODE;
			displayName = SensorNames.ATTACHED_TO_MYRIANODE;
			description = "Sense Logger";
			dataType = SenseDataTypes.STRING;
			success &= checkSensor(name, displayName, dataType, description, "string", deviceType,
					deviceUuid);
		}

		return success;
	}

	@Override
	public synchronized boolean verifySensorIds(String deviceType, String deviceUuid) {
		boolean success = checkAmbienceSensors(deviceType, deviceUuid);
		success &= checkDeviceScanSensors(deviceType, deviceUuid);
		success &= checkLocationSensors(deviceType, deviceUuid);
		success &= checkMotionSensors(deviceType, deviceUuid);
		success &= checkPhoneStateSensors(deviceType, deviceUuid);
		success &= checkDebugSensors(deviceType, deviceUuid);
		return success;
	}
}