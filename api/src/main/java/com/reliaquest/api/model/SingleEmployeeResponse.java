package com.reliaquest.api.model;

import lombok.Data;

@Data
public class SingleEmployeeResponse {

    Employee data;

    public Employee getData() {
        return data;
    }

    public void setData(Employee data) {
        this.data = data;
    }
}
