package desafiospring.moreira_mario.repositories.products;

import desafiospring.moreira_mario.dtos.ProductDTO;
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
public class ProductsRepositoryImpl implements ProductsRepository {
    //recibo una mapa de filtros lee el archico excel configurda en el archivo properties luego le aplica los filtros por cada resgistro que tenga
    // el mapa params ademas valida que no se envien mas de 2 filtro y el orden
    @Override
    public Map<Long, ProductDTO> selectProducts(Map<String, String> params) throws ApiException {
        if (params.size() > 3 || (params.get("order") == null && params.size() == 3)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Error: Cantidad de filtros simultaneos excedida.");
        } else {
            return this.selectProduct(params);
        }
    }

    //recibo una mapa de filtros lee el archico excel configurda en el archivo properties luego le aplica los filtros por cada resgistro que tenga
    // el mapa params
    public Map<Long, ProductDTO> selectProduct(Map<String, String> params) throws ApiException {
        Map<Long, ProductDTO> products = new HashMap<>();
        Map<Long, ArrayList<String>> data = XLSXUtil.readXLSX("productsSheet");
        for (Map.Entry entry : data.entrySet()) {
            List<String> line = (ArrayList<String>) entry.getValue();
            ProductDTO product = new ProductDTO();
            product.setProductId(Long.parseLong(line.get(0).replace(".0", "")));
            product.setName(line.get(1));
            product.setCategory(line.get(2));
            product.setBrand(line.get(3));
            String price = line.get(4).replace("$", "");
            price = price.replace(".", "");
            product.setPrice(Double.parseDouble(price)/10);
            product.setQuantity(Integer.parseInt(line.get(5).replace(".0", "")));
            if (line.get(6).equals("SI")) {
                product.setFreeShipping(true);
            } else {
                product.setFreeShipping(false);
            }
            product.setPrestige(line.get(7));
            products.put(product.getProductId(), product);
        }
        for (Map.Entry<String, String> filter : params.entrySet()) {
            products = this.applyFilters(filter, products);
        }
        return products;
    }

    //recibe una orden de compra y actualiza el stock de los productos dentro de la orden
    @Override
    public void updateStock(PurchaseDTO purchaseDTO) throws ApiException {
        for (int j = 0; j < purchaseDTO.getArticles().size(); j++) {
            PurchaseArticleDTO purchaseArticle = purchaseDTO.getArticles().get(j);
            Map<String, String> filters = new HashMap<>();
            filters.put("productId", purchaseArticle.getProductId().toString());
            filters.put("name", purchaseArticle.getName());
            filters.put("brand", purchaseArticle.getBrand());
            Map<Long, ProductDTO> products;
            products = this.selectProduct(filters);

            if (products.size() == 1) {
                ProductDTO product = products.get(purchaseArticle.getProductId());
                product.setQuantity(product.getQuantity() - purchaseArticle.getQuantity());
                this.updtaeProduct(product);
            }
        }
    }

    private void updtaeProduct(ProductDTO product) throws ApiException {
        Map<String, String> filters = new HashMap<>();
        filters = new HashMap<>();
        filters.put("0", product.getProductId().toString());
        Map<Integer, String> prod = new HashMap<>();
        prod.put(1, product.getName());
        prod.put(2, product.getCategory());
        prod.put(3, product.getBrand());
        prod.put(4, product.getPrice().toString());
        prod.put(5, product.getQuantity().toString());
        if (product.isFreeShipping()) {
            prod.put(6, "SI");
        } else {
            prod.put(6, "NO");
        }
        prod.put(7, product.getPrestige());
        XLSXUtil.updateXLSX(prod, filters, "productsSheet");
    }

    //recibe un productodto, abre el excel que esta configurado en el archivo properties y escribe una nueva linea con los
    //datos que vienen en el productodto en la hoja correspondiente
    @Override
    public ProductDTO createProduct(ProductDTO product) throws ApiException {
        Map<String, String> filters = new HashMap<>();
        filters.put("name", product.getName());
        filters.put("category", product.getCategory());
        filters.put("brand", product.getBrand());
        Map<Long, ProductDTO> products;
        products = this.selectProduct(filters);
        if (products.size() == 0) {

            Map<Integer, String> prod = new HashMap<>();
            prod.put(1, product.getName());
            prod.put(2, product.getCategory());
            prod.put(3, product.getBrand());
            prod.put(4, product.getPrice().toString());
            prod.put(5, product.getQuantity().toString());
            if (product.isFreeShipping()) {
                prod.put(6, "SI");
            } else {
                prod.put(6, "NO");
            }
            prod.put(7, product.getPrestige());
            product.setProductId(XLSXUtil.writeXLSX(prod, "productsSheet"));

            return product;
        } else {
            throw new ApiException(HttpStatus.CONFLICT, "Error: el Producto: " + product.getName() + " " + product.getCategory() + " " + product.getBrand() + " ya existe.");
        }
    }

    // recibe un map.entry  filter con un filtro y un mapa de productos dto, filtro el mapa en funcion de filter
    private Map<Long, ProductDTO> applyFilters(Map.Entry<String, String> filter, Map<Long, ProductDTO> products) throws ApiException {
        try {
            switch (filter.getKey()) {
                case "productId":
                    products = products.entrySet().stream()
                            .filter(product -> product.getKey() == Long.parseLong(filter.getValue()))
                            .collect(Collectors.toMap(product -> product.getKey(), product -> product.getValue()));
                    break;
                case "name":
                    products = products.entrySet().stream()
                            .filter(product -> product.getValue().getName().equals(filter.getValue()))
                            .collect(Collectors.toMap(product -> product.getKey(), product -> product.getValue()));
                    break;
                case "category":
                    products = products.entrySet().stream()
                            .filter(product -> product.getValue().getCategory().equals(filter.getValue()))
                            .collect(Collectors.toMap(product -> product.getKey(), product -> product.getValue()));
                    break;
                case "brand":
                    products = products.entrySet().stream()
                            .filter(product -> product.getValue().getBrand().equals(filter.getValue()))
                            .collect(Collectors.toMap(product -> product.getKey(), product -> product.getValue()));
                    break;
                case "price":
                    products = products.entrySet().stream()
                            .filter(product -> product.getValue().getPrice() == Double.parseDouble(filter.getValue()))
                            .collect(Collectors.toMap(product -> product.getKey(), product -> product.getValue()));
                    break;
                case "quantity":
                    products = products.entrySet().stream()
                            .filter(product -> product.getValue().getQuantity() == Integer.parseInt(filter.getValue()))
                            .collect(Collectors.toMap(product -> product.getKey(), product -> product.getValue()));
                    break;
                case "freeShiping":
                    if (filter.getValue().equals("SI")) {
                        products = products.entrySet().stream()
                                .filter(product -> product.getValue().isFreeShipping() == true)
                                .collect(Collectors.toMap(product -> product.getKey(), product -> product.getValue()));
                    } else {
                        products = products.entrySet().stream()
                                .filter(product -> product.getValue().isFreeShipping() == false)
                                .collect(Collectors.toMap(product -> product.getKey(), product -> product.getValue()));
                    }
                    break;
                case "prestige":
                    String prestige = "";
                    for (int i = 0; i < Integer.parseInt(filter.getValue()); i++) {
                        prestige += '*';
                    }
                    products = products.entrySet().stream()
                            .filter(product -> product.getValue().getPrestige().equals(filter.getValue()))
                            .collect(Collectors.toMap(product -> product.getKey(), product -> product.getValue()));
                    break;
                case "order":
                    Integer order = Integer.parseInt(filter.getValue());
                    products = this.applyOrder(products, order);
                    break;
            }
        } catch (Exception e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Error: Filtros con valores invalidos.");
        }
        return products;
    }

    // recibe un order y un mapa de  productos dto
    // combierto el mapa e un array
    // utilizo un factory obtener la implementacion de ordenamiento correcta
    // ejecuto el ordenamiento y vuelvo a cargar el mapa
    private Map<Long, ProductDTO> applyOrder(Map<Long, ProductDTO> products, Integer order) throws ApiException {
        Sorter sorter = SorterFactory.getInstance("sorter");
        Integer size = products.size();
        ProductDTO[] prods = products.values().toArray(new ProductDTO[0]);

        switch (order) {
            case 0:
                sorter.sort(prods, new ComparatorNameAsc());
                break;
            case 1:
                sorter.sort(prods, new ComparatorNameDesc());
                break;
            case 2:
                sorter.sort(prods, new ComparatorHigherPrice());
                break;
            case 3:
                sorter.sort(prods, new ComparatorLowerPrice());
                break;
        }
        products = new HashMap<>();
        for (int i = 0; i < size; i++) {
            products.put((long) i, prods[i]);
        }
        return products;
    }

}
