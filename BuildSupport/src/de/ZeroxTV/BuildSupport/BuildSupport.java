package de.ZeroxTV.BuildSupport;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class BuildSupport extends JavaPlugin implements Listener, CommandExecutor {
	
	public static HashMap<String, ItemStack> skulls = new HashMap<>();
	
	private static HashMap<String, Location[]> permissions = new HashMap<>();
	private static Location loc1;
	private static Location loc2;
	
	@Override
	public void onEnable() {
		System.out.println("[BuildSupport] Plugin enabled and ready to go");
		this.getServer().getPluginManager().registerEvents(this, this);
		this.getCommand("bs").setExecutor(this);
	}
	
	@Override
	public void onDisable() {
		System.out.println("[BuildSupport] Plugin disabled and permissions cleared");
	}
	
	@EventHandler
	public static void onInteract(PlayerInteractEvent e) {
		if (e.getPlayer().isOp()) {
			if (e.getItem() != null) {
				if (e.getItem().getType().equals(Material.STICK)) {
					if (e.getItem().getItemMeta().getDisplayName() != null) {
						if (e.getItem().getItemMeta().getDisplayName().equals("�fBuildSupport Wand")) {
							Action a = e.getAction();
							Player p = e.getPlayer();
							Location loc = p.getLocation().getBlock().getLocation();
							if (a.equals(Action.LEFT_CLICK_AIR) || a.equals(Action.LEFT_CLICK_BLOCK)) {
								loc1 = loc;
								p.sendMessage("Location 1 set to " + loc.toString());
							} else if (a.equals(Action.RIGHT_CLICK_AIR) || a.equals(Action.RIGHT_CLICK_BLOCK)) {
								loc2 = loc;
								p.sendMessage("Location 2 set to " + loc.toString());
							}
							e.setCancelled(true);
						}
					}
				}
			}
		} else {
			if (e.getAction().equals(Action.LEFT_CLICK_BLOCK) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
				if (permissions.containsKey(e.getPlayer().getName())) {
					Location loc = e.getClickedBlock().getLocation();
					Location l1 = permissions.get(e.getPlayer().getName())[0];
					Location l2 = permissions.get(e.getPlayer().getName())[1];
					
					Double x1 = l1.getX();
					Double x2 = l2.getX();
					Double z1 = l1.getZ();
					Double z2 = l2.getZ();
					
					Double minx = 0d;
					Double maxx = 0d;
					Double minz = 0d;
					Double maxz = 0d;
					
					if (x1 > x2) {
						minx = x2;
						maxx = x1;
					} else {
						minx = x1;
						maxx = x2;
					}
					
					if (z1 > z2) {
						minz = z2;
						maxz = z1;
					} else {
						minz = z1;
						maxz = z2;
					}
					if (!(loc.getX() >= minx && loc.getX() <= maxx && loc.getZ() >= minz && loc.getZ() <= maxz)){
						e.setCancelled(true);
						e.getPlayer().sendMessage("�cDu darfst hier nicht bauen");
					}
				} else {
					e.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public static void onInvClick(InventoryClickEvent e) {
		if (e.getWhoClicked().isOp()) {
			if (e.getInventory().getName() != null) {
				if (e.getInventory().getName().equals("W�hle einen Spieler")) {
					if (e.getCurrentItem() != null) {
						if (e.getCurrentItem().getType().equals(Material.SKULL_ITEM)) {
							SkullMeta sm = (SkullMeta) e.getCurrentItem().getItemMeta();
							String pl = sm.getOwner();
							Location[] locs = {loc1, loc2};
							loc1 = null;
							loc2 = null;
							permissions.put(pl, locs);
							e.setCancelled(true);
							e.getWhoClicked().closeInventory();
							e.getWhoClicked().sendMessage("Assigned location to " + pl);
						}
					}
				}
			}
		}
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.isOp()) {
			sender.sendMessage("�cDu hast daf�r keine Berechtigung");
		} else if (!(sender instanceof Player)){
			sender.sendMessage("�cDieser Befehl ist nur f�r Spieler");
		} else {
			Player p = (Player) sender;
			if (args.length == 2) {
				
				if (args[0].equalsIgnoreCase("reset")) {
					if (args[1].equalsIgnoreCase("a") || args[1].equalsIgnoreCase("all")) {
						permissions = new HashMap<>();
						loc1 = null;
						loc2 = null;
						sender.sendMessage("Alle Zuweisungen wurden zur�ckgesetzt");
					} else {
						if (permissions.containsKey(args[1])) {
							permissions.remove(args[1]);
							sender.sendMessage("Die Zuweisungen des Spielers wurden zur�ckgesetzt");
						} else {
							sender.sendMessage("�cDieser Spieler existiert nicht");
						}
					}
				}
			
			} else if (args.length == 1) {
				
				if (args[0].equalsIgnoreCase("assign")) {
					if (loc1 == null) {
						sender.sendMessage("Du musst noch Position 1 markieren");
					} else if (loc2 == null) {
						sender.sendMessage("Du musst noch Position 2 markieren");
					} else {
						Inventory inv = Bukkit.createInventory(null, 54, "W�hle einen Spieler");
						
						for (Player pl : Bukkit.getOnlinePlayers()) {
							if (!pl.getName().equals(sender.getName())) {
								inv.addItem(getSkull(pl.getUniqueId()));
							}
						}
						p.openInventory(inv);
						
					}
				} else if (args[0].equalsIgnoreCase("wand")) {
					ItemStack wand = new ItemStack(Material.STICK);
					ItemMeta wandM = wand.getItemMeta();
					wandM.setDisplayName("�fBuildSupport Wand");
					wand.setItemMeta(wandM);
					p.getInventory().addItem(wand);
				}
			}
		}
		return true;
	}
	
	
	@SuppressWarnings("deprecation")
	public static ItemStack getSkull(UUID u) {
		
		Player p;
		if (Bukkit.getPlayer(u) != null) {
			p = Bukkit.getPlayer(u);
		} else {
			p = (Player) Bukkit.getOfflinePlayer(u);
		}
		
		if (skulls.get(p.getName()) != null) {
			return skulls.get(p.getName());
		} else {
			Location loc = new Location(Bukkit.getWorlds().get(0), 1, 1, 1);
			Block b = loc.getBlock();
			Material m = b.getType();
			b.setType(Material.SKULL);
			Skull skull = (Skull) b.getState();
			skull.setSkullType(SkullType.PLAYER);
			skull.setOwner(p.getName());
			skull.update(true);
			
			Object[] arr = loc.getBlock().getDrops().toArray();
			ItemStack res = (ItemStack) arr[0];
			res.setDurability((short) 3);
			b.setType(m);
			skulls.put(p.getName(), (ItemStack) arr[0]);
			return (ItemStack) arr[0];
		}
	}
}
