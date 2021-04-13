//package tschipp.buildersbag.client.gui;
//
//import java.util.List;
//
//import tschipp.buildersbag.BuildersBag;
//import tschipp.buildersbag.common.config.BuildersBagConfig;
//
//public class GuiConfigBuildersBag extends GuiConfig
//{
//	private static final String LANG_PREFIX = BuildersBag.MODID + ".category.";
//
//	public GuiConfigBuildersBag(GuiScreen parent)
//	{
//		super(parent, getConfigElements(), BuildersBag.MODID, false, false, "Builder's Bag Configuration");
//	}
//
//	private static List<IConfigElement> getConfigElements()
//	{
//
//		final Configuration configuration = BuildersBagConfig.EventHandler.getConfiguration();
//
//		final ConfigCategory topLevelCategory = configuration.getCategory(Configuration.CATEGORY_GENERAL);
//		topLevelCategory.getChildren().forEach(configCategory -> configCategory.setLanguageKey(GuiConfigBuildersBag.LANG_PREFIX + configCategory.getName()));
//
//		return new ConfigElement(topLevelCategory).getChildElements();
//	}
//
//	@Override
//	public void initGui()
//	{
//		super.initGui();
//	}
//
//	@Override
//	public void drawScreen(int mouseX, int mouseY, float partialTicks)
//	{
//		super.drawScreen(mouseX, mouseY, partialTicks);
//	}
//
//	@Override
//	protected void actionPerformed(GuiButton button)
//	{
//		super.actionPerformed(button);
//	}
//}
