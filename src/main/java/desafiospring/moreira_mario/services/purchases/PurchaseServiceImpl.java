package desafiospring.moreira_mario.services.purchases;

import desafiospring.moreira_mario.dtos.*;
import desafiospring.moreira_mario.exceptions.ApiException;
import desafiospring.moreira_mario.repositories.clients.ClientRepository;
import desafiospring.moreira_mario.repositories.products.ProductsRepository;
import desafiospring.moreira_mario.repositories.purchase.PurchaseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PurchaseServiceImpl implements PurchaseService {

    private final ProductsRepository productsRepository;
    private final PurchaseRepository purchaseRepository;
    private final ClientRepository clientRepository;

    public PurchaseServiceImpl(ProductsRepository productsRepository, PurchaseRepository purchaseRepository, ClientRepository clientRepository) {
        this.productsRepository = productsRepository;
        this.purchaseRepository = purchaseRepository;
        this.clientRepository = clientRepository;
    }
    //recibe un purchase dto lo valida segun la letra y lo envia apra ser impactado en el repositorio
    @Override
    public ResponseDTO purchase(PurchaseDTO purchase) throws ApiException {
        purchase.setId(0L);
        purchase.setTotal(0.0);
        ResponseDTO response = new ResponseDTO();
        Map<String, String> params = new HashMap<>();
        Map<Long, ProductDTO> products = this.productsRepository.selectProducts(params);
        for (PurchaseArticleDTO article : purchase.getArticles()) {
            if (products.get(article.getProductId()) != null) {
                if (products.get(article.getProductId()).getName().equals(article.getName()) && products.get(article.getProductId()).getBrand().equals(article.getBrand())) {
                    if (products.get(article.getProductId()).getQuantity() >= article.getQuantity()) {
                        purchase.setTotal(purchase.getTotal() + (products.get(article.getProductId()).getPrice() * article.getQuantity()));
                    } else {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Error: Producto: " + article.getProductId() + " " + article.getName() + " " + article.getBrand() + " no tiene stock suficiente, stock actual " + products.get(article.getProductId()).getQuantity() + ".");
                    }
                } else {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "Error: Producto: " + article.getProductId() + " " + article.getName() + " " + article.getBrand() + " no encontrado.");
                }
            } else {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Error: Producto: " + article.getProductId() + " " + article.getName() + " " + article.getBrand() + " no encontrado.");
            }
        }
        if (purchase.getArticles() != null && purchase.getTotal() >= 0) {
            purchase = this.purchaseRepository.createTicket(purchase);
            this.productsRepository.updateStock(purchase);
            response.setTicket(purchase);
            StatusCodeDTO statusCodeDTO = new StatusCodeDTO(HttpStatus.OK.value(), "La Solicitud de compra se completó con éxito");
            response.setStatusCode(statusCodeDTO);
        } else {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Error: no ha enviado artículos para crear ticket.");
        }
        return response;
    }
    //recibe un purchsedto valida que el cliente exista q no sea vacio y envia al repositorio a impactar en la planilla xlsx
    @Override
    public ResponseDTO addToCart(PurchaseDTO purchase) throws ApiException {
        purchase.setId(0L);
        purchase.setTotal(0.0);
        ResponseDTO response = new ResponseDTO();
        Map<String, String> params = new HashMap<>();
        Map<Long, ProductDTO> products = this.productsRepository.selectProducts(params);
        Map<Long, ClientDTO> clients = this.clientRepository.selectClients(params);
        for (PurchaseArticleDTO article : purchase.getArticles()) {
            if (products.get(article.getProductId()) != null) {
                if (clients.get(purchase.getIdClient()) != null) {
                    if (products.get(article.getProductId()).getName().equals(article.getName()) && products.get(article.getProductId()).getBrand().equals(article.getBrand())) {
                        purchase.setTotal(purchase.getTotal() + (products.get(article.getProductId()).getPrice() * article.getQuantity()));
                    } else {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Error: Producto: " + article.getProductId() + " " + article.getName() + " " + article.getBrand() + " no encontrado.");
                    }
                } else {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "Error: Cliente: " + purchase.getIdClient() + " no encontrado.");
                }
            } else {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Error: Producto: " + article.getProductId() + " " + article.getName() + " " + article.getBrand() + " no encontrado.");
            }
        }
        if (purchase.getArticles() != null && purchase.getTotal() >= 0) {
            purchase = this.purchaseRepository.addToCart(purchase);
            response.setTicket(purchase);
            StatusCodeDTO statusCodeDTO = new StatusCodeDTO(HttpStatus.OK.value(), "Se ha agregagado al carro con éxito");
            response.setStatusCode(statusCodeDTO);
        } else {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Error: no ha enviado artículos para crear ticket.");
        }
        return response;
    }
    // recibe un cliente id valida que exista y obtiene su carrito de compras
    // envia el carrito de cpmpras como una compra finalizada a impactar en el repositorio
    // limpia el carrito de compras
    @Override
    public ResponseDTO purchaseCart(Long clientId) throws ApiException {
        Map<String, String> filters = new HashMap<>();
        filters.put("idcliente", clientId.toString());
        Map<Long, ClientDTO> clients = this.clientRepository.selectClients(filters);
        if (clients.size() != 0) {
            if (clients.size() == 1) {
                Map<Long, PurchaseArticleDTO> articles = new HashMap<>();
                if (clients.get(clientId).getCart().size() > 0) {
                    for (int i = 0; i < clients.get(clientId).getCart().size(); i++) {
                        for (int j = 0; j < clients.get(clientId).getCart().get(i).getArticles().size(); j++) {
                            if (articles.get(clients.get(clientId).getCart().get(i).getArticles().get(j).getProductId()) == null) {
                                articles.put(clients.get(clientId).getCart().get(i).getArticles().get(j).getProductId(), clients.get(clientId).getCart().get(i).getArticles().get(j));
                            } else {
                                articles.get(clients.get(clientId).getCart().get(i).getArticles().get(j).getProductId()).setQuantity(articles.get(clients.get(clientId).getCart().get(i).getArticles().get(j).getProductId()).getQuantity() + clients.get(clientId).getCart().get(i).getArticles().get(j).getQuantity());
                            }
                        }
                    }
                    PurchaseDTO purchase = new PurchaseDTO();
                    List<PurchaseArticleDTO> purchaseArticles = new ArrayList<>();
                    for (Map.Entry<Long, PurchaseArticleDTO> article : articles.entrySet()) {
                        purchaseArticles.add(article.getValue());
                    }
                    purchase.setIdClient(clientId);
                    purchase.setArticles(purchaseArticles);
                    ResponseDTO response = this.purchase(purchase);
                    if (response.getTicket() != null && response.getTicket().getId() > 0) {
                        this.purchaseRepository.clearCart(clientId);
                    }
                    return this.purchase(purchase);
                } else {
                    ResponseDTO response = new ResponseDTO();
                    StatusCodeDTO status = new StatusCodeDTO();
                    status.setMessage("Informacion: Cliente: sin articulos para confirmar en el carrito.");
                    status.setCode(HttpStatus.OK.value());
                    response.setStatusCode(status);
                    return response;
                }
            } else {
                throw new ApiException(HttpStatus.NOT_FOUND, "Error: Id del Cliente: " + clientId.toString() + " es ambiguo.");
            }
        } else {
            throw new ApiException(HttpStatus.NOT_FOUND, "Error: No se encontro el Cliente: " + clientId.toString() + ".");
        }
    }
}
