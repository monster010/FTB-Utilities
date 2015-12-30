package latmod.ftbu.mod.client;
import cpw.mods.fml.relauncher.*;
import ftb.lib.*;
import ftb.lib.api.config.ClientConfigRegistry;
import ftb.lib.api.gui.LMGuiHandlerRegistry;
import ftb.lib.client.FTBLibClient;
import ftb.lib.mod.client.FTBLibGuiEventHandler;
import ftb.lib.notification.ClientNotifications;
import latmod.ftbu.badges.ThreadLoadBadges;
import latmod.ftbu.mod.*;
import latmod.ftbu.mod.client.gui.claims.ClaimedAreasClient;
import latmod.ftbu.mod.cmd.CmdMath;
import latmod.ftbu.tile.TileLM;
import latmod.ftbu.util.client.LatCoreMCClient;
import latmod.ftbu.world.*;
import latmod.lib.config.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraftforge.client.ClientCommandHandler;

@SideOnly(Side.CLIENT)
public class FTBUClient extends FTBUCommon // FTBLibModClient
{
	public static final ConfigEntryBool render_badges = new ConfigEntryBool("render_badges", true);
	
	public static final ConfigEntryBool render_my_badge = new ConfigEntryBool("render_my_badge", true)
	{
		public boolean get()
		{ return LMWorldClient.inst.getClientPlayer().renderBadge; }
		
		public void set(boolean b)
		{
			if(LMWorldClient.inst != null)
			{
				LMWorldClient.inst.getClientPlayer().renderBadge = b;
				LMWorldClient.inst.getClientPlayer().getSettings().update();
			}
		}
	}.setExcluded();
	
	public static final ConfigEntryBool chat_links = new ConfigEntryBool("chat_links", true)
	{
		public boolean get()
		{ return LMWorldClient.inst.getClientPlayer().getSettings().chatLinks; }
		
		public void set(boolean b)
		{
			if(LMWorldClient.inst != null)
			{
				LMWorldClient.inst.getClientPlayer().getSettings().chatLinks = b;
				LMWorldClient.inst.getClientPlayer().getSettings().update();
			}
		}
	}.setExcluded();
	
	public static final ConfigEntryBool player_options_shortcut = new ConfigEntryBool("player_options_shortcut", false);
	public static final ConfigEntryBool sort_friends_az = new ConfigEntryBool("sort_friends_az", false);
	public static final ConfigEntryBool hide_armor_fg = new ConfigEntryBool("hide_armor_fg", false).setHidden();
	
	public static void onWorldJoined()
	{
		ThreadLoadBadges.init();
		ClientNotifications.init();
	}
	
	public static void onWorldClosed()
	{
		ClientNotifications.init();
		ClaimedAreasClient.clear();
	}
	
	public void preInit()
	{
		JsonHelper.initClient();
		EventBusHelper.register(FTBUClientEventHandler.instance);
		EventBusHelper.register(FTBURenderHandler.instance);
		EventBusHelper.register(FTBUGuiEventHandler.instance);
		EventBusHelper.register(FTBUBadgeRenderer.instance);
		
		ClientConfigRegistry.add(new ConfigGroup("ftbu").addAll(FTBUClient.class, null, false));
		ClientCommandHandler.instance.registerCommand(new CmdMath());
		FTBLibGuiEventHandler.sidebar_buttons_config.addAll(FTBUGuiEventHandler.class, null, false);
	}
	
	public void postInit()
	{
		LMGuiHandlerRegistry.add(FTBUGuiHandler.instance);
		FTBUClickAction.init();
	}
	
	public LMWorld getClientWorldLM()
	{ return LMWorldClient.inst; }
	
	public void readTileData(TileLM t, S35PacketUpdateTileEntity p)
	{
		NBTTagCompound data = p.func_148857_g();
		t.readTileData(data);
		t.readTileClientData(data);
		t.onUpdatePacket();
		LatCoreMCClient.onGuiClientAction();
	}
	
	public static void onReloaded()
	{
		FTBLibClient.clearCachedData();
		ThreadLoadBadges.init();
		
		if(LMWorldClient.inst != null)
		{
			for(LMPlayerClient p : LMWorldClient.inst.playerMap)
				p.onReloaded();
		}
	}
}