package org.ultraminereborned.concurrent;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ThreadSafeListWrapper<T> implements List<T> {
    private final ConcurrentDoublyLinkedList<T> wrapper;

    public ThreadSafeListWrapper(List<T> wrapper) {
        this.wrapper = new ConcurrentDoublyLinkedList<>(wrapper);
    }
    public ThreadSafeListWrapper(Collection<T> wrapper) {
        this.wrapper = new ConcurrentDoublyLinkedList<>(wrapper);
    }

    @Override
    public int size() {
        return wrapper.size();
    }

    @Override
    public boolean isEmpty() {
        return wrapper.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return wrapper.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return wrapper.iterator();
    }

    @Override
    public Object[] toArray() {
        return wrapper.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return wrapper.toArray(a);
    }

    @Override
    public boolean add(T t) {
        return wrapper.add(t);
    }

    @Override
    public boolean remove(Object o) {
        return wrapper.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return wrapper.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return wrapper.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        return wrapper.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return wrapper.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return wrapper.removeAll(c);
    }

    @Override
    public void clear() {
        wrapper.clear();
    }

    @Override
    public T get(int index) {
        return wrapper.get(index);
    }

    @Override
    public T set(int index, T element) {
        return wrapper.set(index, element);
    }

    @Override
    public void add(int index, T element) {
        wrapper.add(index, element);
    }

    @Override
    public T remove(int index) {
        return wrapper.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return wrapper.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return wrapper.lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return wrapper.listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return wrapper.listIterator(index);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return wrapper.subList(fromIndex, toIndex);
    }
}