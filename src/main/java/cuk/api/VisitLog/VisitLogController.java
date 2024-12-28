package cuk.api.VisitLog;

import cuk.api.ResponseEntities.ResponseMessage;
import cuk.api.VisitLog.Request.CreateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Controller
@RequestMapping("/trinity/auth")
@CrossOrigin("*")
public class VisitLogController {

    private VisitLogService visitLogService;

    @Autowired
    public VisitLogController(VisitLogService visitLogService) {
        this.visitLogService = visitLogService;
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
}
