package ui;

import java.util.Objects;

// node structure used in state space search algorithms
public class Node {
    private String state;
    private Double cost;
    private Node parent;
    private Double f_value;

    public Node(String state, Double cost, Node parent){
        this.state = state;
        this.cost = cost;
        this.parent = parent;
    }

    public Node(String state, Double cost, Node parent, Double f_value){
        this.state = state;
        this.cost = cost;
        this.parent = parent;
        this.f_value = f_value;
    }

    public String getState(){
        return this.state;
    }

    public Double getCost(){
        return this.cost;
    }

    public Node getParent(){
        return this.parent;
    }

    public Double getFvalue(){
        return this.f_value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Node node = (Node) obj;
        return Objects.equals(state, node.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state);
    }
}