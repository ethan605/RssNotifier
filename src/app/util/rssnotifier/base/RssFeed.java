package app.util.rssnotifier.base;

import java.util.ArrayList;

public class RssFeed extends RssItem {
	private ArrayList<RssItem> rssList;
	
	public RssFeed() {
		super();
		rssList = new ArrayList<RssItem>();
	}
	
	public ArrayList<RssItem> getList() {
		return this.rssList;
	}
	
	public int addItem(RssItem rssItem) {
		this.rssList.add(rssItem);
		return this.rssList.size();
	}
	
	public void addList(RssFeed _rssFeed) {
		this.rssList.addAll(_rssFeed.getList());
	}
	
	@Override
	public String toString() {
		return getTitle();
	}
}
