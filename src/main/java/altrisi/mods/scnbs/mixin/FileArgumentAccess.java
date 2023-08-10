package altrisi.mods.scnbs.mixin;

import java.nio.file.Path;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import carpet.script.Module;
import carpet.script.argument.FileArgument;

@Mixin(value = FileArgument.class, remap = false)
public interface FileArgumentAccess {
	@Invoker
	Path invokeToPath(Module module);
}
