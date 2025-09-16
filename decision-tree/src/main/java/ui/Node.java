package ui;

import java.util.*;

// structures used in ID3 algorithm(used to generate decision tree)
class Pair{
    String value;
    Node node;

    Pair(String value, Node node){
        this.value = value;
        this.node = node;
    }
}

public class Node {
    private boolean leaf;
    private String label;
    private List<Pair> branches = new ArrayList<Pair>();
    private String mostOftenY;

    // list making
    public Node(String value, boolean leaf) {
        this.label = value;
        this.leaf = leaf;
    }

    // node making
    public Node(List<Pair> branches, String label, boolean leaf, String mostOftenY) {
        this.branches = branches;
        this.label = label;
        this.leaf = leaf;
        this.mostOftenY = mostOftenY;
    }

    public List<Pair> getBranches() {
        return branches;
    }

    public String getLabel() {
        return label;
    }

    public boolean isLeaf() {
        return leaf;
    }

    public String getMostOftenY() {
        return mostOftenY;
    }
}