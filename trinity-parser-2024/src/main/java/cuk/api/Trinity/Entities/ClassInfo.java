package cuk.api.Trinity.Entities;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class ClassInfo {
    private LocalDateTime timestamp;
    private String className;
    private String subjectCode; // 과목 코드
    private String classNo;

    public ClassInfo(LocalDateTime timestamp, String className, String subjectCode, String classNo) {
        this.timestamp = timestamp;
        this.className = className;
        this.subjectCode = subjectCode;
        this.classNo = classNo;
    }

    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String timeToString = timestamp.format(formatter);
        return String.format("[%s]: [classKrName: %s] [classId: %s] [classNo: %s]", timeToString, className, subjectCode, classNo);
    }
}
