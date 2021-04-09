package desafiospring.moreira_mario.repositories.products;

import desafiospring.moreira_mario.dtos.ProductDTO;
import desafiospring.moreira_mario.dtos.PurchaseDTO;
import desafiospring.moreira_mario.exceptions.ApiException;
import desafiospring.moreira_mario.repositories.products.ProductsRepository;
import desafiospring.moreira_mario.services.comparators.ComparatorHigherPrice;
import desafiospring.moreira_mario.services.comparators.ComparatorLowerPrice;
import desafiospring.moreira_mario.services.comparators.ComparatorNameAsc;
import desafiospring.moreira_mario.services.comparators.ComparatorNameDesc;
import desafiospring.moreira_mario.services.sorters.Sorter;
import desafiospring.moreira_mario.services.sorters.SorterFactory;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.util.*;


import java.io.FileInputStream;
import java.util.stream.Collectors;

@Repository
public class ProductsRepositoryImpl implements ProductsRepository {
    //recibo una mapa de filtros lee el archico excel configurda en el archivo properties luego le aplica los filtros por cada resgistro que tenga
    // el mapa params ademas valida que no se envien mas de 2 filtro y el orden
    @Override
    public Map<Long, ProductDTO> selectProducts(Map<String, String> params) throws ApiException {

        Map<Long, ProductDTO> productsMap = this.reedArchive();
        if (params.size() > 3 || (params.get("order") == null && params.size() == 3)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Error: Cantidad de filtros simultaneos excedida.");
        } else {
            for (Map.Entry<String, String> filter : params.entrySet()) {
                productsMap = this.applyFilters(filter, productsMap);
            }
        }

        return productsMap;
    }
    //recibo una mapa de filtros lee el archico excel configurda en el archivo properties luego le aplica los filtros por cada resgistro que tenga
    // el mapa params
    @Override
    public Map<Long, ProductDTO> selectProduct(Map<String, String> params) throws ApiException {
        Map<Long, ProductDTO> productsMap = this.reedArchive();
        for (Map.Entry<String, String> filter : params.entrySet()) {
            productsMap = this.applyFilters(filter, productsMap);
        }
        return productsMap;
    }
    //recibe una orden de compra y actualiza el stock de los productos dentro de la orden
    @Override
    public void updateStock(PurchaseDTO purchaseDTO) throws ApiException {
        String fileName = "";
        String filePath = "";
        Integer sheetNum = 0;
        Properties properties = new Properties();
        XSSFWorkbook book = null;
        FileInputStream file = null;
        try {
            File newFile = new File("");
            properties.load(new FileInputStream(ResourceUtils.getFile("classpath:Products.properties")));
            fileName = properties.get("file").toString();
            filePath = newFile.getAbsolutePath()+properties.get("filePath").toString() + fileName;
            sheetNum = Integer.parseInt(properties.get("productsSheet").toString());
            file = new FileInputStream(ResourceUtils.getFile(filePath));
            book = new XSSFWorkbook(file);

            XSSFSheet sheet = book.getSheetAt(sheetNum);
            Iterator<Row> rowIterator = sheet.rowIterator();
            Row row;
            for (int j = 0; j < purchaseDTO.getArticles().size(); j++) {
                Long producId = purchaseDTO.getArticles().get(j).getProductId();
                while (rowIterator.hasNext()) {
                    row = rowIterator.next();
                    if (row.getRowNum() > 0 && row.getCell(0) != null) {
                        if (row.getCell(0).getNumericCellValue() == producId) {
                            Cell cell = row.getCell(5);
                            cell.setCellValue(cell.getNumericCellValue() - purchaseDTO.getArticles().get(j).getQuantity());
                            break;
                        }
                    }
                }
            }

            file.close();
            FileOutputStream outputStream = new FileOutputStream(filePath);
            book.write(outputStream);
            book.close();
            outputStream.close();

        } catch (IOException | EncryptedDocumentException e) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Error: No se encontro el siguiente Archivo: " + e.getMessage() + ".");
        }
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
            String fileName = "";
            String filePath = "";
            Integer sheetNum = 0;
            Properties properties = new Properties();
            XSSFWorkbook book = null;
            FileInputStream file = null;
            try {
                File newFile = new File("");
                properties.load(new FileInputStream(ResourceUtils.getFile("classpath:Products.properties")));
                fileName = properties.get("file").toString();
                filePath = newFile.getAbsolutePath()+properties.get("filePath").toString() + fileName;
                sheetNum = Integer.parseInt(properties.get("productsSheet").toString());
                file = new FileInputStream(ResourceUtils.getFile(filePath));
                book = new XSSFWorkbook(file);

                XSSFSheet sheet = book.getSheetAt(sheetNum);
                int rowCount = sheet.getLastRowNum();
                Long productId = 0L;
                if (rowCount > 0) {
                    try {
                        productId = (long) sheet.getRow(rowCount).getCell(0).getNumericCellValue() + 1;
                    } catch (NullPointerException e) {
                        productId = (long) rowCount;
                    }
                } else {
                    productId = 1L;
                }
                product.setProductId(productId);
                Row row = sheet.createRow(++rowCount);

                Cell cell = row.createCell(0);
                cell.setCellValue(productId);
                cell = row.createCell(1);
                cell.setCellValue(product.getName());
                cell = row.createCell(2);
                cell.setCellValue(product.getCategory());
                cell = row.createCell(3);
                cell.setCellValue(product.getBrand());
                cell = row.createCell(4);
                cell.setCellValue(product.getPrice());
                cell = row.createCell(5);
                cell.setCellValue(product.getQuantity());
                cell = row.createCell(6);
                if (product.isFreeShipping()) {
                    cell.setCellValue("SI");
                } else {
                    cell.setCellValue("NO");
                }
                cell = row.createCell(7);
                cell.setCellValue(product.getPrestige());

                file.close();
                FileOutputStream outputStream = new FileOutputStream(filePath);
                book.write(outputStream);
                book.close();
                outputStream.close();

            } catch (IOException | EncryptedDocumentException e) {
                throw new ApiException(HttpStatus.NOT_FOUND, "Error: No se encontro el siguiente Archivo: " + e.getMessage() + ".");
            }
            catch (NullPointerException e) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Error:  Formato de JSON invalido.");
            }
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
    // leo el archivo xlsx utilizado como base de datos y devlevo un mapa con productos
    private Map<Long, ProductDTO> reedArchive() throws ApiException {
        String fileName = "";
        String filePath = "";
        Integer sheetNum = 0;
        Properties properties = new Properties();
        XSSFWorkbook book = null;
        FileInputStream file = null;
        Map<Long, ProductDTO> products = new HashMap<>();
        try {
            File newFile = new File("");
            properties.load(new FileInputStream(ResourceUtils.getFile("classpath:Products.properties")));
            fileName = properties.get("file").toString();
            filePath = newFile.getAbsolutePath()+properties.get("filePath").toString() + fileName;
            sheetNum = Integer.parseInt(properties.get("productsSheet").toString());
            file = new FileInputStream(ResourceUtils.getFile(filePath));
            book = new XSSFWorkbook(file);
        } catch (IOException e) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Error: No se encontro el siguiente Archivo: " + e.getMessage() + ".");
        }
        XSSFSheet sheet = book.getSheetAt(sheetNum);
        Iterator<Row> rowIterator = sheet.rowIterator();
        Row row;

        while (rowIterator.hasNext()) {
            row = rowIterator.next();
            ProductDTO product = new ProductDTO();
            if (row.getRowNum() > 0 && row.getCell(0) != null) {
                product.setProductId(((long) row.getCell(0).getNumericCellValue()));
                product.setName(row.getCell(1).toString());
                product.setCategory(row.getCell(2).toString());
                product.setBrand(row.getCell(3).getStringCellValue());
                String price = row.getCell(4).toString().replace("$", "");
                price = price.replace(".", "");
                product.setPrice(Double.parseDouble(price));
                product.setQuantity((int) row.getCell(5).getNumericCellValue());
                if (row.getCell(6).toString().equals("SI")) {
                    product.setFreeShipping(true);
                } else {
                    product.setFreeShipping(false);
                }
                product.setPrestige(row.getCell(7).toString());
                products.put(product.getProductId(), product);
            }
        }
        return products;
    }
}
