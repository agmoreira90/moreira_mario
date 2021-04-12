package desafiospring.moreira_mario.repositories.purchase;

import desafiospring.moreira_mario.dtos.PurchaseDTO;
import desafiospring.moreira_mario.exceptions.ApiException;
import desafiospring.moreira_mario.repositories.XLSXUtil;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class PurchaseRepositoryImpl implements PurchaseRepository {

    //recibe un purchasedto y llama al proceso de escritura de excel
    @Override
    public PurchaseDTO createTicket(PurchaseDTO purchase) throws ApiException {
        this.createPurchase(purchase,"purchaseSheet","purchaseArticlesSheet");
        return purchase;
    }

    // recibe un purchasedto y lo impacta en la hoja de carrito del cliete del xlsx
    @Override
    public PurchaseDTO addToCart(PurchaseDTO purchaseDTO) throws ApiException {
        this.createPurchase(purchaseDTO,"cartsSheet","cartsArticlesSheet");
        return purchaseDTO;
    }

    // actualiza la planilla xlsx borrando los registros de un cliente de la hoja de carrito de compra
    @Override
    public void clearCart(Long clientId) throws ApiException {
        Map<String, String> filters = new HashMap<>();
        filters.put("2", clientId.toString());
        XLSXUtil.deleteXLSX(filters,"cartsSheet");
    }

    @Override
    // actualiza la planilla xlsx borrando los registros de los articulos un carrito de la hoja de carrito de compra
    public void clearArticles(Long idPurchase) throws ApiException {
        Map<String, String> filters = new HashMap<>();
        filters.put("0", idPurchase.toString());
        XLSXUtil.deleteXLSX(filters,"cartsArticlesSheet");
    }

    // guarda en el xlsx la orden de compra y los articulos de la misma en sus respectivvas hojas
    private void createPurchase(PurchaseDTO purchaseDTO, String sheetName, String sheetName2) throws ApiException {

        Map<Integer, String> purchase = new HashMap<>();
        purchase.put(1, purchaseDTO.getTotal().toString());
        if (purchaseDTO.getIdClient() == null) {
            purchase.put(2, "0");
        } else {
            purchase.put(2, purchaseDTO.getIdClient().toString());
        }
        purchaseDTO.setId(XLSXUtil.writeXLSX(purchase, sheetName));
        for (int i = 0; i < purchaseDTO.getArticles().size(); i++) {
            Map<Integer, String> purchaseArticle = new HashMap<>();
            purchaseArticle.put(0, purchaseDTO.getId().toString());
            purchaseArticle.put(1, purchaseDTO.getArticles().get(i).getProductId().toString());
            purchaseArticle.put(2, purchaseDTO.getArticles().get(i).getName());
            purchaseArticle.put(3, purchaseDTO.getArticles().get(i).getBrand());
            purchaseArticle.put(4, purchaseDTO.getArticles().get(i).getQuantity().toString());
            XLSXUtil.writeXLSX(purchaseArticle, sheetName2);
        }

    }

}
