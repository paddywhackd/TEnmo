package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.UserInfo;
import com.techelevator.tenmo.security.jwt.TokenProvider;
import io.swagger.annotations.ApiOperation;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@PreAuthorize("isAuthenticated()")
@RestController
public class AccountController {

    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private UserDao userDao;

    public AccountController(TokenProvider tokenProvider, AuthenticationManagerBuilder authenticationManagerBuilder, UserDao userDao) {
        this.tokenProvider = tokenProvider;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.userDao = userDao;
    }

    @ApiOperation("See a list of users")
    @RequestMapping(path = "/users", method = RequestMethod.GET)
    public List<UserInfo> userList() {
        try {
            return userDao.getAllUserInfo();
        } catch (DataIntegrityViolationException d) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation("Check your balance")
    @RequestMapping(path = "/balance", method = RequestMethod.GET)
    public BigDecimal getBalance(Principal principal) {
        ;
        try {
            return userDao.getBalance(principal.getName());
        } catch (DataIntegrityViolationException d) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
