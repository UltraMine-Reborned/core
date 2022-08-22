package org.ultraminereborned.concurrent;

import org.apache.commons.lang3.NotImplementedException;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class ConcurrentDoublyLinkedList<E> extends AbstractCollection<E> implements List<E>, java.io.Serializable {
    private static boolean usable(Node<?> n) {
        return n != null && !n.isSpecial();
    }

    private static void checkNullArg(Object v) {
        if (v == null)
            throw new NullPointerException();
    }

    private E screenNullResult(E v) {
        if (v == null)
            throw new NoSuchElementException();
        return v;
    }

    private ArrayList<E> toArrayList() {
        ArrayList<E> c = new ArrayList<>();
        for (Node<E> n = header.forward(); n != null; n = n.forward())
            c.add(n.element);
        return c;
    }

    private static final long serialVersionUID = 876323262645176354L;
    private final Node<E> header;
    private final Node<E> trailer;

    public ConcurrentDoublyLinkedList() {
        Node<E> h = new Node<>(null, null, null);
        Node<E> t = new Node<>(null, null, h);
        h.setNext(t);
        header = h;
        trailer = t;
    }

    public ConcurrentDoublyLinkedList(Collection<? extends E> c) {
        this();
        addAll(c);
    }

    public void addFirst(E o) {
        checkNullArg(o);
        while (header.append(o) == null)
            ;
    }

    public void addLast(E o) {
        checkNullArg(o);
        while (trailer.prepend(o) == null)
            ;
    }

    public boolean offerFirst(E o) {
        addFirst(o);
        return true;
    }

    public boolean offerLast(E o) {
        addLast(o);
        return true;
    }

    public E peekFirst() {
        Node<E> n = header.successor();
        return (n == null) ? null : n.element;
    }

    public E peekLast() {
        Node<E> n = trailer.predecessor();
        return (n == null) ? null : n.element;
    }

    public E getFirst() {
        return screenNullResult(peekFirst());
    }

    public E getLast() {
        return screenNullResult(peekLast());
    }

    public E pollFirst() {
        for (;;) {
            Node<E> n = header.successor();
            if (!usable(n))
                return null;
            if (n.delete())
                return n.element;
        }
    }

    public E pollLast() {
        for (;;) {
            Node<E> n = trailer.predecessor();
            if (!usable(n))
                return null;
            if (n.delete())
                return n.element;
        }
    }

    public E removeFirst() {
        return screenNullResult(pollFirst());
    }

    public E removeLast() {
        return screenNullResult(pollLast());
    }

    // *** Queue and stack methods ***
    public boolean offer(E e) {
        return offerLast(e);
    }

    public boolean add(E e) {
        return offerLast(e);
    }

    public E poll() {
        return pollFirst();
    }

    public E remove() {
        return removeFirst();
    }

    public E peek() {
        return peekFirst();
    }

    public E element() {
        return getFirst();
    }

    public void push(E e) {
        addFirst(e);
    }

    public E pop() {
        return removeFirst();
    }

    public boolean removeFirstOccurrence(Object o) {
        checkNullArg(o);
        for (;;) {
            Node<E> n = header.forward();
            for (;;) {
                if (n == null)
                    return false;
                if (o.equals(n.element)) {
                    if (n.delete())
                        return true;
                    else
                        break;
                }
                n = n.forward();
            }
        }
    }

    public boolean removeLastOccurrence(Object o) {
        checkNullArg(o);
        for (;;) {
            Node<E> s = trailer;
            for (;;) {
                Node<E> n = s.back();
                if (s.isDeleted() || (n != null && n.successor() != s))
                    break;
                if (n == null)
                    return false;
                if (o.equals(n.element)) {
                    if (n.delete())
                        return true;
                    else
                        break;
                }
                s = n;
            }
        }
    }

    public boolean contains(Object o) {
        if (o == null)
            return false;
        for (Node<E> n = header.forward(); n != null; n = n.forward())
            if (o.equals(n.element))
                return true;
        return false;
    }

    public boolean isEmpty() {
        return !usable(header.successor());
    }

    public int size() {
        long count = 0;
        for (Node<E> n = header.forward(); n != null; n = n.forward())
            ++count;
        return (count >= Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) count;
    }

    public boolean remove(Object o) {
        return removeFirstOccurrence(o);
    }

    public boolean addAll(Collection<? extends E> c) {
        Iterator<? extends E> it = c.iterator();
        if (!it.hasNext())
            return false;
        do {
            addLast(it.next());
        } while (it.hasNext());
        return true;
    }

    public void clear() {
        while (pollFirst() != null)
            ;
    }

    public Object[] toArray() {
        return toArrayList().toArray();
    }

    public <T> T[] toArray(T[] a) {
        return toArrayList().toArray(a);
    }

    public Iterator<E> iterator() {
        return new CLDIterator();
    }

    final class CLDIterator implements Iterator<E> {
        Node<E> last;
        Node<E> next = header.forward();
        public boolean hasNext() {
            return next != null;
        }
        public E next() {
            Node<E> l = last = next;
            if (l == null)
                throw new NoSuchElementException();
            next = next.forward();
            return l.element;
        }
        public void remove() {
            Node<E> l = last;
            if (l == null)
                throw new IllegalStateException();
            while (!l.delete() && !l.isDeleted())
                ;
        }
    }
    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        throw new NotImplementedException("TODO");
    }
    @Override
    public E get(int index) {
        Node<E> current = header.successor();
        if (current == null) {
            throw new IndexOutOfBoundsException();
        }
        for (; index > 0; index --) {
            current = current.successor();
            if (current == null) {
                throw new IndexOutOfBoundsException();
            }
        }
        return current.element;
    }
    @Override
    public E set(int index, E element) {
        throw new NotImplementedException("INVALID");
    }
    @Override
    public void add(int index, E element) {
        throw new NotImplementedException("INVALID");
    }
    @Override
    public E remove(int index) {
        throw new NotImplementedException("INVALID");
    }
    @Override
    public int indexOf(Object o) {
        throw new NotImplementedException("INVALID");
    }
    @Override
    public int lastIndexOf(Object o) {
        throw new NotImplementedException("INVALID");
    }
    @Override
    public ListIterator<E> listIterator() {
        throw new NotImplementedException("INVALID");
    }
    @Override
    public ListIterator<E> listIterator(int index) {
        throw new NotImplementedException("INVALID");
    }
    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        throw new NotImplementedException("INVALID");
    }
}
class Node<E> extends AtomicReference<Node<E>> {
    private static final long serialVersionUID = 6640557564507962862L;
    private volatile Node<E> prev;
    final E element;
    Node(E element, Node<E> next, Node<E> prev) {
        super(next);
        this.prev = prev;
        this.element = element;
    }
    Node(Node<E> next) {
        super(next);
        this.prev = this;
        this.element = null;
    }
    private Node<E> getNext() {
        return get();
    }
    void setNext(Node<E> n) {
        set(n);
    }
    private boolean casNext(Node<E> cmp, Node<E> val) {
        return compareAndSet(cmp, val);
    }
    private Node<E> getPrev() {
        return prev;
    }
    void setPrev(Node<E> b) {
        prev = b;
    }
    boolean isSpecial() {
        return element == null;
    }
    boolean isTrailer() {
        return getNext() == null;
    }
    boolean isHeader() {
        return getPrev() == null;
    }
    boolean isMarker() {
        return getPrev() == this;
    }
    boolean isDeleted() {
        Node<E> f = getNext();
        return f != null && f.isMarker();
    }
    private Node<E> nextNonmarker() {
        Node<E> f = getNext();
        return (f == null || !f.isMarker()) ? f : f.getNext();
    }
    Node<E> successor() {
        Node<E> f = nextNonmarker();
        for (;;) {
            if (f == null)
                return null;
            if (!f.isDeleted()) {
                if (f.getPrev() != this && !isDeleted())
                    f.setPrev(this); // relink f's prev
                return f;
            }
            Node<E> s = f.nextNonmarker();
            if (f == getNext())
                casNext(f, s); // unlink f
            f = s;
        }
    }
    private Node<E> findPredecessorOf(Node<E> target) {
        Node<E> n = this;
        for (;;) {
            Node<E> f = n.successor();
            if (f == target)
                return n;
            if (f == null)
                return null;
            n = f;
        }
    }
    Node<E> predecessor() {
        Node<E> n = this;
        for (;;) {
            Node<E> b = n.getPrev();
            if (b == null)
                return n.findPredecessorOf(this);
            Node<E> s = b.getNext();
            if (s == this)
                return b;
            if (s == null || !s.isMarker()) {
                Node<E> p = b.findPredecessorOf(this);
                if (p != null)
                    return p;
            }
            n = b;
        }
    }
    Node<E> forward() {
        Node<E> f = successor();
        return (f == null || f.isSpecial()) ? null : f;
    }
    Node<E> back() {
        Node<E> f = predecessor();
        return (f == null || f.isSpecial()) ? null : f;
    }
    Node<E> append(E element) {
        for (;;) {
            Node<E> f = getNext();
            if (f == null || f.isMarker())
                return null;
            Node<E> x = new Node<>(element, f, this);
            if (casNext(f, x)) {
                f.setPrev(x); // optimistically link
                return x;
            }
        }
    }
    Node<E> prepend(E element) {
        for (;;) {
            Node<E> b = predecessor();
            if (b == null)
                return null;
            Node<E> x = new Node<>(element, this, b);
            if (b.casNext(this, x)) {
                setPrev(x);
                return x;
            }
        }
    }
    boolean delete() {
        Node<E> b = getPrev();
        Node<E> f = getNext();
        if (b != null && f != null && !f.isMarker() && casNext(f, new Node<>(f))) {
            if (b.casNext(this, f))
                f.setPrev(b);
            return true;
        }
        return false;
    }
    Node<E> replace(E newElement) {
        for (;;) {
            Node<E> b = getPrev();
            Node<E> f = getNext();
            if (b == null || f == null || f.isMarker())
                return null;
            Node<E> x = new Node<>(newElement, f, b);
            if (casNext(f, new Node<>(x))) {
                b.successor();
                x.successor();
                return x;
            }
        }
    }
}
