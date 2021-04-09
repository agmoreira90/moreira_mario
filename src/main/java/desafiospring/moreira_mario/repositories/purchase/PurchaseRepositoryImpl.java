package desafiospring.moreira_mario.repositories.purchase;

import desafiospring.moreira_mario.dtos.ProductDTO;
import desafiospring.moreira_mario.dtos.PurchaseArticleDTO;
import desafiospring.moreira_mario.dtos.PurchaseDTO;
import desafiospring.moreira_mario.exceptions.ApiException;
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
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class PurchaseRepositoryImpl implements PurchaseRepository {

    private final AtomicLong purchaseId = new AtomicLong();
    //recibe un purchasedto y llama al proceso de escritura de excel
    @Override
    public PurchaseDTO createTicket(PurchaseDTO purchase) throws ApiException {
        insertPurchase(purchase);
        return purchase;
    }
    // recibe un purchasedto y lo impacta en la hoja de carrito del cliete del xlsx
    @Override
    public PurchaseDTO addToCart(PurchaseDTO purchaseDTO) throws ApiException {
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
            sheetNum = Integer.parseInt(properties.get("cartsSheet").toString());
            file = new FileInputStream(ResourceUtils.getFile(filePath));
            book = new XSSFWorkbook(file);

            XSSFSheet sheet = book.getSheetAt(sheetNum);
            int rowCount = sheet.getLastRowNum();
            System.out.println(rowCount);
            if (rowCount == 0) {
                purchaseDTO.setId(1L);
            } else {
                purchaseDTO.setId((long) sheet.getRow(rowCount).getCell(0).getNumericCellValue() + 1);
            }
            Row row = sheet.createRow(++rowCount);

            Cell cell = row.createCell(0);
            cell.setCellValue(purchaseDTO.getId());
            cell = row.createCell(1);
            cell.setCellValue(purchaseDTO.getTotal());
            cell = row.createCell(2);
            cell.setCellValue(purchaseDTO.getIdClient());

            file.close();
            FileOutputStream outputStream = new FileOutputStream(filePath);
            book.write(outputStream);
            book.close();
            outputStream.close();

            properties.load(new FileInputStream(ResourceUtils.getFile("classpath:Products.properties")));
            fileName = properties.get("file").toString();
            filePath = newFile.getAbsolutePath() + properties.get("filePath").toString() + fileName;
            sheetNum = Integer.parseInt(properties.get("cartsArticlesSheet").toString());
            file = new FileInputStream(ResourceUtils.getFile(filePath));
            book = new XSSFWorkbook(file);

            sheet = book.getSheetAt(sheetNum);
            rowCount = sheet.getLastRowNum();

            for (int i = 0; i < purchaseDTO.getArticles().size(); i++) {

                row = sheet.createRow(++rowCount);

                cell = row.createCell(0);
                cell.setCellValue(purchaseDTO.getId());
                cell = row.createCell(1);
                cell.setCellValue(purchaseDTO.getArticles().get(i).getProductId());
                cell = row.createCell(2);
                cell.setCellValue(purchaseDTO.getArticles().get(i).getName());
                cell = row.createCell(3);
                cell.setCellValue(purchaseDTO.getArticles().get(i).getBrand());
                cell = row.createCell(4);
                cell.setCellValue(purchaseDTO.getArticles().get(i).getQuantity());
            }
            file.close();
            outputStream = new FileOutputStream(filePath);
            book.write(outputStream);
            book.close();
            outputStream.close();

        } catch (IOException | EncryptedDocumentException e) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Error: No se encontro el siguiente Archivo: " + e.getMessage() + ".");
        }
        return purchaseDTO;
    }
    // actualiza la planilla xlsx borrando los registros de un cliente de la hoja de carrito de compra
    @Override
    public void clearCart(Long clientId) throws ApiException {
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
            sheetNum = Integer.parseInt(properties.get("cartsSheet").toString());
            file = new FileInputStream(ResourceUtils.getFile(filePath));
            book = new XSSFWorkbook(file);

            XSSFSheet sheet = book.getSheetAt(sheetNum);
            Iterator<Row> rowIterator = sheet.rowIterator();
            Row row;

            while (rowIterator.hasNext()) {
                row = rowIterator.next();
                PurchaseDTO purchase = new PurchaseDTO();
                if (row.getRowNum() > 0 && row.getCell(0) != null && (long) row.getCell(2).getNumericCellValue() == clientId) {
                    this.clearArticles((long) row.getCell(0).getNumericCellValue());
                    sheet.createRow(row.getRowNum());
                }
            }
            file.close();
            FileOutputStream outputStream = new FileOutputStream(filePath);
            book.write(outputStream);
            book.close();
            outputStream.close();
        } catch (IOException e) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Error: No se encontro el siguiente Archivo: " + e.getMessage() + ".");
        }
    }
    // actualiza la planilla xlsx borrando los registros de los articulos un carrito de la hoja de carrito de compra
    private void clearArticles(Long idPurchase) throws ApiException {
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
            sheetNum = Integer.parseInt(properties.get("cartsArticlesSheet").toString());
            file = new FileInputStream(ResourceUtils.getFile(filePath));
            book = new XSSFWorkbook(file);

            XSSFSheet sheet = book.getSheetAt(sheetNum);
            Iterator<Row> rowIterator = sheet.rowIterator();
            Row row;

            while (rowIterator.hasNext()) {
                row = rowIterator.next();
                PurchaseArticleDTO purchaseArticle = new PurchaseArticleDTO();
                if (row.getRowNum() > 0 && row.getCell(0) != null && (long) row.getCell(0).getNumericCellValue() == idPurchase) {
                    sheet.createRow(row.getRowNum());
                }
            }
            file.close();
            FileOutputStream outputStream = new FileOutputStream(filePath);
            book.write(outputStream);
            book.close();
            outputStream.close();
        } catch (IOException e) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Error: No se encontro el siguiente Archivo: " + e.getMessage() + ".");
        }
    }
    // guarda en el xlsx la orden de compra y los articulos de la misma en sus respectivvas hojas
    private void insertPurchase(PurchaseDTO purchaseDTO) throws ApiException {
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
            sheetNum = Integer.parseInt(properties.get("purchaseSheet").toString());
            file = new FileInputStream(ResourceUtils.getFile(filePath));
            book = new XSSFWorkbook(file);

            XSSFSheet sheet = book.getSheetAt(sheetNum);
            int rowCount = sheet.getLastRowNum();
            //validar q no sea primera celda
            if (rowCount > 0) {
                try {
                    purchaseDTO.setId((long) sheet.getRow(rowCount).getCell(0).getNumericCellValue() + 1);
                } catch (NullPointerException e) {
                    purchaseDTO.setId((long) rowCount);
                }
            } else {
                purchaseDTO.setId(1L);
            }

            Row row = sheet.createRow(++rowCount);

            Cell cell = row.createCell(0);
            cell.setCellValue(purchaseDTO.getId());
            cell = row.createCell(1);
            cell.setCellValue(purchaseDTO.getTotal());
            if (purchaseDTO.getIdClient() == null) {
                cell = row.createCell(2);
                cell.setCellValue(0L);
            } else {
                cell = row.createCell(2);
                cell.setCellValue(purchaseDTO.getIdClient());
            }

            file.close();
            FileOutputStream outputStream = new FileOutputStream(filePath);
            book.write(outputStream);
            book.close();
            outputStream.close();

            properties.load(new FileInputStream(ResourceUtils.getFile("classpath:Products.properties")));
            fileName = properties.get("file").toString();
            filePath = newFile.getAbsolutePath() + properties.get("filePath").toString() + fileName;
            sheetNum = Integer.parseInt(properties.get("purchaseArticlesSheet").toString());
            file = new FileInputStream(ResourceUtils.getFile(filePath));
            book = new XSSFWorkbook(file);

            sheet = book.getSheetAt(sheetNum);
            rowCount = sheet.getLastRowNum();

            for (int i = 0; i < purchaseDTO.getArticles().size(); i++) {

                row = sheet.createRow(++rowCount);

                cell = row.createCell(0);
                cell.setCellValue(purchaseDTO.getId());
                cell = row.createCell(1);
                cell.setCellValue(purchaseDTO.getArticles().get(i).getProductId());
                cell = row.createCell(2);
                cell.setCellValue(purchaseDTO.getArticles().get(i).getName());
                cell = row.createCell(3);
                cell.setCellValue(purchaseDTO.getArticles().get(i).getBrand());
                cell = row.createCell(4);
                cell.setCellValue(purchaseDTO.getArticles().get(i).getQuantity());


            }


            file.close();
            outputStream = new FileOutputStream(filePath);
            book.write(outputStream);
            book.close();
            outputStream.close();

        } catch (IOException | EncryptedDocumentException e) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Error: No se encontro el siguiente Archivo: " + e.getMessage() + ".");
        }
    }

}
