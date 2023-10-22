package dev.lone.iamegbenchmark;

import com.ticxo.modelengine.api.nms.network.ProtectedPacket;
import dev.lone.iamegbenchmark.traffic.PacketsCounter;
import dev.lone.iamegbenchmark.traffic.TrafficDebug;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;

public class CustomChannelDuplexHandler extends ChannelDuplexHandler
{
    TrafficDebug trafficDebug = new TrafficDebug();
    private final PacketsCounter traffic = trafficDebug.get("main");


    @SuppressWarnings("rawtypes")
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception
    {
        super.write(ctx, msg, promise);

        // ModelEngine R4.0.2 packet
        if(Main.hasModelEngine4 && msg instanceof ProtectedPacket)
        {
            Packet wrapped = (Packet) ((ProtectedPacket) msg).packet();
            handle(wrapped);
        }
        else // Normal packet, not related to ModelEngine
        {
            handle(msg);
        }
    }

    private void handle(Object packet)
    {
        // Bundled packets
        if(packet instanceof ClientboundBundlePacket)
        {
            Iterable<Packet<ClientGamePacketListener>> subPackets = ((ClientboundBundlePacket) packet).subPackets();
            // ClientboundBundlePacket uses 2 packets as delimiters to explicitly limit the bundle of packets.
            traffic.increase("BUNDLE_START");
            traffic.increase("BUNDLE_END");
            for (Packet<ClientGamePacketListener> subPacket : subPackets)
            {
                // The actual packet
                traffic.increase(subPacket.getClass().getName());
            }
        }
        else // Normal packet
        {
            traffic.increase(packet.getClass().getName());
        }
    }
}
