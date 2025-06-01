package com.example.api_tierces;

import com.example.api_tierces.model.Api;
import com.example.api_tierces.service.ApiService;
import com.example.api_tierces.controller.ApiController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApiControllerTest {

    @Mock
    private ApiService apiService;

    @InjectMocks
    private ApiController apiController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllApis() {
        Api api1 = new Api();
        api1.setId(1L);
        api1.setPath("/test1");

        Api api2 = new Api();
        api2.setId(2L);
        api2.setPath("/test2");

        List<Api> mockApis = Arrays.asList(api1, api2);
        when(apiService.getAllApis()).thenReturn(mockApis);

        List<Api> result = apiController.getAllApis();
        assertEquals(2, result.size());
        verify(apiService, times(1)).getAllApis();
    }

    @Test
    void testGetApiById_Found() {
        Api api = new Api();
        api.setId(1L);
        api.setPath("/test");

        when(apiService.getApiById(1L)).thenReturn(Optional.of(api));

        ResponseEntity<Api> response = apiController.getApiById(1L);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(api, response.getBody());
    }

    @Test
    void testGetApiById_NotFound() {
        when(apiService.getApiById(999L)).thenReturn(Optional.empty());

        ResponseEntity<Api> response = apiController.getApiById(999L);
        assertEquals(404, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    void testGetApiByPath_Found() {
        Api api = new Api();
        api.setId(1L);
        api.setPath("/found");

        when(apiService.getApiByPath("/found")).thenReturn(Optional.of(api));

        ResponseEntity<Api> response = apiController.getApiByPath("/found");
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(api, response.getBody());
    }

    @Test
    void testGetApiByPath_NotFound() {
        when(apiService.getApiByPath("/notfound")).thenReturn(Optional.empty());

        ResponseEntity<Api> response = apiController.getApiByPath("/notfound");
        assertEquals(404, response.getStatusCodeValue());
        assertNull(response.getBody());
    }
}

