package ui;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static ui.HelperFunctions.accuracy;
import static ui.HelperFunctions.confusion_matrix;

public class Solution {

	public static void main(String ... args) {
		DecisionTree dt;						// decision tree build model
		List<String> X = new ArrayList<>();		// possible features
		String Y = "";							// target label, only 1 with arbitrarily values
		List<List<String>> D = new ArrayList<>();		// learning data
		List<List<String>> DTest = new ArrayList<>();	// check data
		int depth = -1;									// decision tree depth, optional

		// checking parameters format
		try {
			if(!args[0].endsWith(".csv") || args[0].contains("test")){
				throw new Exception("Invalid first CSV file");
			} else if(!args[1].endsWith(".csv") || !args[1].contains("test")){
				throw new Exception("Invalid second CSV file");
			} else if(args.length > 2){
				depth = Integer.parseInt(args[2]);
				dt = new DecisionTree(depth);
			} else {
				dt = new DecisionTree();
			}
		} catch (Exception e){
			System.out.println("Error accured: " + e);
			return;
		}

		// load learning data
		File train_set = new File(args[0]);
		try(BufferedReader fileReader = new BufferedReader(new FileReader(train_set))){
			// read feature names, first line, header
			if(fileReader.ready()){
				String line = fileReader.readLine();
				String[] characteristics = line.split(",");
				for(int i = 0; i < characteristics.length - 1; i++)
					X.add(characteristics[i]);
				Y = characteristics[characteristics.length - 1];
			}
			while(fileReader.ready()){
				String line = fileReader.readLine();
				String[] tmp = line.split(",");
				D.add(Arrays.asList(tmp));
			}
		} catch (IOException e) {
			System.out.println("Error accured: " + e);
			return;
        }

		// determine different values of each feature
		List<Set<String>> listSets = new ArrayList<>();
		for(String x : X)
			listSets.add(new HashSet<>());
		for(List<String> x : D) {
			for(int i = 0; i < x.size() - 1; i++){
				listSets.get(i).add(x.get(i));
			}
		}
		for(int i = 0; i < X.size(); i++){
			List<String> list = new ArrayList<>(listSets.get(i));
			dt.setV_x(X.get(i), list);
		}

		// model training on learning set of data
		dt.fit(D, X, Y);

		// load data for prediction
		File predict_set = new File(args[1]);
		try(BufferedReader fileReader = new BufferedReader(new FileReader(predict_set))){
			// remove header
			if(fileReader.ready()){
				fileReader.readLine();
			}
			while(fileReader.ready()){
				String line = fileReader.readLine();
				String[] tmp = line.split(",");
				DTest.add(Arrays.asList(tmp));
			}
		} catch (IOException e) {
			System.out.println("Error accured: " + e);
			return;
		}

		// prediction check and print
		System.out.print("[PREDICTIONS]: ");
		List<String> response = dt.predict(DTest, X);
		for(String r : response){
			System.out.print(r + " ");
		}
		System.out.println();

		// check model's predictions accuracy
		List<String> correct = DTest.stream()
								    .map(l -> l.get(l.size() - 1))
								    .collect(Collectors.toList());
		Double acc = accuracy(response, correct);
		System.out.println("[ACCURACY]: " + String.format("%.5f", acc));

		// calculate and print confusion matrix
		Set<String> Y_values = DTest.stream().map(l -> l.get(l.size() - 1)).collect(Collectors.toSet());
		System.out.println("[CONFUSION_MATRIX]:");
		confusion_matrix(response, correct, Y_values);

        return;
	}
}