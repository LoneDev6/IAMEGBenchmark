package dev.lone.iamegbenchmark.traffic;

import java.util.HashMap;

public final class PacketsCounter extends Counter
{
    final HashMap<String, Counter> count = new HashMap<>();
    public final String groupName;

    public PacketsCounter(String groupName)
    {
        this.groupName = groupName;
    }

    public void increase(String entryName)
    {
        entryName = entryName.replace("net.minecraft.network.protocol.game.", "");
        Counter data = count.computeIfAbsent(entryName, aClass -> new Counter());
        data.receivedPackets++;
        receivedPackets++;
    }
}