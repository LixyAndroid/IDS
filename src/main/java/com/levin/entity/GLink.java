package com.levin.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GLink<T> implements Iterable {

    public Node head = null;
    public Node tail = null;

    public int size;

    public class Node {
        Node next = null;
        Node previous = null;
        public T data;

        public Node(T data) {
            this.data = data;
        }

        @Override
        public String toString() {
            return "data:" + data;
        }
    }

    public GLink() {

    }

    public GLink(T... data) {
        for (T t : data) {
            addNode(t);
        }
    }

    public void addNode(T d) {
        Node newNode = new Node(d);
        if (head == null) {
            head = newNode;
            tail = newNode;
            size = 1;
            return;
        }

        Node tailTmp = tail;
        tailTmp.next = newNode;
        newNode.previous = tailTmp;
        tail = newNode;
        size++;
    }

    public void addNode(T d, int index) {
        if (index < 0 || index > size) {
            System.out.println("数组越界");
            return;
        }

        Node node = new Node(d);
        if (index == 0) {
            node.next = head;
            node.next.previous = node;
            head = node;
        } else if (index == size) {
            node.previous = tail;
            node.previous.next = node;
            tail = node;
        } else {
            Node currentNode = getNode(index);
            node.next = currentNode;
            node.previous = currentNode.previous;
            node.previous.next = node;
            node.next.previous = node;
        }

        size++;
    }

    public void exchange(T d1, T d2) {
        int idx1 = idx(d1);
        int idx2 = idx(d2);

        replaceNode(d1, idx2);
        replaceNode(d2, idx1);
    }

    public void replaceNode(T d, int idx) {
        Node currentNode = getNode(idx);
        currentNode.data = d;
    }

    public Node getHead() {
        return getNode(0);
    }

    public Node getNode(int index) {
        if (index == 0) {
            return head;
        }
        if (index + 1 == size) {
            return tail;
        }
        int i = 1;
        Node curNode = head.next;
        while (curNode != null) {
            if (i == index) {
                return curNode;
            }
            curNode = curNode.next;
            i++;
        }
        return null;
    }

    public int idx(T d) {
        Node p = head;
        int idx = 0;
        while (p.next != null) {
            if (p.data.equals(d)) {
                return idx;
            }
            p = p.next;
            idx++;
        }
        return -1;
    }

    public Node removeNode(T d) {
        Node p = head;
        while (p.next != null) {
            if (p.data.equals(d)) {
                removeNode(p);
                return p;
            }
            p = p.next;
        }
        return null;
    }

    public Node removeNode(Node remove) {
        Node previous = remove.previous;
        Node next = remove.next;
        if (remove.equals(head)) {
            next.previous = null;
            head = next;
        } else if (remove.equals(tail)) {
            previous.next = null;
            tail = previous;
        } else {
            previous.next = next;
            next.previous = previous;
        }
        size--;
        return remove;
    }

    public int size() {
        return size;
    }

    public void print() {
        Node p = head;
        while (p != null) {
            System.out.println(p.data.toString());
            p = p.next;
        }
    }

    public List<T> toArray() {
        List<T> result = new ArrayList<>();
        Node p = head;
        while (p != null) {
            result.add(p.data);
            p = p.next;
        }
        return result;
    }


    @Override
    public Iterator iterator() {
        return new Iterator() {
            int cursor;
            int lastRet = -1;

            @Override
            public boolean hasNext() {
                return cursor != size;
            }

            @Override
            public Object next() {
                lastRet = cursor;
                cursor++;
                return getNode(lastRet);
            }
        };
    }

}