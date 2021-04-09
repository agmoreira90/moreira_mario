package desafiospring.moreira_mario.config;

import desafiospring.moreira_mario.exceptions.ApiError;
import desafiospring.moreira_mario.exceptions.ApiException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalHandlerException {
    public GlobalHandlerException() {
    }

    @ExceptionHandler({ApiException.class})
    protected ResponseEntity<ApiError> handleApiException(ApiException e) {
        Integer statusCode = e.getStatusCode();
        String message = e.getDescription();
        String status = e.getCode();
        ApiError apiError = new ApiError(status, message, statusCode);
        return ResponseEntity.status(apiError.getStatus()).body(apiError);
    }
}
