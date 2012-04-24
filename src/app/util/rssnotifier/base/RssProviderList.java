package app.util.rssnotifier.base;

import java.util.ArrayList;

public class RssProviderList {
	private ArrayList<String> nameList;
	private ArrayList<RssProvider> providerList;
	
	public RssProviderList() {
		nameList = new ArrayList<String>();
		providerList = new ArrayList<RssProvider>();
	}
	
	public RssProviderList(ArrayList<RssProvider> providers) {
		nameList = new ArrayList<String>();
		providerList = providers;
		for (int i = 0; i < providers.size(); i++)
			nameList.add(providers.get(i).getName());
	}
	
	public void addProvider(String name, String link) {
		int nameIndex = nameList.indexOf(name);
		if (nameIndex != -1)
			providerList.get(nameIndex).addLink(link);
		else {
			nameList.add(name);
			providerList.add(new RssProvider(name, link));
		}
	}
	
	public String[] getProviderNames() {
		return nameList.toArray(new String[nameList.size()]);
	}
	
	public String[] getProviderLinks(String name) {
		int index = nameList.indexOf(name);
		if (index == -1)
			return null;
		return providerList.get(nameList.indexOf(name)).getLink();
	}
	
	public int length() {
		return nameList.size();
	}
}
