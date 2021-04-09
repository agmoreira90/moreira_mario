package desafiospring.moreira_mario.controllers;

import desafiospring.moreira_mario.dtos.ClientDTO;
import desafiospring.moreira_mario.dtos.ProductDTO;
import desafiospring.moreira_mario.dtos.PurchaseDTO;
import desafiospring.moreira_mario.dtos.ResponseDTO;
import desafiospring.moreira_mario.exceptions.ApiError;
import desafiospring.moreira_mario.exceptions.ApiException;
import desafiospring.moreira_mario.services.clients.ClientService;
import desafiospring.moreira_mario.services.products.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class ClientController {

    @Autowired
    private ClientService clientService;


    @GetMapping("/clients")
    public List<ClientDTO> getClients(@RequestParam Map<String,String> params) throws ApiException {
        return clientService.getClients(params);
    }


    @PostMapping("/create-client")
    public ClientDTO createProduct(@RequestBody ClientDTO client) throws ApiException {
        return this.clientService.createClient(client);
    }

    /* JSON create-client
    * {
        "dni": "49229184",
        "name":"Ignacio",
        "surname": "Berro",
        "mail": "ignacio.berro@mercadolibre.com",
        "phone": "59899564549",
        "address":"Vazquez 1369 / Apto 101",
        "province":"Montevideo"
    }
    * */

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> clientJSONFormat(HttpMessageNotReadableException e) {
        Integer statusCode = HttpStatus.BAD_REQUEST.value();
        String message = "Error: Formato de JSON invalido.";
        String status = HttpStatus.BAD_REQUEST.name();
        ApiError apiError = new ApiError(status, message, statusCode);
        return ResponseEntity.status(apiError.getStatus()).body(apiError);
    }
}
