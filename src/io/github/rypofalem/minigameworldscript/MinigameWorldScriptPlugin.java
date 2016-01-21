package io.github.rypofalem.minigameworldscript;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.winthier.playercache.PlayerCache;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;

public class MinigameWorldScriptPlugin  extends JavaPlugin implements CommandExecutor {
	MultiverseCore mvcore;
	

	public void onEnable(){
		this.saveDefaultConfig();
		this.getCommand("mgws").setExecutor(this);
		mvcore = (MultiverseCore)Bukkit.getPluginManager().getPlugin("Multiverse-Core");
	}

	// /mgws <playername> <worldname> <minigame> [environment]
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(args.length <3){
			errorMessage(sender,"Not enough arguments!");
			return false;
		}
		if(args.length >4){
			errorMessage(sender,"Too many arguments!");
			return false;
		}
		String playername = args[0];
		String worldname = args[1];
		String minigame = args[2];
		Environment environment = (args.length == 4) ? validateWorldType(args[3]) : Environment.NORMAL;
		
		PlayerCache cachedPlayer = PlayerCache.forName(playername);
		if(cachedPlayer == null){ 
			errorMessage(sender, "Unkown player: \"" + playername + "\"");
			return false; 
		}
		playername = cachedPlayer.getName();
		if(worldExists(worldname)){ 
			errorMessage(sender, "The world \"" + worldname + "\" already exists");
			return false; 
		}
		if(!isMinigame(minigame)){
			errorMessage(sender, "Invalid minigame name \"" + minigame + "\". Is the it listed in the configuration?");
			return false; 
		}
		if(environment == null){
			errorMessage(sender, "The environment \"" + args[3] + "\" is invalid");
			return false; 
		}
		if(!mvcore.getMVWorldManager().addWorld(worldname, environment, "01", WorldType.NORMAL, false, "VoidGenerator")){
			errorMessage(sender, "World creation failed.");
			return true;
		}
		if(mvcore.getMVWorldManager().isMVWorld(worldname)){
			MultiverseWorld mvWorld = mvcore.getMVWorldManager().getMVWorld(worldname);
			World bukkitWorld = mvWorld.getCBWorld();
			bukkitWorld.getBlockAt(0, 65, 0).setType(Material.AIR);
			bukkitWorld.getBlockAt(255, 65, 255).setType(Material.GLASS);
			mvWorld.setSpawnLocation(new Location(bukkitWorld, 255.5, 66, 255.5));
			mvWorld.setGameMode(GameMode.CREATIVE);
			mvWorld.setDifficulty(Difficulty.PEACEFUL);
		}else {
			errorMessage(sender, "World was created but cannot be found. I am just as confused as you are.");
			return true;
		}
		
		// /perm group Builder set multiverse.access.WorldName
		// /perm group Builder set WorldName:worldedit.*
		// /perm group Builder set WorldName:minecraft.command.save-all
		// /perm group Builder set WorldName:colorfall.test
		// /perm player PlayerName addgroup Builder
		setPerm("group Builder set multiverse.access." + worldname);
		setPerm("group Builder set " + worldname + ":worldedit.*");	
		setPerm("group Builder set " + worldname + ":minecraft.command.save-all");
		setPerm("group Builder set " + worldname + ":" + minigame + ".test");
		setPerm("player "+ playername +" addgroup Builder");
		sender.sendMessage(ChatColor.DARK_GREEN + "Successfully created a " + minigame + " world for " + playername + " called \"" + worldname + "\"");
		return true;
	}

	private boolean isMinigame(String minigame) {
		ArrayList<String> minigames;
		try{
			reloadConfig();
			minigames = (ArrayList<String>) getConfig().getStringList("minigames");
			if (minigames == null) return false;
		}catch(Exception e){return false;}
		for(String mg : minigames){
			if(minigame.equalsIgnoreCase(mg)) return true;
		}
		return false;
	}

	private boolean setPerm(String command){
		return Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "perm "+ command);
	}

	private void errorMessage(CommandSender sender, String message){
		if(sender != null && message!= null){
			if(sender instanceof Player && !((Player)sender).isOnline()) return;
			sender.sendMessage(ChatColor.RED + message);
		}
	}

	private boolean worldExists(String worldname){
		if(mvcore == null || mvcore.isEnabled() == false) return false;
		return mvcore.getMVWorldManager().isMVWorld(worldname);
	}

	private Environment validateWorldType(String environment){
		try{
			return Environment.valueOf(environment.toUpperCase());
		} catch(IllegalArgumentException iae){
			return null;
		}catch(NullPointerException npe){
			return null;
		}
	}
}