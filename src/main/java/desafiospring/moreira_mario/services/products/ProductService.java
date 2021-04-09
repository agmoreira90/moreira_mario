package desafiospring.moreira_mario.services.products;

import desafiospring.moreira_mario.dtos.ProductDTO;
import desafiospring.moreira_mario.exceptions.ApiException;

import java.util.List;
import java.util.Map;

public interface ProductService {
    List<ProductDTO> getProducts(Map<String,String> params) throws ApiException;
    ProductDTO createProduct(ProductDTO product) throws ApiException;
}
