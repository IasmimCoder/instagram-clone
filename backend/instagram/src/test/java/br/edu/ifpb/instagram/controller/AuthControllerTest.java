package br.edu.ifpb.instagram.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.edu.ifpb.instagram.exception.FieldAlreadyExistsException;
import br.edu.ifpb.instagram.model.dto.UserDto;
import br.edu.ifpb.instagram.model.request.LoginRequest;
import br.edu.ifpb.instagram.model.request.UserDetailsRequest;
import br.edu.ifpb.instagram.security.JwtUtils;
import br.edu.ifpb.instagram.service.UserService;
import br.edu.ifpb.instagram.service.impl.AuthServiceImpl;
import br.edu.ifpb.instagram.service.impl.UserDetailsServiceImpl;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthServiceImpl authService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnTokenOnSuccessfulSignIn() throws Exception {
        String username = "Luan Fernandes";
        String password = "password123";

        // Dados de requisição
        LoginRequest loginRequest = new LoginRequest(username, password);

        // Simula o comportamento do serviço para retornar um token
        when(authService.authenticate(any(LoginRequest.class)))
                .thenReturn("mock-jwt-token");

        // Executa a requisição e valida a resposta
        mockMvc.perform(post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.username").value(username));
    }

    @Test
    void shouldCreateUserOnSuccessfulSignUp() throws Exception {
    // Dados da requisição
    UserDetailsRequest userDetailsRequest = new UserDetailsRequest(
    null,
    "João da Silva",
    "joao.silva",
    "joao.silva@example.com",
    "password123"
    );

    // DTO que será retornado pelo serviço após a criação
    UserDto createdUserDto = new UserDto(
    1L,
    "João da Silva",
    "joao.silva",
    "joao.silva@example.com",
    null, // A senha não é retornada
    null
    );

    // Simula o comportamento do serviço
    when(userService.createUser(any(UserDto.class))).thenReturn(createdUserDto);

    // Executa a requisição e valida a resposta
    mockMvc.perform(post("/auth/signup")
    .contentType(MediaType.APPLICATION_JSON)
    .content(objectMapper.writeValueAsString(userDetailsRequest)))
    .andExpect(status().isCreated()) // Espera status 201
    .andExpect(jsonPath("$.id").value(1L))
    .andExpect(jsonPath("$.fullName").value("João da Silva"))
    .andExpect(jsonPath("$.username").value("joao.silva"))
    .andExpect(jsonPath("$.email").value("joao.silva@example.com"));
    }

    @Test
    void shouldReturnUnauthorizedOnFailedSignIn() throws Exception {
        // Dados de requisição com credenciais incorretas
        LoginRequest loginRequest = new LoginRequest("usuario_errado", "senha_incorreta");

        // Simula o serviço lançando uma exceção de autenticação
        when(authService.authenticate(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Credenciais inválidas."));

        // Executa a requisição e valida a resposta
        mockMvc.perform(post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized()); // Espera um status 401
    }

    @Test
    void shouldReturnConflictOnSignUpWithExistingEmail() throws Exception {
        // Dados de requisição com um e-mail já existente
        UserDetailsRequest userDetailsRequest = new UserDetailsRequest(
                null,
                "João da Silva",
                "joao.silva",
                "joao.silva@example.com",
                "password123"
        );

        // Simula o serviço lançando uma exceção de conflito
        when(userService.createUser(any(UserDto.class)))
                .thenThrow(new FieldAlreadyExistsException("E-mail já cadastrado."));

        // Executa a requisição e valida a resposta
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDetailsRequest)))
                .andExpect(status().isConflict()); // Espera um status 409
    }

    @Test
    void shouldReturnConflictOnSignUpWithExistingUsername() throws Exception {
        // Dados de requisição com um nome de usuário já existente
        UserDetailsRequest userDetailsRequest = new UserDetailsRequest(
                null,
                "João da Silva",
                "joao.silva",
                "joao.silva@example.com",
                "password123"
        );

        // Simula o serviço lançando uma exceção de conflito
        when(userService.createUser(any(UserDto.class)))
                .thenThrow(new FieldAlreadyExistsException("Nome de usuário já cadastrado."));

        // Executa a requisição e valida a resposta
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDetailsRequest)))
                .andExpect(status().isConflict()); // Espera um status 409
    }
}