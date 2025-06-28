package cuk.api.Logging;

import cuk.api.Trinity.Entities.ClassInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class Logging {
    private final Queue<ClassInfo> task;
    private static final Logger logger = LoggerFactory.getLogger(Logging.class);

    public Logging() {
        this.task = new ConcurrentLinkedQueue<>();
    }

    public void enqueue(ClassInfo info) {
        task.add(info);
    }

    // 주기적으로 로그 처리.
    @Scheduled(fixedDelay = 10000)
    public synchronized void processQueue() throws Exception {
        ClassInfo info;
        while (!task.isEmpty()) {
            info = task.poll();
            logger.info(info.toString());
        }
    }
}
