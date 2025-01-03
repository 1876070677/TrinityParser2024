package cuk.api.VisitLog.Entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class VisitLog {
    private int id;
    private String context;
    private String created_time;
    private boolean visible;
    private int likes;
    private int total_records;
}
