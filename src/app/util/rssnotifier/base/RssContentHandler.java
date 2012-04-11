package app.util.rssnotifier.base;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class RssContentHandler extends DefaultHandler {
	private static final int UNKNOWN_STATE = -1;
	private static final int TITLE_STATE = 0;
	private static final int DESCRIPTION_STATE = 1;
	private static final int LINK_STATE = 2;
	private static final int PUBDATE_STATE = 3;
	
	private int elState = UNKNOWN_STATE;
	private String fullCharacters;
	private boolean elStart = false;
	private RssItem rssItem;
	private RssFeed rssFeed;
	
	public RssContentHandler() {
	}
	
	public RssFeed getFeed() {
		return rssFeed;
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
			rssItem = new RssItem();
			elState = UNKNOWN_STATE;
		} else if (_localName.equalsIgnoreCase("title"))
			elState = TITLE_STATE;
		else if (_localName.equalsIgnoreCase("description"))
			elState = DESCRIPTION_STATE;
		else if (_localName.equalsIgnoreCase("link"))
			elState = LINK_STATE;
		else if (_localName.equalsIgnoreCase("pubdate"))
			elState = PUBDATE_STATE;
		else
			elState = UNKNOWN_STATE;
		
		elStart = true;
		fullCharacters = "";
	}
	
	@Override
	public void endElement(String _uri, String _localName, String _qName) {
		switch (elState) {
		case TITLE_STATE:
			rssItem.setTitle(fullCharacters);
			break;
		case DESCRIPTION_STATE:
			rssItem.setDescription(fullCharacters);
			break;
		case LINK_STATE:
			rssItem.setLink(fullCharacters);
			break;
		case PUBDATE_STATE:
			rssItem.setPubDate(fullCharacters);
			break;
		}
		elState = UNKNOWN_STATE;
		if (_localName.equalsIgnoreCase("item"))
			rssFeed.addItem(rssItem);
		
		elStart = false;
	}
	
	@Override
	public void characters(char[] _ch, int _start, int _length) {
		String strCharacters = new String(_ch, _start, _length);
		if (elStart)
			fullCharacters += strCharacters;
	}
}