package desafiospring.moreira_mario.repositories.products;

import desafiospring.moreira_mario.dtos.ProductDTO;
import desafiospring.moreira_mario.dtos.PurchaseDTO;
import desafiospring.moreira_mario.exceptions.ApiException;

import java.util.Map;

public interface ProductsRepository {

    Map<Long,ProductDTO> selectProducts(Map<String, String> params) throws ApiException;
    Map<Long,ProductDTO> selectProduct(Map<String, String> params) throws ApiException;
    void updateStock(PurchaseDTO purchaseDTO) throws ApiException;
    ProductDTO createProduct(ProductDTO product)throws ApiException;
}
