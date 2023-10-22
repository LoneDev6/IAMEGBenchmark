package dev.lone.iamegbenchmark.traffic;

public class Counter
{
    int receivedPackets = 1;
    private float averageReceivedPackets = 1.0f;

    public float tickAvg()
    {
        this.averageReceivedPackets = lerp((float) this.receivedPackets, this.averageReceivedPackets);
        receivedPackets = 0;
        return this.averageReceivedPackets;
    }

    private static float lerp(float start, float end)
    {
        return start + (float) 0.75 * (end - start);
    }
}
