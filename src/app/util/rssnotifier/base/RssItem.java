package app.util.rssnotifier.base;

public class RssItem {
	private String provider;
	private String title;
	private String description;
	private String link;
	private long pubdate;
	private int updated;
	
	public RssItem() {
		provider = "";
		title = "";
		description = "";
		link = "";
		pubdate = System.currentTimeMillis();
	}
	
	public RssItem(String _provider, String _title, String _description, String _link, long _pubdate, int _updated) {
		setProvider(_provider);
		setTitle(_title);
		setDescription(_description);
		setLink(_link);
		setPubDate(_pubdate);
		setUpdated(_updated);
	}
	
	public void setProvider(String _provider) {
		provider = _provider;
	}
	
	public void setTitle(String _title) {
		title = _title;
	}
	
	public void setDescription(String _description) {
		description = _description;
	}
	
	public void setLink(String _link) {
		link = _link;
	}
	
	public void setPubDate(long _pubdate) {
		pubdate = _pubdate;
	}
	
	public void setUpdated(int _updated) {
		updated = _updated;
	}
	
	public String getProvider() {
		return provider;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getLink() {
		return link;
	}
	
	public long getPubDate() {
		return pubdate;
	}
	
	public int getUpdated() {
		return updated;
	}
	
	@Override
	public String toString() {
		return "RssItem:\n" +
				title + "\n" +
				description + "\n" +
				link + "\n" +
				pubdate + "\n";
	}
}