package ui;

import java.io.File;
import java.io.IOException;														
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Solution {
	static Node s0;
	static List<String> goal = new ArrayList<>();
	static Map<String, List<String[]>> succ = new TreeMap<>();
	static Map<String, Double> heurist = new TreeMap<>();
	static Stack<Node> path = new Stack<>();
	static Set<Node> closed = new HashSet<>();

	// BFS algorithm, breadth-first search
	public static void BFS() throws UnsupportedEncodingException {
		boolean found = false;
		Queue<Node> open = new ArrayDeque<>();
		open.add(s0);

		while(!open.isEmpty()){
			Node s = open.remove();
			closed.add(s);
			if(goal.contains(s.getState())){
				found = true;
				findPath(s);
				break;
			}

			for(String[] state : succ.get(s.getState())){
				if(!closed.contains(new Node(state[0], (Double)0.0, null))){
					open.add(new Node(state[0], (Double)Double.parseDouble(state[1]), s));
				}
			}
		}

		// print solution
		System.out.println("# BFS");
		printFunc(found, "bfs");
	}

	// UCS algorithm, uniform-cost search
	public static void UCS(String check) throws UnsupportedEncodingException {
		boolean found = false;
		// unlike bfs, here the list is a priority queue and adds sorted by the cost of the path to that node
		PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingDouble(n -> n.getCost()));
		open.add(s0);

		while(!open.isEmpty()){
			Node s = open.remove();
			closed.add(s);
			if(goal.contains(s.getState())){
				found = true;
				findPath(s);
				break;
			}

			for(String[] state : succ.get(s.getState())){
				if(!closed.contains(new Node(state[0], (Double)0.0, null))){
					Node newNode = new Node(state[0], (Double)(Double.parseDouble(state[1]) + s.getCost()), s);
					open.add(newNode);
				}
			}
		}

		// print solution, if not already called from check func
		if(check.equals("")){
			System.out.println("# UCS");
			printFunc(found, "ucs");
		}
	}

	// ASTAR algorithm, heuristic search
	public static void ASTAR(String h_path) throws UnsupportedEncodingException {
		boolean found = false;
		PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingDouble(n -> n.getFvalue()));
		open.add(s0);

		while(!open.isEmpty()){
			Node s = open.remove();
			closed.add(s);
			if(goal.contains(s.getState())){
				found = true;
				findPath(s);
				break;
			}

			String parentState = s.getState();
			for(String[] childState : succ.get(parentState)){
				Double childCost = (Double) (Double.parseDouble(childState[1]) + s.getCost());
				// remove from list, if nodes have same state and new node cheaper total path
				boolean exists1 = closed.stream().anyMatch(n -> n.getState().equals(childState[0]));
				boolean exists2 = open.stream().anyMatch(n -> n.getState().equals(childState[0]));
				boolean removed1 = closed.removeIf(n -> n.getState().equals(childState[0]) && n.getCost() > childCost);
				boolean removed2 = open.removeIf(n -> n.getState().equals(childState[0]) && n.getCost() > childCost);
				if((exists1 && !removed1) || (exists2 && !removed2)){
				} else {
					Node newNode = new Node(childState[0], childCost, s, (Double)(childCost + heurist.get(childState[0])));
					open.add(newNode);
				}
			}
		}

		System.out.println("# A-STAR " + h_path);
		printFunc(found, "astar");
	}

	// optimism check function
	public static void checkOptimistic(String h_path) throws UnsupportedEncodingException {
		AtomicInteger i = new AtomicInteger();
		System.out.println("# HEURISTIC-OPTIMISTIC " + h_path);
        heurist.forEach((k, v) -> {
			s0 = new Node(k, (Double)0.0, null);
			path.clear();
			closed.clear();
            try {
				// ucs, so "path" variable has optimal solution (every state's real path cost)
                UCS("check");
            } catch (UnsupportedEncodingException e) {}
			// heuristic is optimal if for every state expression h(s) <= h*(s) is TRUE
            Double h_star = path.get(0).getCost();
			if (h_star >= v) {
				System.out.println("[CONDITION]: [OK] h(" + k + ") <= h*: " + v + " <= " + h_star);
			} else {
				System.out.println("[CONDITION]: [ERR] h(" + k + ") <= h*: " + v + " <= " + h_star);
				i.getAndIncrement();
			}
		});

        if(i.get() > 0)
			System.out.println("[CONCLUSION]: Heuristic is not optimistic.");
		else
			System.out.println("[CONCLUSION]: Heuristic is optimistic.");
	}

	// consistency check function
	public static void checkConsistent(String h_path) {
		System.out.println("# HEURISTIC-CONSISTENT " + h_path);

		AtomicInteger i = new AtomicInteger();
		succ.forEach((k, v) -> {
			for(String[] node: v){
				// heuristic is consistent if for every state expression h(parent) <= h(child) + c is TRUE
				if (heurist.get(k) <= heurist.get(node[0]) + Double.parseDouble(node[1])) {
					System.out.println("[CONDITION]: [OK] h(" + k + ") <= h(" + node[0] + ") + c: " + heurist.get(k) + " <= " + heurist.get(node[0]) + " + " + Double.parseDouble(node[1]));
				} else {
					System.out.println("[CONDITION]: [ERR] h(" + k + ") <= h(" + node[0] + ") + c: " + heurist.get(k) + " <= " + heurist.get(node[0]) + " + " + Double.parseDouble(node[1]));
					i.getAndIncrement();
				}
			}
		});
		if (i.get() > 0)
			System.out.println("[CONCLUSION]: Heuristic is not consistent.");
		else
			System.out.println("[CONCLUSION]: Heuristic is consistent.");
	}

	// path reconstruction function
	public static void findPath(Node node){
		while(node != null){
			path.push(node);
			node = node.getParent();
		}
		return;
	}

	// function for sorting target states of state iterating through
	public static List<String[]> sortValues(List<String[]> values){
		List<String[]> sorted = values.stream().sorted((i, j) -> i[0].compareTo(j[0])).collect(Collectors.toList());
		return sorted;
	}

	// print function
	public static void printFunc(boolean found, String alg) throws UnsupportedEncodingException {
		if(found){
			System.out.println("[FOUND_SOLUTION]: yes");
			System.out.println("[STATES_VISITED]: " + closed.size());
			System.out.println("[PATH_LENGTH]: " + path.size());
			if(alg.equals("bfs"))
				System.out.println("[TOTAL_COST]: " + path.stream().map(n -> n.getCost()).mapToDouble(Double::doubleValue).sum());
			else
				System.out.println("[TOTAL_COST]: " + path.get(0).getCost());

			System.out.print("[PATH]: " );
			while (!path.isEmpty()) {
				String p = path.pop().getState();
				if(path.isEmpty())
					System.out.print(p);
				else
					System.out.print(p + " => ");
			}
		} else {
			System.out.println("[FOUND_SOLUTION]: no");
		}
	}

	public static void main(String... args) throws IOException {
		// to solve problem with encoding utf-8 symbols when printing on stdout
		// croatian diacritical letters
		System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));

		// input parameters
		String alg = "", state_path = "", h_path = "";
		boolean h_opt = false;
		boolean h_cons = false;
		boolean err = false;

		for (int i = 0; i < args.length; i++) {
			switch (args[i]) {
				case "--alg":
					alg = args[i + 1];
					i++;
					break;
				case "--ss":
					state_path = args[i + 1];
					i++;
					break;
				case "--h":
					h_path = args[i + 1];
					i++;
					break;
				case "--check-optimistic":
					h_opt = true;
					break;
				case "--check-consistent":
					h_cons = true;
					break;
				default:
					err = true;
			}
		}

		// parameters format check
		if (err) {
			System.out.println("Parameters are invalid.");
			return;
		}
		if (!alg.equals("")) {
			if (alg.equals("astar") && (state_path.equals("") || h_path.equals(""))) {
				System.out.println("Parameters are invalid.");
				return;
			} else if ((!(alg.equals("bfs") || alg.equals("ucs")) || state_path.equals("")) && !alg.equals("astar")) {
				System.out.println("Parameters are invalid. 3");
				return;
			}
		} else {
			if (state_path.equals("") || h_path.equals("") || !(h_opt || h_cons)) {
				System.out.println("Parameters are invalid.");
				return;
			}
		}

		// loading state space from files
		File file = new File(state_path);
		List<String> list = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
		list.removeIf(l -> l.startsWith("#"));
		s0 = new Node(list.get(0), (Double)0.0, null);
		String[] tmpGoal = list.get(1).split(" ");
		for(String s : tmpGoal)
			goal.add(s);
		// parsing rules for every state transitions
		for (int i = 2; i < list.size(); i++) {
			String key = list.get(i).split(":")[0];
			if(list.get(i).contains(",")) {
				String all = list.get(i).split(":")[1].stripLeading();
				String[] ones = all.split(" ");
				List<String[]> valuesTmp = new ArrayList<>();
				for (String one : ones) {
					valuesTmp.add(one.split(","));
				}
				// sort all transitions of state "key"
				List<String[]> values = sortValues(valuesTmp);
				succ.put(key, values);
			}
		}

		// loading state's heuristics from files
		if(!h_path.equals("")) {
			File fileH = new File(h_path);
			List<String> listH = Files.readAllLines(fileH.toPath(), StandardCharsets.UTF_8);
			listH.removeIf(l -> l.startsWith("#"));
			for(String s : listH) {
				String[] tmp = s.split(":");
				heurist.put(tmp[0], (Double)Double.parseDouble(tmp[1]));
			}
		}

		// determine operation by input parameters
		if(!alg.equals("")) {
			switch(alg){
				case "astar":
					ASTAR(h_path);
					break;
				case "bfs":
					BFS();
					break;
				case "ucs":
					UCS("");
			}
		} else {
			if(h_opt) checkOptimistic(h_path);
			else if(h_cons) checkConsistent(h_path);
			else System.out.println("Something went wrong.");
		}

		return;
	}
}
