package app.util.rssnotifier;

import java.util.List;
import java.util.Vector;

public class RssFeed extends RssItem {
	private List<RssItem> rssList;
	
	public RssFeed() {
		super();
		rssList = new Vector<RssItem>(0);
	}
	
	public RssFeed(String _strTitle, String _strDescription, String _strLink,
			String _strPubDate) {
		super(_strTitle, _strDescription, _strLink, _strPubDate);
		rssList = new Vector<RssItem>(0);
	}
	
	public List<RssItem> getList() {
		return this.rssList;
	}
	
	public int addItem(RssItem rssItem) {
		this.rssList.add(rssItem);
		return this.rssList.size();
	}
	
	@Override
	public String toString() {
		return this.getTitle();
	}
}
