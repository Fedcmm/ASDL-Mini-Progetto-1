package it.unicam.cs.asdl2122.mp1;

import java.util.*;

/**
 * La classe utilizza una {@link HashMap} per salvare gli elementi senza occupare
 * altro spazio ad ogni nuova occorrenza. La mappa memorizza l'elemento come chiave
 * e il numero di occorrenze come valore, in questo modo è facile effettuare tutte le
 * operazioni sulle occorrenze.
 *
 * @author Luca Tesei (template)
 * Federico Maria Cruciani, fedem.cruciani@studenti.unicam.it (implementazione)
 *
 * @param <E> il tipo degli elementi del multiset
 */
@SuppressWarnings({"SuspiciousMethodCalls"})
public class MyMultiset<E> implements Multiset<E> {

    /**
     * La {@link Map} che contiene gli elementi del multiset
     */
    private final Map<E, Integer> elementsMap;
    /**
     * La dimensione del multiset, considerando tutte le occorrenze
     */
    private int size;
    /**
     * Contatore delle modifiche al multiset, necessario perché l'iteratore
     * sia fail-fast
     */
    private int modCount;


    /**
     * Crea un multiset vuoto.
     */
    public MyMultiset() {
        elementsMap = new HashMap<>();
        size = 0;
        modCount = 0;
    }


    @Override
    public int size() {
        return size;
    }

    @Override
    public int count(Object element) {
        if (element == null)
            throw new NullPointerException("Trying to count a null element");

        // Il metodo getOrDefault ritorna 0 se l'elemento non esiste (altrimenti sarebbe null)
        return elementsMap.getOrDefault(element, 0);
    }

    @Override
    public int add(E element, int occurrences) {
        if (element == null)
            throw new NullPointerException("Trying to add a null element");

        // Il metodo getOrDefault ritorna 0 se l'elemento non esiste (altrimenti sarebbe null)
        Integer previousOccurrences = elementsMap.getOrDefault(element, 0);

        if (occurrences < 0 || previousOccurrences + (long) occurrences > Integer.MAX_VALUE)
            throw new IllegalArgumentException("Trying to add an invalid number of elements");

        elementsMap.put(element, previousOccurrences + occurrences);
        size += occurrences;
        modCount++;
        return previousOccurrences;
    }

    // Le eccezioni vengono lanciate dall'altro add
    @Override
    public void add(E element) {
        add(element, 1);
    }

    @SuppressWarnings("unchecked")
    @Override
    public int remove(Object element, int occurrences) {
        if (element == null)
            throw new NullPointerException("Trying to remove a null element");
        if (occurrences < 0)
            throw new IllegalArgumentException("Trying to remove a negative number of elements");

        if (!contains(element))
            return 0;

        // L'elemento è presente, quindi non serve utilizzare getOrDefault per evitare il null
        Integer previousOccurrences = elementsMap.get(element);
        if (occurrences == 0)
            return previousOccurrences; // Nessuna modifica

        int diff = previousOccurrences - occurrences;
        if (diff < 0) {
            // Tutte le occorrenze vanno rimosse
            elementsMap.remove(element);
            size -= previousOccurrences;
        } else {
            // Il cast al generic è sicuro perché element è presente nel multiset
            elementsMap.put((E) element, diff);
            size -= diff;
        }
        modCount++;
        return previousOccurrences;
    }

    // Se il numero precedente era > 1 allora esisteva ed è stata rimossa un'occorrenza dell'elemento.
    // L'eccezione viene lanciata dall'altro remove
    @Override
    public boolean remove(Object element) {
        return remove(element, 1) > 0;
    }

    @Override
    public int setCount(E element, int count) {
        if (element == null)
            throw new NullPointerException("Trying to set a null element");
        if (count < 0)
            throw new IllegalArgumentException("Trying to set a negative number of elements");

        Integer integerCount = elementsMap.put(element, count);
        // integerCount è null se l'elemento non esisteva, quindi va usata una variabile int
        int previousCount = integerCount == null ? 0 : integerCount;
        size += count - previousCount; // La differenza può essere negativa per ridurre la size
        if (previousCount != count)
            modCount++; // Il multiset cambia solo se il nuovo count è diverso
        return previousCount;
    }

    // Il Set ritornato da keySet() non implementa l'add quindi viene messo in un HashSet
    @Override
    public Set<E> elementSet() {
        return new HashSet<>(elementsMap.keySet());
    }

    @Override
    public Iterator<E> iterator() {
        return new Itr();
    }

    @Override
    public boolean contains(Object element) {
        if (element == null)
            throw new NullPointerException("Trying to test existence of a null element");
        
        return elementsMap.containsKey(element);
    }

    @Override
    public void clear() {
        elementsMap.clear();
        size = 0;
        modCount++;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Due multinsiemi sono uguali se e solo se contengono esattamente gli
     * stessi elementi (utilizzando l'equals della classe E) con le stesse
     * molteplicità.
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof MyMultiset))
            return false;

        MyMultiset<?> other = (MyMultiset<?>) obj;
        Iterator<E> thisIterator = this.iterator();
        Iterator<?> otherIterator = other.iterator();
        while (thisIterator.hasNext()) {
            if (!otherIterator.hasNext())
                return false; // L'altro multiset è finito ma questo no
            if (!thisIterator.next().equals(otherIterator.next()))
                return false; // I due multiset hanno un elemento diverso
        }

        // thisIterator è finito, controlla che anche l'altro lo sia
        return !otherIterator.hasNext();
    }

    /**
     * Da ridefinire in accordo con la ridefinizione di equals.
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int hash = 0;
        for (Iterator<E> iterator = iterator(); iterator.hasNext();) {
            hash += 31 * iterator().next().hashCode();
        }
        return hash;
    }


    private class Itr implements Iterator<E> {

        /**
         * Iteratore sui singoli elementi del multiset
         */
        private final Iterator<E> keyIterator = elementSet().iterator();
        /**
         * Il numero di modifiche a cui è stato sottoposto il set fin'ora
         */
        private final int expectedMods = modCount;

        /**
         * L'elemento di cui si stanno restituendo le occorrenze
         */
        private E currentElement;
        /**
         * Contatore delle occorrenze ancora da restituire dell'elemento
         */
        private int currentElCount;


        // Se l'elemento attuale ha ancora delle occorrenze hasNext() deve ritornare true,
        // anche se keyIterator è finito
        @Override
        public boolean hasNext() {
            return keyIterator.hasNext() || currentElCount > 0;
        }

        @Override
        public E next() {
            if (expectedMods != modCount)
                // Il multiset è stato modificato durante l'iterazione
                throw new ConcurrentModificationException("Multiset changed during iteration");

            if (currentElCount == 0) {
                // Tutte le occorrenze dell'elemento attuale sono finite
                currentElement = keyIterator.next();
                currentElCount = elementsMap.get(currentElement);
            }
            currentElCount--;
            return currentElement;
        }
    }
}
