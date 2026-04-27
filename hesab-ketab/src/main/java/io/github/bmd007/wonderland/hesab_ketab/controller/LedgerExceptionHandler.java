package io.github.bmd007.wonderland.hesab_ketab.controller;

import io.github.bmd007.wonderland.hesab_ketab.domain.LedgerException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class LedgerExceptionHandler {

    @ExceptionHandler(LedgerException.AccountNotFound.class)
    public ProblemDetail handleAccountNotFound(LedgerException.AccountNotFound ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(LedgerException.ConcurrencyConflict.class)
    public ProblemDetail handleConcurrencyConflict(LedgerException.ConcurrencyConflict ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(LedgerException.InsufficientBalance.class)
    public ProblemDetail handleInsufficientBalance(LedgerException.InsufficientBalance ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage());
    }
}
