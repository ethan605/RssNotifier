package app.util.rssnotifier.base;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.text.Html;
import android.util.Log;

public class XmlPullHandler {
	public static final String[] DATE_FORMATS = {"EEE, dd MMM yyyy k:mm:ss ZZZ",
												 "M/d/y K:m:s a"};
	
	private static final String TAG = "XmlPullHandler";
	private RssFeed rssFeed = null;
	private RssItem rssItem = null;
    private XmlPullParser parser;
	private String tagContent, provider;
    private boolean tagStart, tagHeader;
    public XmlPullHandler(String _provider, String _url) {
    	provider = _provider;
		try {
        	XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			parser = factory.newPullParser();
			
			String xmlContent = getXmlFromUrl(_url);
			if (xmlContent == null)
				return;
			else {
				if (xmlContent.indexOf('<') > 0)
					xmlContent = xmlContent.substring(xmlContent.indexOf('<'));
				parser.setInput(new StringReader(xmlContent));
				int eventType = parser.getEventType();
				while (eventType != XmlPullParser.END_DOCUMENT) {
					parseTag(eventType);
					eventType = parser.next();
				}				
			}
		} catch (XmlPullParserException e) {
		    e.printStackTrace();
		} catch (IOException e) {
		    e.printStackTrace();
		}
    }

    private void parseTag(int event){
        switch (event) {
        case XmlPullParser.START_DOCUMENT:
        	rssFeed = new RssFeed();
        	rssFeed.setTitle(provider);
        	tagHeader = true;
            break;
        case XmlPullParser.START_TAG:
        	tagStart = true;
        	tagContent = "";
        	if (parser.getName().equalsIgnoreCase("item")) {
        		rssItem = new RssItem();
        		rssItem.setProvider(provider);
        		if (tagHeader)
        			tagHeader = false;
        	}
            break;
        case XmlPullParser.END_TAG:
        	tagStart = false;
        	if (!tagHeader) {
        		tagContent = Html.fromHtml(tagContent).toString().replaceAll("\"", "\'");
        		if (parser.getName().equalsIgnoreCase("item"))
        			rssFeed.addItem(rssItem);
        		else if (parser.getName().equalsIgnoreCase("title"))
        			rssItem.setTitle(tagContent);
        		else if (parser.getName().equalsIgnoreCase("description"))
        			rssItem.setDescription(tagContent.replaceAll("\\uFFFC", "").replaceAll("\\n+"," ").replaceAll("\\s+", " "));
        		else if (parser.getName().equalsIgnoreCase("link"))
        			rssItem.setLink(tagContent);
        		else if (parser.getName().equalsIgnoreCase("pubdate")) {
        			for (String format : DATE_FORMATS) {
        				Date pubDate;
						pubDate = XmlPullHandler.parsePubDate(format, tagContent);
						if (pubDate != null) {
							rssItem.setPubDate(pubDate.toLocaleString());        				
							break;
						}
        			}
        		}
        	}
            break;
        case XmlPullParser.TEXT:
        	if (tagStart)
        		tagContent += parser.getText();
            break;
        default:
        	break;
        }
    }
    
    public static Date parsePubDate(String format, String pubdate) {
    	Date date = null;
    	SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
    	
    	try {
			date = sdf.parse(pubdate);
		} catch (ParseException e) {
//			e.printStackTrace();
		}
    	return date;
    }
    
    private static String getXmlFromUrl(String url) throws IllegalArgumentException, IOException {
    	HttpGet getRequest = new HttpGet(url);
    	DefaultHttpClient client = new DefaultHttpClient();
    	HttpResponse getResponse = client.execute(getRequest);
    	final int statusCode = getResponse.getStatusLine().getStatusCode();
    	if (statusCode != HttpStatus.SC_OK)
    		return null;
    	
    	HttpEntity getResponseEntity = getResponse.getEntity();
    	
    	if (getResponseEntity != null)
    		return EntityUtils.toString(getResponseEntity);
    	
    	return null;
    }
    
    public static boolean feedValidate(String url) {
    	boolean validate = false;
    	
    	try {
    		String xml = getXmlFromUrl(url);
    		if (xml != null)
    			validate = (xml.indexOf("<?xml") != -1 && xml.indexOf("<rss") != -1);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	return validate;
    }
    
    public RssFeed getFeed() {
    	return rssFeed;
    }
}