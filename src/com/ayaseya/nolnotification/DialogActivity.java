package com.ayaseya.nolnotification;

import static com.ayaseya.nolnotification.CommonUtilities.*;

import java.util.ArrayList;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;

public class DialogActivity extends Activity {

	private ArrayList<String> title = new ArrayList<String>();
	private ArrayList<String> url = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// タイトルを非表示にします。
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.dialog_nol_notification);

		//ダイアログの縦横幅を最大にします。
		getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

		Intent intent = getIntent();
		if (intent != null) {
			Bundle extras = intent.getExtras();

			Intent update = null;
			if (extras != null) {
				update = extras.getParcelable("UPDATE");
			}

			if (update != null) {

				extras = update.getExtras();

				int index = Integer.parseInt((String) extras.get("INDEX"));

				Log.v(TAG, "index=" + index);

				if (index != 0) {
					for (int i = 0; i < index; i++) {
						title.add((String) extras.get("TITLE" + (i + 1)));
						url.add((String) extras.get("URL" + (i + 1)));

						Log.v(TAG, title.get(i));
						Log.v(TAG, url.get(i));
					}
				}
			}
		}

		title.add("1.xxxxx");
		url.add("1.xxxxx");
		ListView updateListView = (ListView) findViewById(R.id.updateListView);

		ArrayAdapter<String> adapter =
				new ArrayAdapter<String>(this, R.layout.simple_list_item_layout, title);

		updateListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.v(TAG, "position=" + position);

				Uri uri = Uri.parse(url.get(position));
				Intent browser = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(browser);
			}
		});

		updateListView.setAdapter(adapter);

		// NotificationがクリックされActivityが呼び出された時に
		// Notificationを非表示にする処理
		NotificationManager mNotificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(GcmIntentService.NOTIFICATION_ID);
	}

}
