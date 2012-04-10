package app.util.rssnotifier;

import java.util.List;
import android.app.ListActivity;
import android.content.Context;
import android.view.*;
import android.widget.*;

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
		
		TextView itemTitle = (TextView) row.findViewById(R.id.item_title);
		itemTitle.setText(mRssList.get(position).getTitle());
		TextView itemDescription = (TextView) row.findViewById(R.id.item_description);
		itemDescription.setText(mRssList.get(position).getDescription().replaceAll("\\<.*?>",""));
		TextView itemPubDate = (TextView) row.findViewById(R.id.item_pubdate);
		itemPubDate.setText(mRssList.get(position).getPubDate());
		
		if (position%2 == 0){
			itemTitle.setTextColor(0xff000000);
			itemTitle.setBackgroundColor(0xffffffff);
			itemDescription.setTextColor(0xff000000);
			itemDescription.setBackgroundColor(0xffffffff);
			itemPubDate.setTextColor(0xff000000);
			itemPubDate.setBackgroundColor(0xffffffff);
		} else {
			itemTitle.setTextColor(0xffffffff);
			itemTitle.setBackgroundColor(0xff000000);
			itemDescription.setTextColor(0xffffffff);
			itemDescription.setBackgroundColor(0xff000000);
			itemPubDate.setTextColor(0xffffffff);
			itemPubDate.setBackgroundColor(0xff000000);
		}
		
		return row;
	}

}
