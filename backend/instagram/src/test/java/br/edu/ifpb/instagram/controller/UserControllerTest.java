package br.edu.ifpb.instagram.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.edu.ifpb.instagram.model.dto.UserDto;
import br.edu.ifpb.instagram.model.request.UserDetailsRequest;
import br.edu.ifpb.instagram.security.JwtUtils;
import br.edu.ifpb.instagram.service.UserService;
import br.edu.ifpb.instagram.service.impl.UserDetailsServiceImpl;

import java.util.Arrays;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void shouldReturnListOfUsers() throws Exception {
        List<UserDto> userDtos = Arrays.asList(
            new UserDto(1L, "Usuário 1", "user1", "user1@email.com", null, null),
            new UserDto(2L, "Usuário 2", "user2", "user2@email.com", null, null)
        );

        when(userService.findAll()).thenReturn(userDtos);

        mockMvc.perform(get("/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].username").value("user1"))
            .andExpect(jsonPath("$[1].username").value("user2"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void shouldReturnUserDetailsForGivenId() throws Exception {
        UserDto userDto = new UserDto(1L, "Usuário Teste", "testuser", "test@email.com", null, null);

        when(userService.findById(1L)).thenReturn(userDto);

        mockMvc.perform(get("/users/{id}", 1L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldDeleteUserAndReturnSuccessMessage() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/users/{id}", 1L)
                .with(csrf())) // Adicione esta linha
            .andExpect(status().isOk())
            .andExpect(content().string("user was deleted!"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"}) // Simula autenticação
    void shouldUpdateUserAndReturnUpdatedDetails() throws Exception {
        // Dados de requisição para atualização
        UserDetailsRequest request = new UserDetailsRequest(
            1L, "Nome Atualizado", "user-updated", "updated@email.com", "senha123"
        );
        // DTO retornado após a atualização
        UserDto updatedUserDto = new UserDto(
            1L, "Nome Atualizado", "user-updated", "updated@email.com", null, null
        );

        when(userService.updateUser(any(UserDto.class))).thenReturn(updatedUserDto);

        mockMvc.perform(put("/users")
                .with(csrf()) // Adicione esta linha para incluir o token CSRF
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.fullName").value("Nome Atualizado"))
                .andExpect(jsonPath("$.username").value("user-updated"))
                .andExpect(jsonPath("$.email").value("updated@email.com"));
    } 
}