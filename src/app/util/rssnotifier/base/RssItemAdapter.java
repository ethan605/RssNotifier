package app.util.rssnotifier.base;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import android.app.ListActivity;
import android.content.Context;
import android.view.*;
import android.widget.*;
import app.util.rssnotifier.R;
import app.util.rssnotifier.database.DatabaseQuery;

public class RssItemAdapter extends ArrayAdapter<RssItem> {
	private Context context;
	private List<RssItem> rssList;
	private int trimmedTextSize;
	
	public RssItemAdapter(Context _context, int textViewResourceId, ArrayList<RssItem> objects) {
		super(_context, textViewResourceId, objects);
		context = _context;
		rssList = objects;
		DatabaseQuery dbQuery = new DatabaseQuery(context);
		dbQuery.openDB();
		trimmedTextSize = dbQuery.getRssSettings()[2];
		dbQuery.closeDB();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		
		if (row == null) {
			LayoutInflater layoutInflater = ((ListActivity) context).getLayoutInflater();
			row = layoutInflater.inflate(R.layout.rss_item_list, parent, false);
		}
		
		ImageView itemNew = (ImageView) row.findViewById(R.id.item_new);
		TextView title = (TextView) row.findViewById(R.id.item_title);
		TextView description = (TextView) row.findViewById(R.id.item_description);
		TextView provider = (TextView) row.findViewById(R.id.item_provider);
		TextView pubDate = (TextView) row.findViewById(R.id.item_pubdate);
		
		RssItem item = rssList.get(position);
		title.setText(item.getTitle());
		String desc = item.getDescription();
		if (desc.length() > trimmedTextSize) {
			desc = desc.substring(0, trimmedTextSize+1);
			desc += "...";
		}
		description.setText(desc);
		provider.setText(item.getProvider());
		Date date = new Date(Long.parseLong(item.getPubDate()));
		pubDate.setText(date.toLocaleString());
		
		if (item.getUpdated() == 1)
			itemNew.setVisibility(View.VISIBLE);
		else
			itemNew.setVisibility(View.GONE);
		
		return row;
	}
}