package ui;

import java.util.*;

public class HelperFunctions {

    // success rate function between classification predictions and real classification
    public static Double accuracy(List<String> predictions, List<String> real){
        int total = real.size();
        int correct = 0;

        for(int i = 0; i < real.size(); i++){
            if(predictions.get(i).equals(real.get(i))){
                correct++;
            }
        }
        return correct*1.0/total;
    }

    // confusion matrix function for foreseen and real classifications
    public static void confusion_matrix(List<String> predictions, List<String> real, Set<String> Y){
        // sorting class values alphabetically
        List<String> yList = new ArrayList<>(Y);
        yList.sort(String::compareTo);

        String[][] matrix = new String[Y.size()][Y.size()];
        // list of all pairs between real and foreseen values
        List<String> comb = new ArrayList<>();
        for(int i = 0; i < predictions.size(); i++){
            comb.add(String.join(",", real.get(i), predictions.get(i)));
        }

        for(int i = 0; i < Y.size(); i++){
            for(int j = 0; j < Y.size(); j++){
                matrix[i][j] = String.join(",", yList.get(i), yList.get(j));
            }
        }

        for(int i = 0; i < Y.size(); i++){
            for(int j = 0; j < Y.size(); j++){
                final String el = matrix[i][j];
                int count = (int)comb.stream().filter(s -> s.equals(el)).count();
                matrix[i][j] = String.valueOf(count);
            }
        }

        // print matrix
        for(int i = 0; i < Y.size(); i++){
            for(int j = 0; j < Y.size(); j++){
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }
    }
}