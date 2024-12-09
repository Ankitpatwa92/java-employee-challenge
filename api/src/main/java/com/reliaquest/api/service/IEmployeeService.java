package com.reliaquest.api.service;

import java.util.List;

public interface IEmployeeService<Entity, Input> {

    List<Entity> getAllEmployees();

    List<Entity> getEmployeesByNameSearch(String searchString);

    Entity getEmployeeById(String id);

    Integer getHighestSalaryOfEmployees();

    List<String> getTopTenHighestEarningEmployeeNames();

    String deleteEmployeeById(String id);

    Entity createEmployee(Input employeeInput);
}
