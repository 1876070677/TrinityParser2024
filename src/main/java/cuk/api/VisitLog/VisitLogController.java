package cuk.api.VisitLog;

import cuk.api.ResponseEntities.ResponseMessage;
import cuk.api.VisitLog.Request.CreateRequest;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/trinity/auth")
@CrossOrigin("*")
public class VisitLogController {

    private VisitLogService visitLogService;
    private RedissonClient redissonClient;

    @Autowired
    public VisitLogController(VisitLogService visitLogService, RedissonClient redissonClient) {
        this.visitLogService = visitLogService;
        this.redissonClient = redissonClient;
    }

    @GetMapping("/vl")
    public ResponseEntity<ResponseMessage> getVisitLogs(@RequestParam("cursor") String cursor) {
        if (cursor == null || cursor.isEmpty() || cursor.isBlank())
            throw new RuntimeException("Wrong Input");

        // salt 제거
        int cursorToInt = -1;
        String decoded = "";
        try {
            String raw = cursor.substring(8);
            byte[] decodedBytes = Base64.getDecoder().decode(raw);
            decoded = new String(decodedBytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Wrong Cursor");
        }

        cursorToInt = Integer.parseInt(decoded);

        ResponseMessage resp = new ResponseMessage();
        resp.setStatus(HttpStatus.OK);
        resp.setMessage("Success");

        resp.setData(visitLogService.getVisitLogs(cursorToInt));

        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    @PostMapping("/vl")
    public ResponseEntity<ResponseMessage> createVisitLog(@RequestBody CreateRequest createRequest) {
        ResponseMessage resp = new ResponseMessage();
        resp.setStatus(HttpStatus.OK);
        resp.setMessage("Success");

        if (createRequest.getContext().length() > 200)
            throw new RuntimeException("Context Length Is Too Large");
        else if (createRequest.getContext().isEmpty() || createRequest.getContext().isBlank())
            throw new RuntimeException("Wrong Input");
        visitLogService.createVisitLog(createRequest);

        resp.setData(visitLogService.getVisitLogs(0));
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    @PatchMapping("/vl/likes/{id}")
    public ResponseEntity<ResponseMessage> incrLikes(@PathVariable("id") int id) {
        if (id == 0)
            throw new RuntimeException("Wrong Input");

        // Lock
        final String lockName = Integer.toString(id) + ":lock";
        final RLock lock = redissonClient.getLock(lockName);
        try {
            if (lock.tryLock(1, 3, TimeUnit.SECONDS))
                visitLogService.incrLikes(id);
        } catch (Exception e) {
            throw new RuntimeException("Error");
        } finally {
            if (lock != null && lock.isLocked())
                lock.unlock();
        }
        ResponseMessage resp = new ResponseMessage();
        resp.setStatus(HttpStatus.OK);
        resp.setMessage("Success");

        return new ResponseEntity<>(resp, HttpStatus.OK);
    }
}
