package ftb.utils.cmd.admin;

import ftb.lib.api.ForgePlayerMP;
import ftb.lib.api.cmd.*;
import latmod.lib.LMStringUtils;
import net.minecraft.command.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CmdListFriends extends CommandLM
{
	public CmdListFriends()
	{ super("list_friends", CommandLevel.OP); }
	
	public String getCommandUsage(ICommandSender ics)
	{ return '/' + commandName + " <player>"; }
	
	public boolean isUsernameIndex(String[] args, int i)
	{ return i == 0; }
	
	public void execute(MinecraftServer server, ICommandSender ics, String[] args) throws CommandException
	{
		checkArgs(args, 1);
		ForgePlayerMP p = ForgePlayerMP.get(args[0]);
		ics.addChatMessage(new TextComponentString(LMStringUtils.strip(p.getFriends())));
	}
}