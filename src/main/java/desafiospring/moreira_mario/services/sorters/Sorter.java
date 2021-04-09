package desafiospring.moreira_mario.services.sorters;

import java.util.Comparator;

public interface Sorter<T> {

    public abstract void sort(T arr[], Comparator<T> c);
}
