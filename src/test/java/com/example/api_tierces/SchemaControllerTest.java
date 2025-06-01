package com.example.api_tierces;

import com.example.api_tierces.controller.SchemaController;

import com.example.api_tierces.model.Schema;
import com.example.api_tierces.service.SchemaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SchemaControllerTest {

    @Mock
    private SchemaService schemaService;

    @InjectMocks
    private SchemaController schemaController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllSchemas() {
        Schema schema1 = new Schema();
        schema1.setId(1L);

        Schema schema2 = new Schema();
        schema2.setId(2L);

        List<Schema> mockList = Arrays.asList(schema1, schema2);
        when(schemaService.getAllSchemas()).thenReturn(mockList);

        List<Schema> result = schemaController.getAllSchemas();

        assertEquals(2, result.size());
        verify(schemaService, times(1)).getAllSchemas();
    }

    @Test
    void testGetSchemaById_Found() {
        Schema schema = new Schema();
        schema.setId(1L);

        when(schemaService.getSchemaById(1L)).thenReturn(Optional.of(schema));

        ResponseEntity<Schema> response = schemaController.getSchemaById(1L); //response : contient reponse http + body

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1L, response.getBody().getId());
        verify(schemaService, times(1)).getSchemaById(1L);
    }

    @Test
    void testGetSchemaById_NotFound() {
        when(schemaService.getSchemaById(999L)).thenReturn(Optional.empty());

        ResponseEntity<Schema> response = schemaController.getSchemaById(999L);

        assertEquals(404, response.getStatusCodeValue());
        assertNull(response.getBody());
        verify(schemaService, times(1)).getSchemaById(999L);
    }

    @Test
    void testGetSchemaByName_Found() {
        Schema schema = new Schema();
        schema.setId(1L);
        schema.setName("TestSchema");

        List<Schema> schemas = List.of(schema);
        when(schemaService.getSchemaByName("TestSchema")).thenReturn(schemas);

        ResponseEntity<List<Schema>> response = schemaController.getSchemaByName("TestSchema");

        assertEquals(200, response.getStatusCodeValue());
        assertFalse(response.getBody().isEmpty());
        verify(schemaService, times(1)).getSchemaByName("TestSchema");
    }

    @Test
    void testGetSchemaByName_NotFound() {
        when(schemaService.getSchemaByName("Unknown")).thenReturn(Collections.emptyList());

        ResponseEntity<List<Schema>> response = schemaController.getSchemaByName("Unknown");

        assertEquals(404, response.getStatusCodeValue());
        verify(schemaService, times(1)).getSchemaByName("Unknown");
    }
}

