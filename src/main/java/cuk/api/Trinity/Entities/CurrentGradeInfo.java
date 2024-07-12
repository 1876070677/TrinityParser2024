package cuk.api.Trinity.Entities;

import lombok.Data;
import lombok.ToString;
import org.json.simple.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;

@Data
@ToString
public class CurrentGradeInfo implements Serializable {
    private static final long serialVersionUID = 123L;

    private ArrayList<String> details = new ArrayList<>();
    private String centesScorAdm;
    private String estiYn;
    private String grdAdm;
    private String sbjtKorNm;
    private String sbjtNo;

    public void setGradeInfo(JSONObject obj) {
        Set<String> keys = obj.keySet();

        for (String key : keys) {
            Object value = obj.get(key);
            if (value != null) {
                if (key.contains("apprItem")) {
                    details.add(value.toString());
                }
                else if (key.contains("centesScorAdm")) {
                    centesScorAdm = value.toString();
                }
                else if (key.contains("estiYn")) {
                    estiYn = value.toString();
                }
                else if (key.contains("grdAdm")) {
                    grdAdm = value.toString();
                }
                else if (key.contains("sbjtKorNm")) {
                    sbjtKorNm = value.toString();
                }
                else if (key.contains("sbjtNo")) {
                    sbjtNo = value.toString();
                }
            }
        }
    }
}
