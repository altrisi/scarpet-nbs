package altrisi.mods.scnbs;

import carpet.script.value.NBTSerializableValue;
import carpet.script.value.Value;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtElement;
import nota.player.SongPlayer;

public class SongPlayerValue extends Value {
	private final SongPlayer player;
	
	public SongPlayerValue(SongPlayer player) {
		this.player = player;
	}
	
	public SongPlayer getPlayer() {
		return player;
	}

	@Override
	public String getString() {
		return "(song player of type: " + player.getClass().getSimpleName() + ")";
	}

	@Override
	public boolean getBoolean() {
		return player.isPlaying();
	}
	
	@Override
	public String getTypeString() {
		return "song_player";
	}

	@Override
	public NbtElement toTag(boolean force) {
		if (force) return NbtByte.ZERO;
		throw new NBTSerializableValue.IncompatibleTypeException(this);
	}

}
