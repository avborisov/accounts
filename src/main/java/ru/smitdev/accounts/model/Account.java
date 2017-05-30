package ru.smitdev.accounts.model;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Account {

    private JdbcTemplate jdbcTemplate;
    public static final String ACCOUNTS_TABLE_NAME = "accounts";
    public static final String ACCOUNT_ID_COLUMN = "id";
    public static final String ACCOUNT_NUMBER_COLUMN = "account";
    public static final String SUBSCRIBER_ID_COLUMN = "subscriber_id";

    public Account() {
    }

    public Account(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String createAccountTable() {

        jdbcTemplate.execute("DROP TABLE IF EXISTS " + ACCOUNTS_TABLE_NAME + " CASCADE");
        jdbcTemplate.execute("DROP SEQUENCE IF EXISTS " + ACCOUNTS_TABLE_NAME + "_id_seq");

        jdbcTemplate.execute("CREATE TABLE " + ACCOUNTS_TABLE_NAME + "(\n" +
                ACCOUNT_ID_COLUMN + " INTEGER PRIMARY KEY, \n" +
                ACCOUNT_NUMBER_COLUMN  + " VARCHAR (12) NOT NULL UNIQUE CHECK (" +
                    ACCOUNT_NUMBER_COLUMN + " SIMILAR TO '[[:digit:]]{12}' AND char_length("+ ACCOUNT_NUMBER_COLUMN + ") = 12 ), \n" +
                SUBSCRIBER_ID_COLUMN + " INTEGER REFERENCES subscribers (id));"
        );

        jdbcTemplate.execute("CREATE SEQUENCE " + ACCOUNTS_TABLE_NAME + "_id_seq;");
        jdbcTemplate.execute("ALTER TABLE " + ACCOUNTS_TABLE_NAME + "\n" +
                "ALTER COLUMN id\n" +
                "SET DEFAULT NEXTVAL('" + ACCOUNTS_TABLE_NAME + "_id_seq');");
        return "table " + ACCOUNTS_TABLE_NAME + " created";
    }

    public String insertAccountTable(String number, Number subscriberId) {
        String insertQuery =
                "INSERT INTO " + ACCOUNTS_TABLE_NAME + " (" + ACCOUNT_NUMBER_COLUMN + ", " + SUBSCRIBER_ID_COLUMN + ") VALUES (?, ?);";
        jdbcTemplate.execute(insertQuery, new PreparedStatementCallback<Boolean>(){
            @Override
            public Boolean doInPreparedStatement(PreparedStatement preparedStatement) throws SQLException, DataAccessException {
                preparedStatement.setString(1, number);
                preparedStatement.setInt(2, (Integer) subscriberId);
                return preparedStatement.execute();
            }
        });
        return "account inserted";
    }

}
