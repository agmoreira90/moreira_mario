package desafiospring.moreira_mario.repositories;

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
import java.util.*;

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
            try {
                sheetNum = Integer.parseInt(properties.get(sheetName).toString());
            } catch (NullPointerException e) {
                throw new ApiException(HttpStatus.NOT_FOUND, "Error: No se encontro la hoja: " + sheetName + ".");
            }
            file = new FileInputStream(ResourceUtils.getFile(filePath));
            book = new XSSFWorkbook(file);

            XSSFSheet sheet = book.getSheetAt(sheetNum);
            int rowCount = sheet.getLastRowNum();
            if (rowCount > 0) {
                try {
                    objectId = (long) Integer.parseInt(sheet.getRow(rowCount).getCell(0).toString().replace(".0","")) + 1;
                } catch (NullPointerException e) {
                    objectId = (long) rowCount;
                }
            } else {
                objectId = 1L;
            }
            Row row = sheet.createRow(++rowCount);
            Cell cell = row.createCell(0);
            cell.setCellValue(objectId);
            for (Map.Entry<Integer, String> entry : registr.entrySet()) {
                cell = row.createCell(entry.getKey());
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

    public static Map<Long, ArrayList<String>> readXLSX(String sheetName) throws ApiException {
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
            sheetNum = Integer.parseInt(properties.get(sheetName).toString());
            file = new FileInputStream(ResourceUtils.getFile(filePath));
            book = new XSSFWorkbook(file);
            XSSFSheet sheet = book.getSheetAt(sheetNum);
            Iterator<Row> rowIterator = sheet.rowIterator();
            Row row;
            Map<Long, ArrayList<String>> data = new HashMap<>();
            while (rowIterator.hasNext()) {
                row = rowIterator.next();
                Iterator<Cell> cellIterator = row.iterator();
                if (row.getRowNum() > 0 && row.getCell(0) != null) {
                    Cell cell;
                    ArrayList<String> line = new ArrayList<>();
                    Long id = Long.parseLong(row.getCell(0).toString().replace(".0",""));
                    while (cellIterator.hasNext()) {
                        cell = cellIterator.next();
                        line.add(cell.toString());
                    }
                    data.put((long) row.getRowNum(), line);
                }
            }
            return data;
        } catch (IOException e) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Error: No se encontro el siguiente Archivo: " + e.getMessage() + ".");
        }
    }

    public static void updateXLSX(Map<Integer, String> registr, Map<String, String> filters, String sheetName) throws ApiException { Long objectId = 0L;
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
            sheetNum = Integer.parseInt(properties.get(sheetName).toString());
            file = new FileInputStream(ResourceUtils.getFile(filePath));
            book = new XSSFWorkbook(file);
            XSSFSheet sheet = book.getSheetAt(sheetNum);
            Iterator<Row> rowIterator = sheet.rowIterator();
            Row row;
            while (rowIterator.hasNext()) {
                row = rowIterator.next();
                if (row.getRowNum() > 0 && row.getCell(0) != null) {
                    boolean isOk = true;
                    for (Map.Entry<String, String> entry : filters.entrySet()) {
                        Integer index = Integer.parseInt(entry.getKey());
                        if (!entry.getValue().equals(row.getCell(index).toString().replace(".0",""))) {
                            isOk = false;
                            break;
                        }
                    }
                    if (isOk) {
                        Cell cell;
                        for (Map.Entry<Integer, String> entry : registr.entrySet()) {
                            cell = row.getCell(entry.getKey());
                            cell.setCellValue(entry.getValue());
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

    public static void deleteXLSX(Map<String, String> filters, String sheetName) throws ApiException {
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
            sheetNum = Integer.parseInt(properties.get(sheetName).toString());
            file = new FileInputStream(ResourceUtils.getFile(filePath));
            book = new XSSFWorkbook(file);
            XSSFSheet sheet = book.getSheetAt(sheetNum);
            Iterator<Row> rowIterator = sheet.rowIterator();
            Row row;
            List<Integer> idList = new ArrayList<>();
            while (rowIterator.hasNext()) {
                row = rowIterator.next();
                if (row.getRowNum() > 0 && row.getCell(0) != null) {
                    boolean isOk = true;
                    for (Map.Entry<String, String> entry : filters.entrySet()) {
                        Integer index = Integer.parseInt(entry.getKey());
                        if (!entry.getValue().equals(row.getCell(index).toString().replace(".0",""))) {
                            isOk = false;
                            break;
                        }
                    }
                    if (isOk) {
                       idList.add(row.getRowNum());
                    }
                }
            }
            for (int i = 0; i < idList.size(); i++) {
                sheet.removeRow(sheet.getRow(idList.get(i)));
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
}
