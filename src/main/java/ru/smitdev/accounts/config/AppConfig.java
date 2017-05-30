package ru.smitdev.accounts.config;

import ru.smitdev.accounts.model.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import ru.smitdev.accounts.model.Subscriber;

@Configuration
@PropertySource(value = {"classpath:util.properties"})
public class AppConfig {

    @Autowired
    private Environment environment;

    @Bean
    DriverManagerDataSource jdbcDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(environment.getProperty("jdbc.postgresql.driver"));
        dataSource.setUrl(environment.getProperty("jdbc.postgresql.url"));
        dataSource.setUsername(environment.getProperty("jdbc.postgresql.username"));
        dataSource.setPassword(environment.getProperty("jdbc.postgresql.password"));
        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        jdbcTemplate.setDataSource(jdbcDataSource());
        return jdbcTemplate;
    }

    @Bean
    public Account account(){
        return new Account(jdbcTemplate());
    }

    @Bean
    public Subscriber subscriber(){
        return new Subscriber(jdbcTemplate());
    }


}
