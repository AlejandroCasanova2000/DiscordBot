package Queue;

public class Node<T> {
    private T content;
    private Node<T> next;

    public Node(T content) {
        this.content = content;
        this.next = null;
    }

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }

    public Node<T> getNext() {
        return next;
    }

    public void setNext(Node<T> next) {
        this.next = next;
    }
}
