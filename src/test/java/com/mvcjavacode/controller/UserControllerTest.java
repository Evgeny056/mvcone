package com.mvcjavacode.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mvcjavacode.exception.UserNotFoundException;
import com.mvcjavacode.handler.RestControllerAdvice;
import com.mvcjavacode.model.entity.Order;
import com.mvcjavacode.model.entity.User;
import com.mvcjavacode.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserController userController;
    @Mock
    private ControllerAdvice controllerAdvice;
    private MockMvc mockMvc;

    private User user;
    private User user2;
    private List<User> users;
    private List<Order> orders;
    private Order order;
    private Order order2;
    private ObjectMapper objectMapper;

    private static final String NAME_USER_ONE = "Анатолий";
    private static final String NAME_USER_TWO = "Николай";
    private static final String EMAIL_ONE = "anatoly@example.com";
    private static final String EMAIL_TWO = "nikolay@example.com";
    private static final String PRODUCT = "Soap";
    private static final String PRODUCT_TWO = "Pencil";
    private static final String STATUS_PRODUCT = "Доставлено";
    private static final String STATUS_PRODUCT2 = "В доставке";

    @BeforeEach
    public void setUp() {

        objectMapper = new ObjectMapper();

        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .setControllerAdvice(new RestControllerAdvice())
                .build();

        order = new Order(1L, PRODUCT,STATUS_PRODUCT,user);
        order2 = new Order(2L, PRODUCT_TWO,STATUS_PRODUCT2,user2);

        orders = Arrays.asList(order,order2);

        user = new User(1L,NAME_USER_ONE,EMAIL_ONE,orders);
        user2 = new User(2L,NAME_USER_TWO,EMAIL_TWO,orders);

        users = Arrays.asList(user, user2);
    }

    @Test
    public void getAllUsersTest() throws Exception {
        when(userRepository.findAll()).thenReturn(users);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value(NAME_USER_ONE))
                .andExpect(jsonPath("$[1].name").value(NAME_USER_TWO))
                .andDo(print());
    }

    @Test
    public void getUserByIdTest() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value(NAME_USER_ONE))
                .andExpect(jsonPath("$.email").value(EMAIL_ONE))
                .andDo(print());
    }

    @Test
    public void getUserByIdTestBed() throws Exception {
        when(userRepository.findById(3L)).thenThrow(UserNotFoundException.class);

        mockMvc.perform(get("/api/users/3"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andDo(print());
    }
    @Test
    public void createUserTest() throws Exception {
        User newuser = new User(4L,NAME_USER_TWO,EMAIL_ONE);
        when(userRepository.save(any(User.class))).thenReturn(newuser);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newuser)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(newuser.getId()))
                .andExpect(jsonPath("$.name").value(newuser.getName()))
                .andExpect(jsonPath("$.email").value(newuser.getEmail()))
                .andDo(print());
    }

    @Test
    public void updateUserTest() throws Exception {
        User existingUser = new User(4L,NAME_USER_ONE,EMAIL_TWO);
        User updatedUser = new User(4L,NAME_USER_ONE,EMAIL_ONE);

        when(userRepository.findById(existingUser.getId())).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/4")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(updatedUser.getId()))
                .andExpect(jsonPath("$.name").value(updatedUser.getName()))
                .andExpect(jsonPath("$.email").value(updatedUser.getEmail()))
                .andDo(print());
    }

    @Test
    public void deleteUserTestOk() throws Exception {
        when(userRepository.existsById(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteUserTestUserNotFound() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andDo(print());
    }
}
