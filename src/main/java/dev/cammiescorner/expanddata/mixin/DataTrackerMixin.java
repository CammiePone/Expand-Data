package dev.cammiescorner.expanddata.mixin;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;

import java.util.ArrayList;
import java.util.List;

@Mixin(DataTracker.class)
public abstract class DataTrackerMixin {
	@Shadow private static <T> DataTracker.Entry<T> entryFromPacket(PacketByteBuf buf, int i, TrackedDataHandler<T> trackedDataHandler) { return null; }
	@Unique private static final int MAX_ID = 2147483646;
	@Unique private static final int EOF = 2147483647;

	/**
	 * Increase max number of DataTrackers from 254 to 2,147,483,646.
	 */
	@ModifyConstant(method = "registerData", constant = @Constant(intValue = 254))
	private static int registerDataInt(int value) {
		return MAX_ID;
	}

	@ModifyConstant(method = "startTracking", constant = @Constant(intValue = 254))
	private static int startTrackingInt(int value) {
		return MAX_ID;
	}

	/**
	 * Handle the errors so they give the right error message.
	 */
	@ModifyArg(method = "registerData", at = @At(value = "INVOKE", target = "Ljava/lang/IllegalArgumentException;<init>(Ljava/lang/String;)V"))
	private static String registerDataString(String string) {
		return "Data value id is too big! (Max is 2,147,483,646)";
	}

	@ModifyArg(method = "startTracking", at = @At(value = "INVOKE", target = "Ljava/lang/IllegalArgumentException;<init>(Ljava/lang/String;)V", ordinal = 0))
	private String startTrackingString(String value) {
		return "Data value id is too big! (Max is 2,147,483,646)";
	}

	/**
	 * Change the ByteBuf to use an int instead of a byte.
	 */
	@Redirect(method = "entriesToPacket", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;writeByte(I)Lio/netty/buffer/ByteBuf;"))
	private static ByteBuf entriesToPacket(PacketByteBuf packetByteBuf, int i) {
		return packetByteBuf.writeInt(EOF);
	}

	@Redirect(method = "writeEntryToPacket", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;writeByte(I)Lio/netty/buffer/ByteBuf;"))
	private static ByteBuf writeEntryToPacket(PacketByteBuf packetByteBuf, int i) {
		return packetByteBuf.writeInt(i);
	}

	/**
	 * @author Cammie
	 * @reason Just needed to change a variable from a short to an int.
	 */
	@Nullable
	@Overwrite
	public static List<DataTracker.Entry<?>> deserializePacket(PacketByteBuf buf) {
		ArrayList<DataTracker.Entry<?>> list = null;
		int i;

		while((i = buf.readInt()) != EOF) {
			if(list == null)
				list = Lists.newArrayList();

			int j = buf.readVarInt();
			TrackedDataHandler<?> trackedDataHandler = TrackedDataHandlerRegistry.get(j);

			if(trackedDataHandler == null)
				throw new DecoderException("Unknown serializer type " + j);

			list.add(entryFromPacket(buf, i, trackedDataHandler));
		}

		return list;
	}
}
