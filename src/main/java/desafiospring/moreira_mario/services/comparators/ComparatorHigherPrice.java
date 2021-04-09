package desafiospring.moreira_mario.services.comparators;

import desafiospring.moreira_mario.dtos.ProductDTO;

import java.util.Comparator;

public class ComparatorHigherPrice implements Comparator<ProductDTO> {
    @Override
    public int compare(ProductDTO o1, ProductDTO o2) {
        return o1.getPrice().compareTo(o2.getPrice());
    }
}
