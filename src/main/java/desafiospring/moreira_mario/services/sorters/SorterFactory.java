package desafiospring.moreira_mario.services.sorters;

import desafiospring.moreira_mario.exceptions.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.util.ResourceUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
//factory de sorter para poder tener multiples implementaciones de ordenamiento forma generica
public class SorterFactory {
    public static Sorter getInstance(String objName) throws ApiException{
        Properties properties = new Properties();
        Sorter obj = null;
        try {
            properties.load(new FileInputStream(ResourceUtils.getFile("classpath:Products.properties")));
             obj = (Sorter) Class.forName(properties.get(objName).toString()).newInstance();
        } catch (IOException e) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Error: No se encontro el siguiente Archivo: "+e.getMessage()+".");
        } catch (IllegalAccessException e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Error: No tiene Acceso a: "+e.getMessage()+".");
        } catch (ClassNotFoundException e) {
            throw new ApiException(HttpStatus.NOT_IMPLEMENTED, "Error: No se ha implementado la class: "+e.getMessage()+".");
        } catch (InstantiationException e) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Error: No se encontro el siguiente Archivo: "+e.getMessage()+".");
        }
        return obj;
    }
}
