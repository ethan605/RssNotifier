package app.util.rssnotifier;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class RssContentHandler extends DefaultHandler {
	private static final int UNKNOWN_STATE = -1;
	private static final int ELEMENT_START = 0;
	private static final int TITLE_END = 1;
	private static final int DESCRIPTION_END = 2;
	private static final int LINK_END = 3;
	private static final int PUBDATE_END = 4;
	
	private int iState = UNKNOWN_STATE;
	private String fullCharacters;
	private boolean itemFound = false;
	private RssItem rssItem;
	private RssFeed rssFeed;
	
	public RssContentHandler() {
	}
	
	public RssFeed getFeed() {
		return this.rssFeed;
	}
	
	@Override
	public void startDocument() {
		rssItem = new RssItem();
		rssFeed = new RssFeed();
	}
	
	@Override
	public void endDocument() {
	}
	
	@Override
	public void startElement(String _uri, String _localName, String _qName, Attributes _attributes) {
		if (_localName.equalsIgnoreCase("item")) {
			itemFound = true;
			rssItem = new RssItem();
			this.iState = UNKNOWN_STATE;
		} else
			this.iState = ELEMENT_START;
		fullCharacters = "";
	}
	
	@Override
	public void endElement(String _uri, String _localName, String _qName) {
		if (_localName.equalsIgnoreCase("item"))
			this.rssFeed.addItem(this.rssItem);
		else if (_localName.equalsIgnoreCase("title"))
			this.iState = TITLE_END;
		else if (_localName.equalsIgnoreCase("description"))
			this.iState = DESCRIPTION_END;
		else if (_localName.equalsIgnoreCase("link"))
			this.iState = LINK_END;
		else if (_localName.equalsIgnoreCase("pubDate"))
			this.iState = PUBDATE_END;
		else
			this.iState = UNKNOWN_STATE;
	}
	
	@Override
	public void characters(char[] _ch, int _start, int _length) {
		String strCharacters = new String(_ch, _start, _length);
		if (this.iState == ELEMENT_START)
			fullCharacters += strCharacters;
		else {
			if (!itemFound) {
				switch (this.iState) {
				case TITLE_END:
					this.rssFeed.setTitle(fullCharacters);
					break;
				case DESCRIPTION_END:
					this.rssFeed.setDescription(fullCharacters);
					break;
				case LINK_END:
					this.rssFeed.setLink(fullCharacters);
					break;
				case PUBDATE_END:
					this.rssFeed.setPubDate(fullCharacters);
					break;
				}
			} else {
				switch (this.iState) {
				case TITLE_END:
					this.rssItem.setTitle(fullCharacters);
					break;
				case DESCRIPTION_END:
					this.rssItem.setDescription(fullCharacters);
					break;
				case LINK_END:
					this.rssItem.setLink(fullCharacters);
					break;
				case PUBDATE_END:
					this.rssItem.setPubDate(fullCharacters);
					break;
				}
			}
			this.iState = UNKNOWN_STATE;			
		}
	}
}