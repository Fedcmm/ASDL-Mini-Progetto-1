package it.unicam.cs.asdl2122.mp1;

import java.util.HashSet;
import java.util.Set;

/**
 * La classe utilizza un {@link HashSet} per memorizzare il rappresentante di ogni set,
 * in modo da poterlo ottenere facilmente quando richiesto.
 * I metodi sono implementati per ridurre al minimo le operazioni.
 * 
 * @author Luca Tesei (template)
 * Federico Maria Cruciani, fedem.cruciani@studenti.unicam.it (implementazione)
 */
public class LinkedListDisjointSets implements DisjointSets {

    /**
     * Il {@link Set} che contiene i rappresentanti degli insiemi disgiunti
     */
    private final Set<DisjointSetElement> representativesSet;


    /**
     * Crea una collezione vuota di insiemi disgiunti.
     */
    public LinkedListDisjointSets() {
        representativesSet = new HashSet<>();
    }


    /*
     * Nella rappresentazione con liste concatenate un elemento è presente in
     * qualche insieme disgiunto se il puntatore al suo elemento rappresentante
     * (ref1) non è null.
     */
    @Override
    public boolean isPresent(DisjointSetElement e) {
        return e.getRef1() != null;
    }

    /*
     * Nella rappresentazione con liste concatenate un nuovo insieme disgiunto è
     * rappresentato da una lista concatenata che contiene l'unico elemento. Il
     * rappresentante deve essere l'elemento stesso e la cardinalità deve essere
     * 1.
     */
    @Override
    public void makeSet(DisjointSetElement e) {
        if (e == null)
            throw new NullPointerException("Trying to create a set with a null element");
        if (isPresent(e))
            throw new IllegalArgumentException("The element is already in a set");

        e.setRef1(e);
        e.setRef2(null);
        e.setNumber(1);
        representativesSet.add(e);
    }

    /*
     * Nella rappresentazione con liste concatenate per trovare il
     * rappresentante di un elemento basta far riferimento al suo puntatore
     * ref1.
     */
    @Override
    public DisjointSetElement findSet(DisjointSetElement e) {
        if (e == null)
            throw new NullPointerException("Trying to find representative of a null element");
        if (!isPresent(e))
            throw new IllegalArgumentException("The element isn't in a set");

        return e.getRef1();
    }

    /*
     * Dopo l'unione di due insiemi effettivamente disgiunti il rappresentante
     * dell'insieme unito è il rappresentate dell'insieme che aveva il numero
     * maggiore di elementi tra l'insieme di cui faceva parte {@code e1} e
     * l'insieme di cui faceva parte {@code e2}. Nel caso in cui entrambi gli
     * insiemi avevano lo stesso numero di elementi il rappresentante
     * dell'insieme unito è il rappresentante del vecchio insieme di cui faceva
     * parte {@code e1}.
     * 
     * Questo comportamento è la risultante naturale di una strategia che
     * minimizza il numero di operazioni da fare per realizzare l'unione nel
     * caso di rappresentazione con liste concatenate.
     */
    @Override
    public void union(DisjointSetElement e1, DisjointSetElement e2) {
        if (e1 == null || e2 == null)
            throw new NullPointerException("Trying to unite with a null element");
        if (!isPresent(e1) || !isPresent(e2))
            throw new IllegalArgumentException("One of the elements isn't in a set");

        if (e1.getRef1() == e2.getRef1())
            return; // I due elementi sono nello stesso insieme

        // Vengono presi i rappresentanti dei due insiemi
        DisjointSetElement e1Rep = e1.getRef1();
        DisjointSetElement e2Rep = e2.getRef1();

        if (e1Rep.getNumber() >= e2Rep.getNumber()) {
            unionFromRepresentatives(e1Rep, e2Rep);
        } else {
            unionFromRepresentatives(e2Rep, e1Rep);
        }
    }

    @Override
    public Set<DisjointSetElement> getCurrentRepresentatives() {
        return representativesSet;
    }

    @Override
    public Set<DisjointSetElement> getCurrentElementsOfSetContaining(DisjointSetElement e) {
        if (e == null)
            throw new NullPointerException("Trying to get the set of a null element");
        if (!isPresent(e))
            throw new IllegalArgumentException("The element isn't in a set");

        DisjointSetElement rep = e.getRef1();

        // La dimensione del nuovo Set è nota, quindi viene inizializzato con quella
        // per risparmiare operazioni non necessarie
        Set<DisjointSetElement> elements = new HashSet<>(rep.getNumber());

        DisjointSetElement element = rep;
        while (element != null) {
            elements.add(element);
            element = element.getRef2();
        }
        return elements;
    }

    @Override
    public int getCardinalityOfSetContaining(DisjointSetElement e) {
        if (e == null)
            throw new NullPointerException("Trying to get cardinality of the set of a null element");
        if (!isPresent(e))
            throw new IllegalArgumentException("The element isn't in a set");

        return e.getRef1().getNumber();
    }


    /**
     * Metodo di utilità che unisce i due insiemi rappresentati dagli elementi passati,
     * assumendo che l'insieme del primo rappresentante sia maggiore o uguale in dimensioni
     * all'insieme del secondo (il metodo è privato quindi non c'è nessun controllo sugli argomenti).
     *
     * @param rep1 il rappresentante dell'insieme più grande
     * @param rep2 il rappresentante dell'insieme più piccolo
     */
    private void unionFromRepresentatives(DisjointSetElement rep1, DisjointSetElement rep2) {
        // rep2 non rappresenta più nessun insieme, deve essere rimosso dai rappresentanti
        // e il suo riferimento aggiornato
        rep2.setRef1(rep1);
        representativesSet.remove(rep2);

        DisjointSetElement set2Elem = rep2;
        while (set2Elem.getRef2() != null) {
            set2Elem = set2Elem.getRef2();
            set2Elem.setRef1(rep1); // Ogni riferimento al rappresentante deve essere aggiornato
        }
        // set2Elem è ora l'ultimo elemento del secondo insieme, deve puntare al successivo di rep1
        set2Elem.setRef2(rep1.getRef2());

        rep1.setRef2(rep2); // rep1 è il primo elemento della lista concatenata e deve puntare a rep2
        rep1.setNumber(rep1.getNumber() + rep2.getNumber());
    }
}
