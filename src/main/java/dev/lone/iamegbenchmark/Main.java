package dev.lone.iamegbenchmark;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.generator.blueprint.ModelBlueprint;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import dev.lone.itemsadder.api.CustomEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class Main extends JavaPlugin implements Listener, CommandExecutor, TabCompleter
{
    private static final String ENTITY_ID_ITEMSADDER = "custom:ninja_skeleton";
    private static final String ENTITY_ID_MEG = "ninja_skeleton";
    public static boolean hasModelEngine4;

    List<Entity> entities = new ArrayList<>();

    PacketsBenchmark packetsBenchmark;

    private static Main inst;

    public static Plugin inst()
    {
        return inst;
    }

    @Override
    public void onEnable()
    {
        inst = this;

        hasModelEngine4 = false;
        try
        {
            Class.forName("com.ticxo.modelengine.api.nms.network.ProtectedPacket");
            hasModelEngine4 = true;
        }
        catch (ClassNotFoundException ignored) {}

        Bukkit.getPluginManager().registerEvents(this, this);
        packetsBenchmark = new PacketsBenchmark(this);
    }

    @Override
    public void onDisable()
    {
        packetsBenchmark.cleanup();
    }

    @EventHandler
    private void quit(PlayerQuitEvent e)
    {
        packetsBenchmark.unregister(e.getPlayer());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args)
    {
        if(!(sender instanceof Player player))
        {
            sender.sendMessage("This command can be executed only by players!");
            return true;
        }

        Location loc = player.getLocation();
        loc.setYaw(0);
        loc.setPitch(0);

        String action = getArg(args, 0);
        boolean ai = !getArg(args, 1).equals("noai");
        String pluginName = getArg(args, 2);
        switch (action)
        {
            case "small" -> spawnEntities(player, loc, pluginName, ai, 5);
            case "stress" -> spawnEntities(player, loc, pluginName, ai, (int) Math.sqrt(getArg(args, 3, 500)));
            case "clean" ->
            {
                for (Iterator<Entity> iterator = entities.iterator(); iterator.hasNext(); )
                {
                    Entity entity = iterator.next();
                    entity.remove();
                    iterator.remove();
                }
            }
            case "add-ai-nearest" -> loc.getNearbyEntitiesByType(LivingEntity.class, 10, 10, 10)
                    .stream()
                    .filter(entity -> entity.getType() != EntityType.PLAYER && entity.getType() != EntityType.ARMOR_STAND)
                    .min(Comparator.comparingDouble(a -> a.getLocation().distanceSquared(loc))).ifPresent(livingEntity -> livingEntity.setAI(true));
            case "hook-packets-counter" ->
            {
                packetsBenchmark.unregister(player);
                packetsBenchmark.register(player, getArg(args, 1).equals("only-raw"));
            }
        }

        return true;
    }

    private void spawnEntities(Player player, Location loc, String mode, boolean ai, int iterations)
    {
        switch (mode)
        {
            case "itemsadder" ->
            {
                for (int x = 0; x < iterations; x++)
                {
                    for (int z = 0; z < iterations; z++)
                    {
                        CustomEntity custom = CustomEntity.spawn(ENTITY_ID_ITEMSADDER, loc.clone().add(x + 1, 0, z + 1));
                        LivingEntity entity = (LivingEntity) custom.getEntity();
                        entity.setAI(ai);
                        entity.setInvulnerable(true);
                        entities.add(entity);
                    }
                }
            }
            case "meg" ->
            {
                for (int x = 0; x < iterations; x++)
                {
                    for (int z = 0; z < iterations; z++)
                    {
                        ModelBlueprint blueprint = ModelEngineAPI.getBlueprint(ENTITY_ID_MEG);
                        ActiveModel megModel = ModelEngineAPI.createActiveModel(blueprint);

                        Zombie entity = loc.getWorld().spawn(loc.clone().add(x + 1, 0, z + 1), Zombie.class, en -> {
                            en.setAI(ai);
                            en.setInvulnerable(true);
                        });
                        ModeledEntity megEntity = ModelEngineAPI.createModeledEntity(ModelEngineAPI.createModeledEntity(entity).getBase());

                        megEntity.addModel(megModel, false);
                        megEntity.setBaseEntityVisible(false);
//                        megEntity.getRangeManager().addTrackedPlayer(player);
//                        megEntity.getRangeManager().setRenderDistance(loc.getWorld().getViewDistance() * 16);
//                        megEntity.setState(ModelState.IDLE);

                        entities.add(entity);
                    }
                }
            }
        }
    }

    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args)
    {
        List<String> strings = new ArrayList<>();

        if(args.length == 1)
        {
            strings.add("hook-packets-counter");
            strings.add("add-ai-nearest");
            strings.add("clean");
            strings.add("small");
            strings.add("stress");
        }
        else if(args.length == 2)
        {
            if(args[0].equals("hook-packets-counter"))
            {
                strings.add("only-raw");
            }
            else
            {
                strings.add("ai");
                strings.add("noai");
            }
        }
        else if(args.length == 3)
        {
            strings.add("itemsadder");
            strings.add("meg");
        }
        else if(args.length == 4)
        {
            if(args[0].equals("stress"))
            {
                strings.add("20");
                strings.add("50");
                strings.add("100");
                strings.add("500");
                strings.add("1000");
            }
        }

        return strings;
    }

    public static String getArg(String[] args, int index)
    {
        if(index >= args.length)
            return "";
        return args[index];
    }

    private int getArg(String[] args, int index, int def)
    {
        String arg = getArg(args, index);
        if(arg.equals(""))
            return def;
        try
        {
            return Integer.parseInt(arg);
        }
        catch (Throwable ignored) {}
        return def;
    }
}
