package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.User;
import com.techelevator.tenmo.model.UserInfo;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcUserDao implements UserDao {

    private JdbcTemplate jdbcTemplate;

    public JdbcUserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int findIdByUsername(String username) {
        String sql = "SELECT user_id FROM tenmo_user WHERE username ILIKE ?;";
        Integer id = jdbcTemplate.queryForObject(sql, Integer.class, username);
        if (id != null) {
            return id;
        } else {
            return -1;
        }
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, username, password_hash FROM tenmo_user;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql);
        while(results.next()) {
            User user = mapRowToUser(results);
            users.add(user);
        }
        return users;
    }

    public List<UserInfo> getAllUserInfo() {
        List<UserInfo> userInfoList = new ArrayList<>();
        String sql = "SELECT username, account_id FROM tenmo_user JOIN account as a ON a.user_id = tenmo_user.user_id;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql);
        while(results.next()) {
            UserInfo userInfo = mapRowToUserInfo(results);
            userInfoList.add(userInfo);
        }
        return userInfoList;
    }

    @Override
    public User findByUsername(String username) throws UsernameNotFoundException {
        String sql = "SELECT user_id, username, password_hash FROM tenmo_user WHERE username ILIKE ?;";
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, username);
        if (rowSet.next()){
            return mapRowToUser(rowSet);
        }
        throw new UsernameNotFoundException("User " + username + " was not found.");
    }

    @Override
    public boolean create(String username, String password) {

        // create user
        String sql = "INSERT INTO tenmo_user (username, password_hash) VALUES (?, ?) RETURNING user_id";
        String password_hash = new BCryptPasswordEncoder().encode(password);
        Integer newUserId;
        try {
            newUserId = jdbcTemplate.queryForObject(sql, Integer.class, username, password_hash);
        } catch (DataAccessException e) {
            return false;
        }

        // create account
        String accountSql = "INSERT INTO account (user_id, balance) values (?, 1000);";
        try{
            jdbcTemplate.update(accountSql, newUserId);
        }catch (DataAccessException e){
            return false;
        }

        return true;
    }

    @Override
    public BigDecimal getBalance(String username) {
        String sql = "SELECT balance FROM tenmo_user JOIN account AS a ON a.user_id = tenmo_user.user_id WHERE username = ?";
        BigDecimal balance;
        try{
            balance = jdbcTemplate.queryForObject(sql, BigDecimal.class, username);
        }catch (DataAccessException e){
            balance = null;
        }
        return balance;
    }

    private User mapRowToUser(SqlRowSet rs) {
        User user = new User();
        user.setId(rs.getLong("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password_hash"));
        user.setActivated(true);
        user.setAuthorities("USER");
        return user;
    }

    private UserInfo mapRowToUserInfo(SqlRowSet rs) {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserName(rs.getString("username"));
        userInfo.setAccountId(rs.getLong("account_id"));
        return userInfo;
    }

}
