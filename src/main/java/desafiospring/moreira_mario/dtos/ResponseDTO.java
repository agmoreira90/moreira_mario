package desafiospring.moreira_mario.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseDTO {
    private PurchaseDTO ticket;
    private StatusCodeDTO statusCode;

}
