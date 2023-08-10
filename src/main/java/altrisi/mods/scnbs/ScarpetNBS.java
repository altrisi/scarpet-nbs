package altrisi.mods.scnbs;

import java.util.List;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.reflect.TypeToken;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import carpet.script.annotation.*;
import carpet.script.exception.InternalExpressionException;
import carpet.script.value.ListValue;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import nota.model.Playlist;
import nota.model.Song;
import nota.player.SongPlayer;

public class ScarpetNBS implements ModInitializer, CarpetExtension {
    public static final Logger LOGGER = LoggerFactory.getLogger("scarpet-nbs");

    @Override
	public void onInitialize() {
		CarpetServer.manageExtension(new ScarpetNBS());

		SimpleTypeConverter.registerType(SongValue.class, Song.class, SongValue::getSong, "song");
		SimpleTypeConverter.registerType(SongPlayerValue.class, SongPlayer.class, SongPlayerValue::getPlayer, "song");
		OutputConverter.register(Song.class, SongValue::new);
		OutputConverter.register(SongPlayer.class, SongPlayerValue::new);
		
		@SuppressWarnings("serial")
		var type = typeOf(new TypeToken<List<Song>>() {});
		ValueConverter<List<Song>> converter = ValueConverter.fromAnnotatedType(type);
		
		SimpleTypeConverter.registerType(ListValue.class, Playlist.class, (v, c) -> playlistOf(converter.convert(v, c)), "list of songs");

		AnnotationParser.parseFunctionClass(Functions.class);
	}

	private static Playlist playlistOf(List<Song> songs) {
		if (songs.size() == 0) throw new InternalExpressionException("Must play at least a song");
		return new Playlist(songs.toArray(Song[]::new));
	}
	

    @Override
    public void onPlayerLoggedIn(ServerPlayerEntity player) {
    	PlayerHandler.onLogin(player);
    }

    @Override
    public void onPlayerLoggedOut(ServerPlayerEntity player) {
    	PlayerHandler.onLogout(player);
    }

    @Override
    public void onServerClosed(MinecraftServer server) {
    	PlayerHandler.clear();
    }
    
    @Override
    public String version() {
    	return "scarpet-nbs";
    }

	private static AnnotatedType typeOf(TypeToken<?> token) {
		return ((AnnotatedParameterizedType) token.getClass().getAnnotatedSuperclass()).getAnnotatedActualTypeArguments()[0];
	}
}
