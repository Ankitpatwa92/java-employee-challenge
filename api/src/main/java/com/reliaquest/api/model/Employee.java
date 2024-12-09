package com.reliaquest.api.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Employee {

    private String id;
    private String employee_name;
    private Integer employee_salary;
    private Integer employee_age;
    private String employee_title;
    private String employee_email;

    @Override
    public String toString() {
        return "Employee [id=" + id + ", employee_name=" + employee_name + ", employee_salary=" + employee_salary
                + ", employee_age=" + employee_age + ", employee_title=" + employee_title + ", employee_email="
                + employee_email + "]";
    }
}
