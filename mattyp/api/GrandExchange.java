package mattyp.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Grand Exchange class within custom API
 * @category RSBot API
 * @author Matt Provost
 * @version 1.0
 */
public class GrandExchange {
	private JSONObject item;
	public GrandExchange(int id) throws Exception {
		URL url = new URL("http://services.runescape.com/m=itemdb_rs/api/catalogue/detail.json?item="+id);
		URLConnection spoof = url.openConnection();
		spoof.setRequestProperty( "User-Agent", "Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0; H010818)" );
		BufferedReader in = new BufferedReader(new InputStreamReader(spoof.getInputStream()));
		item = new JSONObject(in.readLine()).getJSONObject("item");
	}
	/**
	 * Gets the item description.
	 * @return Description of the item
	 */
	public String getDescription() {
		try {
			return item.getString("description");
		} catch (JSONException e) {
			e.printStackTrace();
			return "";
		}
	}
	/**
	 * Gets the name of the item.
	 * @return The name of the item.
	 */
	public String getName() {
		try {
			return item.getString("name");
		} catch (JSONException e) {
			e.printStackTrace();
			return "";
		}
	}
	/**
	 * Gets the price of the item.
	 * @return Price of the item
	 */
	public int getPrice() {
		try {
			return item.getJSONObject("current").getInt("price");
		} catch (JSONException e) {
			e.printStackTrace();
			return 0;
		}
	}
	@Override()
	public String toString() {
		return this.getName();
	}
}
