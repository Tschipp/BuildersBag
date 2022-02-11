package tschipp.buildersbag.client;


import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import tschipp.buildersbag.common.item.BuildersBagItem;

public class BuildersBagKeybinds
{
	private static IKeyConflictContext NO_CONFLICT = new IKeyConflictContext() {

		@Override
		public boolean isActive()
		{
			return true;
		}

		@Override
		public boolean conflicts(IKeyConflictContext other)
		{
			return false;
		}
		
	};
	
	
	public static KeyBinding openBaubleBag;
	public static KeyBinding openSelectionWheel;

	public static void registerKeybinds()
	{
		openSelectionWheel = new KeyBinding("keybind.openSelectionWheel", NO_CONFLICT, InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, "buildersbag.name");
		ClientRegistry.registerKeyBinding(openSelectionWheel);
		
		if(ModList.get().isLoaded("baubles"))
		{
			openBaubleBag = new KeyBinding("keybind.openBaubleBag", GLFW.GLFW_KEY_R, "buildersbag.name");
			ClientRegistry.registerKeyBinding(openBaubleBag);
		}
	}
	
	public static boolean isMenuKeyPressed()
	{
		if(Minecraft.getInstance().screen != null)
		{
			return false;
		}
		
		ItemStack main = Minecraft.getInstance().player.getMainHandItem();
		ItemStack off = Minecraft.getInstance().player.getOffhandItem();
		if(!(main.getItem() instanceof BuildersBagItem) && !(off.getItem() instanceof BuildersBagItem))
			return false;
		
		if(KeyModifier.ALT.matches(openSelectionWheel.getKey()))
		{
			return KeyModifier.ALT.isActive(NO_CONFLICT);
		}
		else if(KeyModifier.CONTROL.matches(openSelectionWheel.getKey()))
		{
			return KeyModifier.CONTROL.isActive(NO_CONFLICT);
		}else if(KeyModifier.SHIFT.matches(openSelectionWheel.getKey()))
		{
			return KeyModifier.SHIFT.isActive(NO_CONFLICT);
		}
		else
			return openSelectionWheel.isDown();
	}
	
}
