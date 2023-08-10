package altrisi.mods.scnbs;

import org.apache.commons.lang3.StringUtils;

import carpet.script.exception.InternalExpressionException;
import carpet.script.value.NBTSerializableValue;
import carpet.script.value.NumericValue;
import carpet.script.value.StringValue;
import carpet.script.value.Value;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import nota.model.Song;

public class SongValue extends Value {
	private final Song song;
	
	public SongValue(Song song) {
		this.song = song;
	}
	
	public Song getSong() {
		return song;
	}

	@Override
	public String getString() {
		return StringUtils.isBlank(song.getTitle()) ? "(a song)" : song.getTitle();
	}

	@Override
	public boolean getBoolean() {
		return true;
	}
	
	@Override
	public String getTypeString() {
		return "song";
	}
	
	@Override
	public Value in(Value propValue) {
		String prop = propValue.getString();
		return switch (prop) {
			case "title"           -> emptyToNull(song.getTitle());
			case "length"          -> new NumericValue(song.getLength());
			case "author"          -> emptyToNull(song.getAuthor());
			case "original_author" -> emptyToNull(song.getOriginalAuthor());
			case "speed"           -> new NumericValue(song.getSpeed());
			default                -> throw new InternalExpressionException("Unknown property '" + prop + "'");
		};
	}
	
	private static Value emptyToNull(String str) {
		return StringValue.of(StringUtils.stripToNull(str));
	}

	@Override
	public NbtElement toTag(boolean force) {
		if (!force) {
            throw new NBTSerializableValue.IncompatibleTypeException(this);
        }
		return NbtString.of(song.getTitle());
	}

}
