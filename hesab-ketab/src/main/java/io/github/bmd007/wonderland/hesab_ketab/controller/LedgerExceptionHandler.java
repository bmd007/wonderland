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

    @ExceptionHandler(LedgerException.InsufficientBalance.class)
    public ProblemDetail handleInsufficientBalance(LedgerException.InsufficientBalance ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(LedgerException.IncompatibleCurrencies.class)
    public ProblemDetail handleIncompatibleCurrencies(LedgerException.IncompatibleCurrencies ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }
}
