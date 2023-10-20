package dev.lone.iamegbenchmark;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.animation.state.ModelState;
import com.ticxo.modelengine.api.generator.model.ModelBlueprint;
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
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class Main extends JavaPlugin implements Listener, CommandExecutor, TabCompleter
{
    private static final String ENTITY_ID_ITEMSADDER = "custom:ninja_skeleton";
    private static final String ENTITY_ID_MEG = "ninja_skeleton";

    List<Entity> entities = new ArrayList<>();

    @Override
    public void onEnable()
    {
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args)
    {
        if(!command.getName().equals("benchmarkentities"))
            return true;

        Player player = (Player) sender;
        Location loc = player.getLocation();
        loc.setYaw(0);
        loc.setPitch(0);
        if (args[0].startsWith("itemsadder"))
        {
            for (int x = 0; x < 5; x++)
            {
                for (int z = 0; z < 5; z++)
                {
                    CustomEntity custom = CustomEntity.spawn(ENTITY_ID_ITEMSADDER, loc.clone().add(x + 1, 0, z + 1));
                    LivingEntity entity = (LivingEntity) custom.getEntity();
                    if (args[0].endsWith("noai"))
                        entity.setAI(false);
                    entity.setInvulnerable(true);
                    entities.add(entity);
                }
            }
        }
        else if (args[0].startsWith("meg"))
        {
            for (int x = 0; x < 5; x++)
            {
                for (int z = 0; z < 5; z++)
                {
                    ModelBlueprint blueprint = ModelEngineAPI.getBlueprint(ENTITY_ID_MEG);
                    ActiveModel megModel = ModelEngineAPI.createActiveModel(blueprint);

                    Zombie entity = loc.getWorld().spawn(loc.clone().add(x + 1, 0, z + 1), Zombie.class, en -> {
                        if (args[0].endsWith("noai"))
                            en.setAI(false);
                        en.setInvulnerable(true);
                    });
                    ModeledEntity megEntity = ModelEngineAPI.createModeledEntity(ModelEngineAPI.createModeledEntity(entity).getBase());

                    megEntity.addModel(megModel, false);
                    megEntity.setBaseEntityVisible(false);
                    megEntity.getRangeManager().setRenderDistance(loc.getWorld().getViewDistance() * 16);
                    megEntity.setState(ModelState.IDLE);

                    megEntity.getRangeManager().updatePlayer(player);

                    entities.add(entity);
                }
            }
        }
        else if (args[0].equals("clean"))
        {
            for (Iterator<Entity> iterator = entities.iterator(); iterator.hasNext(); )
            {
                Entity entity = iterator.next();
                entity.remove();
                iterator.remove();
            }
        }

        return true;
    }

    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args)
    {
        List<String> strings = new ArrayList<>();
        strings.add("itemsadder");
        strings.add("itemsadder-noai");
        strings.add("meg");
        strings.add("meg-noai");
        strings.add("remove");
        return strings;
    }
}
