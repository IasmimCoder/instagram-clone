package br.edu.ifpb.instagram.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JwtUtilsTest {

    @Autowired
    JwtUtils jwtUtils;

    /**
     * Testa se o token gerado contém o nome de usuário correto.
     */
    @Test
    void generateToken_givenAuthentication_shouldContainUsername() {
        // Arrange: criar Authentication fake
        String username = "usuarioTeste";
        Authentication auth = new UsernamePasswordAuthenticationToken(username, null);

        // Act: gerar token
        String token = jwtUtils.generateToken(auth);

        // Assert: token não é nulo e username pode ser recuperado
        assertNotNull(token);
        String usernameFromToken = jwtUtils.getUsernameFromToken(token);
        assertEquals(username, usernameFromToken);
    }

    /**
     * Testa se a validação retorna true para um token válido.
     */
    @Test
    void validateToken_givenValidToken_shouldReturnTrue() {
        String username = "usuarioValido";
        Authentication auth = new UsernamePasswordAuthenticationToken(username, null);

        String token = jwtUtils.generateToken(auth);

        boolean isValid = jwtUtils.validateToken(token);
        assertTrue(isValid);
    }

    /**
     * Testa se a validação retorna false para um token inválido.
     */
    @Test
    void validateToken_givenInvalidToken_shouldReturnFalse() {
        String invalidToken = "tokenInvalidoQualquer";

        boolean isValid = jwtUtils.validateToken(invalidToken);
        assertFalse(isValid);
    }

    /**
     * Testa se é possível extrair o username de um token válido.
     */
    @Test
    void getUsernameFromToken_givenValidToken_shouldReturnCorrectUsername() {
        String username = "usuarioExtraido";
        Authentication auth = new UsernamePasswordAuthenticationToken(username, null);

        String token = jwtUtils.generateToken(auth);
        String usernameFromToken = jwtUtils.getUsernameFromToken(token);

        assertEquals(username, usernameFromToken);
    }
}
