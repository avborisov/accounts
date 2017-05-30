package ru.smitdev.accounts.model;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Subscriber {

    private JdbcTemplate jdbcTemplate;
    public static final String SUBSCRIBERS_TABLE_NAME = "subscribers";
    public static final String SUBSCRIBER_ID_COLUMN = "id";
    public static final String SUBSCRIBER_NAME_COLUMN = "name";

    public Subscriber() {
    }

    public Subscriber(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String createSubscriberTable() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS " + SUBSCRIBERS_TABLE_NAME + " CASCADE");
        jdbcTemplate.execute("DROP SEQUENCE IF EXISTS " + SUBSCRIBERS_TABLE_NAME + "_id_seq");

        jdbcTemplate.execute("CREATE TABLE " + SUBSCRIBERS_TABLE_NAME + "(\n" +
                SUBSCRIBER_ID_COLUMN + " INTEGER PRIMARY KEY, \n" +
                SUBSCRIBER_NAME_COLUMN + " VARCHAR (255) NOT NULL);"
        );
        jdbcTemplate.execute("CREATE SEQUENCE " + SUBSCRIBERS_TABLE_NAME + "_id_seq;");
        jdbcTemplate.execute("ALTER TABLE " + SUBSCRIBERS_TABLE_NAME + "\n" +
                "ALTER COLUMN id\n" +
                "SET DEFAULT NEXTVAL('" + SUBSCRIBERS_TABLE_NAME + "_id_seq');");
        return "table " + SUBSCRIBERS_TABLE_NAME + " created";
    }

    public Number insertSubscriberTable(String name) {
        String insertQuery = "INSERT INTO " + SUBSCRIBERS_TABLE_NAME + " (" + SUBSCRIBER_NAME_COLUMN + ") VALUES (?);";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                new PreparedStatementCreator() {
                    public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                        PreparedStatement ps =
                                connection.prepareStatement(insertQuery, new String[]{"id"});
                        ps.setString(1, name);
                        return ps;
                    }
                },
                keyHolder);
        return keyHolder.getKey();
    }


}
