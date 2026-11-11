package com.example.demo.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.repository.AvaliacaoRepository;

@SpringBootTest
@AutoConfigureMockMvc
class AvaliacaoControllerIntegrationTest {

    private static final String EMAIL_CLAIM = "https://avaliacoes-api/email";
    private static final String ROLES_CLAIM = "https://avaliacoes-api/roles";

    private static final String BODY = """
            {
                "emailAvaliado": "fulano@empresa.com",
                "comentarios": "Bom trabalho",
                "nota": 9
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
        mockMvc.perform(get("/avaliacoes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminDeveCriarAvaliacaoComEmailDoToken() throws Exception {
        mockMvc.perform(post("/avaliacoes")
                        .with(jwt().jwt(j -> j.claim(EMAIL_CLAIM, "admin@teste.com"))
                                .authorities(new SimpleGrantedAuthority("ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.emailAvaliador").value("admin@teste.com"))
                .andExpect(jsonPath("$.emailAvaliado").value("fulano@empresa.com"))
                .andExpect(jsonPath("$.nota").value(9))
                .andExpect(jsonPath("$.dataAvaliacao", notNullValue()));

        assertThat(avaliacaoRepository.count()).isEqualTo(1);
    }

    @Test
    void userDeveListar() throws Exception {
        mockMvc.perform(get("/avaliacoes")
                        .with(jwt().authorities(new SimpleGrantedAuthority("USER"))))
                .andExpect(status().isOk());
    }

    @Test
    void userNaoPodeCriar() throws Exception {
        mockMvc.perform(post("/avaliacoes")
                        .with(jwt().authorities(new SimpleGrantedAuthority("USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveRetornarBadRequestComNotaInvalida() throws Exception {
        String body = """
                {
                    "emailAvaliado": "fulano@empresa.com",
                    "nota": 11
                }
                """;

        mockMvc.perform(post("/avaliacoes")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ADMIN")))
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

        assertThat(auth.getAuthorities())
                .extracting("authority")
                .contains("ADMIN");
    }
}
