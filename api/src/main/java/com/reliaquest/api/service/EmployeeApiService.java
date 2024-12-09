package com.reliaquest.api.service;

import com.reliaquest.api.model.DeleteEmployeeInput;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import com.reliaquest.api.model.EmployeeResponse;
import com.reliaquest.api.model.SingleEmployeeResponse;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@EnableRetry
public class EmployeeApiService implements IEmployeeService<Employee, EmployeeInput> {

    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RestTemplate restTemplate;

    @Value("${employee.server.url}")
    private String url;

    @Override
    @Retryable(
            retryFor = HttpClientErrorException.TooManyRequests.class,
            maxAttempts = 5,
            backoff = @Backoff(delay = 31000, multiplier = 1))
    public List<Employee> getAllEmployees() {
        logger.info("getAllEmployees");
        ResponseEntity<EmployeeResponse> response =
                restTemplate.exchange(url, HttpMethod.GET, null, EmployeeResponse.class);
        return response.getBody().getData();
    }

    @Retryable(
            retryFor = HttpClientErrorException.TooManyRequests.class,
            maxAttempts = 5,
            backoff = @Backoff(delay = 31000, multiplier = 1))
    @Override
    public List<Employee> getEmployeesByNameSearch(String searchString) {
        logger.info("getEmployeesByNameSearch:{}", searchString);
        ResponseEntity<EmployeeResponse> response =
                restTemplate.exchange(url, HttpMethod.GET, null, EmployeeResponse.class);

        List<Employee> employees = response.getBody().getData();
        return employees.stream()
                .filter(e -> e.getEmployee_name().contains(searchString))
                .collect(Collectors.toList());
    }

    @Retryable(
            retryFor = HttpClientErrorException.TooManyRequests.class,
            maxAttempts = 5,
            backoff = @Backoff(delay = 31000, multiplier = 1))
    @Override
    public Employee getEmployeeById(String id) {
        logger.info("getEmployeeById:{}", id);
        ResponseEntity<SingleEmployeeResponse> response;
        response = restTemplate.exchange(url + "/" + id, HttpMethod.GET, null, SingleEmployeeResponse.class);
        return response.getBody().getData();
    }

    @Retryable(
            retryFor = HttpClientErrorException.TooManyRequests.class,
            maxAttempts = 5,
            backoff = @Backoff(delay = 31000, multiplier = 1))
    @Override
    public Integer getHighestSalaryOfEmployees() {
        logger.info("getHighestSalaryOfEmployees");
        ResponseEntity<EmployeeResponse> response =
                restTemplate.exchange(url, HttpMethod.GET, null, EmployeeResponse.class);
        List<Employee> employees = response.getBody().getData();
        return employees.stream()
                .max(Comparator.comparing(Employee::getEmployee_salary))
                .get()
                .getEmployee_salary();
    }

    @Retryable(
            retryFor = HttpClientErrorException.TooManyRequests.class,
            maxAttempts = 5,
            backoff = @Backoff(delay = 31000, multiplier = 1))
    @Override
    public List<String> getTopTenHighestEarningEmployeeNames() {
        logger.info("getTopTenHighestEarningEmployeeNames");
        ResponseEntity<EmployeeResponse> response =
                restTemplate.exchange(url, HttpMethod.GET, null, EmployeeResponse.class);
        List<Employee> employees = response.getBody().getData();
        List<String> list = employees.stream()
                .sorted(Comparator.comparing(Employee::getEmployee_salary))
                .limit(10)
                .map(Employee::getEmployee_name)
                .collect(Collectors.toList());
        return list;
    }

    @Retryable(
            retryFor = HttpClientErrorException.TooManyRequests.class,
            maxAttempts = 5,
            backoff = @Backoff(delay = 31000, multiplier = 1))
    @Override
    public String deleteEmployeeById(String id) {
        logger.info("deleteEmployeeById:{}", id);

        DeleteEmployeeInput input = new DeleteEmployeeInput();
        input.setName(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<DeleteEmployeeInput> entity = new HttpEntity<>(input, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
        return response.getBody();
    }

    @Retryable(
            retryFor = HttpClientErrorException.TooManyRequests.class,
            maxAttempts = 5,
            backoff = @Backoff(delay = 31000, multiplier = 1))
    @Override
    public Employee createEmployee(EmployeeInput employeeInput) {
        logger.info("createEmployee Current:{}", employeeInput.getName());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EmployeeInput> entity = new HttpEntity<>(employeeInput, headers);
        ResponseEntity<SingleEmployeeResponse> responseEntity =
                restTemplate.exchange(url, HttpMethod.POST, entity, SingleEmployeeResponse.class);
        return responseEntity.getBody().getData();
    }
}
