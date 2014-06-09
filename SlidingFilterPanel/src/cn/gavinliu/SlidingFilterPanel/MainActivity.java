package cn.gavinliu.SlidingFilterPanel;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {
	private SlidingFilterPanel mPanle;
	private ListView mListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setupListView();
		mPanle = (SlidingFilterPanel) findViewById(R.id.panel);
	}

	private void setupListView() {
		mListView = (ListView) findViewById(R.id.listView);
		SampleAdapter adapter = new SampleAdapter();
		mListView.setAdapter(adapter);
	}

	public class SampleAdapter extends BaseAdapter {

		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(getApplication()).inflate(R.layout.item_simple, null);
			}
			TextView title = (TextView) convertView
					.findViewById(R.id.row_title);
			title.setText(position + ". HelloWorld");
			return convertView;
		}

		@Override
		public int getCount() {
			return 30;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_show:
			mPanle.show();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

}
