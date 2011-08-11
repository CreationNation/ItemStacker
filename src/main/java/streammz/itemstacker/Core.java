package streammz.itemstacker;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class Core extends JavaPlugin {
	public static PermissionHandler permissionHandler;
	
	public void onDisable() {
	}

	public void onEnable() {
		setupPermissions();
		
		PluginDescriptionFile pdfFile = getDescription();
	    System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		String cmd = command.getName().toLowerCase();
		Player p = (Player)sender;
		
		if (cmd.equals("stack")) {
			//Global data
			ItemStack hand = p.getItemInHand().clone();
			Material handMat = hand.getType();
			boolean durabilityCheck = false;
			boolean useData = false;
			byte data = 0x0;
			PlayerInventory inv = p.getInventory();
			short MaxStackSize = 64;
			
			//Air stacking check
			if (handMat.equals(Material.AIR)) {
				p.sendMessage(ChatColor.AQUA + "You try to puzzle your hands together, but it looks nasty.");
				return true;
			}
			//Permissions
			if (hasPermission(p, "itemstacker.debug")) p.sendMessage("Looking for node: " + "itemstacker." + handMat.getId());
			if (!hasPermission(p, "itemstacker." + handMat.getId())) {
				p.sendMessage(ChatColor.AQUA + "You try to puzzle your " + handMat.name().toLowerCase() + " together, but it doesn't fit");
				return true;
			}
			if (hasPermission(p, "itemstacker.99stack." + handMat.getId())) {
				MaxStackSize = 99;
			}
			
			//Durability check
			if (handMat.getMaxDurability() > 0) {
				durabilityCheck = true;
				p.sendMessage(ChatColor.AQUA + "Please note that damaged items won't stack!");
			}
			
			//Data check
			try {
				data = hand.getData().getData();
				useData = true;
			} catch (NullPointerException e) {}
			
			//Count items
			int amount = 0;
			for (int i=0; i<inv.getSize(); i++) {
				ItemStack cur = inv.getItem(i);
				if (cur.getType().equals(handMat)) {
					if (durabilityCheck) {//Tools
						if (cur.getDurability() != 0) {
							continue;
						}
					}
					if (useData && data != cur.getData().getData()) {//Wool etc
						continue;
					}
					amount += cur.getAmount();
					inv.setItem(i, null);
				}
			}

			//Make other stacks
			while (amount > 0) {
				int max = maximize(amount, MaxStackSize);
				
				ItemStack ItemStackNow = null;
				if (durabilityCheck) {
					if (useData) ItemStackNow = new ItemStack(handMat, max, (short)0, data);
					else ItemStackNow = new ItemStack(handMat, max, (short)0);
				} else {
					if (useData) ItemStackNow = new ItemStack(handMat, max, (short)-1, data);
					else ItemStackNow = new ItemStack(handMat, max, (short)-1);
				}
				
				
				inv.addItem(ItemStackNow);
				amount -= max;
			}
			
			//Send final message
			p.sendMessage(ChatColor.AQUA + "You puzzle your " + handMat.name().toLowerCase() + " together! :D");
		}
		
		return true;
	}
	
	
	private void setupPermissions() {
	    Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
	    if (permissionHandler == null) {
	    	if (permissionsPlugin != null) {
	    		permissionHandler = ((Permissions) permissionsPlugin).getHandler();
         	} else {
	     	}
	     }
	}
	public static boolean hasPermission(Player p, String node) {
		if (p.isOp()) { return true; }
		if (!Bukkit.getServer().getPluginManager().isPluginEnabled("Permissions")) { return false; }
		if (permissionHandler.has(p, node)) { return true; }
		return false;
	}
	
	private int maximize(int current, int max) {
		if (current > max) { return max; }
		else { return current; }
	}


}