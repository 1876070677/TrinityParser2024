package cuk.api.Logging;

import cuk.api.Trinity.Entities.ClassInfo;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class Logging {
    private final Queue<ClassInfo> task;
    private final Path logFilePath = Paths.get("/usr/local/tomcat/webapps/logs/log.txt");

    public Logging() {
        this.task = new ConcurrentLinkedQueue<>();
    }

    public void enqueue(ClassInfo info) {
        task.add(info);
    }

    // 주기적으로 로그 처리 (예: 5초마다)
    @Scheduled(fixedDelay = 10000)
    public void processQueue() throws Exception {
        while (!task.isEmpty()) {
            try (BufferedWriter writer = Files.newBufferedWriter(
                    logFilePath,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND)) {

                ClassInfo info;
                while ((info = task.poll()) != null) {
                    writer.write(info.toString());
                    writer.newLine();
                }
            } catch (IOException e) {
                throw new Exception("Logging Failed!!");
            }
        }
    }

}
