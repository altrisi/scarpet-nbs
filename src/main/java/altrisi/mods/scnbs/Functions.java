package altrisi.mods.scnbs;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import altrisi.mods.scnbs.mixin.FileArgumentAccess;
import carpet.script.Context;
import carpet.script.annotation.ScarpetFunction;
import carpet.script.annotation.Locator;
import carpet.script.argument.FileArgument;
import carpet.script.exception.InternalExpressionException;
import carpet.script.exception.ThrowStatement;
import carpet.script.exception.Throwables;
import carpet.script.value.BlockValue;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import nota.model.Playlist;
import nota.model.Song;
import nota.player.EntitySongPlayer;
import nota.player.PositionSongPlayer;
import nota.player.RadioSongPlayer;
import nota.player.SongPlayer;
import nota.utils.NBSDecoder;

import static carpet.script.annotation.ScarpetFunction.UNLIMITED_PARAMS;

public class Functions {
	private static final Throwables INVALID_NBS = new Throwables("invalid_nbs", Throwables.IO_EXCEPTION);
	private static final Identifier ID = new Identifier("scnbs", "music_player");
	
	@ScarpetFunction
	public static Song load_song(Context c, String path, boolean shared) {
		var resource = FileArgument.recognizeResource(path + ".nbs", false, FileArgument.Type.ANY);
		var argument = new FileArgument(resource.getLeft(), FileArgument.Type.ANY, resource.getRight(), false, shared, FileArgument.Reason.READ, c.host);
		Path p = ((FileArgumentAccess) argument).invokeToPath(c.host.main);
		if (Files.notExists(p)) {
			return null; // returns null if file doesn't exist
		}
		try (InputStream is = Files.newInputStream(p)) { // fun fact, this API leaks file descriptors unless you open them yourself!
			Song ret = NBSDecoder.parse(is);
			// who designed this api???
			if (ret == null)
				throw new ThrowStatement("Invalid NBS file, probably", INVALID_NBS); // who knows why! only the log knows! maybe!
			return ret;
		} catch (IOException e) {
			throw new ThrowStatement("Error while reading nbs file", INVALID_NBS);
		}
	}
	
	@ScarpetFunction(maxParams = UNLIMITED_PARAMS)
	public static SongPlayer create_radio_player(Playlist songs, ServerPlayerEntity... targets) {
		var player = new RadioSongPlayer(songs);
		for (var target : targets)
			player.addPlayer(target);
		configure(player);
		PlayerHandler.registerRadio(player);
		return player;
	}
	
	@ScarpetFunction(maxParams = UNLIMITED_PARAMS)
	public static SongPlayer create_positioned_player(Playlist songs, @Locator.Block BlockValue pos, int distance) {
		var player = new PositionSongPlayer(songs, pos.getWorld());
		player.setBlockPos(pos.getPos());
		player.setDistance(distance);
		PlayerHandler.register(player, pos.getWorld().getServer());
		configure(player);
		return player;
	}
	
	@ScarpetFunction(maxParams = UNLIMITED_PARAMS)
	public static SongPlayer create_entity_player(Playlist songs, Entity e, int distance) {
		var player = new EntitySongPlayer(songs);
		player.setEntity(e);
		player.setDistance(distance);
		PlayerHandler.register(player, e.getServer());
		configure(player);
		return player;
	}
	
	@ScarpetFunction
	public static void set_playing(SongPlayer player, boolean playing) {
		player.setPlaying(playing);
	}
	
	@ScarpetFunction
	public static void add_to_radio(SongPlayer player, ServerPlayerEntity p) {
		asRadio(player).addPlayer(p);
	}

	@ScarpetFunction
	public static void remove_from_radio(SongPlayer player, ServerPlayerEntity p) {
		asRadio(player).removePlayer(p);
	}
	
	private static RadioSongPlayer asRadio(SongPlayer player) {
		if (!(player instanceof RadioSongPlayer radio))
			throw new InternalExpressionException("Provided player isn't a radio player");
		return radio;
	}
	
	// basic configurations for all players. Needs to be done last or risks killing the player because of stupid threading
	private static void configure(SongPlayer player) {
		player.setId(ID); // doesn't seem to do anything but why not ig
		player.setPlaying(true);
		player.setAutoDestroy(true);
	}
}
