//package tschipp.buildersbag.client.gui;
//
//import java.util.Set;
//
//import net.minecraft.client.Minecraft;
//
//public class GuiFactoryBuildersBag implements IModGuiFactory
//{
//    @Override
//    public void initialize(Minecraft minecraftInstance) {
//        // Do nothing
//    }
//
//    /*
//    @Override
//    public Class<? extends GuiScreen> mainConfigGuiClass() {
//        return GuiConfigCarryOn.class;
//    } */
//
//    @Override
//    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
//        return null;
//    }
//
//    /*
//    @Override
//    public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
//        return null;
//    } */
//
//   
//	@Override
//	public boolean hasConfigGui()
//	{
//		return true;
//	}
//
//	@Override
//	public GuiScreen createConfigGui(GuiScreen parentScreen)
//	{
//
//		return new GuiConfigBuildersBag(parentScreen);
//	}
//	
//
//
//}