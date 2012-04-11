package app.util.rssnotifier.base;

import java.util.ArrayList;

public class RssProviderList {
	private ArrayList<String> nameList;
	private ArrayList<String> linkList;
	
	public RssProviderList() {
		this.nameList = new ArrayList<String>();
		this.linkList = new ArrayList<String>();
	}
	
	public void addProvider(String name, String link) {
		this.nameList.add(name);
		this.linkList.add(link);
	}
	
	public String[] getProviderNames() {
		return nameList.toArray(new String[nameList.size()]);
	}
	
	public String[] getProviderLinks() {
		return linkList.toArray(new String[linkList.size()]);
	}
}
