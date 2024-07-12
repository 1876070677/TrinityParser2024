package cuk.api.Trinity.Request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class SubjtNoRequest {
    @NotBlank
    private String sujtNo;
    @NotBlank
    private String classNo;
}
