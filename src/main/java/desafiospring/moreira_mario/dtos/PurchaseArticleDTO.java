package desafiospring.moreira_mario.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseArticleDTO {
    private Long productId;
    private String name;
    private String brand;
    private Integer quantity;

}
