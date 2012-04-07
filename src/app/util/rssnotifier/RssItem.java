package app.util.rssnotifier;

public class RssItem {
	private String strTitle;
	private String strDescription;
	private String strLink;
	private String strPubDate;
	
	public RssItem() {
		this.strTitle = "";
		this.strDescription = "";
		this.strLink = "";
		this.strPubDate = "";
	}
	
	public RssItem(String _strTitle, String _strDescription, String _strLink, String _strPubDate) {
		this.setTitle(_strTitle);
		this.setDescription(_strDescription);
		this.setLink(_strLink);
		this.setPubDate(_strPubDate);
	}
	
	public void setTitle(String _strTitle) {
		this.strTitle = _strTitle;
	}
	
	public void setDescription(String _strDescription) {
		this.strDescription = _strDescription;
	}
	
	public void setLink(String _strLink) {
		this.strLink = _strLink;
	}
	
	public void setPubDate(String _strPubDate) {
		this.strPubDate = _strPubDate;
	}
	
	public String getTitle() {
		return this.strTitle;
	}
	
	public String getDescription() {
		return this.strDescription;
	}
	
	public String getLink() {
		return this.strLink;
	}
	
	public String getPubDate() {
		return this.strPubDate;
	}
	
	@Override
	public String toString() {
		return this.strTitle;
	}
}