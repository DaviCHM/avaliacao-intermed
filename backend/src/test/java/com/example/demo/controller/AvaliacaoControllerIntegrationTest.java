package com.example.demo.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.model.Avaliacao;
import com.example.demo.repository.AvaliacaoRepository;

@SpringBootTest
@AutoConfigureMockMvc
class AvaliacaoControllerIntegrationTest {

    private static final String EMAIL_CLAIM = "https://avaliacoes-api/email";
    private static final String ROLES_CLAIM = "https://avaliacoes-api/roles";

    private static final String BODY = """
            {
                "projeto": "Projeto X",
                "titulo": "Criar tela de login",
                "descricao": "Usar Auth0",
                "prioridade": 9
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    @Autowired
    private JwtAuthenticationConverter jwtAuthenticationConverter;

    @BeforeEach
    void limpar() {
        avaliacaoRepository.deleteAll();
    }

    @Test
    void deveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/tarefa")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/tarefa").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/tarefa/1")).andExpect(status().isUnauthorized());
    }

    @Test
    void deveCriarTarefaComEmailDoToken() throws Exception {
        mockMvc.perform(post("/tarefa")
                        .with(jwt().jwt(j -> j.claim(EMAIL_CLAIM, "user@teste.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.titulo").value("Criar tela de login"))
                .andExpect(jsonPath("$.prioridade").value(9))
                .andExpect(jsonPath("$.emailCriador").value("user@teste.com"));

        assertThat(avaliacaoRepository.count()).isEqualTo(1);
    }

    @Test
    void deveListar() throws Exception {
        mockMvc.perform(get("/tarefa").with(jwt()))
                .andExpect(status().isOk());
    }

    @Test
    void deveDeletar() throws Exception {
        Avaliacao salva = avaliacaoRepository.save(Avaliacao.builder()
                .projeto("P").titulo("T").prioridade(1)
                .emailCriador("a@a.com").dataCriacao(LocalDateTime.now()).build());

        mockMvc.perform(delete("/tarefa/" + salva.getId()).with(jwt()))
                .andExpect(status().isNoContent());

        assertThat(avaliacaoRepository.count()).isZero();
    }

    @Test
    void deveRetornar404AoDeletarInexistente() throws Exception {
        mockMvc.perform(delete("/tarefa/999").with(jwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornarBadRequestComPrioridadeInvalida() throws Exception {
        String body = """
                { "projeto": "P", "titulo": "T", "prioridade": 11 }
                """;

        mockMvc.perform(post("/tarefa")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void converterDeveLerRolesDaClaimDoAuth0() {
        Jwt token = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "auth0|123")
                .claim(ROLES_CLAIM, List.of("ADMIN"))
                .build();

        var auth = jwtAuthenticationConverter.convert(token);

        assertThat(auth.getAuthorities()).extracting("authority").contains("ADMIN");
    }
}
