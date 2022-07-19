package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.exception.TransactionNotFoundException;
import com.techelevator.tenmo.exception.UsernameNotFoundException;
import com.techelevator.tenmo.model.Request;
import com.techelevator.tenmo.model.Respond;
import com.techelevator.tenmo.model.Transfer;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.SQLWarningException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcTransferDao implements TransferDao {

    private JdbcTemplate jdbcTemplate;

    public JdbcTransferDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String updateTransfer(Transfer transfer, int statusId) {
        return null;
    }

    @Override
    public boolean acceptRequest(Respond respond, String fromUsername) {
        String sql = "UPDATE account SET balance = account.balance - (SELECT transfer_amount FROM transfer WHERE transfer_id = ?) FROM tenmo_user WHERE tenmo_user.user_id = account.user_id AND username = ?;" +
                " UPDATE account SET balance = account.balance + (SELECT transfer_amount FROM transfer WHERE transfer_id = ?) FROM tenmo_user WHERE tenmo_user.user_id = account.user_id AND username = (SELECT tousername FROM transfer WHERE transfer_id = ?);" +
                " UPDATE transfer SET status = 'APPROVED', timestamp = now() WHERE transfer_id = ?;";
        try {
            if(respond.getAccept()) {
                jdbcTemplate.update(sql, respond.getTransferId(), fromUsername, respond.getTransferId(), respond.getTransferId(), respond.getTransferId());
                return true;
            }else throw new IllegalArgumentException();
        } catch (DataAccessException | IllegalArgumentException e) {
            System.out.println(e);
            return false;
        }
    }

    @Override
    public boolean rejectRequest(Respond respond){
        String sql = " UPDATE transfer SET status = 'REJECTED', timestamp = now() WHERE transfer_id = ?";
        try{
            jdbcTemplate.update(sql, respond.getTransferId());
        }catch (DataAccessException e){
            System.out.println(e);
            return false;
        }
        return false; }

    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public boolean transferRequest(Request request, String toUsername) {
        String sql = "INSERT INTO transfer (toUsername, fromUsername, message, status, transfer_amount) VALUES (?, ?, ?, 'PENDING', ?);";
        try{
            jdbcTemplate.update(sql, toUsername, request.getFromUsername(), request.getMessage(), request.getTransferAmount());
            return true;
        }catch (DataAccessException e){
            return false;
        }
    }

    @Override
    public boolean sendTransfer(String fromUsername, String toUsername, String message, BigDecimal transferAmount) {
        if (checkBalance(fromUsername, transferAmount)) {
            String sql = "UPDATE account set balance = account.balance - ? FROM tenmo_user where tenmo_user.user_id = account.user_id AND username = ?;" +
                    " UPDATE account set balance = account.balance + ? FROM tenmo_user where tenmo_user.user_id = account.user_id AND username = ?;" +
                    " INSERT INTO transfer (toUsername, fromUsername, message, status, transfer_amount) VALUES (?, ?, ?, 'APPROVED', ?);";

            jdbcTemplate.update(sql, transferAmount, fromUsername, transferAmount, toUsername, toUsername, fromUsername, message, transferAmount);
            return true;
        }
        return false;
    }

    public boolean checkBalance(String fromUsername, BigDecimal transferAmount) {
        String sql = "SELECT balance FROM account JOIN tenmo_user as tu ON tu.user_id = account.user_id where username = ?";
        BigDecimal balance;
        try {
            balance = jdbcTemplate.queryForObject(sql, BigDecimal.class, fromUsername);
        } catch (DataAccessException e) {
            balance = null;
        }
        return balance.compareTo(transferAmount) != -1;
    }

    @Override
    public List<Transfer> pendingRequests(String username) {
        try {
            String sql = "select transfer_id, tousername, fromusername, message, status, transfer_amount, timestamp from transfer WHERE (tousername = ? OR fromusername = ?) AND status = 'PENDING' ORDER BY fromusername = ? DESC, timestamp;";
            SqlRowSet result = jdbcTemplate.queryForRowSet(sql, username, username, username);
            List<Transfer> transfers = new ArrayList<>();
            while (result.next()) {
                Transfer transfer = mapRowSetToTransfer(result);
                transfers.add(transfer);
            }
            return transfers;
        } catch (DataAccessException e) {
            System.out.println(e);
        }
        return null;
    }

    @Override
    public List<Transfer> allTransfers(String username) {
        try {
            String sql = "select transfer_id, tousername, fromusername, message, status, transfer_amount, timestamp from transfer WHERE tousername = ? OR fromusername = ?;";
            SqlRowSet result = jdbcTemplate.queryForRowSet(sql, username, username);
            List<Transfer> transfers = new ArrayList<>();
            while (result.next()) {
                Transfer transfer = mapRowSetToTransfer(result);
                transfers.add(transfer);
            }
            return transfers;
        } catch (DataAccessException e) {
            System.out.println(e);
        }
        return null;
    }

    @Override
    public Transfer getTransferById(int transferId) throws TransactionNotFoundException {
        String sql = "SELECT transfer_id, tousername, fromusername, status, transfer_amount, timestamp FROM transfer WHERE transfer_id = ?;";
        SqlRowSet result = jdbcTemplate.queryForRowSet(sql, transferId);
        if (result.next()) {
            return mapRowSetToTransfer(result);
        } else {
            throw new TransactionNotFoundException();
        }
    }

    @Override
    public boolean checkValidResponder(String fromUsername, int transferId) {
        String sql = "SELECT count(*) FROM transfer JOIN tenmo_user as tu ON tu.username = transfer.fromusername WHERE fromusername = ? and transfer_id = ? AND status='PENDING';";
        try{
            SqlRowSet result = jdbcTemplate.queryForRowSet(sql, fromUsername, transferId);
            if(result.next()){
                if(result.getInt("count") == 1){
                    return true;
                }
            }
        }catch (DataAccessException e){
            System.out.println(e);
        }
        return false;
    }

    public Transfer mapRowSetToTransfer(SqlRowSet result) {
        Transfer transfer = new Transfer();
        transfer.setTransferId(result.getInt("transfer_id"));
        transfer.setToUsername(result.getString("tousername"));
        transfer.setFromUsername(result.getString("fromusername"));
        transfer.setStatus(result.getString("status"));
        transfer.setTransferAmount(result.getBigDecimal("transfer_amount"));
        transfer.setTimestamp(result.getString("timestamp"));
        transfer.setMessage(result.getString("message"));
        return transfer;
    }
}
