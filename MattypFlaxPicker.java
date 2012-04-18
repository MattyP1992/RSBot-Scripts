import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.powerbot.concurrent.Task;
import org.powerbot.concurrent.strategy.Strategy;
import org.powerbot.game.api.ActiveScript;
import org.powerbot.game.api.Manifest;
import org.powerbot.game.api.methods.Calculations;
import org.powerbot.game.api.methods.Tabs;
import org.powerbot.game.api.methods.Walking;
import org.powerbot.game.api.methods.Widgets;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.methods.node.SceneEntities;
import org.powerbot.game.api.methods.tab.Inventory;
import org.powerbot.game.api.methods.widget.Camera;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.util.Time;
import org.powerbot.game.api.util.Timer;
import org.powerbot.game.api.wrappers.Tile;
import org.powerbot.game.api.wrappers.map.TilePath;
import org.powerbot.game.api.wrappers.node.SceneObject;
import org.powerbot.game.bot.event.MessageEvent;
import org.powerbot.game.bot.event.listener.MessageListener;
import org.powerbot.game.bot.event.listener.PaintListener;

@Manifest(
		name = "MattyP's Flax Picker",
		authors = "MattyP",
		version = 2.3D,
		description = "A flax picker",
		website = "http://www.facebook.com/MattypScripting",
		premium = false
		)

public class MattypFlaxPicker extends ActiveScript implements PaintListener, MessageListener {

	int picked = 0;
	int flaxPrice = 0;
	String status = "";
	TilePath path;

	private final static int FLAX = 1779;
	private final static int FLAX_LOCATION = 2646;
	private final static int BANK_BOOTH = 25808;

	/* Tiles */
	public final Tile[] PATH = {
			new Tile(2725, 3490, 0), new Tile(2725, 3485, 0),
			new Tile(2725, 3480, 0), new Tile(2726, 3475, 0),
			new Tile(2726, 3470, 0), new Tile(2727, 3465, 0),
			new Tile(2727, 3460, 0), new Tile(2729, 3455, 0),
			new Tile(2730, 3450, 0), new Tile(2733, 3446, 0),
			new Tile(2737, 3443, 0) 
	};

	@Override
	protected void setup() {
		GrandExchange flaxGE = new GrandExchange(FLAX);
		flaxPrice = flaxGE.getPrice();
		log.info("Flax price: "+flaxPrice);
		Camera.setPitch(true);
		Camera.setPitch(91);
		if(Tabs.getCurrent() != Tabs.INVENTORY)
			Tabs.INVENTORY.open();
		PickFlax flax = new PickFlax();
		Bank bank = new Bank();
		Walk walking = new Walk();
		provide(new Strategy(flax,flax));
		provide(new Strategy(bank,bank));
		provide(new Strategy(walking,walking));

	}

	private class PickFlax extends Strategy implements Task {

		@Override
		public void run() {
			status = "Picking flax";
			while(!inventoryIsFull()) {
				if(Tabs.getCurrent() != Tabs.INVENTORY)
					Tabs.INVENTORY.open();
				if(Players.getLocal().getAnimation()!=-1 
						&& Players.getLocal().isMoving())
					sleep(400,500);
					SceneEntities.getNearest(FLAX_LOCATION).click(true);
					antiban();
					sleep(400,500);
			}
		}

		@Override
		public boolean validate() {
			if(!inventoryIsFull())
				return locationOnScreen(FLAX_LOCATION);
			return false;
		}
	}

	private class Bank extends Strategy implements Task {
		@Override
		public void run() {
			status = "Banking";
			if(Players.getLocal().isMoving())
				sleep(500,1000);
			openBank();
			while(!inventoryIsEmpty()) {
				if(Widgets.get(762).getChild(34).validate())
					Widgets.get(762).getChild(34).click(true);
				sleep(200,300);
			}
			closeBank();
		}

		@Override
		public boolean validate() {
			if(!inventoryIsEmpty())
				return locationOnScreen(BANK_BOOTH);
			return false;
		}

		public void openBank() {
			while (!bankIsOpen()) {
				SceneEntities.getNearest(BANK_BOOTH).click(true);
				sleep(500,1000);
			}
		}

		public void closeBank() {
			while (bankIsOpen()){
				sleep(100,200);
				Widgets.get(762).getChild(45).interact("Close");
			}
		}

		public boolean bankIsOpen() {
			return Widgets.get(762, 1).isOnScreen();
		}
	}

	private class Walk extends Strategy implements Task {

		@Override
		public void run() {

			path = new TilePath(PATH);
			if(!Walking.isRunEnabled() && Walking.getEnergy() > Random.nextInt(70, 80))
				Walking.setRun(true);

			if(!inventoryIsFull()) {
				status = "Walking to flax";
				walkPath(PATH);
			}
			else if(inventoryIsFull()) {
				status = "Walking to bank";
				walkPath(path.reverse().toArray());
			}
		}

		@Override
		public boolean validate() {
			if(inventoryIsEmpty())
				return !locationOnScreen(FLAX_LOCATION);

			else if(inventoryIsFull())
				return !locationOnScreen(BANK_BOOTH);
			return true;
		}
		// Thank you Konnected for this walking algo
		private boolean walkPath(final Tile[] path) {
			boolean a = false;
			final Tile next = getNext(path);
			final Tile start = path[0];
			final Tile dest = Walking.getDestination();
			final Tile myTile = Players.getLocal().getLocation();
			if (dest.getX() == -1 || Calculations.distance(myTile, dest) < 6
					|| Calculations.distance(next, Walking.getDestination()) > 3) {

				if (!Walking.walk(next)) {
					if (Walking.walk(start)) {
						Time.sleep(500);
						a = true;
					} else {
						if (dividePath(start))
							Time.sleep(500);

					}
				} else {
					Time.sleep(500);
					a = true;
				}
			}
			return a;
		}

		private Tile getNext(final Tile[] tiles) {
			for (int i = tiles.length - 1; i >= 0; --i) {
				if (Calculations.distance(Players.getLocal().getLocation(),
						tiles[i]) < 15) {
					return tiles[i];
				}
			}
			return null;
		}

		private boolean dividePath(final Tile t) {
			final Tile mine = Players.getLocal().getLocation();
			final int x = t.getX(), y = t.getY(), z = t.getPlane(), myX = mine
					.getX(), myY = mine.getY();
			final Tile newT = new Tile((int) (x + myX) / 2, (int) (y + myY) / 2, z);
			if (Walking.walk(newT)) {
				return true;
			}
			return dividePath(newT);
		}
	}

	private class GrandExchange {
		private String line;

		public GrandExchange(int id) {
			try {
				getItem(id);
			}catch(Exception e){
				log.info("Cannot connect to GE");
			}
		}

		private void getItem(int id) throws Exception {


			URL url = new URL("http://services.runescape.com/m=itemdb_rs/api/catalogue/detail.json?item="+id);
			URLConnection spoof = url.openConnection();

			spoof.setRequestProperty( "User-Agent", "Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0; H010818)" );
			BufferedReader in = new BufferedReader(new InputStreamReader(spoof.getInputStream()));
			line = in.readLine();
		}

		private int getPrice() {
			String regex = "\"price\":(.*?)},\"";
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(line);
			if(m.find()) {
				return new Integer(m.group(1));
			}
			return 0;
		}
	}

	public void sleep(int low, int high) {
		Time.sleep(Random.nextInt(low, high));
	}

	public boolean locationOnScreen(int id) {
		for(SceneObject loc1 : SceneEntities.getLoaded())
			if(loc1.getId()==id)
				return SceneEntities.getNearest(id).isOnScreen();
		return false;
	}

	public boolean inventoryIsFull() {
		return Inventory.getCount() == 28;
	}

	public boolean inventoryIsEmpty() {
		return Inventory.getCount() == 0;
	}

	public void antiban() {
		switch(Random.nextInt(0, 7)) {
		case 0: 
			Camera.setAngle(Random.nextInt(0, 360));
			break;
		}
	}

	public String format(long value) {
		DecimalFormat df2 = new DecimalFormat( "#,##0.0");
		if(value/10000000 >= 1)
			return df2.format((double)value/10000000) +"M";
		else if(value/1000 >= 1)
			return df2.format((double)value/1000)+"K";
		return ""+value;
	}

	Timer elapsed = new Timer(0);

	@Override
	public void onRepaint(Graphics g) {
		final Color color1 = new Color(0, 0, 0);
		final Font font1 = new Font("Arial", 1, 26);
		final Font font2 = new Font("Arial", 1, 12);

		final long runtime = elapsed.getElapsed();

		long flaxHr = (long)((3600000.0 / runtime) * picked);
		long profitHr = (long)(flaxHr*flaxPrice);

		g.setColor(color1);
		g.drawRect(2, 219, 517, 119);
		g.setFont(font1);

		g.drawString("MattyP's Flax Picker", 5, 250);

		g.setFont(font2);

		g.drawString("Time running: " + Time.format(runtime), 5, 275);
		g.drawString("Status: " + status, 5, 296);
		g.drawString("Flax Picked: " + format(picked), 329, 234);
		g.drawString("Flax/Hour: " + format(flaxHr), 329, 256);
		g.drawString("Profit: " + format(flaxPrice*picked), 329, 278);
		g.drawString("Profit/Hour: " + format(profitHr), 329, 300);

	}

	@Override
	public void messageReceived(MessageEvent msg) {
		String message = msg.getMessage();
		if(message.contains("pick some flax")) 
			picked++;
	}
}
