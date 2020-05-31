package tschipp.buildersbag.client;


import org.lwjgl.input.Keyboard;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Loader;

public class BuildersBagKeybinds
{

	public static KeyBinding openBaubleBag;
	
	public static void registerKeybinds()
	{
		if(Loader.isModLoaded("baubles"))
		{
			openBaubleBag = new KeyBinding("keybind.openBaubleBag", Keyboard.KEY_R, "buildersbag.name");
			ClientRegistry.registerKeyBinding(openBaubleBag);
		}
	}
	
}
