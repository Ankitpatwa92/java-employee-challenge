package com.reliaquest.api.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmployeeInput {

    private String name;

    private Integer salary;

    private Integer age;

    private String title;
}
