package desafiospring.moreira_mario.services.sorters;

import java.util.Comparator;
//implementacion quick sort
public class QuickSortSorterImp implements Sorter {

    // A utility function to swap two elements
    private void swap(Object[] arr, int i, int j) {
        Object temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }


    private int partition(Object[] arr, Comparator c, int low, int high) {


        Object pivot = arr[high];


        int i = (low - 1);

        for (int j = low; j <= high - 1; j++) {


            if (c.compare(arr[j], pivot) < 0) {

                i++;
                swap(arr, i, j);
            }
        }
        swap(arr, i + 1, high);
        return (i + 1);
    }


    private void printArray(int[] arr, int size) {
        for (int i = 0; i < size; i++)
            System.out.print(arr[i] + " ");
        System.out.println();
    }

    private void quickSort(Object[] arr, Comparator c, int inicio, int fin) {
        if (inicio < fin) {

            int pi = partition(arr, c, inicio, fin);


            quickSort(arr, c, inicio, pi - 1);
            quickSort(arr, c, pi + 1, fin);
        }
    }

    @Override
    public void sort(Object[] arr, Comparator c) {
        quickSort(arr, c, 0, arr.length - 1);
    }
}
