package desafiospring.moreira_mario.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseDTO {
    private Long id;
    private List<PurchaseArticleDTO> articles;
    private Double total;
    private Long idClient;

}
