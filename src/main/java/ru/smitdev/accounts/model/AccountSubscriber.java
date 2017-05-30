package ru.smitdev.accounts.model;

public class AccountSubscriber {

    private String id;
    private String account;
    private String subscriber;

    public AccountSubscriber() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getSubscriber() {
        return subscriber;
    }

    public void setSubscriber(String subscriber) {
        this.subscriber = subscriber;
    }
}
