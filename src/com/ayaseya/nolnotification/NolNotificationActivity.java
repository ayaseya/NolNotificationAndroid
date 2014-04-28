package com.ayaseya.nolnotification;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class NolNotificationActivity extends Activity {
	public static final String EXTRA_MESSAGE = "message";
	public static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

	/**
	 * Substitute you own sender ID here. This is the project number you got
	 * from the API Console, as described in "Getting Started."
	 */
	String SENDER_ID = "977505068557";

	/**
	 * Tag used on log messages.
	 */
	static final String TAG = "GCM Demo";

	TextView mDisplay;
	GoogleCloudMessaging gcm;
	AtomicInteger msgId = new AtomicInteger();
	Context context;

	String regid;
	private AsyncTask<Void, Void, Void> mRegisterTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nol_notification);

		Log.v(TAG, "++++++++++++++++++++++++++++++++++++++++++++++++++");

		mDisplay = (TextView) findViewById(R.id.display);
		context = getApplicationContext();

		NotificationManager mNotificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(GcmIntentService.NOTIFICATION_ID);

		// Check device for Play Services APK. If check succeeds, proceed with
		// GCM registration.
		if (checkPlayServices()) {
			gcm = GoogleCloudMessaging.getInstance(this);
			Log.v(TAG, "onCreate()_getInstance()");
			regid = getRegistrationId(context);
			Log.v(TAG, "onCreate()_getRegistrationId()_regid = " + regid);
			if (regid.isEmpty()) {
				registerInBackground();
				Log.v(TAG, "registerInBackground()");
			}
		} else {
			Log.i(TAG, "No valid Google Play Services APK found.");
		}

		findViewById(R.id.registration).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						Log.v(TAG, "登録");

					}
				});

		findViewById(R.id.unregister).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.v(TAG, "解除");

			}
		});

	}

	@Override
	protected void onResume() {
		super.onResume();
		// Check device for Play Services APK.
		checkPlayServices();
	}

	/**
	 * Check the device to make sure it has the Google Play Services APK. If it
	 * doesn't, display a dialog that allows users to download the APK from the
	 * Google Play Store or enable it in the device's system settings.
	 */
	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
						PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Log.i(TAG, "This device is not supported.");
				finish();
			}
			// Log.v(TAG, "checkPlayServices()=false");
			return false;
		}
		// Log.v(TAG, "checkPlayServices()=true");
		return true;
	}

	/**
	 * Stores the registration ID and the app versionCode in the application's
	 * {@code SharedPreferences}.
	 * 
	 * @param context
	 *            application's context.
	 * @param regId
	 *            registration ID
	 */
	private void storeRegistrationId(Context context, String regId) {
		final SharedPreferences prefs = getGcmPreferences(context);
		int appVersion = getAppVersion(context);
		Log.i(TAG, "Saving regId on app version " + appVersion);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_REG_ID, regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		editor.commit();
		Log.v(TAG, "storeRegistrationId()");
	}

	/**
	 * Gets the current registration ID for application on GCM service, if there
	 * is one.
	 * <p>
	 * If result is empty, the app needs to register.
	 * 
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	private String getRegistrationId(Context context) {
		final SharedPreferences prefs = getGcmPreferences(context);
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if (registrationId.isEmpty()) {
			Log.i(TAG, "Registration not found.");
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION,
				Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion) {
			Log.i(TAG, "App version changed.");
			return "";
		}
		// Log.v(TAG, "getRegistrationId()_registrationId=" + registrationId);
		return registrationId;
	}

	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and the app versionCode in the application's
	 * shared preferences.
	 */
	private void registerInBackground() {
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				try {
					if (gcm == null) {
						gcm = GoogleCloudMessaging.getInstance(context);
					}
					regid = gcm.register(SENDER_ID);
					msg = "Device registered, registration ID=" + regid;

					// You should send the registration ID to your server over
					// HTTP, so it
					// can use GCM/HTTP or CCS to send messages to your app.
					sendRegistrationIdToBackend();

					// For this demo: we don't need to send it because the
					// device will send
					// upstream messages to a server that echo back the message
					// using the
					// 'from' address in the message.

					// Persist the regID - no need to register again.
					storeRegistrationId(context, regid);
				} catch (IOException ex) {
					msg = "Error :" + ex.getMessage();
					// If there is an error, don't just keep trying to register.
					// Require the user to click a button again, or perform
					// exponential back-off.
				}
				Log.v(TAG, "doInBackground_msg=" + msg);
				return msg;
			}

			@Override
			protected void onPostExecute(String msg) {
				mDisplay.append(msg + "\n");

			}
		}.execute(null, null, null);
	}

	// Send an upstream message.
	public void onClick(final View view) {

		if (view == findViewById(R.id.send)) {
			new AsyncTask<Void, Void, String>() {
				@Override
				protected String doInBackground(Void... params) {
					String msg = "";
					try {
						Bundle data = new Bundle();
						data.putString("my_message", "Hello World");
						data.putString("my_action",
								"com.google.android.gcm.demo.app.ECHO_NOW");
						String id = Integer.toString(msgId.incrementAndGet());
						gcm.send(SENDER_ID + "@gcm.googleapis.com", id, data);
						msg = "Sent message";
					} catch (IOException ex) {
						msg = "Error :" + ex.getMessage();
					}
					Log.v(TAG, "onClick()_msg=" + msg);
					return msg;
				}

				@Override
				protected void onPostExecute(String msg) {
					mDisplay.append(msg + "\n");
				}
			}.execute(null, null, null);
		} else if (view == findViewById(R.id.clear)) {
			mDisplay.setText("");
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private SharedPreferences getGcmPreferences(Context context) {
		// This sample app persists the registration ID in shared preferences,
		// but
		// how you store the regID in your app is up to you.
		return getSharedPreferences(
				NolNotificationActivity.class.getSimpleName(),
				Context.MODE_PRIVATE);
	}

	/**
	 * Sends the registration ID to your server over HTTP, so it can use
	 * GCM/HTTP or CCS to send messages to your app. Not needed for this demo
	 * since the device sends upstream messages to a server that echoes back the
	 * message using the 'from' address in the message.
	 */
	private void sendRegistrationIdToBackend() {
		// Your implementation here.
		Log.v(TAG, "sendRegistrationIdToBackend()");

		final Context context = this;
		// 非同期処理で別スレッドに登録処理を任せます。（GUIスレッドではHTTP通信はできないため）
		mRegisterTask = new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				ServerUtilities.register(context, regid);
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {

				mRegisterTask = null;
			}

		};
		mRegisterTask.execute(null, null, null);
	}

}
