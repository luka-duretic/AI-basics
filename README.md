# AI-basics
Implementation some of basic algorithms in Machine Learning field.

## State Space Search Algorithms
### Overview
This project implements several state space search algorithms and heuristic evaluation methods used in artificial intelligence and problem solving. It analyzes the complexity of various blind and heuristic search algorithms. Used input data are about shortest path from Pula to Buzet, places in Croatia.

### Key Features
Breadth-First Search (BFS) - explores nodes level by level, ensuring the shortest path in terms of the number of steps
  - algorithm is complete and optimal
  - exponential time and space complexity
    
Uniform Cost Search (UCS) – expands the least-cost node first, guaranteeing the optimal solution when costs are non-negative
  - algorithm is complete and optimal
  - exponential time and space complexity
    
A* Search – uses a heuristic function to guide the search towards the goal more efficiently while maintaining optimality (if the heuristic is admissible and consistent)
  - algorithm is complete
  - better time and space complexity considering algorithms above

Heuristic Analysis - the project includes methods to check
  - Optimistic (Admissible) Heuristic – Ensures that the heuristic never overestimates the true cost to the goal.
  - Consistent (Monotonic) Heuristic – Ensures the heuristic satisfies the triangle inequality, which is required for A* to be both complete and optimal.

### Input and Output Examples
./solution --alg astar --ss istra.txt --h istra_heuristic.txt
```
[FOUND_SOLUTION]: yes
[STATES_VISITED]: 6
[PATH_LENGTH]: 3
[TOTAL_COST]: 21.0
[PATH]: enroll_artificial_intelligence => fail_lab => fail_course
```

## Refutation Resolution (Cooking Assistant)
### Overview
This project focuses on automated reasoning using the resolution inference rule.
The main goal is to implement a refutation-based resolution system that can assist with cooking by reasoning about available ingredients and recipes.

The system loads a knowledge base representing a cookbook written in clausal form, and then uses resolution to answer queries, add or remove knowledge, and help the user decide which recipe can be prepared with currently available ingredients.

The last clause in the initial file is treated as the goal clause – the statement we aim to prove.
If a contradiction (NIL) is reached during resolution, the goal clause is successfully proven.

### Key Features
Refutation Resolution Implementation:
  - uses the Set of Support (SOS) strategy for guiding the search
  - implements a deletion strategy that:
    - removes redundant clauses
    - removes irrelevant clauses to keep the knowledge base clean and efficient

Goal Proof Tracking:
  - if the goal clause is successfully proven, the system outputs the step-by-step resolution process that led to NIL
    
Dynamic Knowledge Base:
  - Queries – check whether a particular fact or recipe can be proven from the current knowledge base
  - Adding clauses – extend the knowledge base with new facts or rules
  - Deleting clauses – remove outdated or incorrect information

### Cooking Assistant Mode
Once the resolution system is implemented, it is extended into a cooking assistant.
The assistant helps determine which recipes can be made with the available ingredients by:
  - loading a cookbook (knowledge base) from a text file
  - reading a set of user commands from another file and executing them sequentially

The program requires three arguments:
  1. Mode – the first argument must be the keyword cooking, which tells the system to start in cooking assistant mode
  2. Knowledge Base File – path to the file containing the initial set of clauses (cookbook)
  3. Commands File – path to the file containing user commands to be executed sequentially

### Input and Output Examples
./solution cooking recipes.txt commands.txt
```
Constructed with knowledge:
coffee_powder
~heater v ~water v hot_water
~hot_water v coffee v ~coffee_powder
water
heater

User’s command: water ?
1. water
2. ~water
===============
3. NIL (1, 2)
===============
[CONCLUSION]: water is true

User’s command: hot_water ?
1. water
2. heater
3. ~heater v ~water v hot_water
4. ~hot_water
===============
5. ~heater v ~water (3, 4)
6. ~water (2, 5)
7. NIL (1, 6)
===============
[CONCLUSION]: hot_water is true
...
```

## Decision Tree (ID3 Algorithm)
### Overview
This project implements the ID3 decision tree algorithm for machine learning.
The focus is on:
  - building a decision tree from CSV datasets
  - using that model for making predictions
  - understanding limitations of the ID3 algorithm and possible extensions
  - evaluating model performance using accuracy and a confusion matrix

### Key Features
  - ID3 Algorithm – recursively builds a decision tree using information gain (IG)
  - Model Training and Prediction:
    - fit() – trains the model on the provided dataset
    - predict() – uses the trained model to generate predictions without further learning
  - Performance Metrics:
    - Accuracy – ratio of correctly classified examples to total examples
    - Confusion Matrix – summarizes correct and incorrect predictions per class
  - Overfitting and Regularization - ID3 can overfit by growing the tree until all examples are perfectly classified
    - to improve generalization, maximum depth of the decision tree can be restricted

### Input and Output Examples
./solution datasets/volleyball.csv datasets/volleyball_test.csv 1
```
IG(weather)=0.2467 IG(humidity)=0.1518 IG(wind)=0.0481 IG(temperature)=0.0292
[BRANCHES]:
1:weather=cloudy yes
1:weather=rainy yes
1:weather=sunny no
[PREDICTIONS]: yes no no yes yes no yes yes yes yes yes yes yes no no yes yes
yes yes
[ACCURACY]: 0.36842
[CONFUSION_MATRIX]:
2 9
3 5
```
