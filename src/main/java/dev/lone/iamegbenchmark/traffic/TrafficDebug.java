package dev.lone.iamegbenchmark.traffic;

import dev.lone.iamegbenchmark.Main;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;

public class TrafficDebug
{
    HashMap<String, PacketsCounter> counters = new HashMap<>();

    public TrafficDebug()
    {
        counters.put("main", new PacketsCounter("main"));

        Bukkit.getScheduler().runTaskTimerAsynchronously(Main.inst(), this::tick, 20L, 20L);
    }

    public PacketsCounter get(String groupName)
    {
        return counters.get(groupName);
    }

    private void tick()
    {
        System.out.println("--------");

        for (PacketsCounter counter : counters.values())
        {
            for (Map.Entry<String, Counter> entry : counter.count.entrySet())
            {
                System.out.println(counter.groupName + " - " + entry.getKey() + " | " + entry.getValue().tickAvg());
            }
        }

        for (PacketsCounter counter : counters.values())
        {
            System.out.println("#################");
            System.out.println("# " + counter.groupName + " rx: " + counter.tickAvg());
            System.out.println("#################");
        }

        System.out.println("--------");
    }
}