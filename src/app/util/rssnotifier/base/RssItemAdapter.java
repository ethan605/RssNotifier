package app.util.rssnotifier.base;

import java.util.List;
import android.app.ListActivity;
import android.content.Context;
import android.view.*;
import android.widget.*;
import app.util.rssnotifier.R;

public class RssItemAdapter extends ArrayAdapter<RssItem> {
	public static int MAX_DESCRIPTION_LENGTH = 500;
	private Context mContext;
	private List<RssItem> mRssList;
	
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
		TextView itemDescription = (TextView) row.findViewById(R.id.item_description);
		TextView itemProvider = (TextView) row.findViewById(R.id.item_provider);
		TextView itemPubDate = (TextView) row.findViewById(R.id.item_pubdate);
		
		itemTitle.setText(mRssList.get(position).getTitle());
		String description = mRssList.get(position).getDescription();
		if (description.length() > MAX_DESCRIPTION_LENGTH) {
			description = description.substring(0, MAX_DESCRIPTION_LENGTH+1);
			description += "...";
		}
		itemDescription.setText(description);
		itemProvider.setText(mRssList.get(position).getProvider());
		itemPubDate.setText(mRssList.get(position).getPubDate());
		
		return row;
	}

}
