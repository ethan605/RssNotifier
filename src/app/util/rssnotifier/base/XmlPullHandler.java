package app.util.rssnotifier.base;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class XmlPullHandler {
	private RssFeed rssFeed = null;
	private RssItem rssItem = null;
    private XmlPullParser parser;
	private String tagContent, provider;
    private boolean tagStart, tagHeader;
    public XmlPullHandler(String _provider, String _url) {
    	provider = _provider;
		try {
			/*HttpClient client = new DefaultHttpClient();
        	HttpGet request = new HttpGet(_url);
        	HttpResponse responce = client.execute(request);
        	BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(responce.getEntity().getContent()));

        	StringBuffer sb = new StringBuffer("");
        	String line = "";
        	String newLine = System.getProperty("line.separator");
        	while ((line = bufferedReader.readLine()) != null)
        		sb.append(line + newLine);
        	bufferedReader.close();
        	String xmlStr = sb.toString();*/
			
        	XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			parser = factory.newPullParser();
//			parser.setInput(new StringReader(xmlStr));
			InputStream inputStream = new URL(_url).openStream();
			parser.setInput(inputStream, "UTF-8");
			int eventType = parser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				parseTag(eventType);
				eventType = parser.next();
			}
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
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
        		if (parser.getName().equalsIgnoreCase("item"))
        			rssFeed.addItem(rssItem);
        		else if (parser.getName().equalsIgnoreCase("title"))
        			rssItem.setTitle(tagContent);
        		else if (parser.getName().equalsIgnoreCase("description"))
        			rssItem.setDescription(tagContent);
        		else if (parser.getName().equalsIgnoreCase("link"))
        			rssItem.setLink(tagContent);
        		else if (parser.getName().equalsIgnoreCase("pubdate"))
        			rssItem.setPubDate(tagContent);
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
    
    public RssFeed getFeed() {
    	return rssFeed;
    }
}