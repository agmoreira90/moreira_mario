package desafiospring.moreira_mario.services.purchases;

import desafiospring.moreira_mario.dtos.PurchaseArticleDTO;
import desafiospring.moreira_mario.dtos.PurchaseDTO;
import desafiospring.moreira_mario.dtos.ResponseDTO;
import desafiospring.moreira_mario.exceptions.ApiException;

import java.util.List;

public interface PurchaseService {
    ResponseDTO purchase(PurchaseDTO purchase)throws ApiException;
    ResponseDTO addToCart(PurchaseDTO purchase)throws ApiException;
    ResponseDTO purchaseCart(Long clientId)throws ApiException;
}
