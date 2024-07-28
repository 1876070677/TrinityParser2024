package cuk.api.Management.Request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ConfigRequest {
    @NotBlank
    private String shtm;
    @NotBlank
    private String yyyy;
}
