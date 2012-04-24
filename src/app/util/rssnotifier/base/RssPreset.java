package app.util.rssnotifier.base;


import java.util.ArrayList;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name="preset")
public class RssPreset {
	@ElementList(inline=true)
	ArrayList<RssProvider> provider;
	
	public RssPreset() {
		provider = new ArrayList<RssProvider>();
	}
	
	public ArrayList<RssProvider> getProvider() {
		return provider;
	}
	
	public void setProvider(ArrayList<RssProvider> _provider) {
		provider = _provider;
	}
}
