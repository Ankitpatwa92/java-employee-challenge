package com.reliaquest.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import com.reliaquest.api.service.EmployeeApiService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.client.HttpClientErrorException;

@SpringBootTest
@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApiApplicationTest {

    @Autowired
    EmployeeApiService employeeApiService;

    List<Employee> employees = new ArrayList<Employee>();

    @Test
    void getEmployeesByNameSearchTest() {
        initEmployee();
        String employeeNametoSearch = employees.stream().findAny().get().getEmployee_name();
        List<Employee> employees = employeeApiService.getEmployeesByNameSearch(employeeNametoSearch);
        assertTrue(
                employees.stream()
                        .map(e -> e.getEmployee_name())
                        .collect(Collectors.toList())
                        .contains(employeeNametoSearch),
                "Searched employee not found in the response list");
    }

    @Test
    void getEmployeesByNameSearch_InvalidInputName_Test() {
        initEmployee();
        List<Employee> employeeList = employeeApiService.getEmployeesByNameSearch("ABC");
        assertTrue(employeeList.isEmpty(), "Employee list should be empty for invalid name search");
    }

    @Test
    void getEmployeeByIdTest() {
        initEmployee();
        Employee employee = employeeApiService.getEmployeeById(employees.get(0).getId());
        assertEquals(employee, employees.get(0), "Search employee not found");
    }

    @Test
    void getEmployeeById_invalidId_Test() {
        initEmployee();

        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
            employeeApiService.getEmployeeById("1acc00ea-f25f-4d20-835f-966d781e9cb6");
        });

        assertTrue(
                exception.getStatusCode().value() == 404,
                "Expected status code 404 but found "
                        + exception.getStatusCode().value());
    }

    void getHighestSalaryOfEmployeesTest() {
        initEmployee();
        Integer highestSalary = employeeApiService.getHighestSalaryOfEmployees();
        Employee employeeHavingHighestSalary = employees.stream()
                .max(Comparator.comparing(Employee::getEmployee_salary))
                .get();
        assertEquals(
                employeeHavingHighestSalary.getEmployee_salary(),
                highestSalary,
                "Highest Salary is not matching with caluclating highest salary");
    }

    @Test
    void getHighestSalaryOfEmployees_afterAddingNewEmployee_Test() {
        initEmployee();
        Employee employeeHavingHighestSalary = employees.stream()
                .max(Comparator.comparing(Employee::getEmployee_salary))
                .get();

        Integer expected_highest_salary = employeeHavingHighestSalary.getEmployee_salary() + 10000;

        EmployeeInput employeeInput = EmployeeInput.builder()
                .age(23)
                .name("Ramesh Ozha")
                .salary(expected_highest_salary)
                .title("Executive")
                .build();
        employeeApiService.createEmployee(employeeInput);

        Integer actual_highest_salary = employeeApiService.getHighestSalaryOfEmployees();

        assertEquals(expected_highest_salary, actual_highest_salary);
    }

    @Test
    void getTopTenHighestEarningEmployeeNamesTest() {
        initEmployee();
        List<String> expected_top10SalariedEmployee = employees.stream()
                .sorted(Comparator.comparing(Employee::getEmployee_salary))
                .map(Employee::getEmployee_name)
                .limit(10)
                .collect(Collectors.toList());

        List<String> actual_top10SalariedEmployee = employeeApiService.getTopTenHighestEarningEmployeeNames();
        assertIterableEquals(expected_top10SalariedEmployee, actual_top10SalariedEmployee, "The lists are not equal");
    }

    @Test
    void createEmployeeTest() {
        initEmployee();
        EmployeeInput employeeInput = EmployeeInput.builder()
                .age(25)
                .name("Sara")
                .salary(2000)
                .title("CEO")
                .build();
        Employee newlyAddedEmployee = employeeApiService.createEmployee(employeeInput);
        assertTrue(newlyAddedEmployee.getId() != null, "Employee Id is null");
        assertFalse(
                employees.stream().anyMatch(e -> e.getId().equals(newlyAddedEmployee.getId())),
                "Existing employee returned new employee not created");
    }

    @Test
    void deleteEmployeeByIdTest() {
        initEmployee();
        String expected_response = "{\"data\":true,\"status\":\"Successfully processed request.\"}";
        String employeename = employees.get(0).getEmployee_name();
        String actual_response = employeeApiService.deleteEmployeeById(employeename);
        assertEquals(expected_response, actual_response, "response is not match");
        employees = employeeApiService.getAllEmployees();
        assertFalse(
                employees.stream().anyMatch(e -> e.getEmployee_name().equals(employeename)),
                "Employee exist after deltion");
    }

    @Test
    void deleteEmployeeById_invalidEmployee_Test() {
        initEmployee();
        String expected_response = "{\"data\":false,\"status\":\"Successfully processed request.\"}";
        String employeename = "ABC";
        String actual_response = employeeApiService.deleteEmployeeById(employeename);
        assertEquals(expected_response, actual_response);
    }

    private void initEmployee() {
        if (employees.size() == 0) {
            employees = employeeApiService.getAllEmployees();
        }
    }
}
