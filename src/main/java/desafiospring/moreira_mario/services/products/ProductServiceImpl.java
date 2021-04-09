package desafiospring.moreira_mario.services.products;

import desafiospring.moreira_mario.dtos.ClientDTO;
import desafiospring.moreira_mario.dtos.ProductDTO;
import desafiospring.moreira_mario.repositories.products.ProductsRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import desafiospring.moreira_mario.exceptions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductsRepository productsRepository;

    public ProductServiceImpl(ProductsRepository productsRepository) {
        this.productsRepository = productsRepository;
    }
    //recibe un mapa de filtros carga todos los productos en un mapa y lo manda filtrar  y ordenar para devolverlo en forma de lista
    @Override
    public List<ProductDTO> getProducts(Map<String, String> params) throws ApiException {

        if (params.size() > 3 || (params.get("order") == null && params.size() == 3)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Error: Cantidad de filtros simultaneos excedida.");
        } else {
            List<ProductDTO> arrProducts = new ArrayList<>();
            Map<Long, ProductDTO> products = this.productsRepository.selectProducts(params);
            for (Map.Entry<Long, ProductDTO> filter : products.entrySet()) {
                arrProducts.add(filter.getValue());
            }
            return arrProducts;
        }
    }
    //recibe un productodto lo manda validar todo esta ok lo envia al repositorio a impactar en la palnilla excel
    @Override
    public ProductDTO createProduct(ProductDTO product) throws ApiException {
        this.productValidate(product);
        return this.productsRepository.createProduct(product);
    }
    //valida q todos los datos que vienen en el productodto sean validos
    private void productValidate(ProductDTO product) throws ApiException {

        if (product.getName() != null) {
            if (!product.getName().equals("")) {
            } else {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Error: Campo Name invalido.");
            }
        } else {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Error: Campo Name invalido.");
        }
        if (product.getCategory() != null) {
            if (!product.getCategory().equals("")) {
            } else {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Error: Campo Category invalido.");
            }
        } else {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Error: Campo Category invalido.");
        }
        if (product.getBrand() != null) {
            if (!product.getBrand().equals("")) {
            } else {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Error: Campo Brand invalido.");
            }
        } else {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Error: Campo Brand invalido.");
        }
        if (product.getPrice() != null) {
            if (product.getPrice() >= 0.0) {
            } else {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Error: Campo Price invalido.");
            }
        } else {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Error: Campo Price invalido.");
        }
        if (product.getQuantity() != null) {
            if (product.getQuantity() >= 0) {
            } else {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Error: Campo Quantity invalido.");
            }
        } else {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Error: Campo Quantity invalido.");
        }
        if (product.getPrestige() != null) {
            if (!product.getPrestige().equals("")) {

            } else {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Error: Campo Prestige invalido.");
            }
        } else {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Error: Campo Prestige invalido.");
        }
    }

}
