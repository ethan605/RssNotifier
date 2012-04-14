package app.util.rssnotifier.base;

import java.util.ArrayList;

public class RssProvider {
	private String name;
	private ArrayList<String> linkList;
	
	public RssProvider() {
		name = new String();
		linkList = new ArrayList<String>();
	}
	
	public RssProvider(String _name, String _link) {
		name = _name;
		linkList = new ArrayList<String>();
		linkList.add(_link);
	}
	
	public void addLink(String link) {
		linkList.add(link);
	}
	
	public String getName() {
		return name;
	}
	
	public String[] getLink() {
		return linkList.toArray(new String[linkList.size()]);
	}
}
