package app.util.rssnotifier.base;


import java.util.ArrayList;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name="provider")
public class RssProvider {
	@Attribute(name="name")
	private String name;
	
	@ElementList(inline=true)
	private ArrayList<String> urlList;
	
	public RssProvider() {
		name = new String();
		urlList = new ArrayList<String>();
	}
	
	public RssProvider(String _name, String _link) {
		name = _name;
		urlList = new ArrayList<String>();
		urlList.add(_link);
	}
	
	public void addLink(String link) {
		urlList.add(link);
	}
	
	public String getName() {
		return name;
	}
	
	public String[] getLink() {
		return urlList.toArray(new String[urlList.size()]);
	}
}
