package com.spendsmart.authservice.config;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserProviderConstraintSync implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(UserProviderConstraintSync.class);

    private final JdbcTemplate jdbcTemplate;

    public UserProviderConstraintSync(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<String> providerCheckConstraints;
        try {
            providerCheckConstraints = jdbcTemplate.queryForList(
                    """
                    select con.conname
                    from pg_constraint con
                    join pg_class rel on rel.oid = con.conrelid
                    join pg_namespace ns on ns.oid = rel.relnamespace
                    where con.contype = 'c'
                      and ns.nspname = current_schema()
                      and rel.relname = 'users'
                      and pg_get_constraintdef(con.oid) ilike '%provider%'
                    """,
                    String.class
            );
        } catch (DataAccessException exception) {
            log.debug("Skipping provider constraint sync because the database catalog is not PostgreSQL-compatible: {}", exception.getMessage());
            return;
        }

        for (String constraintName : providerCheckConstraints) {
            jdbcTemplate.execute("alter table users drop constraint if exists \"" + constraintName + "\"");
        }

        try {
            jdbcTemplate.execute(
                    "alter table users add constraint users_provider_check " +
                    "check (provider in ('LOCAL','GOOGLE','GITHUB'))"
            );
            log.info("Synchronized users.provider check constraint with allowed OAuth providers");
        } catch (DataAccessException ex) {
            String message = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
            if (message.contains("already exists")) {
                log.debug("users_provider_check already exists; skipping recreate");
                return;
            }
            throw ex;
        }
    }
}
