package com.epam.gymapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import com.epam.gymapp.util.TransactionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ControllerAdvice
public class ExceptionConfig {

    private static final Logger transactionLogger = LoggerFactory.getLogger("transactionLogger");
    private static final Logger operationLogger = LoggerFactory.getLogger("operationLogger");

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ExceptionDto> handleNotFoundException(NotFoundException ex) {
        ExceptionDto exceptionDto = new ExceptionDto(ex.getMessage());
        return new ResponseEntity<>(exceptionDto, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ExceptionDto> generalException(RuntimeException ex) {
        ExceptionDto exceptionDto = new ExceptionDto(ex.getMessage());
        return new ResponseEntity<>(exceptionDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TrainerWorkloadException.class)
    public ResponseEntity<String> handleTrainerWorkloadException(TrainerWorkloadException ex) {
        String transactionId = TransactionContext.getTransactionId();

        transactionLogger.error("[{}] Response: 500 Internal Server Error, Message: {}", transactionId, ex.getMessage(), ex);
        operationLogger.error("[{}] Error updating trainer workload: {}", transactionId, ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body("Error updating trainer workload: " + ex.getMessage());
    }

    @ExceptionHandler(TrainerSummaryNotFoundException.class)
    public ResponseEntity<String> handleTrainerSummaryNotFound(TrainerSummaryNotFoundException ex) {
        String transactionId = TransactionContext.getTransactionId();

        transactionLogger.warn("[{}] Response: 404 Not Found, Message: {}", transactionId, ex.getMessage());
        operationLogger.warn("[{}] {}", transactionId, ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}
