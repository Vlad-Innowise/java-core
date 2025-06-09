package javaCore.customLinkedList;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CustomLinkedListImpl<E> implements CustomList<E> {

    private Node<E> head;
    private Node<E> tail;
    private int size;

    public CustomLinkedListImpl() {
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void addFirst(E e) {
        if (isFirstAdd()) {
            addFirstEver(e);
        } else {
            addAsHead(e);
        }
        size++;
    }

    private void addAsHead(E e) {
        Node<E> old = head;
        Node<E> newNode = new Node<>(null, e, old);
        old.prev = newNode;
        head = newNode;
    }

    private void addFirstEver(E e) {
        Node<E> node = new Node<>(null, e, null);
        head = node;
        tail = node;
    }

    private boolean isFirstAdd() {
        return Objects.isNull(head) && Objects.isNull(tail);
    }

    @Override
    public void addLast(E e) {
        if (isFirstAdd()) {
            addFirstEver(e);
        } else {
            addAsTail(e);
        }
        size++;
    }

    private void addAsTail(E e) {
        Node<E> old = tail;
        Node<E> newNode = new Node<>(old, e, null);
        old.next = newNode;
        tail = newNode;
    }

    @Override
    public void add(int index, E e) {

        if (index < 0 || index > size) {
            throw new IllegalArgumentException("Incorrect index provided: " + index);
        }

        if (size == 0) {
            addFirstEver(e);
        } else if (index == 0) {
            addAsHead(e);
        } else if (index == size) {
            addAsTail(e);
        } else {
            Node<E> prev = iterateToElementByIndex(index - 1);
            Node<E> next = prev.next;
            Node<E> newNode = new Node<>(prev, e, next);
            prev.next = newNode;
            next.prev = newNode;
        }

        size++;
    }

    private Node<E> iterateToElementByIndex(int index) {
        return size / 2 >= index ? iterateToIndexForward(index) : iterateToIndexBackward(index);
    }

    @Override
    public E getFirst() {
        Node<E> first = getHeadNode();
        return first.element;
    }

    private Node<E> getHeadNode() {
        Node<E> first = head;
        if (first == null) {
            throw new NoSuchElementException("The requested element doesn't exist");
        }
        return first;
    }

    @Override
    public E getLast() {
        Node<E> last = getTailNode();
        return last.element;
    }

    private Node<E> getTailNode() {
        Node<E> last = tail;
        if (last == null) {
            throw new NoSuchElementException("The requested element doesn't exist");
        }
        return last;
    }

    @Override
    public E get(int index) {
        checkIndex(index);
        Node<E> received = iterateToIndexForward(index);
        return received.element;
    }

    private Node<E> iterateToIndexForward(int index) {
        int counter = 0;
        Node<E> current = head;
        while (counter != index) {
            current = current.next;
            counter++;
        }
        return current;
    }

    private Node<E> iterateToIndexBackward(int index) {
        int counter = size - 1;
        Node<E> current = tail;
        while (counter != index) {
            current = current.prev;
            counter--;
        }
        return current;
    }

    private void checkIndex(int index) {
        if (index < 0 || index > size - 1) {
            throw new IllegalArgumentException("Incorrect index provided: " + index);
        }
    }

    @Override
    public E removeFirst() {
        Node<E> toRemove = getHeadNode();
        Node<E> newHead = toRemove.next;
        newHead.prev = null;
        head = newHead;
        size--;
        return toRemove.element;
    }

    @Override
    public E removeLast() {
        Node<E> toRemove = getTailNode();
        Node<E> newTail = toRemove.prev;
        newTail.next = null;
        tail = newTail;
        size--;
        return toRemove.element;
    }

    @Override
    public E remove(int index) {
        checkIndex(index);
        Node<E> toRemove = iterateToElementByIndex(index);
        Node<E> prior = toRemove.prev;
        Node<E> after = toRemove.next;
        if (prior != null) {
            prior.next = after;
        }
        if (after != null) {
            after.prev = prior;
        }

        size--;
        return toRemove.element;
    }

    @Override
    public String toString() {
        return Stream.iterate(head, Objects::nonNull, n -> n.next)
                     .map(n -> n.element.toString())
                     .collect(Collectors.joining(", ", "[", "]"));
    }

    private static class Node<E> {

        private final E element;

        private Node<E> next;

        private Node<E> prev;

        public Node(Node<E> prev, E element, Node<E> next) {
            this.prev = prev;
            this.element = element;
            this.next = next;
        }
    }
}
