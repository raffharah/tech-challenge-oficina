package br.com.fiap.oficina.infrastructure.http;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.fiap.oficina.domain.ConflitoException;
import br.com.fiap.oficina.domain.DomainException;
import br.com.fiap.oficina.domain.NaoEncontradoException;
import br.com.fiap.oficina.infrastructure.TestcontainersConfig;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest
@AutoConfigureMockMvc
@Import({TestcontainersConfig.class, ErroHandlerIT.ControllerDeTeste.class})
class ErroHandlerIT {

    @Autowired
    MockMvc mvc;

    @Test
    void requestInvalidoRetorna400ComCampos() throws Exception {
        mvc.perform(post("/teste/validacao").with(jwt()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("REQUISICAO_INVALIDA"))
                .andExpect(jsonPath("$.mensagem").isNotEmpty())
                .andExpect(jsonPath("$.campos[0].campo").value("nome"))
                .andExpect(jsonPath("$.campos[0].mensagem").isNotEmpty());
    }

    @Test
    void recursoInexistenteRetorna404() throws Exception {
        mvc.perform(get("/teste/nao-encontrado").with(jwt()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("CLIENTE_NAO_ENCONTRADO"))
                .andExpect(jsonPath("$.mensagem").value("Cliente inexistente"));
    }

    @Test
    void conflitoDeNegocioRetorna409() throws Exception {
        mvc.perform(get("/teste/conflito").with(jwt()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CPF_DUPLICADO"))
                .andExpect(jsonPath("$.mensagem").value("CPF ja cadastrado"));
    }

    @Test
    void violacaoDeIntegridadeRetorna409() throws Exception {
        mvc.perform(get("/teste/integridade").with(jwt()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONFLITO"))
                .andExpect(jsonPath("$.mensagem").isNotEmpty());
    }

    @Test
    void regraDeNegocioRetorna422ComDetalhes() throws Exception {
        mvc.perform(get("/teste/dominio").with(jwt()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("SALDO_INSUFICIENTE"))
                .andExpect(jsonPath("$.mensagem").value("Saldo insuficiente"))
                .andExpect(jsonPath("$.detalhes[0]").value("item-1"));
    }

    @Test
    void erroNaoTratadoRetorna500SemStackTrace() throws Exception {
        mvc.perform(get("/teste/erro").with(jwt()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.codigo").value("ERRO_INTERNO"))
                .andExpect(jsonPath("$.mensagem").value("Erro interno do servidor"))
                .andExpect(content().string(not(containsString("detalhe interno"))))
                .andExpect(content().string(not(containsString("stackTrace"))))
                .andExpect(content().string(not(containsString("at br.com"))));
    }

    record Corpo(@NotBlank String nome) {
    }

    @RestController
    static class ControllerDeTeste {

        @PostMapping("/teste/validacao")
        void validacao(@Valid @RequestBody Corpo corpo) {
        }

        @GetMapping("/teste/nao-encontrado")
        void naoEncontrado() {
            throw new NaoEncontradoException("CLIENTE_NAO_ENCONTRADO", "Cliente inexistente");
        }

        @GetMapping("/teste/conflito")
        void conflito() {
            throw new ConflitoException("CPF_DUPLICADO", "CPF ja cadastrado");
        }

        @GetMapping("/teste/integridade")
        void integridade() {
            throw new DataIntegrityViolationException("fk_violada");
        }

        @GetMapping("/teste/dominio")
        void dominio() {
            throw new DomainException("SALDO_INSUFICIENTE", "Saldo insuficiente", List.of("item-1"));
        }

        @GetMapping("/teste/erro")
        void erro() {
            throw new IllegalStateException("detalhe interno");
        }
    }
}
