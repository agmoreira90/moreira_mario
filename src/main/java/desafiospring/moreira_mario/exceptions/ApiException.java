package desafiospring.moreira_mario.exceptions;

import org.springframework.http.HttpStatus;

public class ApiException extends Exception {
    private final String code;
    private final String description;
    private final Integer statusCode;

    public ApiException(HttpStatus httpStatus, String description) {
        super(description);
        this.code = httpStatus.name();
        this.description = description;
        this.statusCode = httpStatus.value();
    }

    public ApiException(String code, String description, Integer statusCode) {
        super(description);
        this.code = code;
        this.description = description;
        this.statusCode = statusCode;
    }

    public String getCode() {
        return this.code;
    }

    public String getDescription() {
        return this.description;
    }

    public Integer getStatusCode() {
        return this.statusCode;
    }
}
