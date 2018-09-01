package esz.dev.circularlist;

public class CircularList<T> {
    private Node<T> head;
    private Node<T> current;

    public CircularList() {}

    public Node<T> getHead() {
        return head;
    }

    public Node<T> getCurrent() {
        return current;
    }

    public void next() {
        current = current.getNextNode();
    }

    public void previous() {
        current = current.getPreviousNode();
    }

    public void reset() {
        current = head;
    }

    public void addLast(T content) {
        Node<T> node = new Node<>(content);
        if (head == null) {
            node.setPreviousNode(node);
            node.setNextNode(node);
            head = node;
        } else {
            Node lastNode = head.getPreviousNode();
            lastNode.setNextNode(node);
            head.setPreviousNode(node);
        }
    }

    public void addFirst(T content) {
        Node<T> node = new Node<>(content);
        if (head == null) {
            node.setNextNode(node);
            node.setPreviousNode(node);
        } else {
            node.setNextNode(head);
            node.setPreviousNode(head.getPreviousNode());
        }
        head = node;
    }

    public void addBefore(T content, Node<T> node) {
        Node<T> newNode = new Node<>(content);
        newNode.setPreviousNode(node.getPreviousNode());
        newNode.setNextNode(node);
        node.setPreviousNode(newNode);
    }

    public void addAfter(T content, Node<T> node) {
        Node<T> newNode = new Node<>(content);
        newNode.setPreviousNode(node);
        newNode.setNextNode(node.getNextNode());
        node.setNextNode(newNode);
    }
}
