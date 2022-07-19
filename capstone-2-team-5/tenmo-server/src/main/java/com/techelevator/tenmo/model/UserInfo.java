package com.techelevator.tenmo.model;

import java.math.BigDecimal;
import java.util.Objects;

public class UserInfo {

    private String userName;
    private Long accountId;


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserInfo userInfo = (UserInfo) o;
        return userName == userInfo.userName &&
                accountId == userInfo.accountId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName, accountId);
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "Username=" + userName +
                ", Account Number='" + accountId + '}';
    }

}
