package app.util.rssnotifier.base;

public class RssItem {
	private String provider;
	private String title;
	private String description;
	private String link;
	private String pubdate;
	
	public RssItem() {
		provider = "";
		title = "";
		description = "";
		link = "";
		pubdate = "";
	}
	
	public RssItem(String _provider, String _title, String _description, String _link, String _pubdate) {
		setProvider(_provider);
		setTitle(_title);
		setDescription(_description);
		setLink(_link);
		setPubDate(_pubdate);
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
	
	public void setPubDate(String _pubdate) {
		pubdate = _pubdate;
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
	
	public String getPubDate() {
		return pubdate;
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