package cuk.api.ResponseEntities;

import lombok.Data;
import org.springframework.http.HttpStatus;
@Data
public class ResponseMessage {
    private HttpStatus status;
    private String message;
    private Object data;

    public ResponseMessage() {
        this.status = HttpStatus.BAD_REQUEST;
        this.message = null;
        this.data = null;
    }
}
