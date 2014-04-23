package net.minecraft.network.play.server;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.io.IOException;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

public class S2BPacketChangeGameState extends Packet
{
	public static final String[] field_149142_a = new String[] {"tile.bed.notValid", null, null, "gameMode.changed"};
	private int field_149140_b;
	private float field_149141_c;
	private static final String __OBFID = "CL_00001301";

	public S2BPacketChangeGameState() {}

	public S2BPacketChangeGameState(int p_i45194_1_, float p_i45194_2_)
	{
		this.field_149140_b = p_i45194_1_;
		this.field_149141_c = p_i45194_2_;
	}

	public void readPacketData(PacketBuffer p_148837_1_) throws IOException
	{
		this.field_149140_b = p_148837_1_.readUnsignedByte();
		this.field_149141_c = p_148837_1_.readFloat();
	}

	public void writePacketData(PacketBuffer p_148840_1_) throws IOException
	{
		p_148840_1_.writeByte(this.field_149140_b);
		p_148840_1_.writeFloat(this.field_149141_c);
	}

	public void processPacket(INetHandlerPlayClient p_149139_1_)
	{
		p_149139_1_.handleChangeGameState(this);
	}

	public void processPacket(INetHandler p_148833_1_)
	{
		this.processPacket((INetHandlerPlayClient)p_148833_1_);
	}

	@SideOnly(Side.CLIENT)
	public int func_149138_c()
	{
		return this.field_149140_b;
	}

	@SideOnly(Side.CLIENT)
	public float func_149137_d()
	{
		return this.field_149141_c;
	}
}