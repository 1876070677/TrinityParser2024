package cuk.api.Trinity.Entities;

import lombok.Data;
import org.json.simple.JSONObject;

import java.io.Serializable;

@Data
public class TrinityInfo implements Serializable {
    private static final long serialVersionUID = 123L;

    private String userNm;
    private String userNo;
    private String deptNm;
    private String campFg;

    private String shtmFg;
    private String shtmYyyy;

    private String shtm;
    private String Yyyy;

    public void setTrinityInfo(JSONObject obj) {
        this.userNm = obj.get("userNm").toString();
        this.userNo = obj.get("userNo").toString();
        this.deptNm = obj.get("deptNm").toString();
        this.campFg = obj.get("campFg").toString();
    }

    public void setSchoolInfo(JSONObject obj) {
        this.shtmFg = obj.get("SHTM_FG").toString();
        this.shtmYyyy = obj.get("YYYY").toString();
    }

}
