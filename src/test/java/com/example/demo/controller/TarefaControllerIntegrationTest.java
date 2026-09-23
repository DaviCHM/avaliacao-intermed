package com.example.demo.controller;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.repository.AuditoriaRepository;
import com.example.demo.repository.TarefaRepository;

@SpringBootTest
@AutoConfigureMockMvc
class TarefaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TarefaRepository tarefaRepository;

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    @Test
    void deveCriarTarefaComSucesso() throws Exception {
        String requestBody = """
                {
                    "titulo": "Estudar Spring Boot",
                    "descricao": "Revisar padrao Observer",
                    "prioridade": "ALTA"
                }
                """;

        mockMvc.perform(post("/tarefas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.titulo").value("Estudar Spring Boot"))
                .andExpect(jsonPath("$.descricao").value("Revisar padrao Observer"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.prioridade").value("ALTA"))
                .andExpect(jsonPath("$.dataCriacao", notNullValue()));

        org.assertj.core.api.Assertions.assertThat(tarefaRepository.count()).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(auditoriaRepository.count()).isEqualTo(1);
    }

    @Test
    void deveRetornarBadRequestQuandoTituloEmBranco() throws Exception {
        String requestBody = """
                {
                    "titulo": "",
                    "descricao": "Descricao valida",
                    "prioridade": "MEDIA"
                }
                """;

        mockMvc.perform(post("/tarefas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }
}
