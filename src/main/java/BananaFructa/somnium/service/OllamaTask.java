package BananaFructa.somnium.service;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public abstract class OllamaTask {

    public LocalTime startTime = null;
    public List<Float> timeTaken = new ArrayList<>();
    public boolean started = false;
    public boolean finished = false;
    public boolean error = false;
    public String name;

    public OllamaTask(String name) {
        this.name = name;
    }

    public float totalTimeTaken() {
        float total = 0;
        for (float f : timeTaken) {
            total += f;
        }
        return total;
    }

    public abstract void run();
    public abstract String[] stages();
    public void nextStage() {
        startTime = LocalTime.now();
        timeTaken.add(0.0f);
    }
    public void updateTime() {
        if (!timeTaken.isEmpty()) {
            if (startTime != null) {
                timeTaken.set(timeTaken.size() - 1, Duration.between(startTime, LocalTime.now()).toMillis()/1000.0f);
            }
        }
    }


}
