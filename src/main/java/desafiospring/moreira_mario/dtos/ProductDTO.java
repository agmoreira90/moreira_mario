package desafiospring.moreira_mario.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {

    private Long productId;
    private String name;
    private String category;
    private String brand;
    private Double price;
    private Integer quantity;
    private boolean freeShipping;
    private String prestige;

}
