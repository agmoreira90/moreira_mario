package desafiospring.moreira_mario.repositories;

import desafiospring.moreira_mario.dtos.ProductDTO;
import desafiospring.moreira_mario.exceptions.ApiException;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class XLSXUtil {

    public static Long writeXLSX(Map<Integer, String> registr, String sheetName) throws ApiException {
        Long objectId = 0L;
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
            sheetNum = Integer.parseInt(properties.get("productsSheet").toString());
            file = new FileInputStream(ResourceUtils.getFile(filePath));
            book = new XSSFWorkbook(file);

            XSSFSheet sheet = book.getSheetAt(sheetNum);
            int rowCount = sheet.getLastRowNum();
            if (rowCount > 0) {
                try {
                    objectId = (long) sheet.getRow(rowCount).getCell(0).getNumericCellValue() + 1;
                } catch (NullPointerException e) {
                    objectId = (long) rowCount;
                }
            } else {
                objectId = 1L;
            }
            Row row = sheet.createRow(++rowCount);
            for (Map.Entry<Integer, String> entry : registr.entrySet()) {
                Cell cell = row.createCell(entry.getKey());
                cell.setCellValue(entry.getValue());
            }
            file.close();
            FileOutputStream outputStream = new FileOutputStream(filePath);
            book.write(outputStream);
            book.close();
            outputStream.close();
            return objectId;
        } catch (IOException | EncryptedDocumentException e) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Error: No se encontro el siguiente Archivo: " + e.getMessage() + ".");
        }
    }
}
