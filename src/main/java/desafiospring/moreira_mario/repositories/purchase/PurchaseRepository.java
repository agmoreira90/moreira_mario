package desafiospring.moreira_mario.repositories.purchase;

import desafiospring.moreira_mario.dtos.PurchaseArticleDTO;
import desafiospring.moreira_mario.dtos.PurchaseDTO;
import desafiospring.moreira_mario.exceptions.ApiException;

public interface PurchaseRepository {

    PurchaseDTO createTicket(PurchaseDTO purchase) throws ApiException;
    PurchaseDTO addToCart(PurchaseDTO purchase) throws ApiException;
    void clearCart(Long clientId) throws ApiException;
    void clearArticles(Long idPurchase) throws ApiException;
}
