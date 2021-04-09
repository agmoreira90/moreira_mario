package desafiospring.moreira_mario.services.clients;

import desafiospring.moreira_mario.dtos.ClientDTO;
import desafiospring.moreira_mario.dtos.ProductDTO;
import desafiospring.moreira_mario.exceptions.ApiException;

import java.util.List;
import java.util.Map;

public interface ClientService {
    List<ClientDTO> getClients(Map<String,String> params) throws ApiException;
    ClientDTO createClient(ClientDTO client) throws ApiException;
}
