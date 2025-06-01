package com.example.api_tierces;

import com.example.api_tierces.controller.EmailController;
import com.example.api_tierces.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailControllerTest {


    @Mock
    private EmailService emailService;

    @InjectMocks
    private EmailController emailController;


    @Test
    void sendTestEmailSuccess() {

        String testTo = "recipient@example.com";
        String testSubject = "Test Email Subject";
        String testBody = "This is the test email body.";
        String expectedResponse = "E-mail envoyé avec succès à " + testTo;

        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());
        String actualResponse = emailController.sendTestEmail(testTo, testSubject, testBody);

        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);

        verify(emailService, times(1)).sendEmail(testTo, testSubject, testBody);

    }

    @Test
    void sendTestEmailException() {
        String testTo = "recipient@example.com";
        String testSubject = "Test Subject Fail";
        String testBody = "Test Body Fail";
        RuntimeException simulatedException = new RuntimeException("sending failure");

        doThrow(simulatedException).when(emailService).sendEmail(testTo, testSubject, testBody);

        RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
            emailController.sendTestEmail(testTo, testSubject, testBody);
        });

        assertEquals(simulatedException.getMessage(), thrownException.getMessage());

        verify(emailService, times(1)).sendEmail(testTo, testSubject, testBody);
    }
}