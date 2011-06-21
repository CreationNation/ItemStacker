package streammz.itemstacker;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
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
			Material mat = p.getItemInHand().getType();
			if (mat.equals(Material.AIR)) { p.sendMessage(ChatColor.AQUA + "You try to puzzle your hands together... ew, that looks nasty"); return true; }
			
			if (!hasPermission(p, "itemstacker." + mat.getId())) {
				p.sendMessage(ChatColor.AQUA + "You try to puzzle your " + mat.name().toLowerCase() + " together, but it doesn't fit");
				return true;
			}
			
			Inventory inv = p.getInventory();
			int amount = 0;
			for (int i=0; i<p.getInventory().getSize(); i++) {
				ItemStack current = p.getInventory().getItem(i);
				if (current.getType().equals(mat)) { amount += current.getAmount(); inv.clear(i); }
			}

			inv.setItem(p.getInventory().getHeldItemSlot(), new ItemStack(mat, maximize(amount, 64)));
			amount -= maximize(amount, 64);
			
			while (amount > 0) {
				inv.setItem(inv.firstEmpty(), new ItemStack(mat, maximize(amount, 64)));
				amount -= maximize(amount, 64);
			}
			p.sendMessage(ChatColor.AQUA + "You puzzle your " + mat.name().toLowerCase() + " together! :D");
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