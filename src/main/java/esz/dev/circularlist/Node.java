package esz.dev.circularlist;

public class Node<T> {
    private Node<T> previousNode;
    private Node<T> nextNode;
    private T content;

    public Node() {}

    public Node(T content) {
        this.content = content;
    }

    public void setPreviousNode(Node<T> previousNode) {
        this.previousNode = previousNode;
    }

    public Node<T> getPreviousNode() {
        return previousNode;
    }

    public void setNextNode(Node<T> nextNode) {
        this.nextNode = nextNode;
    }

    public Node<T> getNextNode() {
        return nextNode;
    }

    public void setContent(T content) {
        this.content = content;
    }

    public T getContent() {
        return content;
    }
}
