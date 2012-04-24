package app.util.rssnotifier.base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

public class RssContentHandler {
	static final String TAG = "RssContentHandler";
	public static final String[] DATE_FORMATS = {"EEE, dd MMM yyyy k:mm:ss ZZZ",
												 "M/d/yyyy K:m:s a",
												 "yyyy-M-d H:m:s"};
	
	private RssFeed rssFeed = null;
	private RssItem rssItem = null;
    private XmlPullParser parser;
	private String tagContent, provider;
    private boolean tagStart, tagHeader, blankItem;
    public RssContentHandler(String _provider, String _url) {
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
        	tagHeader = true;
            break;
        case XmlPullParser.START_TAG:
        	tagStart = true;
        	tagContent = "";
        	if (parser.getName().equalsIgnoreCase("item")) {
        		rssItem = new RssItem();
        		rssItem.setProvider(provider);
        		blankItem = false;
        		if (tagHeader)
        			tagHeader = false;
        	}
            break;
        case XmlPullParser.END_TAG:
        	tagStart = false;
        	if (!tagHeader) {
        		tagContent = Html.fromHtml(tagContent).toString().replaceAll("\"", "\'");
        		if (parser.getName().equalsIgnoreCase("item")) {
        			rssItem.setUpdated(1);
        			rssFeed.addItem(rssItem);
        		}
        		else if (parser.getName().equalsIgnoreCase("title")) {
//        			Log.i(TAG, String.valueOf(tagContent.length()) + " " + tagContent);
        			rssItem.setTitle(tagContent);
        		}
        		else if (parser.getName().equalsIgnoreCase("description"))
        			rssItem.setDescription(tagContent.replaceAll("\\uFFFC", "").replaceAll("\\n+"," ").replaceAll("\\s+", " "));
        		else if (parser.getName().equalsIgnoreCase("link"))
        			rssItem.setLink(tagContent);
        		else if (parser.getName().equalsIgnoreCase("pubdate")) {
        			Date pubDate = null;
        			for (String format : DATE_FORMATS) {
        				pubDate = RssContentHandler.parsePubDate(format, tagContent);
						if (pubDate != null) {
							rssItem.setPubDate(pubDate.getTime());
							break;
						}
        			}
        			if (pubDate == null)
        				rssItem.setPubDate(System.currentTimeMillis());
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
    
    public static String getXmlFromUrl(String url) throws IllegalArgumentException, IOException {
		HttpGet getRequest = new HttpGet(url);
		DefaultHttpClient client = new DefaultHttpClient();
		HttpResponse getResponse = client.execute(getRequest);
		final int statusCode = getResponse.getStatusLine().getStatusCode();
		if (statusCode != HttpStatus.SC_OK)
			return null;
		
		HttpEntity entity = getResponse.getEntity();
		if (entity == null)
			return null;
		
		StringBuilder builder = new StringBuilder();
		InputStream content = entity.getContent();
		BufferedReader reader = new BufferedReader(new InputStreamReader(content));
		String line;
		while ((line = reader.readLine()) != null)
			builder.append(line);
		
		return builder.toString();
    }
    
    public static boolean feedValidate(String url) {
    	try {
    		String xml = getXmlFromUrl(url);
//    		Log.i(TAG, xml);
    		if (xml != null)
    			return (xml.indexOf("<rss") != -1);
		} catch (IllegalArgumentException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
    	
    	return false;
    }
    
    public RssFeed getFeed() {
    	return rssFeed;
    }
}