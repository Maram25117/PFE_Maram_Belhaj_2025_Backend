package com.example.api_tierces;

import com.example.api_tierces.controller.ApiParametersController;
import com.example.api_tierces.model.Api;
import com.example.api_tierces.model.ApiParameters;
import com.example.api_tierces.service.ApiParametersService;
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

class ApiParametersControllerTest {

    @Mock
    private ApiParametersService apiParametersService;

    @InjectMocks
    private ApiParametersController apiParametersController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllParameters() {
        ApiParameters param1 = new ApiParameters();
        param1.setId(1L);
        param1.setName("param1");

        ApiParameters param2 = new ApiParameters();
        param2.setId(2L);
        param2.setName("param2");

        List<ApiParameters> mockList = Arrays.asList(param1, param2);
        when(apiParametersService.getAllParameters()).thenReturn(mockList);

        List<ApiParameters> result = apiParametersController.getAllParameters();

        assertEquals(2, result.size());
        verify(apiParametersService, times(1)).getAllParameters();
    }

    @Test
    void testGetParameterById_Found() {
        ApiParameters param = new ApiParameters();
        param.setId(1L);
        param.setName("key");

        when(apiParametersService.getParameterById(1L)).thenReturn(Optional.of(param));

        ResponseEntity<ApiParameters> response = apiParametersController.getParameterById(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(param, response.getBody());
    }

    @Test
    void testGetParameterById_NotFound() {
        when(apiParametersService.getParameterById(999L)).thenReturn(Optional.empty());

        ResponseEntity<ApiParameters> response = apiParametersController.getParameterById(999L);

        assertEquals(404, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    void testGetParametersByApiId() {
        Api api = new Api();
        api.setId(10L);

        ApiParameters param = new ApiParameters();
        param.setId(1L);
        param.setApi(api);
        param.setName("example");

        List<ApiParameters> mockParams = Arrays.asList(param);
        when(apiParametersService.getParametersByApiId(10L)).thenReturn(mockParams);

        List<ApiParameters> result = apiParametersController.getParametersByApiId(10L);

        assertEquals(1, result.size());
        assertEquals("example", result.get(0).getName());
        verify(apiParametersService, times(1)).getParametersByApiId(10L);
    }
}
