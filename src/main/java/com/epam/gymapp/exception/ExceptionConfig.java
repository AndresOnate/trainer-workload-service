package com.epam.gymapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.epam.gymapp.exception.common.InvalidActionTypeException;
import com.epam.gymapp.exception.trainer.MonthSummaryNotFoundException;
import com.epam.gymapp.exception.trainer.TrainerNotFoundException;
import com.epam.gymapp.exception.trainer.YearSummaryNotFoundException;
import com.epam.gymapp.util.TransactionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ControllerAdvice
public class ExceptionConfig {

    private static final Logger transactionLogger = LoggerFactory.getLogger("transactionLogger");
    private static final Logger operationLogger = LoggerFactory.getLogger("operationLogger");

    @ExceptionHandler({
        TrainerNotFoundException.class,
        YearSummaryNotFoundException.class,
        MonthSummaryNotFoundException.class
    })
    public ResponseEntity<ExceptionDto> handleNotFoundExceptions(RuntimeException ex) {
        String transactionId = TransactionContext.getTransactionId();

        transactionLogger.warn("[{}] Not Found: {}", transactionId, ex.getMessage());
        operationLogger.warn("[{}] {}", transactionId, ex.getMessage());

        ExceptionDto dto = new ExceptionDto(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(dto);
    }

    @ExceptionHandler(InvalidActionTypeException.class)
    public ResponseEntity<ExceptionDto> handleInvalidActionTypeException(InvalidActionTypeException ex) {
        String transactionId = TransactionContext.getTransactionId();

        transactionLogger.error("[{}] Bad Request: {}", transactionId, ex.getMessage());
        operationLogger.error("[{}] Invalid action type encountered: {}", transactionId, ex.getMessage());

        ExceptionDto dto = new ExceptionDto(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(dto);
    }


    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ExceptionDto> handleGenericRuntimeException(RuntimeException ex) {
        String transactionId = TransactionContext.getTransactionId();

        transactionLogger.error("[{}] Unexpected RuntimeException: {}", transactionId, ex.getMessage(), ex);
        ExceptionDto dto = new ExceptionDto("Unexpected error occurred: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(dto);
    }
}

