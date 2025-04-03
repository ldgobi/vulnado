package com.scalesec.vulnado;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class LoginControllerTests {

    @Value("${app.secret}")
    private String secret;

    // Helper method to create a mock User object
    private User createMockUser(String username, String hashedPassword, String token) {
        User user = mock(User.class);
        when(user.hashedPassword).thenReturn(hashedPassword);
        when(user.token(secret)).thenReturn(token);
        return user;
    }

    // Helper method to mock Postgres.md5 behavior
    private String mockMd5(String input) {
        return "mockedMd5Hash_" + input;
    }

    @Test
    public void login_ValidCredentials_ShouldReturnToken() {
        // Arrange
        LoginController controller = new LoginController();
        controller.secret = "testSecret";

        LoginRequest request = new LoginRequest();
        request.username = "testUser";
        request.password = "testPassword";

        User mockUser = createMockUser("testUser", mockMd5("testPassword"), "testToken");
        Mockito.mockStatic(User.class);
        Mockito.mockStatic(Postgres.class);

        when(User.fetch("testUser")).thenReturn(mockUser);
        when(Postgres.md5("testPassword")).thenReturn(mockMd5("testPassword"));

        // Act
        LoginResponse response = controller.login(request);

        // Assert
        assertNotNull("Response should not be null", response);
        assertEquals("Token should match expected value", "testToken", response.token);

        // Cleanup
        Mockito.clearAllCaches();
    }

    @Test(expected = Unauthorized.class)
    public void login_InvalidCredentials_ShouldThrowUnauthorized() {
        // Arrange
        LoginController controller = new LoginController();
        controller.secret = "testSecret";

        LoginRequest request = new LoginRequest();
        request.username = "testUser";
        request.password = "wrongPassword";

        User mockUser = createMockUser("testUser", mockMd5("testPassword"), "testToken");
        Mockito.mockStatic(User.class);
        Mockito.mockStatic(Postgres.class);

        when(User.fetch("testUser")).thenReturn(mockUser);
        when(Postgres.md5("wrongPassword")).thenReturn(mockMd5("wrongPassword"));

        // Act
        controller.login(request);

        // Cleanup
        Mockito.clearAllCaches();
    }

    @Test(expected = ResponseStatusException.class)
    public void login_NonExistentUser_ShouldThrowUnauthorized() {
        // Arrange
        LoginController controller = new LoginController();
        controller.secret = "testSecret";

        LoginRequest request = new LoginRequest();
        request.username = "nonExistentUser";
        request.password = "testPassword";

        Mockito.mockStatic(User.class);
        Mockito.mockStatic(Postgres.class);

        when(User.fetch("nonExistentUser")).thenReturn(null);

        // Act
        controller.login(request);

        // Cleanup
        Mockito.clearAllCaches();
    }
}
