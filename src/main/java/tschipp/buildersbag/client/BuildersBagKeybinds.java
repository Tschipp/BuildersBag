package tschipp.buildersbag.client;


import org.lwjgl.input.Keyboard;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Loader;

public class BuildersBagKeybinds
{

	public static final KeyBinding openBaubleBag = new KeyBinding("keybind.openBaubleBag", Keyboard.KEY_R, "buildersbag.name");
	
	public static void registerKeybinds()
	{
		if(Loader.isModLoaded("baubles"))
			ClientRegistry.registerKeyBinding(openBaubleBag);
	}
	
}
