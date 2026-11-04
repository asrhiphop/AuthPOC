package com.example.authpoc.integration;

import com.example.authpoc.auth.token.RefreshToken;
import com.example.authpoc.auth.token.RefreshTokenRepository;
import com.example.authpoc.user.Role;
import com.example.authpoc.user.User;
import com.example.authpoc.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("integration")
@Transactional
class PostgreSqlMigrationIT {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    void migrationsRunInOrderAndCreateTheApplicationSchema() {
        List<Map<String, Object>> migrationHistory = jdbcTemplate.queryForList("""
                SELECT version, success
                FROM flyway_schema_history
                ORDER BY installed_rank
                """);

        assertThat(migrationHistory).isNotEmpty();
        assertThat(migrationHistory)
                .allSatisfy(row -> assertThat(row.get("success")).isEqualTo(true));

        assertThat(jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = 'public'
                  AND table_name IN ('users', 'refresh_tokens')
                """, Long.class))
                .isEqualTo(2L);
    }

    @Test
    void migrationsCreateExpectedConstraintsAndIndexes() {
        assertThat(jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.table_constraints
                WHERE table_schema = 'public'
                  AND table_name = 'users'
                  AND constraint_type = 'UNIQUE'
                """, Long.class))
                .isEqualTo(2L);

        assertThat(jdbcTemplate.queryForObject("""
                SELECT rc.delete_rule
                FROM information_schema.referential_constraints rc
                JOIN information_schema.table_constraints tc
                  ON tc.constraint_schema = rc.constraint_schema
                 AND tc.constraint_name = rc.constraint_name
                WHERE tc.table_schema = 'public'
                  AND tc.table_name = 'refresh_tokens'
                  AND tc.constraint_type = 'FOREIGN KEY'
                """, String.class))
                .isEqualTo("CASCADE");

        List<String> indexes = jdbcTemplate.queryForList("""
                SELECT indexname
                FROM pg_indexes
                WHERE schemaname = 'public'
                  AND tablename = 'refresh_tokens'
                """, String.class);

        assertThat(indexes).contains(
                "idx_refresh_tokens_token",
                "idx_refresh_tokens_family_id",
                "idx_refresh_tokens_user_id"
        );
    }

    @Test
    void postgresSchemaSupportsJpaPersistence() {
        String suffix = UUID.randomUUID().toString();
        User user = userRepository.saveAndFlush(new User(
                "integration-" + suffix,
                "integration-" + suffix + "@authpoc.test",
                "encoded-password",
                Role.USER
        ));

        RefreshToken refreshToken = refreshTokenRepository.saveAndFlush(new RefreshToken(
                user,
                "integration-token-" + suffix,
                Instant.now().plusSeconds(3600),
                UUID.randomUUID()
        ));

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM refresh_tokens WHERE id = ? AND user_id = ?",
                Long.class,
                refreshToken.getId(),
                user.getId()
        )).isEqualTo(1L);
    }
}