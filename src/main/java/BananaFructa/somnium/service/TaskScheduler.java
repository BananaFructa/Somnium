package BananaFructa.somnium.service;

import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.time.LocalTime;
import java.util.*;

public class TaskScheduler {

    public static Queue<OllamaTask> tasks = new LinkedList<>();


    public static void update() {
        if (!tasks.isEmpty()) {
            OllamaTask task = tasks.peek();
            if (!task.started) {
                task.started = true;
                new Thread(()->{
                    try {
                        task.startTime = LocalTime.now(); // In case it doesn't start with a sub section
                        task.run();
                        task.finished = true;
                    } catch (Exception err) {
                        err.printStackTrace();
                        task.error = true;
                    }
                }).start();
            } else {
                task.updateTime();
            }
            if (task.finished) {
                tasks.poll();
            }
            if (task.error) {
                tasks.poll();
            }
        }
    }

    public static void schedule(OllamaTask task) {
        tasks.add(task);
    }

    private static String withTime(String s, int spacing, float time, boolean waiting) {
        s += StringUtils.repeat(" ",spacing - s.length());
        if (waiting) s += "(waiting)";
        else s += "\u00a76" + String.format("%.2f",time)+" sec";
        return s;
    }

    public static List<String> times() {
        List<String> lines = new ArrayList<>();

        for (OllamaTask task : tasks) {
            if (task.started) {
                lines.add("\u00a76" + String.format("%.2f",task.totalTimeTaken())+" sec");
            } else {
                lines.add("\u00a77(waiting)");
            }
            for (int i = 0;i < task.stages().length;i++) {
                boolean started = task.timeTaken.size() > i;
                if (started) lines.add("\u00a76" + String.format("%.2f",task.timeTaken.get(i))+" sec");
                else lines.add("\u00a77(waiting)");
            }
        }
        return lines;
    }

    public static List<String> info() {
        List<String> lines = new ArrayList<>();
        lines.add("\u00a7cSomnium Task Scheduler");

        for (OllamaTask task : tasks) {
            String colorCode;
            if (task.started) {
                colorCode = "";
            } else {
                colorCode = "\u00a77";
            }
            lines.add(colorCode + task.name);
            for (int i = 0;i < task.stages().length;i++) {
                boolean started = task.timeTaken.size() > i;
                float time = 0;
                if (started) time = task.timeTaken.get(i);
                String subColorCode;
                if (started) subColorCode = "";
                else subColorCode = "\u00a77";
                lines.add(subColorCode + "   " + task.stages()[i]);
            }
        }
        return lines;
    }

}
