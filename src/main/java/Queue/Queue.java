package Queue;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public class Queue<T> {
    private Node<T> firstNode;
    private int size;

    public Queue() {
        this.firstNode = null;
        this.size = 0;
    }

    public Node<T> getFirstNode() {
        return firstNode;
    }

    public void setFirstNode(Node<T> firstNode) {
        this.firstNode = firstNode;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void push(T object) {
        if (size == 0) {
            firstNode = new Node<T>(object);
        } else if (size == 1) {
            firstNode.setNext(new Node<T>(object));
        } else {
            Node<T> puntero = firstNode;
            for (int i = 0; i < size; i++) {
                if (i == size - 1) {
                    puntero.setNext(new Node<T>(object));
                    break;
                } else {
                    puntero = puntero.getNext();
                }
            }
        }
        size++;
    }

    public T pop() {
        if (size > 1) {
            Node<T> aux = firstNode;
            firstNode = firstNode.getNext();
            size--;
            return aux.getContent();
        } else {
            Node<T> aux = firstNode;
            firstNode = null;
            size--;
            return aux.getContent();
        }
    }

    public T read(int index) {
        Node<T> puntero = this.getFirstNode();
        for(int i = 0; i < this.getSize(); i++) {
            if(i == index) return puntero.getContent();
            puntero = puntero.getNext();
        }
        return null;
    }
}
