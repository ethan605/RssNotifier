package app.util.rssnotifier.base;

import java.util.List;
import android.app.ListActivity;
import android.content.Context;
import android.view.*;
import android.widget.*;
import app.util.rssnotifier.R;

public class RssItemAdapter extends ArrayAdapter<RssItem> {
	Context mContext;
	List<RssItem> mRssList;
	
	public RssItemAdapter(Context context, int textViewResourceId, List<RssItem> objects) {
		super(context, textViewResourceId, objects);
		mContext = context;
		mRssList = objects;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		
		if (row == null) {
			LayoutInflater layoutInflater = ((ListActivity) mContext).getLayoutInflater();
			row = layoutInflater.inflate(R.layout.rss_item_list, parent, false);
		}
		
		LinearLayout itemList = (LinearLayout) row.findViewById(R.id.item_list);
//		itemList.setBackgroundDrawable(d);
		TextView itemTitle = (TextView) row.findViewById(R.id.item_title);
		itemTitle.setText(mRssList.get(position).getTitle());
		TextView itemDescription = (TextView) row.findViewById(R.id.item_description);
		itemDescription.setText(mRssList.get(position).getDescription().replaceAll("\\<.*?>",""));
		TextView itemProvider = (TextView) row.findViewById(R.id.item_provider);
		itemProvider.setText(mRssList.get(position).getProvider());
		TextView itemPubDate = (TextView) row.findViewById(R.id.item_pubdate);
		itemPubDate.setText(mRssList.get(position).getPubDate());
		
		if (position % 2 == 0) {
			itemList.setBackgroundColor(0xff000000);
			itemTitle.setTextColor(0xffffffff);
			itemDescription.setTextColor(0xffffffff);
			itemProvider.setTextColor(0xffffffff);
			itemPubDate.setTextColor(0xffffffff);
		} else {
			itemList.setBackgroundColor(0xffffffff);
			itemTitle.setTextColor(0xff000000);
			itemDescription.setTextColor(0xff000000);
			itemProvider.setTextColor(0xff000000);
			itemPubDate.setTextColor(0xff000000);
		}
		
		return row;
	}

}
