package com.n75jr.apitesttask.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.n75jr.apitesttask.dao.SockRepository;
import com.n75jr.apitesttask.model.Sock;
import com.n75jr.apitesttask.model.SockID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
public class SockControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private SockRepository repository;

    // POSTs:
    //
    @Test
    public void postIncomeWhenSockAbsentsOrExistsByValidCheck() throws Exception {
//         cottonPart is bad because < 0
        Sock sock = new Sock("green", -1, 10);

        mvc.perform(MockMvcRequestBuilders.
                post("/api/socks/income")
                .content(objectMapper.writeValueAsString(sock))
                .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(sock)));

//         cottonPart is bad because == 0
        sock.setCottonPart(0);

        mvc.perform(MockMvcRequestBuilders.
                post("/api/socks/income")
                .content(objectMapper.writeValueAsString(sock))
                .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(sock)));

//         quantity is bad because < 0
        sock.setCottonPart(1);
        sock.setQuantity(-1);

        mvc.perform(MockMvcRequestBuilders.
                post("/api/socks/income")
                .content(objectMapper.writeValueAsString(sock))
                .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(sock)));

//         quantity is bad because == 0
        sock.setQuantity(0);

        mvc.perform(MockMvcRequestBuilders.
                post("/api/socks/income")
                .content(objectMapper.writeValueAsString(sock))
                .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(sock)));
    }

    @Test
    public void postIncomeWhenSockExists() throws Exception {
        int addQuantity = 100;
        Sock sock = new Sock("green", 75, 10);
        Sock addedSock = new Sock(sock.getColor(), sock.getCottonPart(), addQuantity);
        Sock expectedSock = new Sock(sock.getColor(), sock.getCottonPart(), sock.getQuantity() + addQuantity);
        SockID id = new SockID(sock.getColor(), sock.getCottonPart());

        Mockito.when(repository.existsById(id)).thenReturn(true);
        Mockito.when(repository.getById(id)).thenReturn(sock);
        Mockito.when(repository.save(sock)).thenReturn(expectedSock);

        mvc.perform(MockMvcRequestBuilders.
                post("/api/socks/income")
                .content(objectMapper.writeValueAsString(addedSock))
                .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedSock)));
    }
}
