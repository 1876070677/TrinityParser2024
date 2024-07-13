package cuk.api.Trinity.Entities;

import cuk.api.Trinity.Request.LoginRequest;
import cuk.api.Trinity.Entities.Role;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@ToString
public class TrinityUser implements Serializable {
    private static final long serialVersionUID = 123L;

    private String samlRequest;
    private String SAMLResponse;
    private String _csrf;
    private String trinityId;
    private String password;
    private Role role;

    // 트리니티 실제 정보
    private TrinityInfo trinityInfo;

    public TrinityUser(LoginRequest loginRequest) {
        this.trinityInfo = new TrinityInfo();
        this.trinityId = loginRequest.getTrinityId();
        this.password = loginRequest.getPassword();
    }
}
