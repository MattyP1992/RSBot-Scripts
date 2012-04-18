package mattyp.api;

import java.util.LinkedList;
import java.util.List;

import org.powerbot.game.api.methods.Widgets;
import org.powerbot.game.api.methods.input.Keyboard;
import org.powerbot.game.api.methods.input.Mouse;
import org.powerbot.game.api.methods.interactive.NPCs;
import org.powerbot.game.api.methods.node.Menu;
import org.powerbot.game.api.methods.node.SceneEntities;
import org.powerbot.game.api.methods.tab.Inventory;
import org.powerbot.game.api.wrappers.node.Item;
import org.powerbot.game.api.wrappers.widget.WidgetChild;

/**
 * Banking class within custom API
 * @category RSBot API
 * @author Matt Provost
 * @version 1.0
 */
public class Banking {

	private final static int[] BANKER = {44, 45, 166, 494, 495,
		496, 498, 499, 553, 909, 
		953, 1036, 1360, 1702, 2163, 
		2164, 2354, 2355, 2568, 2569, 
		2570, 2718, 2759, 3046, 3198,
		3199, 3293, 3416, 3418, 4456, 
		4457, 4458, 4459, 4519, 4907, 
		5258, 5260, 5776, 5777, 6200, 
		7049, 7050, 7605, 8948, 9710};

	private final static int[] BANK_BOOTH = {782, 2213, 3045, 5276, 6084,
		10517, 11338, 11758, 12798, 12799,
		12800, 12801, 14369, 14370, 16700, 
		19230, 20325, 20326, 20327, 20328,
		22819, 24914, 25808, 26972, 29805,
		34205, 34752, 35647, 35648, 36262,
		36786, 37474, 49018, 49019, 52397,
		52589};

	private final static int[] BANK_CHEST = {4483, 8981, 14382, 20607, 21301,
		27663, 42192, 57437};
	
	private final static int[] DEPOSIT_BOX = {2045, 2132, 2133, 6836, 9398,
		15985, 20228, 24995, 25937, 26969,
		32924, 32930, 32931, 34755, 36788,
		39830};
	
	public static int[] getBanker() {
		return BANKER;
	}
	public static int[] getBankBooth() {
		return BANK_BOOTH;
	}
	public static int[] getBankChest() {
		return BANK_CHEST;
	}
	public static int[] getDepositBox() {
		return DEPOSIT_BOX;
	}
	/**
	 * Checks to make sure the bank window is on the screen.
	 * @return True - Bank  window is open<br />
	 * 		   False - Bank window is closed
	 */
	public static boolean bankIsOpen() {
		return Widgets.get(762, 1).isOnScreen();
	}
	/**
	 * Closes the bank window.
	 * @return True - Successfully closed the window.
	 */
	public static boolean closeBank() {
		while(bankIsOpen())
			if(!Widgets.get(762).getChild(45).click(true))
				Methods.sleep(500,700);
		return true;
	}
	/**
	 * Deposits all items in inventory
	 * @return 
	 */
	public boolean deposit() {
		while(!Methods.inventoryIsEmpty()) {
        	while(!Widgets.get(762).getChild(34).validate())
        		Methods.sleep(200,300);
        	Widgets.get(762).getChild(34).click(true);
        	Methods.sleep(900,1000);
        }
		return true;
	}
	/**
	 * Deposits all of a selected item
	 * @param id ID of item to deposit
	 * @return True if deposited
	 */
	public static boolean deposit(int id) {
		return Inventory.getItem(id).getWidgetChild().interact("Deposit-all");
	}
	/**
	 * Deposits all of selected items
	 * @param ids IDs of items to deposit
	 * @return True if deposited
	 */
	public boolean deposit(int... ids) {
		for(int id : ids) 
			while(!deposit(id))
				Methods.sleep(800, 1000);
		return true;
	}
	/**
	 * Deposits a certain amount of a selected item
	 * @param id ID of item to deposit
	 * @param amount Amount of item to deposit
	 * @return True if deposited
	 */
	public boolean deposit(int id, int amount) {
		switch(amount) {
			case -1:
				return deposit();
			case 0:
				return deposit(id);
			case 1:
				return Inventory.getItem(id).getWidgetChild().interact("Deposit-1");
			case 5:
				return Inventory.getItem(id).getWidgetChild().interact("Deposit-5");
			case 10:
				return Inventory.getItem(id).getWidgetChild().interact("Deposit-10");
			default:
				for(String action : Inventory.getItem(id).getWidgetChild().getActions())
					if(action.equals("Deposit-"+amount))
						return Menu.select("Deposit-"+amount);
				if(Menu.select("Deposit-X")) {
					Methods.sleep(1400,1600);
					Keyboard.sendText("" + amount, true);
					return true;
				}
		}
		return false;
	}
	/**
	 * Retrieve a count of a stack size of a particular item.
	 * @param id ID of item to look for.
	 * @return Count of the particular item with the given id.
	 */
	public static int getCount(int id) {
		for (Item item : getItems())
			if (item.getId() == id)
				return item.getStackSize();
		return 0;
	}
	/**
	 * Retrieve a count of a stack size of a particular item.
	 * @param name Name of item to look for.
	 * @return Count of the particular item with the given name.
	 */
	public static int getCount(String name) {
		for (Item item : getItems())
			if (item.getName().equalsIgnoreCase(name))
				return item.getStackSize();
		return 0;
	}
	/**
	 * Gets all of the items in the current bank tab.
	 * @return An array of items in current bank tab.
	 */
	public static Item[] getItems() {
		List<Item> items = new LinkedList<Item>();
		if (Widgets.get(762).getChild(95).validate())
			for (WidgetChild item : Widgets.get(762).getChild(95).getChildren())
				if (item != null && item.getChildId() != -1)
					items.add(new Item(item));
		return items.toArray(new Item[items.size()]);
	}
	/**
	 * Checks that an item is in the bank
	 * @param id ID of item to look for.
	 * @return True - Item is in bank<br />
	 * 		   False - Item not in bank
	 */
	public static boolean haveItems(int id) {
		if(getCount(id) == 0)
			return false;
		return true;
	}
	/**
	 * Checks if a number of items are in the bank
	 * @param ids IDs of items to look for
	 * @return True - Items are in bank<br />
	 * 		   False - Items are not in bank
	 */
	public static boolean haveItems(int... ids) {
		for(int id : ids)
			if(!haveItems(id))
				return false;
		return true;
	}
	/**
	 * Checks if a number of items are in the bank
	 * @param name Name of item to look for
	 * @return True - Item is in bank<br />
	 * 		   False - Item is not in bank
	 */
	public boolean haveItems(String name) {
		if(getCount(name) == 0)
			return false;
		return true;
	}
	/**
	 * Checks if a number of items are in the bank
	 * @param names Names of items to look for
	 * @return True - Items are in bank<br />
	 * 		   False - Items are not in bank
	 */
	public boolean haveItems(String... names) {
		for(String name : names)
			if(!haveItems(name))
				return false;
		return true;
	}
	/** 
	 * Method opens the bank window.<br />
	 * Checks nearest booth then banker then chest.
	 * @return True - Successfully opens bank window
	 */
	public static boolean openBank() {
		while(!bankIsOpen())
			if(!SceneEntities.getNearest(BANK_BOOTH).click(true) 
					&& !NPCs.getNearest(BANKER).interact("Bank") 
					&& !SceneEntities.getNearest(BANK_CHEST).click(true))
				Methods.sleep(2000,2500);
		return true;
	}
	/** 
	 * Method opens the bank window.<br />
	 * Checks nearest booth then banker then chest.
	 * @param depositBox True to use deposit box only
	 * @return True - Successfully opens bank window
	 */
	public static boolean openBank(boolean depositBox) {
		if(!depositBox)
			return openBank();
		while(!bankIsOpen())
			if(!SceneEntities.getNearest(DEPOSIT_BOX).click(true))
				Methods.sleep(2000,2500);
		return true;
	}
	/**
	 * Withdraw an item from the bank
	 * @param id ID of item to withdraw
	 * @param amount Amount of item to withdraw
	 * @return Success of withdraw (True or false)
	 */
	public static boolean withdraw(int id, int amount) {
		WidgetChild bank = Widgets.get(762).getChild(95);
		int baseX = bank.getAbsoluteX(), baseY = bank.getAbsoluteY();
		for (Item item : getItems()) 
			if (item.getId() == id) {
				WidgetChild child = item.getWidgetChild();
				while(!Mouse.click(child.getRelativeX() + baseX + (child.getWidth()/2),
						child.getRelativeY() + baseY + (child.getHeight()/2), false))
					Methods.sleep(600,1000);
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
						Methods.sleep(1400,1600);
						Keyboard.sendText("" + amount, true);
						return true;
					}
				}
			}
		return false;
	}
}
