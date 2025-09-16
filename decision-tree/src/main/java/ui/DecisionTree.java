package ui;

import java.lang.Math;		       
import java.math.BigDecimal;        
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

public class DecisionTree {
    static int depth = -1;
    private static Map<String, List<String>> V_x = new LinkedHashMap<>();
    private Node model;

    public DecisionTree() {}
    public DecisionTree(int depth) {
        this.depth = depth;
    }

    public void printBranches() {
        System.out.println("[BRANCHES]:");
        printBranchesR(model, new ArrayList<>(), 1);
    }

    private void printBranchesR(Node node, List<String> branch, int depth) {
        if (node == null) return;

        // print branch if node is leaf
        if (node.isLeaf()) {
            System.out.println(String.join(" ", branch) + " " + node.getLabel());
            return;
        }

        for (Pair pair : node.getBranches()) {
            // add new branch to leaf
            List<String> newBranch = new ArrayList<>(branch);
            newBranch.add(depth + ":" + node.getLabel() + "=" + pair.value);

            // recursive descent through "tree"
            printBranchesR(pair.node, newBranch, depth + 1);
        }
    }


    public void setV_x(String x, List<String> values) {
        V_x.put(x, values);
    }

    // model training - building decision tree
    public void fit(List<List<String>> D, List<String> X, String Y){
        model = ID3(D, D, X, Y, this.depth);
        printBranches();
    }

    // predict target label base on trained model
    public List<String> predict(List<List<String>> D, List<String> X){
        boolean done = false;
        List<String> predictions = new ArrayList<>();

        for(List<String> list : D){
            Node node = model;
            List<String> pr = new ArrayList<>(list);
            done = false;
            while (!node.isLeaf()) {
                boolean found = false;
                for (Pair pair : node.getBranches()) {
                    if (pr.get(X.indexOf(node.getLabel())).equals(pair.value)) {
                        node = pair.node;
                        found = true;
                        break;
                    }
                }
                if (!found){
                    done = true;
                    predictions.add(node.getMostOftenY());
                    break;
                }
            }

            if (node.isLeaf() && !done) {
                predictions.add(node.getLabel());
            }
        }
        return predictions;
    }

    // id3 algorithm in machine learning - building decision tree
    public static Node ID3(List<List<String>> D, List<List<String>> Dparent, List<String> X, String Y, int d){
        if(D.isEmpty()){
            Map<String, Double> freq = relativeFrequency(Dparent, Dparent.get(0).size() - 1);
            String y = argmax(freq);
            return new Node(y, true);
        }

        Map<String, Double> freq = relativeFrequency(D, D.get(0).size() - 1);
        final String y = argmax(freq);
        List<List<String>> Dv = D.stream()
                                 .filter(l -> l.get(l.size() - 1).equals(y))
                                 .collect(Collectors.toList());
        List<String> tmpX = X.stream()
                             .filter(s -> !s.equals(("-")))
                             .collect(Collectors.toList());
        if(tmpX.isEmpty() || D.equals(Dv) || d == 0){
            return new Node(y, true);
        }

        Map<String, Double> IGs = new HashMap<>();
        for(int i = 0; i < X.size(); i++){
            if(!X.get(i).equals("-"))
                IGs.put(X.get(i), IG(D, i, X.get(i)));
        }
        System.out.println();
        String Xmax = argmax(IGs);
        List<Pair> subtrees = new ArrayList<>();
        for(String values: V_x.get(Xmax)) {
            List<String> newX = X.stream()
                                 .map(s -> s.equals(Xmax) ? s = "-" : s)
                                 .collect(Collectors.toList());
            List<List<String>> newDv = D.stream()
                                       .filter(l -> l.get(X.indexOf(Xmax)).equals(values))
                                       .collect(Collectors.toList());
            Node node = ID3(newDv, D, newX, Y, d - 1);
            subtrees.add(new Pair(values, node));
        }

        return new Node(subtrees, Xmax, false, y);
    }

    // calculate information gain of feature n
    // IG(D, x) = E(D) - SUM_vx( (|Dx=v| / |D|) * E(Dx=v) ), vx is every value of x
    public static double IG(List<List<String>> D, int n, String characX){
        double size = D.size();
        double entropy = entropy(D);
        BigDecimal ig;
        double suma = 0.0;

        Map<String, Double> frekvX = relativeFrequency(D, n);
        for(Map.Entry<String, Double> entry : frekvX.entrySet()) {
            // filter examples where feature value = value of iteration
            List<List<String>> tmp = new ArrayList<>(D.stream()
                                                   .filter(l -> l.get(n).equals(entry.getKey()))
                                                   .collect(Collectors.toList()));
            suma += (entry.getValue() / size) * entropy(tmp);
        }

        ig = new BigDecimal(entropy - suma)
                .setScale(4, RoundingMode.HALF_UP);
        System.out.print("IG(" + characX + ")=" + ig + " ");
        return ig.doubleValue();
    }

    // calculate entropy for every class in D, set of data
    // E = - SUM_y'( P(y')*log2(P(y')) ), y' for each
    public static double entropy(List<List<String>> D) {
        double size = D.size();
        double entropy = 0.0;
        Map<String, Double> frekvY = relativeFrequency(D, D.get(0).size() - 1);

        // calculate entropy
        for(Map.Entry<String, Double> entry : frekvY.entrySet()) {
            entropy += (entry.getValue() / size) * log2(entry.getValue() / size);
        }
        entropy *= -1.0;

        return entropy;
    }

    // log2 function
    public static double log2(double x) {
        return Math.log(x) / Math.log(2);
    }

    // relative frequency for every value of features
    public static Map<String, Double> relativeFrequency(List<List<String>> D, int n){
        Map<String, Double> freq = new HashMap<>();

        for(List<String> x : D) {
            if(freq.containsKey(x.get(n))){
                double old = freq.get(x.get(n));
                double newValue = old + 1.0;
                freq.put(x.get(n), newValue);
            } else {
                freq.put(x.get(n), 1.0);
            }
        }
        return freq;
    }

    // returns argument with max value
    public static String argmax(Map<String, Double> map){
        List<String> max = new ArrayList<>();
        String maxString = "";
        double maxStringValue = -1.0;
        for(Map.Entry<String, Double> entry : map.entrySet()){
            if(entry.getValue() > maxStringValue){
                maxString = entry.getKey();
                maxStringValue = entry.getValue();
                max.clear();
                max.add(entry.getKey());
            }
        }

        for(Map.Entry<String, Double> entry : map.entrySet()){
            if(entry.getValue() == maxStringValue && !entry.getKey().equals(maxString))
                max.add(entry.getKey());
        }

        max.sort(String::compareTo);
        return max.get(0);
    }

}
