package altrisi.mods.scnbs;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import nota.player.RadioSongPlayer;
import nota.player.SongPlayer;

/**
 * Handles players other than the radio one, given the others are quite bad in their api
 * (with this they still are, but at least are more usable).<p>
 * 
 * <s>Now it also handles radios, given otherwise they'll never be freed, and servers won't be able
 * to stop.</s> No, they still won't be able to stop given this thing also leaks threads!
 */
public class PlayerHandler {
    private static final Set<SongPlayer> handledPlayers = Collections.newSetFromMap(new WeakHashMap<>());
    private static final Set<RadioSongPlayer> handledRadios = Collections.newSetFromMap(new WeakHashMap<>());

    static void register(SongPlayer player, MinecraftServer server) {
    	handledPlayers.add(player);
    	for (var p : server.getPlayerManager().getPlayerList()) {
    		player.addPlayer(p);
    	}
    }

    static void onLogin(ServerPlayerEntity player) {
    	for (SongPlayer musicPlayer : handledPlayers)
    		musicPlayer.addPlayer(player);
    }
    
    static void onLogout(ServerPlayerEntity player) {
    	for (SongPlayer musicPlayer : handledPlayers)
    		musicPlayer.removePlayer(player);
    }
    
    // I don't trust this api to free stuff, though neither with this...
    // No need to not trust, it just doesn't free anything lol, had to add radios
    static void clear() {
    	for (var player : handledPlayers) {
    		player.destroy(); // ...
    	}
    	for (var player : handledRadios) {
    		player.destroy();
    	}
    	handledPlayers.clear();
    	handledRadios.clear();
    }

    // Radios are not fred at all
    static void registerRadio(RadioSongPlayer radio) {
    	handledRadios.add(radio);
    }
}
