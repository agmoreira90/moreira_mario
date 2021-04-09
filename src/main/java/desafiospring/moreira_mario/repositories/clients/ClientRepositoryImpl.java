package desafiospring.moreira_mario.repositories.clients;

import desafiospring.moreira_mario.dtos.ClientDTO;
import desafiospring.moreira_mario.dtos.ProductDTO;
import desafiospring.moreira_mario.dtos.PurchaseArticleDTO;
import desafiospring.moreira_mario.dtos.PurchaseDTO;
import desafiospring.moreira_mario.exceptions.ApiException;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
                filePath = newFile.getAbsolutePath() + properties.get("filePath").toString() + fileName;
                sheetNum = Integer.parseInt(properties.get("clientsSheet").toString());
                file = new FileInputStream(ResourceUtils.getFile(filePath));
                book = new XSSFWorkbook(file);

                XSSFSheet sheet = book.getSheetAt(sheetNum);
                int rowCount = sheet.getLastRowNum();
                Long clientId = 0L;
                System.out.println(rowCount);
                if (rowCount > 0) {
                    try {
                        clientId = (long) sheet.getRow(rowCount).getCell(0).getNumericCellValue() + 1;
                    } catch (NullPointerException e) {
                        clientId = (long) rowCount;
                    }
                } else {
                    clientId = 1L;
                }
                client.setIdClient(clientId);
                Row row = sheet.createRow(++rowCount);

                Cell cell = row.createCell(0);
                cell.setCellValue(clientId);
                cell = row.createCell(1);
                cell.setCellValue(client.getDni());
                cell = row.createCell(2);
                cell.setCellValue(client.getName());
                cell = row.createCell(3);
                cell.setCellValue(client.getSurname());
                cell = row.createCell(4);
                cell.setCellValue(client.getMail());
                cell = row.createCell(5);
                cell.setCellValue(client.getPhone());
                cell = row.createCell(6);
                cell.setCellValue(client.getAddress());
                cell = row.createCell(7);
                cell.setCellValue(client.getProvince());

                file.close();
                FileOutputStream outputStream = new FileOutputStream(filePath);
                book.write(outputStream);
                book.close();
                outputStream.close();

            } catch (IOException | EncryptedDocumentException | NullPointerException e) {
                throw new ApiException(HttpStatus.NOT_FOUND, "Error: No se encontro el siguiente Archivo: " + e.getMessage() + ".");
            }
            return client;
        } else {
            throw new ApiException(HttpStatus.CONFLICT, "Error: el Cliente: " + client.getDni() + " " + client.getMail() + " ya existe.");
        }

    }

    //recibo una mapa de filtros lee el archico excel configurda en el archivo properties luego le aplica los filtros por cada resgistro que tenga
    // el mapa params
    @Override
    public Map<Long, ClientDTO> selectClients(Map<String, String> params) throws ApiException {
        Map<Long, ClientDTO> clientsMap = this.reedArchive();
        for (Map.Entry<String, String> filter : params.entrySet()) {
            clientsMap = this.applyFilters(filter, clientsMap);

        }
        return clientsMap;
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
    // leo el archivo xlsx utilizado como base de datos y devlevo un mapa con clientes
    private Map<Long, ClientDTO> reedArchive() throws ApiException {
        String fileName = "";
        String filePath = "";
        Integer sheetNum = 0;
        Properties properties = new Properties();
        XSSFWorkbook book = null;
        FileInputStream file = null;
        Map<Long, ClientDTO> clients = new HashMap<>();
        try {
            File newFile = new File("");
            properties.load(new FileInputStream(ResourceUtils.getFile("classpath:Products.properties")));
            fileName = properties.get("file").toString();
            filePath = newFile.getAbsolutePath() + properties.get("filePath").toString() + fileName;
            sheetNum = Integer.parseInt(properties.get("clientsSheet").toString());
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
            ClientDTO client = new ClientDTO();
            if (row.getRowNum() > 0 && row.getCell(0) != null) {
                client.setIdClient(((long) row.getCell(0).getNumericCellValue()));
                client.setDni(row.getCell(1).toString());
                client.setName(row.getCell(2).toString());
                client.setSurname(row.getCell(3).toString());
                client.setMail(row.getCell(4).toString());
                client.setPhone(row.getCell(5).toString());
                client.setAddress(row.getCell(6).toString());
                client.setProvince(row.getCell(7).toString());
                client.setCart(this.getPurchase((long) row.getCell(0).getNumericCellValue(), "cartsSheet", "cartsArticlesSheet"));
                client.setOrders(this.getPurchase((long) row.getCell(0).getNumericCellValue(), "purchaseSheet", "purchaseArticlesSheet"));
                clients.put(client.getIdClient(), client);
            }
        }
        return clients;
    }
    // leo el archivo xlsx utilizado como base de datos y devlevo el carrito de compras que tiene pendiente el cliente
    private List<PurchaseDTO> getPurchase(Long clientId, String sheetName, String sheetName2) throws ApiException {
        String fileName = "";
        String filePath = "";
        Integer sheetNum = 0;
        Properties properties = new Properties();
        XSSFWorkbook book = null;
        FileInputStream file = null;
        List<PurchaseDTO> purchases = new ArrayList();
        try {
            File newFile = new File("");
            properties.load(new FileInputStream(ResourceUtils.getFile("classpath:Products.properties")));
            fileName = properties.get("file").toString();
            filePath = newFile.getAbsolutePath() + properties.get("filePath").toString() + fileName;
            sheetNum = Integer.parseInt(properties.get(sheetName).toString());
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
            PurchaseDTO purchase = new PurchaseDTO();
            if (row.getRowNum() > 0 && row.getCell(0) != null && (long) row.getCell(2).getNumericCellValue() == clientId) {
                purchase.setId(((long) row.getCell(0).getNumericCellValue()));
                purchase.setTotal((double) row.getCell(1).getNumericCellValue());
                purchase.setIdClient(((long) row.getCell(2).getNumericCellValue()));
                purchase.setArticles(this.getPurchaseArticles((long) row.getCell(0).getNumericCellValue(), sheetName2));
                purchases.add(purchase);
            }
        }
        return purchases;

    }
    // leo el archivo xlsx utilizado como base de datos y devlevo los articulos del carrito de compra del cliente
    private List<PurchaseArticleDTO> getPurchaseArticles(Long idPurchase, String sheetName) throws ApiException {
        String fileName = "";
        String filePath = "";
        Integer sheetNum = 0;
        Properties properties = new Properties();
        XSSFWorkbook book = null;
        FileInputStream file = null;
        List<PurchaseArticleDTO> purchasesArticles = new ArrayList();
        try {
            File newFile = new File("");
            properties.load(new FileInputStream(ResourceUtils.getFile("classpath:Products.properties")));
            fileName = properties.get("file").toString();
            filePath = newFile.getAbsolutePath() + properties.get("filePath").toString() + fileName;
            sheetNum = Integer.parseInt(properties.get(sheetName).toString());
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
            PurchaseArticleDTO purchaseArticle = new PurchaseArticleDTO();
            if (row.getRowNum() > 0 && row.getCell(0) != null && (long) row.getCell(0).getNumericCellValue() == idPurchase) {
                purchaseArticle.setProductId(((long) row.getCell(1).getNumericCellValue()));
                purchaseArticle.setName(row.getCell(2).getStringCellValue());
                purchaseArticle.setBrand(row.getCell(3).getStringCellValue());
                purchaseArticle.setQuantity((int) row.getCell(1).getNumericCellValue());
                purchasesArticles.add(purchaseArticle);
            }
        }
        return purchasesArticles;
    }

}
