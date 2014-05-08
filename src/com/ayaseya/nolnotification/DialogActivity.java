package com.ayaseya.nolnotification;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;

public class DialogActivity extends Activity {
	private String[] update = { "aaa", "bbb", "ccc", "ddd", "eee", "fff", "ggg", "hhh", "iii" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// タイトルを非表示にします。
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.dialog_nol_notification);

		//ダイアログの縦横幅を最大にします。
		getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		ListView updateListView = (ListView) findViewById(R.id.updateListView);

		ArrayAdapter<String> adapter =
				new ArrayAdapter<String>(this, R.layout.simple_list_item_layout, update);

		updateListView.setAdapter(adapter);

		// NotificationがクリックされActivityが呼び出された時に
		// Notificationを非表示にする処理
		NotificationManager mNotificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(GcmIntentService.NOTIFICATION_ID);
	}

}
