package dev.lone.iamegbenchmark;

import io.netty.channel.ChannelPipeline;
import net.minecraft.network.Connection;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class PacketsBenchmark
{
    static Field field_connection;

    static
    {
        field_connection = getField(ServerCommonPacketListenerImpl.class, Connection.class);
    }

    private final Plugin plugin;
    private final List<Player> players = new ArrayList<>();

    @Nullable
    public static Field getField(Class<?> clazz, Class<?> type)
    {
        for (Field field : clazz.getDeclaredFields())
        {
            if (field.getType() == type)
            {
                field.setAccessible(true);
                return field;
            }
        }
        return null;
    }

    public PacketsBenchmark(Plugin plugin)
    {
        this.plugin = plugin;
    }

    public void register(Player player, boolean onlyRaw)
    {
        final ServerGamePacketListenerImpl listener = ((CraftPlayer) player).getHandle().connection;
        if (!listener.isDisconnected())
        {
            try
            {
                Connection conn = (Connection) field_connection.get(listener);
                ChannelPipeline pipeline = conn.channel.pipeline();
                if(onlyRaw)
                {
                    pipeline.addAfter("decoder", "ia_meg_benchmark", new CustomChannelDuplexHandler());
                }
                else
                {
                    try
                    {
                        pipeline.addAfter("model_engine_packet_handler", "ia_meg_benchmark", new CustomChannelDuplexHandler());
                    }
                    catch (NoSuchElementException e)
                    {
                        pipeline.addAfter("packet_handler", "ia_meg_benchmark", new CustomChannelDuplexHandler());
                    }
                }
            }
            catch (Throwable e)
            {
                plugin.getLogger().severe("Failed to read player connection field. Some features might not work correctly!");
                e.printStackTrace();
            }
        }

        players.add(player);
    }

    public void unregister(Player player)
    {
        if(!players.remove(player))
            return;

        final ServerGamePacketListenerImpl listener = ((CraftPlayer) player).getHandle().connection;
        if (!listener.isDisconnected())
        {
            try
            {
                Connection conn = (Connection) field_connection.get(listener);
                try
                {
                    conn.channel.pipeline().remove("ia_meg_benchmark");
                }
                catch (NoSuchElementException ignored) {}
            }
            catch (Throwable e)
            {
                plugin.getLogger().severe("Failed to read player connection field. Some features might not work correctly!");
                e.printStackTrace();
            }
        }
    }

    public void cleanup()
    {
        for (Player player : players)
        {
            unregister(player);
        }
    }
}