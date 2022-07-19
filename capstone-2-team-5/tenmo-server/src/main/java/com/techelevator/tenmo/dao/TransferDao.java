package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.exception.TransactionNotFoundException;
import com.techelevator.tenmo.model.Request;
import com.techelevator.tenmo.model.Respond;
import com.techelevator.tenmo.model.Transfer;

import java.math.BigDecimal;
import java.util.List;

public interface TransferDao {

    public String updateTransfer (Transfer transfer, int statusId);

    public boolean acceptRequest(Respond respond, String toUsername);

    public boolean rejectRequest(Respond respond);

    public boolean transferRequest (Request request, String toUsername);

    public boolean sendTransfer (String fromUsername, String toUsername, String message, BigDecimal transferAmount);

    public List<Transfer> pendingRequests (String username);

    public List<Transfer> allTransfers (String username);

    public Transfer getTransferById (int transferId) throws TransactionNotFoundException;

    public boolean checkValidResponder(String username, int transferId);
}
