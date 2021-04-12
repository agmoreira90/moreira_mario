package desafiospring.moreira_mario.repositories.clients;

import desafiospring.moreira_mario.dtos.ClientDTO;
import desafiospring.moreira_mario.dtos.PurchaseArticleDTO;
import desafiospring.moreira_mario.dtos.PurchaseDTO;
import desafiospring.moreira_mario.exceptions.ApiException;
import desafiospring.moreira_mario.repositories.XLSXUtil;
import desafiospring.moreira_mario.services.comparators.ComparatorHigherPrice;
import desafiospring.moreira_mario.services.comparators.ComparatorLowerPrice;
import desafiospring.moreira_mario.services.comparators.ComparatorNameAsc;
import desafiospring.moreira_mario.services.comparators.ComparatorNameDesc;
import desafiospring.moreira_mario.services.sorters.Sorter;
import desafiospring.moreira_mario.services.sorters.SorterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class ClientRepositoryImpl implements ClientRepository {
    //recibe un clientedto, abre el excel que esta configurado en el archivo properties y escribe una nueva linea con los
    //datos que vienen en el clientedto en la hoja correspondiente
    @Override
    public ClientDTO createClient(ClientDTO client) throws ApiException {
        Map<String, String> filters = new HashMap<>();
        filters.put("dni", client.getDni());
        filters.put("mail", client.getMail());
        Map<Long, ClientDTO> clients;
        clients = this.selectClients(filters);
        if (clients.size() == 0) {

            Map<Integer, String> cli = new HashMap<>();
            cli.put(1, client.getDni());
            cli.put(2, client.getName());
            cli.put(3, client.getSurname());
            cli.put(4, client.getMail());
            cli.put(5, client.getPhone());
            cli.put(6, client.getAddress());
            cli.put(7, client.getProvince());
            client.setIdClient(XLSXUtil.writeXLSX(cli, "clientsSheet"));

            return client;
        } else {
            throw new ApiException(HttpStatus.CONFLICT, "Error: el Cliente: " + client.getDni() + " " + client.getMail() + " ya existe.");
        }
    }

    //recibo una mapa de filtros lee el archico excel configurda en el archivo properties luego le aplica los filtros por cada resgistro que tenga
    // el mapa params
    @Override
    public Map<Long, ClientDTO> selectClients(Map<String, String> params) throws ApiException {
        Map<Long, ClientDTO> clients = new HashMap<>();
        Map<Long, ArrayList<String>> data = XLSXUtil.readXLSX("clientsSheet");
        for (Map.Entry entry : data.entrySet()) {
            List<String> line = (ArrayList<String>) entry.getValue();
            ClientDTO client = new ClientDTO();
            client.setIdClient(Long.parseLong(line.get(0).replace(".0", "")));
            client.setDni(line.get(1));
            client.setName(line.get(2));
            client.setSurname(line.get(3));
            client.setMail(line.get(4));
            client.setPhone(line.get(5));
            client.setAddress(line.get(6));
            client.setProvince(line.get(7));
            client.setCart(this.getPurchase(client.getIdClient(), "cartsSheet", "cartsArticlesSheet"));
            client.setOrders(this.getPurchase(client.getIdClient(), "purchaseSheet", "purchaseArticlesSheet"));
            clients.put(client.getIdClient(), client);
        }
        for (Map.Entry<String, String> filter : params.entrySet()) {
            clients = this.applyFilters(filter, clients);
        }
        return clients;
    }

    // recibe un map.entry  filter con un filtro y un mapa de cliente dto, filtro el mapa en funcion de filter
    private Map<Long, ClientDTO> applyFilters(Map.Entry<String, String> filter, Map<Long, ClientDTO> clients) throws ApiException {
        try {
            switch (filter.getKey()) {
                case "idcliente":
                    clients = clients.entrySet().stream()
                            .filter(client -> client.getKey() == Long.parseLong(filter.getValue()))
                            .collect(Collectors.toMap(client -> client.getKey(), client -> client.getValue()));
                    break;
                case "dni":
                    clients = clients.entrySet().stream()
                            .filter(client -> client.getValue().getDni().equals(filter.getValue()))
                            .collect(Collectors.toMap(client -> client.getKey(), client -> client.getValue()));
                    break;
                case "name":
                    clients = clients.entrySet().stream()
                            .filter(client -> client.getValue().getName().equals(filter.getValue()))
                            .collect(Collectors.toMap(client -> client.getKey(), client -> client.getValue()));
                    break;
                case "surname":
                    clients = clients.entrySet().stream()
                            .filter(client -> client.getValue().getSurname().equals(filter.getValue()))
                            .collect(Collectors.toMap(client -> client.getKey(), client -> client.getValue()));
                    break;
                case "mail":
                    clients = clients.entrySet().stream()
                            .filter(client -> client.getValue().getMail().equals(filter.getValue()))
                            .collect(Collectors.toMap(client -> client.getKey(), client -> client.getValue()));
                    break;
                case "phone":
                    clients = clients.entrySet().stream()
                            .filter(client -> client.getValue().getPhone().equals(filter.getValue()))
                            .collect(Collectors.toMap(client -> client.getKey(), client -> client.getValue()));
                    break;
                case "address":
                    clients = clients.entrySet().stream()
                            .filter(client -> client.getValue().getAddress().equals(filter.getValue()))
                            .collect(Collectors.toMap(client -> client.getKey(), client -> client.getValue()));
                    break;
                case "province":
                    clients = clients.entrySet().stream()
                            .filter(client -> client.getValue().getProvince().equals(filter.getValue()))
                            .collect(Collectors.toMap(client -> client.getKey(), client -> client.getValue()));
                    break;
                case "order":
                    Integer order = Integer.parseInt(filter.getValue());
                    clients = this.applyOrder(clients, order);
                    break;
            }
        } catch (Exception e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Error: Filtros con valores invalidos.");
        }
        return clients;
    }

    // recibe un order y un mapa de  cliente dto
    // combierto el mapa e un array
    // utilizo un factory obtener la implementacion de ordenamiento correcta
    // ejecuto el ordenamiento y vuelvo a cargar el mapa
    private Map<Long, ClientDTO> applyOrder(Map<Long, ClientDTO> clients, Integer order) throws ApiException {
        Sorter sorter = SorterFactory.getInstance("sorter");
        Integer size = clients.size();
        ClientDTO[] clis = clients.values().toArray(new ClientDTO[0]);

        switch (order) {
            case 0:
                sorter.sort(clis, new ComparatorNameAsc());
                break;
            case 1:
                sorter.sort(clis, new ComparatorNameDesc());
                break;
            case 2:
                sorter.sort(clis, new ComparatorHigherPrice());
                break;
            case 3:
                sorter.sort(clis, new ComparatorLowerPrice());
                break;
        }
        clients = new HashMap<>();
        for (int i = 0; i < size; i++) {
            clients.put((long) i, clis[i]);
        }
        return clients;
    }

    // leo el archivo xlsx utilizado como base de datos y devlevo el carrito de compras que tiene pendiente el cliente
    private List<PurchaseDTO> getPurchase(Long clientId, String sheetName, String sheetName2) throws ApiException {
        List<PurchaseDTO> purchases = new ArrayList();
        Map<Long, ArrayList<String>> data = XLSXUtil.readXLSX(sheetName);
        for (Map.Entry entry : data.entrySet()) {
            List<String> line = (ArrayList<String>) entry.getValue();
            Long id = Long.parseLong(line.get(2).replace(".0", ""));
            if (id == clientId) {
                PurchaseDTO purchase = new PurchaseDTO();
                purchase.setId(Long.parseLong(line.get(0).replace(".0", "")));
                purchase.setTotal(Double.parseDouble(line.get(1)));
                purchase.setIdClient(id);
                purchase.setArticles(getPurchaseArticles(purchase.getId(), sheetName2));
                purchases.add(purchase);
            }
        }
        return purchases;
    }

    // leo el archivo xlsx utilizado como base de datos y devlevo los articulos del carrito de compra del cliente
    private List<PurchaseArticleDTO> getPurchaseArticles(Long idPurchase, String sheetName) throws ApiException {
        List<PurchaseArticleDTO> purchasesArticles = new ArrayList();
        Map<Long, ArrayList<String>> data = XLSXUtil.readXLSX(sheetName);
        data = data.entrySet().stream()
                .filter(article -> Long.parseLong(article.getValue().get(0).replace(".0", "")) == idPurchase)
                .collect(Collectors.toMap(article -> article.getKey(), article -> article.getValue()));

        for (Map.Entry entry : data.entrySet()) {
            List<String> line = (ArrayList<String>) entry.getValue();

            PurchaseArticleDTO purchaseArticle = new PurchaseArticleDTO();
            purchaseArticle.setProductId(Long.parseLong(line.get(1).replace(".0", "")));
            purchaseArticle.setName(line.get(2));
            purchaseArticle.setBrand(line.get(3));
            purchaseArticle.setQuantity(Integer.parseInt(line.get(4).replace(".0", "")));
            purchasesArticles.add(purchaseArticle);
        }
        return purchasesArticles;
    }

}
