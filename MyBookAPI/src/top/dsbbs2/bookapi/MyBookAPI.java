package top.dsbbs2.bookapi;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class MyBookAPI extends JavaPlugin {
	public static final String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",")
			.split(",")[3];
	public static Class<?> CustomPacket;
	public static Class<?> PacketSer;
	public static Constructor<?> CustomCon;
	public static Constructor<?> PacketSerCon;
	public static Class<?> CraftPlayerClass;
	public static Class<?> EntityPlayerClass;
	public static Class<?> PlayerConnectionClass;
	public static Class<?> PacketClass;
	public static Method sendPacketMethod;
	public static Field playerConnectionField;
	public static Method getHandleMethod;
	static {
		try {
			CustomPacket = Class.forName("net.minecraft.server." + version + ".PacketPlayOutCustomPayload");
			PacketSer = Class.forName("net.minecraft.server." + version + ".PacketDataSerializer");
			PacketSerCon = PacketSer.getConstructor(ByteBuf.class);
			CustomCon = CustomPacket.getConstructor(String.class, PacketSer);
			CraftPlayerClass = Class.forName("org.bukkit.craftbukkit." + version + ".entity.CraftPlayer");
			EntityPlayerClass = Class.forName("net.minecraft.server." + version + ".EntityPlayer");
			PlayerConnectionClass = Class.forName("net.minecraft.server." + version + ".PlayerConnection");
			PacketClass = Class.forName("net.minecraft.server." + version + ".Packet");
			sendPacketMethod = PlayerConnectionClass.getMethod("sendPacket", PacketClass);
			playerConnectionField = EntityPlayerClass.getField("playerConnection");
			getHandleMethod = CraftPlayerClass.getMethod("getHandle", new Class<?>[0]);
		} catch (Throwable e) {
			throw new RuntimeException(new Error("Could not initialize MyBookAPI", e));
		}
	}

	@Deprecated
	public void openBook_noExc(Player p, ItemStack book) {
		try {
			openBook(p, book);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public void openBook(Player p, ItemStack book) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, SecurityException {
		int slot = p.getInventory().getHeldItemSlot();
		ItemStack old = p.getInventory().getItem(slot);
		try {
			p.getInventory().setItem(slot, book);
			ByteBuf buf = Unpooled.buffer(256);
			buf.setByte(0, (byte) 0);
			buf.writerIndex(1);
			sendPacketMethod.invoke(
					playerConnectionField.get(getHandleMethod.invoke(CraftPlayerClass.cast(p), new Object[0])),
					CustomCon.newInstance("MC|BOpen", PacketSerCon.newInstance(buf)));
		} finally {
			p.getInventory().setItem(slot, old);
		}
	}
}
