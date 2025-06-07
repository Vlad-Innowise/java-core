package javaCore.customLinkedList;

public interface CustomList<E> {

    int size();

    void addFirst(E e);

    void addLast(E e);

    void add(int index, E e);

    E getFirst();

    E getLast();

    E get(int index);

    E removeFirst();

    E removeLast();

    E remove(int index);
}
