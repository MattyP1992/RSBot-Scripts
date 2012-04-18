import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;
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
import org.powerbot.game.api.methods.input.Keyboard;
import org.powerbot.game.api.methods.input.Mouse;
import org.powerbot.game.api.methods.interactive.NPCs;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.methods.node.SceneEntities;
import org.powerbot.game.api.methods.node.Menu;
import org.powerbot.game.api.methods.tab.Inventory;
import org.powerbot.game.api.methods.tab.Skills;
import org.powerbot.game.api.methods.widget.Camera;
import org.powerbot.game.api.util.Filter;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.util.Time;
import org.powerbot.game.api.util.Timer;
import org.powerbot.game.api.wrappers.Tile;
import org.powerbot.game.api.wrappers.interactive.NPC;
import org.powerbot.game.api.wrappers.map.TilePath;
import org.powerbot.game.api.wrappers.node.Item;
import org.powerbot.game.api.wrappers.node.SceneObject;
import org.powerbot.game.api.wrappers.widget.WidgetChild;
import org.powerbot.game.bot.event.listener.PaintListener;

@Manifest(
		name = "MattyP's Flax Spinner",
		authors = "MattyP",
		version = 3.0D,
		description = "A flax spinner",
		website = "http://www.facebook.com/MattypScripting",
		premium = false
		)

public class MattypFlaxSpinner extends ActiveScript implements PaintListener, MouseListener {

	private boolean ready = false;

	int completed = 0;
	int startingExp = 0;
	long xpGained = 0;
	int startingLvl = 0;
	String status = "";

	/* locations */
	int location;
	final static private int LUMBRIDGE = 0;
	final static private int SEERS = 1;

	/* Prices */
	int flaxPrice = 0;
	int bowstring = 0;

	/* Seers Path */
	final static private Tile[] PATH =  {
		new Tile(2717, 3472, 0), new Tile(2721, 3475, 0),
		new Tile(2723, 3480, 0), new Tile(2724, 3485, 0),
		new Tile(2725, 3490, 0) 
	};

	final static private int BANKER = 494;
	final static private int FLAX = 1779;
	final static private int WHEEL[] = {36970, 25824};
	final static private int BOW_STRING = 1777;
	final static private int DOOR = 25819;

	/* Stairs */
	final static private int LOWSTAIR = 36774;
	final static private int TOPSTAIR = 36775;
	final static private int SEERS_STAIR_TOP = 25939;
	final static private int SEERS_STAIR_BOTTOM = 25938;

	/* Tiles */
	final static private Tile AT_WHEEL[] = {new Tile(3209,3213, 1), new Tile(2715,2471, 1)};
	final static private Tile BOTTOM_STAIR = new Tile(3205,3209, 1);
	final static private Tile TOP_STAIR = new Tile(3205,3209, 2);
	final static private Tile IN_BANK[] = {new Tile(3208,3219, 2), new Tile(2725, 3491, 0)};

	@Override
	protected void setup() {
		GrandExchange flaxGE = new GrandExchange(FLAX);
		flaxPrice = flaxGE.getPrice();
		GrandExchange bowstringGE = new GrandExchange(BOW_STRING);
		bowstring = bowstringGE.getPrice();
		log.info("Profit per string: "+(bowstring-flaxPrice));
		if(Tabs.getCurrent() != Tabs.INVENTORY)
			Tabs.INVENTORY.open();
		Camera.setPitch(true);
		Camera.setPitch(91);
		startingExp = Skills.getExperience(Skills.CRAFTING);
		startingLvl = Skills.getLevel(Skills.CRAFTING);
		Spin spinWheel = new Spin();
		Walk walk = new Walk();
		Bank bank = new Bank();
		provide(new Strategy(spinWheel,spinWheel));
		provide(new Strategy(bank,bank));
		provide(new Strategy(walk,walk));
	}

	private class Spin extends Strategy implements Task {

		private int sleep;

		@Override
		public void run() {
			status = "spinning";
			while(!Inventory.selectItem(FLAX))
				Time.sleep(500);
			Time.sleep(Random.nextInt(500, 1000));
			while(!SceneEntities.getNearest(WHEEL).interact("Use"))
				Time.sleep(500);
			int loc = (location == SEERS ? 2000 : 1400);
			sleep(loc,loc+100);
			if(Widgets.get(905).getChild(14).click(true)) {
				sleep = Random.nextInt(0, 49500);
				Time.sleep(sleep);
				antiban();
				Time.sleep(49500-sleep);
			}
		}

		@Override
		public boolean validate() {
			return invGetCount(FLAX) != 0 
					&& Players.getLocal().getAnimation() == -1
					&& locationOnScreen(WHEEL[location]) 
					&& ready;
		}

	}

	private class Walk extends Strategy implements Task {

		@Override
		public void run() {
			if(!Walking.isRunEnabled())
				Walking.setRun(true);
			if(invGetCount(FLAX) == 0 
					&& !Players.getLocal().getLocation().equals(IN_BANK[location])) {
				status = "walking to bank";
				switch(location) {
				case LUMBRIDGE: 
					if(Players.getLocal().getPlane()==1)
						if(climbStair())
							Time.sleep(2500);
					toBank();
					break;
				case SEERS:
					while(Players.getLocal().getPlane()==1) {
						descendStair();
						sleep(500,1000);
					}
					while(locationOnScreen(DOOR)) {
						SceneEntities.getNearest(DOOR).click(true);
						sleep(500,1000);
					}
					toBank();
					break;
				}
			}
			else if(invGetCount(FLAX) != 0 
					&& !Players.getLocal().getLocation().equals(AT_WHEEL[location])) {
				status = "walking to wheel";
				switch(location) {
				case LUMBRIDGE:
					if(Players.getLocal().getPlane()==2)
						if(descendStair())
							Time.sleep(2500);
					toWheel();
					break;
				case SEERS:
					if(Players.getLocal().getPlane()==0) {
						toWheel();
						while(locationOnScreen(DOOR)) {
							SceneEntities.getNearest(DOOR).click(true);
							sleep(500,1000);
						}
						while(locationOnScreen(SEERS_STAIR_BOTTOM) 
								&& !inSeersBank()) {
							climbStair();
							sleep(500,1000);
						}
					}
				}
			}

		}

		@Override
		public boolean validate() {
			if(invGetCount(FLAX) == 0)
				return !npcOnScreen(BANKER) && ready;
				
			else if(invGetCount(FLAX) != 0)
				return !locationOnScreen(WHEEL[location]) && ready;
			
			return true;
		}

		public boolean climbStair() {
			switch(location) {
			case LUMBRIDGE:
				if(Players.getLocal().getPlane()==1) {
					Walking.walk(BOTTOM_STAIR);
					Time.sleep(Random.nextInt(3500, 4500));
					return SceneEntities.getNearest(LOWSTAIR).interact("Climb-up");
				}break;
			case SEERS:
				if(Players.getLocal().getPlane()==0 
					&& SceneEntities.getNearest(SEERS_STAIR_BOTTOM).isOnScreen())
					return SceneEntities.getNearest(SEERS_STAIR_BOTTOM).click(true);
				break;
			}
			return false;
		}

		public boolean toBank() {
			switch(location) {
			case LUMBRIDGE:
				if(Players.getLocal().getPlane()==2) {
					Walking.walk(IN_BANK[location]);
					Time.sleep(Random.nextInt(4500, 5500));
					return true;
				} break;
			case SEERS:
				if(Players.getLocal().getPlane()==0) {
					walkPath(PATH);
				}break;
			}
			return false;
		}

		public boolean descendStair() {
			switch(location) {
			case LUMBRIDGE:
				if(Players.getLocal().getPlane()==2) {
					Walking.walk(TOP_STAIR);
					Time.sleep(Random.nextInt(4500, 5500));
					return SceneEntities.getNearest(TOPSTAIR).interact("Climb-down");
				}
				break;
			case SEERS:
				if(Players.getLocal().getPlane()==1)
					return SceneEntities.getNearest(SEERS_STAIR_TOP).click(true);
				break;
			}
			return false;
		}

		public boolean toWheel() {
			TilePath path = new TilePath(PATH);
			switch(location) {
			case LUMBRIDGE:
				if(Players.getLocal().getPlane()==1) {
					Walking.walk(AT_WHEEL[location]);
					Time.sleep(Random.nextInt(3500, 4500));
					return true;
				}break;
			case SEERS:
				if(Players.getLocal().getPlane()==0) {
					walkPath(path.reverse().toArray());
				}break;
			}
			return false;
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

	private class Bank extends Strategy implements Task {

		@Override
		public void run() {
			status = "banking";
			openBank();
			if(!inventoryIsEmpty())
				while(!Widgets.get(762).getChild(34).click(true))
					Time.sleep(600);
			Time.sleep(1500);
			if(getCount(FLAX) > 0)
				while(!withdraw(FLAX,0))
					Time.sleep(600);
			else {
				log.info("Out of flax");
				log.info(completed + " Bow strings made");
				stop();
			}
			closeBank();
			status = "walking to wheel";
		}

		@Override
		public boolean validate() {
			return invGetCount(FLAX) == 0
				&& npcOnScreen(BANKER)
				&& Players.getLocal().getAnimation() == -1
				&& ready;
		}

		public boolean withdraw(int id, int amount) {
			WidgetChild bank = Widgets.get(762).getChild(95);
			int baseX = bank.getAbsoluteX(), baseY = bank.getAbsoluteY();
			for (Item item : getItems()) 
				if (item.getId() == id) {
					WidgetChild child = item.getWidgetChild();
					while(!Mouse.click(child.getRelativeX() + baseX + (child.getWidth()/2),
							child.getRelativeY() + baseY + (child.getHeight()/2), false))
						sleep(600,1000);
					switch(amount) {
					case -1:
						return Menu.select("Withdraw-All but one");
					case 0:
						return Menu.select("Withdraw-All");
					case 1:  
						return Menu.select("Withdraw-1");
					case 5:
						return Menu.select("Withdraw-5");
					case 10:
						return Menu.select("Withdraw-10");
					default:
						for(String action : Menu.getActions())
							if(action.equals("Withdraw-"+amount))
								return Menu.select("Withdraw-"+amount);
						if(Menu.select("Withdraw-X")) {
							sleep(1400,1600);
							Keyboard.sendText("" + amount, true);
							return true;
						}
					}
				}
			return false;
		}

		public void openBank() {
			while (!bankIsOpen()) {
				NPCs.getNearest(BANKER).interact("Bank");
				Time.sleep(1500);
			}
		}

		public boolean bankIsOpen() {
			return Widgets.get(762, 1).isOnScreen();
		}

		public void closeBank() {
			while (bankIsOpen()){
				Time.sleep(700);
				Widgets.get(762).getChild(45).interact("Close");
			}
		}

		public Item[] getItems() {
			List<Item> items = new LinkedList<Item>();
			if (Widgets.get(762).getChild(95).validate()) {
				for (WidgetChild item : Widgets.get(762).getChild(95).getChildren()) {
					if (item != null && item.getChildId() != -1) {
						items.add(new Item(item));
					}
				}
			}
			return items.toArray(new Item[items.size()]);
		}

		public int getCount(int id) {
			for (Item item : getItems()) {
				if (item.getId() == id) {
					return item.getStackSize();
				}
			}
			return 0;
		}
	}
	
	public boolean locationOnScreen(int id) {
		for(SceneObject loc1 : SceneEntities.getLoaded())
			if(loc1.getId()==id)
				return SceneEntities.getNearest(id).isOnScreen();
		return false;
	}
	
	public boolean npcOnScreen(int id) {
		for(NPC npc : NPCs.getLoaded())
			if(npc.getId()==id)
				return NPCs.getNearest(id).isOnScreen();
		return false;
	}
	
	private class GrandExchange {
		private String line = "";

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

	public int invGetCount(final int id) {
		return Inventory.getCount(new Filter<Item>() {
			public boolean accept(Item i) {
				return i.getId() == id;
			}
		});
	}

	public boolean inventoryIsEmpty() {
		return Inventory.getCount() == 0;
	}

	public boolean inSeersBank() {
		Tile player = Players.getLocal().getLocation();
		if(player.getX() >= 2721 
				&& player.getX() <= 2730
				&& player.getY() >= 3487
				&& player.getY() <= 3498)
			return true;
		return false;
	}

	public void antiban() {
		switch(Random.nextInt(0, 5)) {
		case 0: 
			Camera.setAngle(Random.nextInt(0, 360));
			Time.sleep(3500);
			break;
		case 1:
			int x = Widgets.get(320).getChild(59).getAbsoluteX();
			int y = Widgets.get(320).getChild(59).getAbsoluteY();
			Tabs.STATS.open();
			Time.sleep(1750);
			Mouse.move(x, y);
			Time.sleep(1750);
			Tabs.INVENTORY.open();
			break;
		case 2:
			int tabIndex = 4;
			while(tabIndex==4)
				tabIndex = Random.nextInt(0, 17);
			Tabs tab = null;
			for (Tabs c : Tabs.values())
				if(c.getIndex()==tabIndex) {
					tab = c;
					break;
				}
			tab.open();
			Time.sleep(3500);
			Tabs.INVENTORY.open();
			break;
		default: // Skip sometimes
		Time.sleep(3500);
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
		final Color color2 = new Color(255, 255, 255);
		final Font font1 = new Font("Arial", 1, 26);
		final Font font2 = new Font("Arial", 1, 18);
		final Font font3 = new Font("Arial", 1, 12);

		final long runtime = elapsed.getElapsed();
		long oldXP = xpGained;

		xpGained = Skills.getExperience(Skills.CRAFTING) - startingExp;
		long xpPerHour = (long)((3600000.0 / runtime) * xpGained);

		int currentLvl = Skills.getLevel(Skills.CRAFTING);
		completed += (xpGained - oldXP)/15;
		
		long profit = (long)(completed*(bowstring-flaxPrice));

		long stringsHr = (long)((3600000.0 / runtime) * completed);
		long profitHr = (long)(stringsHr * (bowstring - flaxPrice));

		if(!ready){
			g.setColor(color1);
			g.fillRect(4, 2, 512, 334);
			g.setFont(font1);
			g.setColor(color2);
			g.drawString("Please select location", 115, 83);
			g.fillRect(179, 112, 100, 25);
			g.fillRect(179, 153, 100, 25);
			g.setFont(font2);
			g.setColor(color1);
			g.drawString("Lumbridge", 180, 133);
			g.drawString("Seers", 201, 176);
		}

		else {
			g.setColor(color1);
			g.drawRect(2, 219, 517, 119);
			g.setFont(font1);

			g.drawString("MattyP's Flax Spinner", 5, 250);

			g.setFont(font3);

			g.drawString("Time running: " + Time.format(runtime), 4, 275);
			g.drawString("Status: " + status, 4, 296);
			g.drawString("Strings spun: " + format(completed), 5, 316);
			g.drawString("Strings/hour: " + format(stringsHr), 5, 335);
			g.drawString("XP Gained: " + format(xpGained), 329, 234);
			g.drawString("XP/hour: " + format(xpPerHour), 329, 252);
			g.drawString("XP to level: " + Skills.getExperienceToLevel(Skills.CRAFTING, currentLvl+1) , 329, 270);
			g.drawString("Level: " + currentLvl + " (" + (currentLvl - startingLvl) +" gained)", 329, 288);
			g.drawString("Profit: " + format(profit), 329, 306);
			g.drawString("Profit/Hour: " + format(profitHr), 329, 324);
		}
	}

	public void mouseClicked(MouseEvent e) {
		Rectangle lumby = new Rectangle(179, 112, 100, 25);
		Rectangle seers = new Rectangle(179, 153, 100, 25);
		if(lumby.contains(e.getPoint())) {
			location = LUMBRIDGE;
			ready = true;
		}else if (seers.contains(e.getPoint())) {
			location = SEERS;
			ready = true;
		}
		e.consume();
	}
	@Override
	public void mouseEntered(MouseEvent e) {
	}
	@Override
	public void mouseExited(MouseEvent e) {
	}
	@Override
	public void mousePressed(MouseEvent e) {
	}
	@Override
	public void mouseReleased(MouseEvent e) {
	}
}
