package desafiospring.moreira_mario.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientDTO {
    private Long idClient;
    private String dni;
    private String name;
    private String surname;
    private String mail;
    private String phone;
    private String address;
    private String province;
    private List<PurchaseDTO> cart;
    private List<PurchaseDTO> orders;

}
