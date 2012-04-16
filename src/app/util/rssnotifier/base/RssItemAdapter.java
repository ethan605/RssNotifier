package app.util.rssnotifier.base;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import android.app.ListActivity;
import android.content.Context;
import android.view.*;
import android.widget.*;
import app.util.rssnotifier.R;

public class RssItemAdapter extends ArrayAdapter<RssItem> {
	public static int MAX_DESCRIPTION_LENGTH = 500;
	private Context context;
	private List<RssItem> rssList;
	
	public RssItemAdapter(Context _context, int textViewResourceId, ArrayList<RssItem> objects) {
		super(_context, textViewResourceId, objects);
		context = _context;
		rssList = objects;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		
		if (row == null) {
			LayoutInflater layoutInflater = ((ListActivity) context).getLayoutInflater();
			row = layoutInflater.inflate(R.layout.rss_item_list, parent, false);
		}
		
		TextView title = (TextView) row.findViewById(R.id.item_title);
		TextView description = (TextView) row.findViewById(R.id.item_description);
		TextView provider = (TextView) row.findViewById(R.id.item_provider);
		TextView pubDate = (TextView) row.findViewById(R.id.item_pubdate);
		
		RssItem item = rssList.get(position);
		title.setText(item.getTitle());
		String desc = item.getDescription();
		if (desc.length() > MAX_DESCRIPTION_LENGTH) {
			desc = desc.substring(0, MAX_DESCRIPTION_LENGTH+1);
			desc += "...";
		}
		description.setText(desc);
		provider.setText(item.getProvider());
		Date date = new Date(Long.parseLong(item.getPubDate()));
		pubDate.setText(date.toLocaleString());
		
		return row;
	}

}