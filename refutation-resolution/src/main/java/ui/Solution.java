package ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class Solution {
    static List<String> real_start_clauses = new ArrayList<>();     // holds starting premises for later print
    static List<String> negative_goal_clauses = new ArrayList<>();  // negative target clauses
    static List<String> start_clauses = new ArrayList<>();          // premises clauses
    static List<String> SoS = new ArrayList<>();                    // Set of support, negative target clause and new ones
    static Map<String, String[]> used = new HashMap<>();            // checked pairs, key -> new clause, value -> parents
    static Set<Set<String>> done = new HashSet<>();                 // all checked pairs
    static String goal;                                             // target clause

    // function that do refutation resolution
    public static void PlResolution() {
        Set<String> newSet = new HashSet<>();
        negativeGoal();

        String check = "check";
        while (check.equals("check")) {
            newSet.clear();
            newSet = controlStrategy();
            if (newSet.contains("NIL")) {
                check = "true";
                break;
            }
            Set<String> set1 = new HashSet<>(start_clauses);
            Set<String> set2 = new HashSet<>(SoS);
            // nothing added = end, no contradictory clauses, nothing new is "learned" in iteration, from all possible combinations
            if (set1.containsAll(newSet) || set2.containsAll(newSet)) {
                check = "false";
            } else {
                set2.addAll(newSet);
                SoS.clear();
                SoS.addAll(set2);
            }
        }

        printProcedure(check);
    }

    // delete strategy function
    public static void simplifyStrategy() {
        // remove redudant clauses, when one clause is subset of other one
        // keep smaller set
        List<String> sosToRemove = new ArrayList<>();
        SoS.forEach(s -> {
            start_clauses.forEach(c -> {
                Set<String> set1 = new HashSet<>(Arrays.asList(s.split(" v ")));
                Set<String> set2 = new HashSet<>(Arrays.asList(c.split(" v ")));
                if (set1.containsAll(set2) && !set1.equals(set2))
                    sosToRemove.add(s);
            });
        });
        SoS.removeAll(sosToRemove);

        List<String> startToRemove = new ArrayList<>();
        start_clauses.forEach(s -> {
            SoS.forEach(c -> {
                Set<String> set1 = new HashSet<>(Arrays.asList(s.split(" v ")));
                Set<String> set2 = new HashSet<>(Arrays.asList(c.split(" v ")));
                if (set1.containsAll(set2) && !set1.equals(set2))
                    startToRemove.add(s);
            });
        });
        start_clauses.removeAll(startToRemove);

        Set<String> toRemove = new HashSet<>();
        SoS.forEach(s -> {
            SoS.forEach(c -> {
                Set<String> set1 = new HashSet<>(Arrays.asList(s.split(" v ")));
                Set<String> set2 = new HashSet<>(Arrays.asList(c.split(" v ")));
                if (set1.containsAll(set2) && !set1.equals(set2))
                    toRemove.add(s);
            });
        });
        SoS.removeAll(toRemove);

        // remove unimportant clauses, tautology
        List<String> fix = new ArrayList<>();
        for (String s : start_clauses) {
            List<String> check = new ArrayList<>(Arrays.asList(s.split(" v ")));
            // group literals x and ~x, if group size > 1, that is tautology
            Map<String, Long> groups = check.stream()
                                            .collect(Collectors.groupingBy(e -> e.startsWith("~") ? e.substring(1) : e, Collectors.counting()));
            // remove literals from check list if their group size is > 1
            check.removeIf(e -> (!e.startsWith("~") && groups.get(e) > 1) || (e.startsWith("~") && groups.get(e.substring(1)) > 1));
            String newClause;
            if (check.size() == 1)
                newClause = check.get(0);
            else
                newClause = String.join(" v ", check);
            fix.add(newClause);
        }
        start_clauses.clear();
        start_clauses.addAll(fix);

        List<String> fix2 = new ArrayList<>();
        for (String s : SoS) {
            List<String> check = new ArrayList<>(Arrays.asList(s.split(" v ")));
            Map<String, Long> groups = check.stream()
                                            .collect(Collectors.groupingBy(e -> e.startsWith("~") ? e.substring(1) : e, Collectors.counting()));
            check.removeIf(e -> (!e.startsWith("~") && groups.get(e) > 1) || (e.startsWith("~") && groups.get(e.substring(1)) > 1));
            String newClause;
            if (check.size() == 1)
                newClause = check.get(0);
            else
                newClause = String.join(" v ", check);
            fix2.add(newClause);
        }
        SoS.clear();
        SoS.addAll(fix2);

    }

    // control strategy function for Set of support (SoS)
    public static Set<String> controlStrategy() {
        // simplification strategy for every time SoS is expanded
        simplifyStrategy();
        Set<String> result = new HashSet<>();

        // choose 2 clauses with contradictory literals
        for (String clause1 : start_clauses) {
            for (String clause2 : SoS) {
                Set<String> resolve = new HashSet<>();
                if (done.contains(new HashSet<>(Arrays.asList(clause1, clause2)))) {
                    // already checked, skip
                } else {
                    String[] arr1 = clause1.split(" v ");
                    Set<String> set2 = new HashSet<>(List.of(clause2.split(" v ")));
                    for (String s : arr1) {
                        resolve.clear();
                        // do if clauses have contradictory literals
                        if ((s.startsWith("~") && set2.contains(s.substring(1))) || (!s.startsWith("~") && set2.contains("~" + s))) {
                            resolve.addAll(plResolve(clause1, clause2));
                            done.add(new HashSet<>(Arrays.asList(clause1, clause2)));
                            result.addAll(resolve);
                            if (resolve.stream()
                                        .anyMatch(r -> r.equals("NIL"))
                            )
                                return result;
                        }
                    }
                }
            }
        }

        // same as above, but between clauses and SoS
        for (int i = 0; i < SoS.size() - 1; i++) {
            for (int j = i + 1; j < SoS.size(); j++) {
                String clause1 = SoS.get(i);
                String clause2 = SoS.get(j);

                Set<String> resolve = new HashSet<>();
                if (done.contains(new HashSet<>(Arrays.asList(clause1, clause2)))) {
                    // skip
                } else {
                    String[] arr1 = clause1.split(" v ");
                    Set<String> set2 = new HashSet<>(Arrays.asList(clause2.split(" v ")));
                    for (String s : arr1) {
                        if ((s.startsWith("~") && set2.contains(s.substring(1))) || (!s.startsWith("~") && set2.contains("~" + s))) {
                            resolve.addAll(plResolve(clause1, clause2));
                            done.add(new HashSet<>(Arrays.asList(clause1, clause2)));
                            result.addAll(resolve);
                            if (resolve.stream().anyMatch(r -> r.equals("NIL")))
                                return result;
                        }
                    }
                }
            }
        }
        return result;
    }

    // resolution function (A v ~B, C v B ==> A v C)
    public static Set<String> plResolve(String clause1, String clause2) {
        Set<String> result = new HashSet<>();

        // check every literal
        for (String c1 : clause1.split(" v ")) {
            for (String c2 : clause2.split(" v ")) {
                // search contradictory ones
                if ((c1.startsWith("~") && c2.equals(c1.substring(1))) || (!c1.startsWith("~") && c2.equals("~" + c1))) {
                    List<String> tmp1 = new ArrayList<>(Arrays.asList(clause1.split(" v ")));
                    List<String> tmp2 = new ArrayList<>(Arrays.asList(clause2.split(" v ")));

                    tmp1.removeIf(c -> c.equals(c1));
                    tmp2.removeIf(c -> c.equals(c2));
                    if (tmp1.isEmpty() && tmp2.isEmpty()) {
                        result.add("NIL");
                        used.put("NIL", new String[]{clause1, clause2});
                        break;
                    }
                    tmp1.addAll(tmp2);
                    String tmpResult = factorization(String.join(" v ", tmp1));
                    result.add(tmpResult);
                    used.put(tmpResult, new String[]{clause1, clause2});
                }
            }
            if (result.contains("NIL"))
                break;
        }

        return result;
    }

    // function that give negative target clauses
    public static void negativeGoal() {
        // target clause is always one and in CNF format, every negation iz conjunction of multiple negations
        String[] sos = goal.split(" v ");
        for (int i = 0; i < sos.length; i++) {
            String ss = sos[i];
            if (ss.contains("~"))
                sos[i] = ss.replace("~", "");
            else
                sos[i] = new String("~" + ss);
        }
        SoS.addAll(new ArrayList<>(Arrays.asList(sos)));
        negative_goal_clauses.addAll(new ArrayList<>(Arrays.asList(sos)));
    }

    // factorization func (A v A v ~A ==> A v ~A)
    public static String factorization(String clause) {
        Set<String> set = new HashSet<>();
        String[] tmp = clause.split(" v ");
        for (String s : tmp)
            set.add(s);

        String[] tmpResult = set.toArray(String[]::new);
        if (tmpResult.length == 1)
            return tmpResult[0];

        String result = String.join(" v ", tmpResult);
        return result;
    }

    // print resolution process
    public static void printProcedure(String check) {

        if (check.equals("false")) {
            System.out.println("[CONCLUSION]: " + goal.trim() + " is unknown");
        } else {
            // sets to determine in which stack to push checked clause
            Set<String> set1 = new HashSet<>(real_start_clauses);
            Set<String> set2 = new HashSet<>(negative_goal_clauses);
            // stacks to protect order of generated clauses
            Stack<String> final_up_premises = new Stack<>();
            Stack<String> final_down = new Stack<>();
            Stack<String> final_up_goal = new Stack<>();
            int br = 0;

            String[] nil = used.get("NIL");
            final_down.push("NIL " + "(" + nil[0] + ", " + nil[1] + ")");
            while (br < final_down.size()) {
                // take gen. clause
                String key = final_down.get(br).split(" \\(")[0];
                String[] clause = used.get(key);

                // push clause on stack that's belongs to
                if (set1.contains(clause[0]) && !final_up_premises.contains(clause[0])) {
                    final_up_premises.push(clause[0]);
                } else if (set2.contains(clause[0]) && !final_up_goal.contains(clause[0])) {
                    final_up_goal.push(clause[0]);
                } else if (used.containsKey(clause[0])) {
                    String[] parent = used.get(clause[0]);
                    final_down.push(clause[0] + " (" + parent[0] + ", " + parent[1] + ")");
                }

                if (set1.contains(clause[1]) && !final_up_premises.contains(clause[1])) {
                    final_up_premises.push(clause[1]);
                } else if (set2.contains(clause[1]) && !final_up_goal.contains(clause[1])) {
                    final_up_goal.push(clause[1]);
                } else if (used.containsKey(clause[1])) {
                    String[] parent = used.get(clause[1]);
                    final_down.push(clause[1] + " (" + parent[0] + ", " + parent[1] + ")");
                }
                br++;
            }

            Integer i = 1;
            Map<String, Integer> map = new HashMap<>();
            // mapping every clause from stack to serial number
            // for easier printing
            while (!final_up_premises.isEmpty()) {
                String clause = final_up_premises.pop();
                map.put(clause, i);
                System.out.println(i + ". " + clause);
                i++;
            }
            while (!final_up_goal.isEmpty()) {
                String clause = final_up_goal.pop();
                map.put(clause, i);
                System.out.println(i + ". " + clause);
                i++;
            }
            System.out.println("===============");
            while (!final_down.isEmpty()) {
                String key = final_down.pop();
                map.put(key.split("\\(")[0].trim(), i);
                // getting parent clauses from record: a v ~b (a v ~b v ~c, c)
                String[] tmpArr = key.split("\\(")[1].replace(")", " ").trim().split(", ");
                System.out.println(i + ". " + key.split("\\(")[0].trim() + " (" + map.get(tmpArr[0]) + ", " + map.get(tmpArr[1]) + ")");
                i++;
            }
            System.out.println("===============");
            System.out.println("[CONCLUSION]: " + goal + " is true");
        }
    }

    // main method, loading database and commands
    public static void main(String[] args) throws IOException {
        String clausese_path = "";
        String commands_path = "";

        if (args[0].equals("resolution")) {
            clausese_path = args[1];
        } else if (args[0].equals("cooking")) {
            commands_path = args[2];
            clausese_path = args[1];
        } else {
            System.out.println("Invalid arguments");
            return;
        }

        // loading cluses
        File file = new File(clausese_path);
        List<String> file_rows = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        file_rows.removeIf(l -> l.startsWith("#")); // remove comments, starting with #
        file_rows.replaceAll(String::toLowerCase); // all lines lower case

        // save target clause
        if (commands_path.isEmpty()) {
            String newGoal = factorization(file_rows.remove(file_rows.size() - 1));
            goal = newGoal;
        }
        // save premises
        List<String> premises = new ArrayList<>();
        for (String line : file_rows) {
            String newLine = factorization(line);
            premises.add(newLine);
        }
        real_start_clauses = new ArrayList<>(premises);
        start_clauses = premises;

        // simplification strategy of starting premises
        Set<String> toRemove = new HashSet<>();
        start_clauses.forEach(s -> {
            start_clauses.forEach(c -> {
                Set<String> set1 = new HashSet<>(Arrays.asList(s.split(" v ")));
                Set<String> set2 = new HashSet<>(Arrays.asList(c.split(" v ")));
                // if s contains c and s != c then c is subset of s
                if (set1.containsAll(set2) && !set1.equals(set2))
                    toRemove.add(s);
            });
        });
        start_clauses.removeAll(toRemove);

        // load and execute commands
        if (!commands_path.equals("")) {
            try (BufferedReader reader = new BufferedReader(new FileReader(commands_path))) {
                System.out.println("Constructed with knowledge:");
                start_clauses.forEach(c -> System.out.println(c));
                System.out.println();

                while (reader.ready()) {
                    String tmpLine = reader.readLine();
                    if(!tmpLine.startsWith("#")){
                        String[] line = tmpLine.split(" ");
                        switch (line[line.length - 1]) {
                            case "?": // reset all except premises, we are checking new goal
                                SoS.clear();
                                done.clear();
                                used.clear();
                                negative_goal_clauses.clear();
                                start_clauses.clear();
                                start_clauses.addAll(new ArrayList<>(real_start_clauses));

                                line[line.length - 1] = ""; // remove checked operator
                                String newGoal = factorization(String.join(" ", line).toLowerCase());
                                goal = newGoal.trim();
                                System.out.println("Users command: " + goal + " ?");

                                PlResolution();
                                break;
                            case "-":
                                line[line.length - 1] = ""; // remove checked operator
                                String remove = String.join(" ", line).toLowerCase().trim();
                                System.out.println("Users command: " + remove + " -");

                                Set<String> set = new HashSet<>(Arrays.asList(remove.split(" v ")));
                                real_start_clauses.removeIf(c -> set.equals(new HashSet<>(Arrays.asList(c.split(" v ")))));
                                System.out.println("removed: " + remove);
                                break;
                            case "+":
                                line[line.length - 1] = ""; // remove checked operator
                                String add = String.join(" ", line).toLowerCase().trim();
                                System.out.println("Users command: " + add + " +");

                                String newAdd = factorization(add);
                                if (!real_start_clauses.contains(newAdd)) {
                                    real_start_clauses.add(newAdd);
                                }
                                System.out.println("added: " + add);
                                break;
                        }
                        System.out.println();
                    }
                }
            } catch (IOException e) {
                System.out.println("Error reading commands file: " + e.getMessage());
            }
        } else {
            // if we didn't get cookbook do only refutation resolution for target clause
            PlResolution();
        }

        return;
    }
}