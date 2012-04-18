import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.powerbot.concurrent.Task;
import org.powerbot.concurrent.strategy.Strategy;
import org.powerbot.game.api.ActiveScript;
import org.powerbot.game.api.Manifest;
import org.powerbot.game.api.methods.Tabs;
import org.powerbot.game.api.methods.Widgets;
import org.powerbot.game.api.methods.input.Keyboard;
import org.powerbot.game.api.methods.input.Mouse;
import org.powerbot.game.api.methods.interactive.Players;
import org.powerbot.game.api.methods.node.Menu;
import org.powerbot.game.api.methods.node.SceneEntities;
import org.powerbot.game.api.methods.tab.Inventory;
import org.powerbot.game.api.methods.tab.Skills;
import org.powerbot.game.api.methods.widget.Camera;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.util.Time;
import org.powerbot.game.api.util.Timer;
import org.powerbot.game.api.wrappers.node.Item;
import org.powerbot.game.api.wrappers.widget.WidgetChild;
import org.powerbot.game.bot.event.MessageEvent;
import org.powerbot.game.bot.event.listener.MessageListener;
import org.powerbot.game.bot.event.listener.PaintListener;

@Manifest(
        name = "MattyP's Ultimate Fletcher",
        authors = "MattyP",
        version = 2.2D,
        description = "A simple fletcher",
        website = "http://www.facebook.com/MattypScripting",
        premium = false
        )

public class AutoFletcher extends ActiveScript implements PaintListener, MessageListener {
	
	private int startingExp = 0;
	private int startingLvl = 0;
	
	boolean guiIsDone = false;
	
	int completed = 0;
	int logType = 0;
	int bowType = 0;
	int arrowHeads = 0;
	int selectedOption = 0;
	String fletching = "Select one";
	String logs = "select one";
	String arrowHead = "select one";
	
	String status = "Waiting for GUI input";
	
	final static int BANK_BOX = 42192;
	final static int ARROW_SHAFT = 52;
	final static int FEATHERS = 314;
	final static int HEADLESS_ARROW = 53;
	final static int BOWSTRING = 1777;
	
	/* Types of actions */
	final static int ARROWSHAFTS = 0; //Cut arrow shafts
	final static int HEADLESSARROWS = 1; //Attach feathers
	final static int HEADLESSARROWLOG = 2; //Attach feathers from log
	final static int ARROWHEADS = 3; //Attach heads
	final static int ARROWS = 4; //Fletch arrows
	final static int UNSTRUNGBOW = 5; //Fletch bows
	final static int STRINGBOW = 6; //String bows
	final static int COMPLETEBOW = 7; //Complete bows
	
	/* Types of arrowheads */
	final static int BRONZE = 39;
	final static int IRON = 40;
	final static int STEEL = 41;
	final static int MITHRIL = 42;
	final static int ADAMANT = 43;
	final static int RUNE = 44;
	
	/* Types of logTypes */
	final static int NORMAL = 1511;
	final static int OAK = 1521;
	final static int WILLOW = 1519;
	final static int MAPLE = 1517;
	final static int YEW = 1515;
	final static int MAGIC = 1513;
	
	/* Unstrung Bows */
	final static int NORMAL_SHORT = 50;
	final static int NORMAL_LONG = 48;
	final static int OAK_SHORT = 54;
	final static int OAK_LONG = 56;
	final static int WILLOW_SHORT = 60;
	final static int WILLOW_LONG = 58;
	final static int MAPLE_SHORT = 64;
	final static int MAPLE_LONG = 62;
	final static int YEW_SHORT =68;
	final static int YEW_LONG = 66;
	final static int MAGIC_SHORT = 72;
	final static int MAGIC_LONG = 70;

	@Override
	protected void setup() {
		startingExp = Skills.getExperience(Skills.FLETCHING);
		startingLvl = Skills.getLevel(Skills.FLETCHING);
		try {
			runGUI();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if(Tabs.getCurrent() != Tabs.INVENTORY)
			Tabs.INVENTORY.open();
		Camera.setPitch(true);
		Camera.setAngle(205);
		Camera.setPitch(91);
		Fletcher fletcher = new Fletcher();
		Bank bank = new Bank();
		Strategy fletchingStrategy = new Strategy(fletcher,fletcher);
		Strategy bankStrategy = new Strategy(bank,bank);
		provide(fletchingStrategy);
		provide(bankStrategy);	
	}
	
	private class Fletcher extends Strategy implements Task {
		
		private int sleep;
		
		@Override
		public void run() {
			switch(selectedOption) {
				case ARROWSHAFTS: case UNSTRUNGBOW:
					fletch();
					sleep = Random.nextInt(0, 48000);
					Time.sleep(sleep);
					antiban();
					Time.sleep(48000-sleep);
					break;
				case STRINGBOW:
					while(!stringBows())
						sleep(500,1000);
					sleep = Random.nextInt(0, 13000);
					Time.sleep(sleep);
					antiban();
					Time.sleep(13000-sleep);
					break;
				case COMPLETEBOW:
					fletch();
					sleep = Random.nextInt(0, 23000);
					Time.sleep(sleep);
					antiban();
					Time.sleep(23000-sleep);
					while(!stringBows())
						sleep(500,1000);
					sleep = Random.nextInt(0, 13000);
					Time.sleep(sleep);
					antiban();
					Time.sleep(13000-sleep);
					break;
				case HEADLESSARROWS:
					attachFeathers();
					break;
				case HEADLESSARROWLOG:
					fletch();
					sleep = Random.nextInt(0, 46000);
					Time.sleep(sleep);
					antiban();
					Time.sleep(46000-sleep);
					attachFeathers();
					break;
				case ARROWHEADS:
					attachArrowheads();
					break;
				case ARROWS:
					fletch();
					sleep = Random.nextInt(0, 45500);
					Time.sleep(sleep);
					antiban();
					Time.sleep(45500-sleep);
					attachFeathers();
					attachArrowheads();
					break;
			}
		}

		@Override
		public boolean validate() {
			return suppliesInInventory()
					&& Players.getLocal().getAnimation() == -1
					&& guiIsDone;
		}
		
		private void fletch() {
			status = "fletching";
			clickInventoryItem(logType);
			Time.sleep(1500);
			if(Widgets.get(1179).getChild(12).validate()) {
				Widgets.get(1179).getChild(12).click(true);
				Time.sleep(1500);
			}
			
			if(logType == NORMAL) {
				if(fletching.toLowerCase().contains("arrow")) {
					if(!Widgets.get(905).getChild(14).validate())
						sleep(1500,1600);
					Widgets.get(905).getChild(14).click(true);
				}else if(fletching.contains("Shortbows")) {
					if(!Widgets.get(905).getChild(15).validate())
						sleep(1500,1600);
					Widgets.get(905).getChild(15).click(true);
				}else if(fletching.contains("Longbows")) {
					if(!Widgets.get(905).getChild(16).validate())
						sleep(1500,1600);
					Widgets.get(905).getChild(16).click(true);
				}
			} else {
				if(fletching.contains("Shortbows")) {
					if(!Widgets.get(905).getChild(14).validate())
						sleep(1500,1600);
					Widgets.get(905).getChild(14).click(true);
				}else if(fletching.contains("Longbows")) {
					if(!Widgets.get(905).getChild(14).validate())
						sleep(1500,1600);
					Widgets.get(905).getChild(15).click(true);
				}
			}
		}
		
		private void attachFeathers() {
			while(suppliesInInventory(FEATHERS,ARROW_SHAFT)) {
				clickInventoryItem(FEATHERS);
				sleep(200,300);
				clickInventoryItem(ARROW_SHAFT);
				if(!Widgets.get(905).getChild(14).validate())
					sleep(1500,1600);
				Widgets.get(905).getChild(14).click(true);
				int sleep = Random.nextInt(0, 8000);
				if(Random.nextInt(0, 10) >= 7) {
					Time.sleep(sleep);
					antiban();
					Time.sleep(8000-sleep);
				}
				else
					Time.sleep(11500);
			}
		}
		
		private void attachArrowheads() {
			while(suppliesInInventory(HEADLESS_ARROW,arrowHeads)) {
				clickInventoryItem(HEADLESS_ARROW);
				sleep(200,300);
				clickInventoryItem(arrowHeads);
				if(!Widgets.get(905).getChild(14).validate())
					sleep(1500,1600);
				Widgets.get(905).getChild(14).click(true);
				int sleep = Random.nextInt(0, 8000);
				if(Random.nextInt(0, 10) >= 7) {
					Time.sleep(sleep);
					antiban();
					Time.sleep(8000-sleep);
				}
				else
					Time.sleep(11500);
			}
		}
		
		private boolean stringBows() {
			status = "Stringing";
			clickInventoryItem(bowType);
			sleep(200,300);
			clickInventoryItem(BOWSTRING);
			if(!Widgets.get(905).getChild(14).validate())
				sleep(1500,1600);
			return Widgets.get(905).getChild(14).click(true);
		}
		
	}
	
	private class Bank extends Strategy implements Task {
		
		@Override
		public void run() {
			status = "banking";
            openBank();
            
            switch(selectedOption) {
            	case ARROWSHAFTS: case UNSTRUNGBOW:
            		deposit();
            		if(haveSupplies(logType))
                		while(!withdraw(logType,0))
                			Time.sleep(600);
                	else {
                		log.info("Out of logs");
                		log.info(completed + " Bows made");
                		stop();
                	}
            		break;
            		
            	case STRINGBOW:
            		deposit();
            		if(haveSupplies(bowType,BOWSTRING)) {
    	            	while(!withdraw(bowType,14))
    	            		Time.sleep(600);
    	            	Time.sleep(600);
    	            	while(!withdraw(BOWSTRING,14))
    	            		Time.sleep(600);
    	            }
                	else {
                		log.info("Out of supplies");
                		log.info(completed + " Bows made");
                		stop();
                	}
            		break;
            		
            	case COMPLETEBOW:
            		deposit();
            		if(haveSupplies(logType,BOWSTRING)) {
    		        	while(!withdraw(logType,14))
    		        		Time.sleep(600);
    		        	Time.sleep(600);
    		        	while(!withdraw(BOWSTRING,14))
    		        		Time.sleep(600);
    		        }
                	else {
                		log.info("Out of supplies");
                		log.info(completed + " Bows made");
                		stop();
                	}
            		break;
            		
            	case HEADLESSARROWS:
            		deposit();
            		if(haveSupplies(ARROW_SHAFT,FEATHERS)) {
                		while(!withdraw(ARROW_SHAFT,0))
                			Time.sleep(600);
            			Time.sleep(600);
            			while(!withdraw(FEATHERS,0))
                			Time.sleep(600);
            		}
                	else {
                		log.info("Out of supplies");
                		log.info(completed + " Headless arrows made");
                		stop();
                	}
            		break;
            		
            	case HEADLESSARROWLOG:
            		for(Item item : Inventory.getItems())
            			if(item.getId() != FEATHERS)
            				while(Inventory.getCount(item.getId()) != 0) {
            					deposit(item.getId());
                    			sleep(500,700);
            				}
            		if(Inventory.getItem(FEATHERS).getStackSize() == 0)
            				if(haveSupplies(FEATHERS))
            					while(!withdraw(FEATHERS,0))
            						Time.sleep(600);
            				else {
            					log.info("Out of Feathers");
            					log.info(completed + " Headless arrows made");
            					stop();
            				}
            		if(haveSupplies(logType)) {
            			while(!withdraw(logType,0))
                			Time.sleep(600);
            		}
                	else {
                		log.info("Out of logs");
                		log.info(completed + " Headless arrows made");
                		stop();
                	}
            		break;
            		
            	case ARROWHEADS:
            		deposit();
            		if(haveSupplies(HEADLESS_ARROW,arrowHeads)) {
                		while(!withdraw(HEADLESS_ARROW,0))
                			Time.sleep(600);
            			Time.sleep(600);
            			while(!withdraw(arrowHeads,0))
                			Time.sleep(600);
            		}
                	else {
                		log.info("Out of supplies");
                		log.info(completed + " Arrows made");
                		stop();
                	}
            		break;
            	
            	case ARROWS:
            		for(Item item : Inventory.getItems())
            			if(item.getId() != FEATHERS 
            				&& item.getId() != arrowHeads)
            				while(Inventory.getCount(item.getId()) != 0
            				|| item.getStackSize() !=0) {
            					deposit(item.getId());
                    			sleep(500,700);
            				}
            		if(Inventory.getItem(FEATHERS).getStackSize() == 0)
            				if(haveSupplies(FEATHERS))
            					while(!withdraw(FEATHERS,0))
            						Time.sleep(600);
            				else {
            					log.info("Out of feathers");
            					log.info(completed + " Arrows made");
            					stop();
            				}
            		if(Inventory.getItem(arrowHeads).getStackSize() == 0)
            				if(haveSupplies(FEATHERS))
            					while(!withdraw(arrowHeads,0))
            						Time.sleep(600);
            				else {
            					log.info("Out of Arrowheads");
            					log.info(completed + " Arrows made");
            					stop();
            				}
            		if(haveSupplies(logType)) {
            			while(!withdraw(logType,0))
                			Time.sleep(600);
            		}
            		else {
                		log.info("Out of logs");
                		log.info(completed + " Arrows made");
                		stop();
                	}
            		break;
            }
            closeBank();
		}

		@Override
		public boolean validate() {
			return !suppliesInInventory() 
					&& guiIsDone
					&& Players.getLocal().getAnimation() == -1;
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
		
		public boolean deposit() {
			while(!inventoryIsEmpty()) {
            	while(!Widgets.get(762).getChild(34).validate())
					sleep(200,300);
            	Widgets.get(762).getChild(34).click(true);
            	sleep(600,700);
            } return true;
		}
        
        public boolean deposit(int id) {
    		return Inventory.getItem(id).getWidgetChild().interact("Deposit-all");
    	}

        public void openBank() {
            while (!bankIsOpen()) {
            	SceneEntities.getNearest(BANK_BOX).click(true);
                Time.sleep(1000);
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
        
        public boolean haveSupplies(int id) {
        	if(getCount(id) == 0)
        		return false;
        	return true;
        }
        
        public boolean haveSupplies(int... ids) {
        	for(int id : ids) {
        		if(!haveSupplies(id))
        			return false;
        	}
        	return true;
        }
	}
	
	public void antiban() {
		switch(Random.nextInt(0, 5)) {
			case 0: 
				Camera.setAngle(Random.nextInt(0, 360));
				Time.sleep(3500);
				break;
			case 1:
				int x = Widgets.get(320).getChild(72).getAbsoluteX();
				int y = Widgets.get(320).getChild(72).getAbsoluteY();
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
			default: // Skip it
				Time.sleep(3500); 
				break;
		}
	}
	
	public void sleep(int low, int high) {
		Time.sleep(Random.nextInt(low, high));
	}
	
	public boolean suppliesInInventory() {
		switch(selectedOption) {
			case ARROWSHAFTS: case UNSTRUNGBOW:
				return suppliesInInventory(logType);
			case STRINGBOW:
				return suppliesInInventory(BOWSTRING,bowType);
			case COMPLETEBOW:
				return suppliesInInventory(BOWSTRING,logType);
			case HEADLESSARROWS:
				return suppliesInInventory(ARROW_SHAFT,FEATHERS);
			case HEADLESSARROWLOG:
				return suppliesInInventory(logType,FEATHERS);
			case ARROWHEADS:
				return suppliesInInventory(HEADLESS_ARROW,arrowHeads);
			case ARROWS:
				return suppliesInInventory(logType,arrowHeads,FEATHERS);
			default:
				return false;
		}
	}
	
	public boolean suppliesInInventory(int id) {
		if(Inventory.getCount(id) == 0)
			return false;
		return true;
	}
	
	public boolean suppliesInInventory(int... ids) {
		for(int id : ids) {
			if(!suppliesInInventory(id))
				return false;
		}
		return true;
	}
	
	public boolean clickInventoryItem(int id) {
		if (Inventory.getCount(id) != 0) {
			for(Item i : Inventory.getItems()) {
				if (i.getId() == id) {
					return i.getWidgetChild().click(true);
				}
			}
		}
		return false;
	}
	
	public boolean inventoryIsEmpty()  {
        return Inventory.getCount() == 0;
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
		
		long bowsHr = (long)((3600000.0 / runtime) * completed);
		long xpGained = Skills.getExperience(Skills.FLETCHING) - startingExp;
		long xpPerHour = (long)((3600000.0 / runtime) * xpGained);
		
		int currentLvl = Skills.getLevel(Skills.FLETCHING);
		
		String type = "Bows";
		if(fletching.toLowerCase().contains("arrow"))
			type = "Arrows";
		
		long xpToLvl = Skills.getExperienceToLevel(Skills.FLETCHING, currentLvl+1);
		long timeToLvl = 0;
		if(xpGained >0)
			timeToLvl = (long)((xpToLvl*runtime/xpGained));
		
    	g.setColor(color1);
    	g.drawRect(2, 219, 517, 119);
    	g.setFont(font1);
    	
    	g.drawString("MattyP's Auto Fletcher", 5, 250);
    	
    	g.setFont(font2);
    	
    	g.drawString("Time running: " + Time.format(runtime), 5, 275);
    	g.drawString("Status: " + status, 5, 296);
    	g.drawString(type+" fleched: " + format(completed), 5, 316);
    	g.drawString(type+"/hour: " + format(bowsHr), 5, 335);
    	g.drawString("XP Gained: " + format(xpGained), 329, 234);
    	g.drawString("XP/hour: " + format(xpPerHour), 329, 256);
    	g.drawString("XP to level: " + format(xpToLvl) , 329, 278);
    	g.drawString("Time to level: " + Time.format(timeToLvl), 329, 301);
    	g.drawString("Level: " + currentLvl + " (" + (currentLvl - startingLvl) +" gained)", 329, 324);
    }
	
	private void runGUI() throws InterruptedException {
		@SuppressWarnings("unused")
		JFrame g = new GUI();
	}
	
	public class GUI extends JFrame {
		private static final long serialVersionUID = 1L;
		
		private JLabel action;
		private JComboBox actions;
		private JLabel bow;
		private JComboBox bows;
		private JButton go;
		private JLabel arrow;
		private JComboBox arrows;
		
		public GUI() {
            initComponents();
            setVisible(true);
        }
		
		public void initComponents() {
			final Container cp = getContentPane();
			cp.setBackground(Color.BLACK);
			this.setTitle("Auto Fletcher");
			this.setResizable(false);
			setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
			setAlwaysOnTop(true);
			setBackground(new java.awt.Color(0, 0, 0));
			setMinimumSize(new java.awt.Dimension(407, 320));
			cp.setLayout(null);
			
			this.addWindowListener(
					new WindowAdapter() {
						public void windowClosing(WindowEvent e) {
							Window w = e.getWindow();
							w.dispose();
						}
					}
				);
			
			action = new JLabel();
			action.setBackground(new Color(0, 153, 0));
			action.setForeground(new Color(0, 153, 0));
			action.setFont(new Font("Tempus Sans ITC", 1, 24));
			action.setText("What to fletch");
			cp.add(action);
			action.setBounds(129, 28, 167, 33);
			
			actions = new JComboBox();
			actions.addItem("Select one");
			actions.addItem("Shortbows");
			actions.addItem("String Shortbows");
			actions.addItem("Complete Shortbows");
			actions.addItem("Longbows");
			actions.addItem("String Longbows");
			actions.addItem("Complete Longbows");
			actions.addItem("Arrowshafts");
			actions.addItem("Headless Arrows");
			actions.addItem("Headless Arrows from log");
			actions.addItem("Attach Arrowheads");
			actions.addItem("Arrows from scratch");
			cp.add(actions);
			actions.setBounds(129, 85, 144, 25);
			
			bow = new JLabel();
			bow.setBackground(new Color(0, 153, 0));
			bow.setForeground(new Color(0, 153, 0));
			bow.setFont(new Font("Tempus Sans ITC", 1, 24));
			bow.setText("Type of bow");
			
			bows = new JComboBox();
			bows.addItem("Select one");
			bows.addItem("Normal");
			bows.addItem("Oak");
			bows.addItem("Willow");
			bows.addItem("Maple");
			bows.addItem("Yew");
			bows.addItem("Magic");
			
			arrow = new JLabel();
			arrow.setBackground(new Color(0, 153, 0));
			arrow.setForeground(new Color(0, 153, 0));
			arrow.setFont(new Font("Tempus Sans ITC", 1, 24));
			arrow.setText("Type of arrow");
			
			arrows = new JComboBox();
			arrows.addItem("Select one");
			arrows.addItem("Bronze");
			arrows.addItem("Iron");
			arrows.addItem("Steel");
			arrows.addItem("Mithril");
			arrows.addItem("Adamamt");
			arrows.addItem("Rune");
			
			actions.addActionListener(
					new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							JComboBox cb = (JComboBox)e.getSource();
							fletching = (String)cb.getSelectedItem();
							if(fletching.contains("bow")) {
								cp.remove(arrow);
								cp.remove(arrows);
								cp.add(bow);
								bow.setBounds(129, 125, 280, 25);
								cp.add(bows);
								bows.setBounds(129, 157, 144, 25);
							}
							else if(fletching.contains("Arrowheads") || fletching.contains("from scratch")) {
								cp.remove(bow);
								cp.remove(bows);
								cp.add(arrow);
								arrow.setBounds(129, 125, 280, 25);
								cp.add(arrows);
								arrows.setBounds(129, 157, 144, 25);
							}
							else {
								cp.remove(bow);
								cp.remove(bows);
								if(fletching.equals("Arrowshafts")) {
									logType = AutoFletcher.NORMAL;
									selectedOption = AutoFletcher.ARROWSHAFTS;
								}
							}
						}
					}
				);
			
			bows.addActionListener(
					new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							JComboBox cb = (JComboBox)e.getSource();
							logs = (String)cb.getSelectedItem();
						}
					}
				);
			
			arrows.addActionListener(
					new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							JComboBox cb = (JComboBox)e.getSource();
							arrowHead = (String)cb.getSelectedItem();
						}
					}
				);
			
			go = new JButton();
			go.setFont(new java.awt.Font("Traditional Arabic", 1, 24));
			go.setForeground(new java.awt.Color(255, 0, 0));
            go.setText("Start!");
            getContentPane().add(go);
            go.setBounds(93, 221, 213, 46);
            
            go.addActionListener(
            		new ActionListener() {
            			public void actionPerformed(ActionEvent e) {
            				startScript();
            			}
            		}
            	);
		}
		
		public void startScript() {
			if(fletching.contains("bow")){
				if(logs.equals("Normal")) {
					logType = AutoFletcher.NORMAL;
					if(fletching.equals("String Shortbows"))
						bowType = AutoFletcher.NORMAL_SHORT;
					else
						bowType = NORMAL_LONG;
				}
				else if(logs.equals("Oak")) {
					logType = AutoFletcher.OAK;
					if(fletching.equals("String Shortbows"))
						bowType = AutoFletcher.OAK_SHORT;
					else
						bowType = AutoFletcher.OAK_LONG;
				}
				else if(logs.equals("Willow")) {
					logType = AutoFletcher.WILLOW;
					if(fletching.equals("String Shortbows"))
						bowType = AutoFletcher.WILLOW_SHORT;
					else
						bowType = AutoFletcher.WILLOW_LONG;
				}
				else if(logs.equals("Maple")) {
					logType = AutoFletcher.MAPLE;
					if(fletching.equals("String Shortbows"))
						bowType = AutoFletcher.MAPLE_SHORT;
					else
						bowType = AutoFletcher.MAPLE_LONG;
				}
				else if(logs.equals("Yew")) {
					logType = AutoFletcher.YEW;
					if(fletching.equals("String Shortbows"))
						bowType = AutoFletcher.YEW_SHORT;
					else
						bowType = AutoFletcher.YEW_LONG;
				}
				else if(logs.equals("Magic")) {
					logType = AutoFletcher.MAGIC;
					if(fletching.equals("String Shortbows"))
						bowType = AutoFletcher.MAGIC_SHORT;
					else
						bowType = AutoFletcher.MAGIC_LONG;
				}
			}
			
			if(fletching.contains("Attach") || fletching.contains("from scratch")) {
				if(arrowHead.equals("Bronze")) 
					arrowHeads = AutoFletcher.BRONZE;
				else if(arrowHead.equals("Iron")) 
					arrowHeads = AutoFletcher.IRON;
				else if(arrowHead.equals("Steel")) 
					arrowHeads = AutoFletcher.STEEL;
				else if(arrowHead.equals("Mithril")) 
					arrowHeads = AutoFletcher.MITHRIL;
				else if(arrowHead.equals("Adamant"))
					arrowHeads = AutoFletcher.ADAMANT;
				else if(arrowHead.equals("Rune")) 
					arrowHeads = AutoFletcher.RUNE;
			}
			
			if (fletching.equals("Shortbows") || fletching.equals("Longbows"))
				selectedOption = AutoFletcher.UNSTRUNGBOW;
			else if (fletching.contains("String")) 
				selectedOption = AutoFletcher.STRINGBOW;
			else if (fletching.contains("Complete"))
				selectedOption = AutoFletcher.COMPLETEBOW;
			else if (fletching.equals("Headless Arrows"))
				selectedOption = AutoFletcher.HEADLESSARROWS;
			else if (fletching.contains("from log")) {
				selectedOption = AutoFletcher.HEADLESSARROWLOG;
				logType=AutoFletcher.NORMAL;
			}
			else if (fletching.contains("Attach"))
				selectedOption = AutoFletcher.ARROWHEADS;
			else if (fletching.contains("from scratch")) {
				selectedOption = AutoFletcher.ARROWS;
				logType=AutoFletcher.NORMAL;
			}
			
        	guiIsDone = true;
			this.dispose();
        }
	}

	@Override
	public void messageReceived(MessageEvent msg) {
		String message = msg.getMessage();
		if(message.contains("carefully cut")) {
			if(fletching.toLowerCase().contains("arrow"))
				completed+=15;
			else
				completed++;
		}
		else if(selectedOption == STRINGBOW && message.contains("add a string"))
			completed++;
		else if(selectedOption == HEADLESSARROWS && message.contains("feathers"))
			completed+=15;
		else if(selectedOption == ARROWHEADS && message.contains("arrowheads"))
			completed+=15;
	}

}