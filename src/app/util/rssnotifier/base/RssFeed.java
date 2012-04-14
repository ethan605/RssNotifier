package app.util.rssnotifier.base;

import java.util.ArrayList;

public class RssFeed extends RssItem {
	private ArrayList<RssItem> rssList;
	
	public RssFeed() {
		super();
		rssList = new ArrayList<RssItem>();
	}
	
	public ArrayList<RssItem> getList() {
		return rssList;
	}
	
	public int addItem(RssItem rssItem) {
		rssList.add(rssItem);
		return rssList.size();
	}
	
	public void addList(RssFeed _rssFeed) {
		if (_rssFeed != null)
			rssList.addAll(_rssFeed.getList());
	}
	
	public int size() {
		return rssList.size();
	}
	
	@Override
	public String toString() {
		return getTitle();
	}
}
