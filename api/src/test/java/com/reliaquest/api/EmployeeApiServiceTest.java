package com.reliaquest.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;

import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import com.reliaquest.api.model.EmployeeResponse;
import com.reliaquest.api.model.SingleEmployeeResponse;
import com.reliaquest.api.service.EmployeeApiService;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
public class EmployeeApiServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private EmployeeApiService apiService;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(apiService, "url", "http://localhost:8080");
    }

    private static Employee createEmployee(String id, String name, int age, int salary, String title, String email) {
        return Employee.builder()
                .id(id)
                .employee_name(name)
                .employee_age(age)
                .employee_salary(salary)
                .employee_title(title)
                .employee_email(email)
                .build();
    }

    private static ResponseEntity<EmployeeResponse> prepareEmployeeResponse() {
        List<Employee> employees = List.of(
                createEmployee("abc-098", "Aman Bajpayee", 23, 2000, "Sr Executive", "abc@avc.com"),
                createEmployee("abc-087", "Aman Agrwal", 23, 2000, "Sr Executive", "abc@avc.com"),
                createEmployee("abc-086", "Sagar Agrwal", 23, 2000, "Sr Executive", "abc@avc.com"));
        EmployeeResponse response = new EmployeeResponse();
        response.setData(employees);
        return ResponseEntity.ok(response);
    }

    private static ResponseEntity<SingleEmployeeResponse> prepareEmployeeResponse2() {
        Employee employee = createEmployee("abc-098", "Aman Bajpayee", 23, 2000, "Sr Executive", "abc@avc.com");
        SingleEmployeeResponse response = new SingleEmployeeResponse();
        response.setData(employee);
        return ResponseEntity.ok(response);
    }

    @Test
    public void test_getAllEmployees_thenResponseShouldMatch() {
        Mockito.when(restTemplate.exchange("http://localhost:8080", HttpMethod.GET, null, EmployeeResponse.class))
                .thenReturn(prepareEmployeeResponse());

        List<Employee> employees = apiService.getAllEmployees();
        assertEquals(employees, prepareEmployeeResponse().getBody().getData());
    }

    @Test
    public void test_getEmployeesByNameSearch_thenResponseShouldMatch() {
        Mockito.when(restTemplate.exchange("http://localhost:8080", HttpMethod.GET, null, EmployeeResponse.class))
                .thenReturn(prepareEmployeeResponse());

        List<Employee> employees = apiService.getEmployeesByNameSearch("Aman");
        List<Employee> expectedEmployees = prepareEmployeeResponse().getBody().getData().stream()
                .filter(e -> e.getEmployee_name().contains("Aman"))
                .collect(Collectors.toList());

        assertEquals(employees.size(), expectedEmployees.size());

        assertLinesMatch(
                employees.stream().map(Employee::getEmployee_name).collect(Collectors.toList()),
                expectedEmployees.stream().map(Employee::getEmployee_name).collect(Collectors.toList()));
    }

    @Test
    public void test_getEmployeesById_thenResponseShouldMatch() {
        Mockito.when(restTemplate.exchange(
                        "http://localhost:8080/abc-987", HttpMethod.GET, null, SingleEmployeeResponse.class))
                .thenReturn(prepareEmployeeResponse2());

        Employee employee = apiService.getEmployeeById("abc-987");
        assertEquals(
                employee.getId(), prepareEmployeeResponse2().getBody().getData().getId());
    }

    @Test
    public void test_getHighestSalaryOfEmployees_thenResponseShouldMatch() {
        Mockito.when(restTemplate.exchange("http://localhost:8080", HttpMethod.GET, null, EmployeeResponse.class))
                .thenReturn(prepareEmployeeResponse());

        Integer responseSalary = apiService.getHighestSalaryOfEmployees();
        Integer expectedHighestSalary = prepareEmployeeResponse().getBody().getData().stream()
                .map(Employee::getEmployee_salary)
                .max(Integer::compareTo)
                .orElseThrow(); // Throw exception if no employee exists

        assertEquals(responseSalary, expectedHighestSalary);
    }

    @Test
    public void test_getTopTenHighestEarningEmployeeNames_thenResponseShouldMatch() {
        Mockito.when(restTemplate.exchange("http://localhost:8080", HttpMethod.GET, null, EmployeeResponse.class))
                .thenReturn(prepareEmployeeResponse());

        List<String> responseEmployees = apiService.getTopTenHighestEarningEmployeeNames();
        List<String> expectedEmployees = prepareEmployeeResponse().getBody().getData().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getEmployee_salary(), e1.getEmployee_salary()))
                .limit(10)
                .map(Employee::getEmployee_name)
                .collect(Collectors.toList());

        assertEquals(responseEmployees, expectedEmployees);
    }

    @Test
    public void test_deleteEmployeeById_thenResponseShouldMatch() {
        Mockito.when(restTemplate.exchange(
                        eq("http://localhost:8080"),
                        eq(HttpMethod.DELETE),
                        Mockito.any(HttpEntity.class),
                        eq(String.class)))
                .thenReturn(ResponseEntity.ok("SUCCESS"));

        String response = apiService.deleteEmployeeById("Aman Bajpayee");
        assertEquals("SUCCESS", response);
    }

    @Test
    public void test_createEmployee_thenResponseShouldMatch() {
        Mockito.when(restTemplate.exchange(
                        eq("http://localhost:8080"),
                        eq(HttpMethod.POST),
                        Mockito.any(HttpEntity.class),
                        eq(SingleEmployeeResponse.class)))
                .thenReturn(ResponseEntity.ok(getEmployeeResponse()));

        EmployeeInput employeeInput =
                EmployeeInput.builder().name("Aman Bajpayee").build();
        Employee employee = apiService.createEmployee(employeeInput);
        assertTrue(employee.getEmployee_name().equals("Aman Bajpayee"));
    }

    private SingleEmployeeResponse getEmployeeResponse() {
        Employee employee = createEmployee("abc-098", "Aman Bajpayee", 23, 2000, "Sr Executive", "abc@avc.com");
        SingleEmployeeResponse employeeResponse2 = new SingleEmployeeResponse();
        employeeResponse2.setData(employee);
        return employeeResponse2;
    }
}
