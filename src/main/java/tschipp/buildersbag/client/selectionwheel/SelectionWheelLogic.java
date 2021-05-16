//package tschipp.buildersbag.client.selectionwheel;
//
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//import net.minecraft.item.ItemStack;
//import tschipp.buildersbag.common.helper.ItemHelper;
//
//public class SelectionWheelLogic TODO
//{
//
//	static class SelectionPage
//	{
//		List<ItemStack> items = new ArrayList<ItemStack>(9);
//		Set<Integer> paletteIndices = new HashSet<Integer>();
//		int index;
//	
//		@Override
//		public String toString()
//		{
//			return items.toString();
//		}
//	}
//
//	static class SelectionFilter
//	{
//		List<SelectionPage> pages = new ArrayList<SelectionPage>();
//		private String name = "";
//	
//		public SelectionFilter(List<SelectionPage> pages, String name)
//		{
//			this.pages = pages;
//			this.name = name;
//		}
//	
//		@Override
//		public String toString()
//		{
//			return pages.toString();
//		}
//	}
//
//	static List<SelectionPage> createPages(List<ItemStack> stacks)
//	{
//		List<SelectionPage> pages = new ArrayList<SelectionPage>();
//	
//		int counter = 0;
//		int pageCount = 0;
//		SelectionPage currentPage = new SelectionPage();
//		for (int i = 0; i < stacks.size(); i++)
//		{
//			ItemStack provideable = stacks.get(i).copy();
//			currentPage.items.add(provideable);
//			if(!ItemHelper.containsStack(provideable, SelectionWheel.cap.getPalette()).isEmpty())
//				currentPage.paletteIndices.add(counter);
//			counter++;
//	
//			if (counter == 9)
//			{
//				pageCount++;
//				counter = 0;
//				pages.add(currentPage);
//				currentPage = new SelectionPage();
//				currentPage.index = pageCount;
//			}
//		}
//		if (!currentPage.items.isEmpty())
//			pages.add(currentPage);
//	
//		return pages;
//	}
//	
//	
//
//}
