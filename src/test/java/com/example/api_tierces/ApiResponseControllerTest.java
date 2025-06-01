package com.example.api_tierces;

import com.example.api_tierces.controller.ApiResponseController;
import com.example.api_tierces.model.Api;
import com.example.api_tierces.model.ApiResponse;
import com.example.api_tierces.service.ApiResponseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApiResponseControllerTest {

    @Mock
    private ApiResponseService apiResponseService;

    @InjectMocks
    private ApiResponseController apiResponseController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllResponses() {
        ApiResponse response1 = new ApiResponse();
        response1.setId(1L);

        ApiResponse response2 = new ApiResponse();
        response2.setId(2L);

        List<ApiResponse> mockList = Arrays.asList(response1, response2);
        when(apiResponseService.getAllResponses()).thenReturn(mockList);

        List<ApiResponse> result = apiResponseController.getAll();

        assertEquals(2, result.size());
        verify(apiResponseService, times(1)).getAllResponses();
    }

    @Test
    void testGetResponseById_Found() {
        ApiResponse response = new ApiResponse();
        response.setId(1L);

        when(apiResponseService.getResponseById(1L)).thenReturn(Optional.of(response));

        Optional<ApiResponse> result = apiResponseController.getById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        verify(apiResponseService, times(1)).getResponseById(1L);
    }

    @Test
    void testGetResponseById_NotFound() {
        when(apiResponseService.getResponseById(999L)).thenReturn(Optional.empty());

        Optional<ApiResponse> result = apiResponseController.getById(999L);

        assertFalse(result.isPresent());
        verify(apiResponseService, times(1)).getResponseById(999L);
    }

    @Test
    void testGetResponsesByApiId() {
        Api api = new Api();
        api.setId(10L);

        ApiResponse response = new ApiResponse();
        response.setId(1L);
        response.setApi(api);

        List<ApiResponse> mockList = Arrays.asList(response);
        when(apiResponseService.getResponsesByApiId(10L)).thenReturn(mockList);

        List<ApiResponse> result = apiResponseController.getByApiId(10L);

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getApi().getId());
        verify(apiResponseService, times(1)).getResponsesByApiId(10L);
    }
}

