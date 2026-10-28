package br.com.fiap.oficina.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
@Import(TestcontainersConfig.class)
class OficinaApplicationIT {

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void sobeContextoComFlywayAplicadoEmBancoVazio() {
        Integer tabelas = jdbc.queryForObject(
                "select count(*) from information_schema.tables where table_name = 'flyway_schema_history'",
                Integer.class);

        assertThat(tabelas).isEqualTo(1);
    }
}
