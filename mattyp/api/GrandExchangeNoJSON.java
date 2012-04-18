package mattyp.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GrandExchangeNoJSON {
	private String line = "";
	public GrandExchangeNoJSON(int id) throws Exception{
		URL url = new URL("http://services.runescape.com/m=itemdb_rs/api/catalogue/detail.json?item="+id);
		URLConnection spoof = url.openConnection();
		spoof.setRequestProperty( "User-Agent", "Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0; H010818)" );
		BufferedReader in = new BufferedReader(new InputStreamReader(spoof.getInputStream()));
		line = in.readLine();
	}
	/**
	 * Gets the price of the item.
	 * @return Price of the item
	 */
	public int getPrice() {
		String regex = "\"price\":(.*?)},\"";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(line);
		if(m.find()) {
			return new Integer(m.group(1));
		}
		return 0;
	}
}