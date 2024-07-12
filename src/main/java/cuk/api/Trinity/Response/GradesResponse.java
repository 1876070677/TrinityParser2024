package cuk.api.Trinity.Response;

import cuk.api.Trinity.Entities.CurrentGradeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@NoArgsConstructor
public class GradesResponse {
    private ArrayList<CurrentGradeInfo> grades = new ArrayList<>();

    public void addGrade(CurrentGradeInfo currentGradeInfo) {
        grades.add(currentGradeInfo);
    }
}
