package desafiospring.moreira_mario.repositories.clients;

import desafiospring.moreira_mario.dtos.ClientDTO;
import desafiospring.moreira_mario.dtos.ProductDTO;
import desafiospring.moreira_mario.exceptions.ApiException;

import java.util.Map;

public interface ClientRepository {

    ClientDTO createClient(ClientDTO client)throws ApiException;
    Map<Long,ClientDTO> selectClients(Map<String, String> params) throws ApiException;
}
