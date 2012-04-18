package mattyp.api;

import org.powerbot.game.api.methods.node.SceneEntities;
import org.powerbot.game.api.methods.tab.Inventory;
import org.powerbot.game.api.util.Random;
import org.powerbot.game.api.util.Time;
import org.powerbot.game.api.wrappers.node.SceneObject;

/**
 * Set of methods to work with API
 * @category RSBot API
 * @author Matt Provost
 * @version 1.1
 */
public class Methods {
	/**
	 * Sleep for a random amount of time
	 * @param min Minimum amount of time
	 * @param max Maximum amount of time
	 */
	public static void sleep(int min, int max) {
		Time.sleep(Random.nextInt(min, max));
	}
	/**
	 * Checks contents of the inventory
	 * @return True if empty
	 */
	public static boolean inventoryIsEmpty() {
		return Inventory.getCount() == 0;
	}
	/**
	 * Checks the contents of the inventory
	 * @return True if full
	 */
	public static boolean inventoryIsFull() {
		return Inventory.getCount() == 28;
	}
	/**
	 * A method that works as a failsafe to ensure a location is on the screen.
	 * @param id ID of location to check
	 * @return True if location is loaded and on screen
	 */
	public static boolean locationOnScreen(int id) {
		for(SceneObject loc1 : SceneEntities.getLoaded())
			if(loc1.getId()==id)
				return SceneEntities.getNearest(id).isOnScreen();
		return false;
	}
	/**
	 * A method that works as a failsafe to ensure a location is on the screen.<br />
	 * Works with an array of IDs.
	 * @param ids IDs of location to check 
	 * @return True if location is loaded and on screen
	 */
	public static boolean locationOnScreen(int... ids) {
		for(int id : ids)
			if(locationOnScreen(id))
				return true;
		return false;
	}
}
