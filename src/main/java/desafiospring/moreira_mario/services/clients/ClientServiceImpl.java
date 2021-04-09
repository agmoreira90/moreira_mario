package desafiospring.moreira_mario.services.clients;

import desafiospring.moreira_mario.dtos.ClientDTO;
import desafiospring.moreira_mario.exceptions.ApiException;
import desafiospring.moreira_mario.repositories.clients.ClientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ClientServiceImpl implements ClientService {
    private final ClientRepository clientRepository;

    public ClientServiceImpl(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    //recibo un mapa de parametros se los paso al repositorio de cliente para obtener loc clientes que coincidan
    @Override
    public List<ClientDTO> getClients(Map<String, String> params) throws ApiException {
        List<ClientDTO> arrClients = new ArrayList<>();
        Map<Long, ClientDTO> clients = this.clientRepository.selectClients(params);
        for (Map.Entry<Long, ClientDTO> filter : clients.entrySet()) {
            arrClients.add(filter.getValue());
        }
        return arrClients;
    }

    //recibo un clientedto lo valiod y lo impacto en la planila xlsx
    @Override
    public ClientDTO createClient(ClientDTO client) throws ApiException {
        this.clientValidate(client);
        return this.clientRepository.createClient(client);
    }

    //valido que todos los campos tengan datos validos y sino devulevo una exception
    private void clientValidate(ClientDTO client) throws ApiException {

        if (client.getDni() != null) {
            if (!client.getDni().equals("")) {
            } else {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Error: Campo Dni invalido.");
            }
        } else {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Error: Campo Dni invalido.");
        }
        if (client.getName() != null) {
            if (!client.getName().equals("")) {
            } else {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Error: Campo Name invalido.");
            }
        } else {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Error: Campo Name invalido.");
        }
        if (client.getSurname() != null) {
            if (!client.getSurname().equals("")) {
            } else {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Error: Campo Surname invalido.");
            }
        } else {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Error: Campo Surname invalido.");
        }
        if (client.getMail() != null) {
            if (!client.getMail().equals("")) {
            } else {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Error: Campo Mail invalido.");
            }
        } else {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Error: Campo Mail invalido.");
        }
        if (client.getPhone() != null) {
            if (!client.getPhone().equals("")) {
            } else {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Error: Campo Phone invalido.");
            }
        } else {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Error: Campo Phone invalido.");
        }
        if (client.getAddress() != null) {
            if (!client.getAddress().equals("")) {
            } else {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Error: Campo Addrees invalido.");
            }
        } else {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Error: Campo Addrees invalido.");
        }
        if (client.getProvince() != null) {
            if (!client.getProvince().equals("")) {

            } else {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Error: Campo Province invalido.");
            }
        } else {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Error: Campo Province invalido.");
        }
    }

}
