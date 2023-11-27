public class Node implements Comparable<Node> {
    public Character character;
    public int frequency;
    public Node leftChild;
    public Node rightChild;

    public Node(Character character, int frequency) {
        this.character = character;
        this.frequency = frequency;
    }

    public Node(Character character, int frequency, Node left, Node right) {
        this(character, frequency);
        leftChild = left;
        rightChild = right;
    }

    public boolean isLeaf() {
        return (leftChild == null) && (rightChild == null);
    }

    @Override
    public int compareTo(Node o) {
        return frequency - o.frequency;
    }
}