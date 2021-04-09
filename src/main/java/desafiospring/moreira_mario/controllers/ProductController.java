package desafiospring.moreira_mario.controllers;

import desafiospring.moreira_mario.dtos.ProductDTO;
import desafiospring.moreira_mario.dtos.PurchaseDTO;
import desafiospring.moreira_mario.dtos.ResponseDTO;
import desafiospring.moreira_mario.exceptions.ApiError;
import desafiospring.moreira_mario.exceptions.ApiException;
import desafiospring.moreira_mario.services.purchases.PurchaseService;
import desafiospring.moreira_mario.services.products.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private PurchaseService purchaseService;

    @GetMapping("/articles")
    public List<ProductDTO> getProducts(@RequestParam Map<String, String> params) throws ApiException {
        return productService.getProducts(params);
    }

    @PostMapping("/purchase-request")
    public ResponseDTO createLink(@RequestBody PurchaseDTO purchase) throws ApiException {
        return this.purchaseService.purchase(purchase);
    }

    /* JSON add-cart
    {
    "idClient":4,
    "articles":[
        {
            "productId":1,
            "name":"Desmalezadora",
            "brand":"Makita",
            "quantity":1
        },
        {
            "productId":5,
            "name":"Zapatillas Deportivas",
            "brand":"Nike",
            "quantity":1
        }
    ]
}*/
    @PostMapping("/add-cart")
    public ResponseDTO addToCart(@RequestBody PurchaseDTO purchase) throws ApiException {
        return this.purchaseService.addToCart(purchase);
    }

    // /api/v1/purchase-cart/4 id cliente 4 ya existe
    @GetMapping("/purchase-cart/{clientId}")
    public ResponseDTO purchaseCart(@PathVariable Long clientId) throws ApiException {
        return purchaseService.purchaseCart(clientId);
    }

    /* JSON create-product
    * {
        "name": "Desmalezadora",
        "category": "Herragh7gi5entas",
        "brand": "Makita",
        "price": 9600.0,
        "quantity": 560,
        "freeShipping":true,
        "prestige": "****"
    }*/
    @PostMapping("/create-product")
    public ProductDTO createProduct(@RequestBody ProductDTO product) throws ApiException {
        return this.productService.createProduct(product);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> productJSONFormat(HttpMessageNotReadableException e) {
        Integer statusCode = HttpStatus.BAD_REQUEST.value();
        String message = "Error: Formato de JSON invalido.";
        String status = HttpStatus.BAD_REQUEST.name();
        ApiError apiError = new ApiError(status, message, statusCode);
        return ResponseEntity.status(apiError.getStatus()).body(apiError);
    }
}
