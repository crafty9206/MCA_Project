package com.maatricare.tracking;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("local")
@Order(0)
public class LocalTaskSchemaMigration implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public LocalTaskSchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        repairWeeklyReminderDays();

        Integer legacyTitleColumns = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE table_schema = DATABASE()
                  AND table_name = 'care_tasks'
                  AND column_name = 'title'
                """, Integer.class);

        if (legacyTitleColumns == null || legacyTitleColumns == 0) return;

        jdbcTemplate.update("""
                INSERT IGNORE INTO task_details (id, title)
                SELECT UUID_TO_BIN(UUID()), title
                FROM care_tasks
                WHERE title IS NOT NULL
                GROUP BY title
                """);
        jdbcTemplate.update("""
                UPDATE care_tasks care
                JOIN task_details details ON details.title = care.title
                SET care.task_detail_id = details.id
                WHERE care.task_detail_id IS NULL
                """);

        List<String> legacyIndexes = jdbcTemplate.queryForList("""
            SELECT DISTINCT index_name
            FROM information_schema.statistics
            WHERE table_schema = DATABASE()
              AND table_name = 'care_tasks'
              AND column_name = 'title'
              AND index_name <> 'PRIMARY'
            """, String.class);
        legacyIndexes.forEach(indexName -> jdbcTemplate.execute(
            "ALTER TABLE care_tasks DROP INDEX `" + indexName.replace("`", "``") + "`"));

        jdbcTemplate.execute("""
            ALTER TABLE care_tasks
                DROP COLUMN title,
                MODIFY task_detail_id BINARY(16) NOT NULL
            """);
    }

        private void repairWeeklyReminderDays() {
                Integer preferenceColumns = jdbcTemplate.queryForObject("""
                                SELECT COUNT(*)
                                FROM information_schema.columns
                                WHERE table_schema = DATABASE()
                                    AND table_name = 'users'
                                    AND column_name = 'weekly_pregnancy_reminder_day'
                                """, Integer.class);
                if (preferenceColumns == null || preferenceColumns == 0) return;
                jdbcTemplate.update("""
                                UPDATE users
                                SET weekly_pregnancy_reminder_day = 1
                                WHERE weekly_pregnancy_reminder_day < 1
                                     OR weekly_pregnancy_reminder_day > 7
                                """);
        }
}
