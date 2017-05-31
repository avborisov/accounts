package ru.smitdev.accounts.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.smitdev.accounts.model.Account;
import ru.smitdev.accounts.model.AccountSubscriber;
import ru.smitdev.accounts.model.Subscriber;
import ru.smitdev.accounts.utils.NamesGenerator;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class AppController {
    @Autowired
    private Account account;

    @Autowired
    private Subscriber subscriber;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final int PAGE_SIZE = 100;

    @RequestMapping("/")
    public String app() {
        return "index";
    }

    @RequestMapping("/createdb")
    public String createTables() {
        account.createAccountTable();
        subscriber.createSubscriberTable();

        Thread thread1 = new Thread(new EntryGenerator(0, 250000 - 1));
        Thread thread2 = new Thread(new EntryGenerator(250000, 500000 - 1));
        Thread thread3 = new Thread(new EntryGenerator(500000, 750000 - 1));
        Thread thread4 = new Thread(new EntryGenerator(750000, 1000000 - 1));

        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();

        return "create";
    }

    @RequestMapping(value = "/getentries/name/{name}/account/{account}/page/{page}", method = RequestMethod.GET)
    @ResponseBody
    public List<AccountSubscriber> getEntryList(@PathVariable(value = "name") String name,
                                                @PathVariable(value = "account") String account,
                                                @PathVariable(value = "page") int page) {
        if (name.equals("all")) name = "";
        if (account.equals("all")) account = "";

        return selectRecordsFromTable(name, account, page);
    }

    @RequestMapping(value = "/count/name/{name}/account/{account}", method = RequestMethod.GET)
    @ResponseBody
    public int getEntryCount(@PathVariable(value = "name") String name,
                                                @PathVariable(value = "account") String account) {
        if (name.equals("all")) name = "";
        if (account.equals("all")) account = "";
        return selectEntryCount(name, account);
    }

    @RequestMapping(value = "/remove/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    public int removeEntry(@PathVariable(value = "id") String id) {
        return deleteEntry(id);
    }

    @RequestMapping(value = "/add/name/{name}/account/{account}", method = RequestMethod.PUT)
    @ResponseBody
    public boolean addEntry(@PathVariable(value = "name") String name,
                        @PathVariable(value = "account") String account) {
        return insertEntry(name, account);
    }

    /**
     * Insert new rows in tables
     * @param name - name of subscriber
     * @param accountNumber - number of account
     * @return true
     */
    private boolean insertEntry(String name, String accountNumber) {
        Number subscriberId = subscriber.insertSubscriberTable(name);
        account.insertAccountTable(accountNumber, subscriberId);
        return true;
    }

    /**
     * Remove row from tables
     * @param id - id of subscriber
     * @return count of removed rows
     */
    private int deleteEntry(String id) {
        String deleteAccounts = "DELETE FROM " + Account.ACCOUNTS_TABLE_NAME +
                " WHERE " + Account.SUBSCRIBER_ID_COLUMN + " = '" + id + "'";
        String deleteSubscriber = "DELETE FROM " + Subscriber.SUBSCRIBERS_TABLE_NAME +
                " WHERE " + Subscriber.SUBSCRIBER_ID_COLUMN + " = '" + id + "'";
        jdbcTemplate.update(deleteAccounts);
        return jdbcTemplate.update(deleteSubscriber);
    }

    /**
     * get count of entries in DB
     * @param name - name of subscriber
     * @param account - account number
     * @return
     */
    private int selectEntryCount(String name, String account) {
        String sql = "SELECT COUNT(" + Subscriber.SUBSCRIBERS_TABLE_NAME + "." + Subscriber.SUBSCRIBER_ID_COLUMN + ") " +
                "FROM " + Subscriber.SUBSCRIBERS_TABLE_NAME + " " +
                "JOIN " + Account.ACCOUNTS_TABLE_NAME + " " +
                "ON " + Subscriber.SUBSCRIBERS_TABLE_NAME + "." + Subscriber.SUBSCRIBER_ID_COLUMN + "=" +
                Account.ACCOUNTS_TABLE_NAME + "." + Account.SUBSCRIBER_ID_COLUMN + " " +
                "WHERE " + Subscriber.SUBSCRIBER_NAME_COLUMN + " LIKE '%" + name + "%' " +
                "AND " + Account.ACCOUNT_NUMBER_COLUMN + " LIKE '%" + account + "%' ";

        System.out.println(sql);

        int count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count;
    }

    /**
     * get Account and Subscribes records from DB
     * @param name - name of subscriber
     * @param account - account number
     * @param page - number of page
     * @return List of Account with Subscribers
     */
    private List<AccountSubscriber> selectRecordsFromTable(String name, String account, int page) {

        String sql = "SELECT " + Subscriber.SUBSCRIBERS_TABLE_NAME + "." + Subscriber.SUBSCRIBER_ID_COLUMN + ", " +
                Subscriber.SUBSCRIBER_NAME_COLUMN + ", " +
                Account.ACCOUNT_NUMBER_COLUMN + " " +
                "FROM " + Subscriber.SUBSCRIBERS_TABLE_NAME + " " +
                "JOIN " + Account.ACCOUNTS_TABLE_NAME + " " +
                "ON " + Subscriber.SUBSCRIBERS_TABLE_NAME + "." + Subscriber.SUBSCRIBER_ID_COLUMN + "=" +
                Account.ACCOUNTS_TABLE_NAME + "." + Account.SUBSCRIBER_ID_COLUMN + " " +
                "WHERE " + Subscriber.SUBSCRIBER_NAME_COLUMN + " LIKE '%" + name + "%' " +
                "AND " + Account.ACCOUNT_NUMBER_COLUMN + " LIKE '%" + account + "%' " +
                "ORDER BY " + Subscriber.SUBSCRIBERS_TABLE_NAME + "." + Subscriber.SUBSCRIBER_ID_COLUMN + " " +
                "ASC OFFSET " + PAGE_SIZE*(page-1) + " LIMIT " + PAGE_SIZE;

        System.out.println(sql);

        List<AccountSubscriber> results = jdbcTemplate.query(sql, new RowMapper<AccountSubscriber>() {
            @Override
            public AccountSubscriber mapRow(ResultSet rs, int rowNum) throws SQLException {
                AccountSubscriber ac = new AccountSubscriber();
                ac.setId(rs.getString(1));
                ac.setSubscriber(rs.getString(2));
                ac.setAccount(rs.getString(3));
                return ac;
            }
        });

        return results;

    }

    private String getTwelveDigits(int i) {
        StringBuilder sb = new StringBuilder();
        for (int k = 0; k < 12 - String.valueOf(i).length(); k++) {
            sb.append("0");
        }
        sb.append(i);
        return sb.toString();
    }

    private class EntryGenerator implements Runnable {

        private NamesGenerator namesGenerator = new NamesGenerator();
        private int startIndex;
        private int endIndex;

        public EntryGenerator(int startIndex, int endIndex) {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }

        @Override
        public void run() {
            for (int i = startIndex; i <= endIndex; i++) {
                Number subscriberId = subscriber.insertSubscriberTable(namesGenerator.generateManName());
                account.insertAccountTable(getTwelveDigits(i), subscriberId);
                if (i % 100 == 0) System.out.println("Account with index " + i + " inserted");
            }
        }
    }
}
