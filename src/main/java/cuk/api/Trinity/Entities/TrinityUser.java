package cuk.api.Trinity.Entities;

import cuk.api.Trinity.Request.LoginRequest;
import lombok.Data;
import lombok.ToString;
import java.io.Serializable;
import java.util.HashMap;

@Data
@ToString
public class TrinityUser implements Serializable {
    private static final long serialVersionUID = 123L;

    private String samlRequest;
    private String SAMLResponse;
    private String _csrf;
    private String trinityId;
    private String password;
    private HashMap<String, HashMap<String, Object>> cookies = new HashMap<>();
    private Role role;

    // 트리니티 실제 정보
    private TrinityInfo trinityInfo;

    public TrinityUser(LoginRequest loginRequest) {
        this.trinityInfo = new TrinityInfo();
        this.trinityId = loginRequest.getTrinityId();
        this.password = loginRequest.getPassword();
    }

    public void addCookie(String key, HashMap<String, Object> values) {
        cookies.put(key, values);
    }
}
