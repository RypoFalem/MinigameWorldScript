package io.github.rypofalem.minigameworldscript;

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

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;

public class MinigameWorldScriptPlugin  extends JavaPlugin implements CommandExecutor {
	MultiverseCore mvcore;

	public void onEnable(){
		this.getCommand("mgwc").setExecutor(this);
		mvcore = (MultiverseCore)Bukkit.getPluginManager().getPlugin("Multiverse-Core");
		if(mvcore == null || !mvcore.isEnabled()){
			Bukkit.getLogger().info("Multiverse-Core not found. Disabling MinigameWorldScript");
			this.getPluginLoader().disablePlugin(this);
		}
	}

	///mgws <playername> <worldname> <minigame> [environment]
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(cmd.getName().equalsIgnoreCase("mgwc") && sender instanceof Player){
			Player player = (Player) sender;
			if(args.length <3){
				errorMessage(player,"Not enough arguments!");
				return false;
			}
			if(args.length >4){
				errorMessage(player,"Too many arguments!");
				return false;
			}
			String playername = args[0];
			String worldname = args[1];
			String minigame = args[2];
			Environment environment = (args.length == 4) ? validateWorldType(args[3]) : Environment.NORMAL;
			if(Bukkit.getPlayerExact(playername) == null){ //TODO: replace with server-specific code (check players that have logged into the server at least once)
				errorMessage(player, "Unkown player: \"" + playername + "\"");
				return false; 
			}
			if(worldExists(worldname)){ 
				errorMessage(player, "The world \"" + worldname + "\" already exists");
				return false; 
			}
			if(false){ //todo: check if minigame name is valid
				errorMessage(player, "Invalid minigame name \"" + minigame + "\".");
				return false; 
			}
			if(environment == null){
				errorMessage(player, "The environment \"" + args[3] + "\" is invalid");
				return false; 
			}
			if(!mvcore.getMVWorldManager().addWorld(worldname, environment, "01", WorldType.NORMAL, false, "VoidGenerator")){
				errorMessage(player, "World creation failed :X");
				return false;
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
				errorMessage(player, "World was created but cannot be found");
				return false;
			}
			setPerm("group Builder set multiverse.access." + worldname);				// /perm group Builder set multiverse.access.WorldName
			setPerm("group Builder set " + worldname + ":worldedit.*");					// /perm group Builder set WorldName:worldedit.*
			setPerm("group Builder set " + worldname + ":minecraft.command.save-all");	// /perm group Builder set WorldName:minecraft.command.save-all
			setPerm("group Builder set " + worldname + ":" + minigame + ".test");		// /perm group Builder set WorldName:colorfall.test
			setPerm("player "+ playername +" addgroup Builder");						// /perm player PlayerName addgroup Builder
			return true;
		}
		return false;
	}

	private boolean setPerm(String command){
		return Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "perm "+ command);
	}

	private void errorMessage(Player player, String message){
		if(player != null && player.isOnline() && message!= null){
			player.sendMessage(ChatColor.RED + message);
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