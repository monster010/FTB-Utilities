package ftb.utils.client.gui;

import ftb.lib.TextureCoords;
import ftb.lib.api.client.FTBLibClient;
import ftb.lib.api.gui.GuiLM;
import ftb.lib.api.gui.widgets.*;
import ftb.utils.api.guide.*;
import ftb.utils.client.FTBUClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.util.*;

public class GuiGuide extends GuiLM
{
	public static final ResourceLocation tex = new ResourceLocation("ftbu", "textures/gui/guide.png");
	public static final TextureCoords tex_slider = new TextureCoords(tex, 0, 240, 12, 18, 512, 512);
	public static final TextureCoords tex_back = new TextureCoords(tex, 0, 260, 15, 11, 512, 512);
	public static final TextureCoords tex_close = new TextureCoords(tex, 0, 271, tex_back.width, tex_back.height, 512, 512);
	
	public static final int textColor = 0xFF7B6534;
	public static final int textColorOver = 0xFF9D6A00;
	public static final int maxCategoryButtons = 15;
	public static final int maxTextLines = 20;
	
	public final GuiGuide parentGui;
	public final GuideCategory category;
	public GuideCategory selectedCategory;
	
	public final SliderLM sliderCategories, sliderText;
	public final ButtonLM buttonBack;
	
	public final WidgetLM categoriesPanel, textPanel;
	public final List<GuideTextLine> allTextLines;
	public final List<GuideButtonCategory> categoryButtons; // Max 16
	public final GuideButtonTextLine[] textLines; // Max 20
	
	public static GuiGuide clientGuideGui = null;
	
	public static GuiGuide openClientGui(boolean open)
	{
		if(clientGuideGui == null) clientGuideGui = new GuiGuide(null, GuideRepoList.clientGuideFile.main);
		if(open) FTBLibClient.openGui(clientGuideGui);
		return clientGuideGui;
	}
	
	public GuiGuide(GuiGuide g, GuideCategory c)
	{
		super(null, tex);
		parentGui = g;
		category = c;
		selectedCategory = category;
		
		mainPanel.width = 328;
		mainPanel.height = 240;
		
		sliderCategories = new SliderLM(this, 11, 14, tex_slider.widthI(), 210, tex_slider.heightI())
		{
			public boolean canMouseScroll()
			{ return gui.mouse().x < mainPanel.posX + mainPanel.width / 2; }
			
			public boolean isEnabled()
			{ return category.subcategories.size() > maxCategoryButtons; }
		};
		
		sliderCategories.isVertical = true;
		
		sliderText = new SliderLM(this, 304, 14, tex_slider.widthI(), 210, tex_slider.heightI())
		{
			public boolean canMouseScroll()
			{ return gui.mouse().x > mainPanel.posX + mainPanel.width / 2; }
			
			public boolean isEnabled()
			{ return allTextLines.size() > maxTextLines; }
		};
		
		sliderText.isVertical = true;
		
		buttonBack = new ButtonLM(this, 35, 12, tex_back.widthI(), tex_back.heightI())
		{
			public void onButtonPressed(int b)
			{
				FTBLibClient.playClickSound();
				
				if(selectedCategory == category || category.getFormattedText().isEmpty())
				{
					if(parentGui == null) mc.thePlayer.closeScreen();
					else
					{
						parentGui.selectedCategory = parentGui.category;
						parentGui.sliderText.value = 0F;
						FTBLibClient.openGui(parentGui);
					}
				}
				else
				{
					selectedCategory = category;
					sliderText.value = 0F;
					if(parentGui != null) parentGui.refreshText();
					initLMGui();
				}
			}
		};
		
		categoriesPanel = new WidgetLM(this, 33, 29, 128, 200);
		textPanel = new PanelLM(this, 167, 10, 128, 219)
		{
			public void addWidgets()
			{ addAll(textLines); }
		};
		
		allTextLines = new ArrayList<>();
		categoryButtons = new ArrayList<>();
		textLines = new GuideButtonTextLine[maxTextLines];
		
		for(int i = 0; i < maxTextLines; i++)
			textLines[i] = new GuideButtonTextLine(this, i);
	}
	
	public void addWidgets()
	{
		mainPanel.add(sliderCategories);
		mainPanel.add(sliderText);
		mainPanel.add(buttonBack);
		mainPanel.add(textPanel);
		
		categoryButtons.clear();
		int catl = category.subcategories.size();
		int off = 0;
		
		if(catl > maxCategoryButtons)
		{
			float f = sliderCategories.value * (catl - 1 - maxCategoryButtons);
			off = (int) f;
			sliderCategories.scrollStep = 1F / (catl - 1 - maxCategoryButtons);
		}
		
		List<GuideCategory> categoryList = new ArrayList<>();
		categoryList.addAll(category.subcategories.values());
		Collections.sort(categoryList);
		
		for(int i = 0; i < maxCategoryButtons; i++)
		{
			if(i + off < catl)
			{
				categoryButtons.add(new GuideButtonCategory(GuiGuide.this, categoriesPanel.posX, categoriesPanel.posY + i * 13, categoriesPanel.width, 13, categoryList.get(off + i)));
			}
		}
		
		mainPanel.addAll(categoryButtons);
	}
	
	public void initLMGui()
	{
		if(category.getParentTop() == GuideRepoList.clientGuideFile.main) clientGuideGui = this;
		
		allTextLines.clear();
		
		GuideFile file = selectedCategory.getFile();
		if(file == null) return;
		
		String s = selectedCategory.getFormattedText();
		if(s != null && s.length() > 0)
		{
			boolean uni = fontRendererObj.getUnicodeFlag();
			fontRendererObj.setUnicodeFlag(FTBUClient.guide_unicode.get());
			List<String> list = fontRendererObj.listFormattedStringToWidth(s.trim(), textPanel.width);
			
			for(int i = 0; i < list.size(); i++)
			{
				GuideTextLine l = new GuideTextLine(null);
				l.text = list.get(i);
				l.special = file.getGuideLink(l.text);
				allTextLines.add(l);
				
				if(l.special != null)
				{
					if(l.special.type.isText())
					{
						l.text = l.special.getTitle().getFormattedText();
						List<String> list1 = fontRendererObj.listFormattedStringToWidth(l.text, textPanel.width);
						
						if(list1.size() > 1)
						{
							l.text = list1.get(0);
							for(int j = 1; j < list1.size(); j++)
							{
								GuideTextLine l1 = new GuideTextLine(l);
								l1.text = list1.get(j);
								l1.special = l.special;
								allTextLines.add(l1);
							}
						}
					}
					else if(l.special.type.isImage())
					{
						try
						{
							TextureCoords tex = ((GuideLink.GuideImage) l.special).getTexture();
							
							if(tex.isValid())
							{
								l.text = "";
								int lines = (int) (1D + tex.getHeight(Math.min(textPanel.width, tex.width)) / 11D);
								
								TextureCoords[] splitTex = tex.split(1, lines);
								
								l.texture = splitTex[0];
								
								for(int j = 1; j < lines; j++)
								{
									GuideTextLine l1 = new GuideTextLine(l);
									l1.text = "";
									l1.special = l.special;
									l1.texture = splitTex[j];
									allTextLines.add(l1);
								}
							}
						}
						catch(Exception e1)
						{
							e1.printStackTrace();
						}
					}
				}
				
				if(!l.text.isEmpty()) l.text = l.text.replace('\ufffd', '\u00a7');
			}
			
			fontRendererObj.setUnicodeFlag(uni);
		}
		
		refreshText();
	}
	
	public void refreshText()
	{
		for(int i = 0; i < textLines.length; i++)
			textLines[i].line = null;
		
		int lines = allTextLines.size();
		int off = 0;
		
		if(lines > maxTextLines)
		{
			float f = sliderText.value * (lines - maxTextLines);
			off = (int) f;
			sliderText.scrollStep = 1F / (lines - maxTextLines);
		}
		
		for(int i = 0; i < maxTextLines; i++)
		{
			if(i + off < lines) textLines[i].line = allTextLines.get(i + off);
		}
	}
	
	public void drawTexturedModalRectD(double x, double y, double u, double v, double w, double h)
	{ drawTexturedModalRectD(x, y, zLevel, u, v, w, h, 512, 512); }
	
	public void drawBackground()
	{
		if(sliderCategories.isEnabled() && sliderCategories.update()) refreshWidgets();
		
		if(sliderText.isEnabled() && sliderText.update()) refreshText();
		
		super.drawBackground();
		
		if(sliderCategories.isEnabled()) sliderCategories.renderSlider(tex_slider);
		if(sliderText.isEnabled()) sliderText.renderSlider(tex_slider);
		
		FTBLibClient.setGLColor(buttonBack.mouseOver() ? textColorOver : textColor, 255);
		buttonBack.render((parentGui == null) ? tex_close : tex_back);
		GlStateManager.color(1F, 1F, 1F, 1F);
		
		fontRendererObj.drawString(category.getTitleComponent().getFormattedText(), mainPanel.posX + 53, mainPanel.posY + 14, textColor);
		
		boolean uni = fontRendererObj.getUnicodeFlag();
		fontRendererObj.setUnicodeFlag(FTBUClient.guide_unicode.get());
		for(int i = 0; i < textLines.length; i++)
			textLines[i].renderWidget();
		fontRendererObj.setUnicodeFlag(uni);
		
		if(!categoryButtons.isEmpty())
		{
			for(int i = 0; i < categoryButtons.size(); i++)
				categoryButtons.get(i).renderWidget();
		}
	}
}