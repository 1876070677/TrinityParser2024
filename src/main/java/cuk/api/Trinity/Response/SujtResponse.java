package cuk.api.Trinity.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SujtResponse {
    String sujtNo;
    String classNo;
    String tlsnAplyRcnt;
    String tlsnLmtRcnt;
    String sbjtKorNm;
    String sustCd;
    String extraCnt;
}
