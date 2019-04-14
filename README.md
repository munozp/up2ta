# up2ta
Unified Path Planning &amp; Task Planning Architecture (UP2TA) is an AI planner that interleaves path planning and task planning for mobile robotics applications. The planner integrates a modified PDDL planner with a path planning algorithm, combining domain-independent heuristics and a domain-specific heuristic for path planning. Then, UP2TA can generate shorter paths while performing hierarchical tasks in an efficient ordered way. 

# Resources

For more details, this is the [UP2TA research article](https://doi.org/10.1016/j.robot.2016.04.010).

Video demonstration of [UP2TA in Youtube](https://www.youtube.com/watch?v=iRlg25wF6jw) controlling a TurtleBot 2 robot using the [MoBAr autonomous controller](https://github.com/ISG-UAH/mobar-turtlebot).


# Components and execution

UP2TA is based on a modified PDDL planner and a path planning algorithm. This version uses the following components:
- PDDL: a modified version of the [FF planner](https://fai.cs.uni-saarland.de/hoffmann/ff.html).
- Path planning: there are varius path planning algorithms (A*, Dijkstra, Theta*, S-Theta*) implemented in Java that can be used.

Both components are integrated using pipes to share information (see the article to view the execution flow). The PDDL planner is the main executable.

To create the executable, use the makefile and create the Java executable in the way you prefer (you can use the NetBeans project). For testing, you can use the files located in the pddl folder.  The path planning algorithms requires at least a DEM file or a travesal cost file (or both).

To execute the path planner, you must deploy a shell script: /usr/share/pathplanning.sh with the following instruction:

```java -jar  PathPlanning.jar $1 $2 $3```

The pipes will be created in /tmp/. 

Previous requirements can be modified in the FF main.c file and ff.h for the PDDL planner and in the TaskPlannerConnect.java for the path planner.

To execute the system use the following command:

```./ff -o operators.pddl -f facts.pddl path-algorithm demfile costfile outfile```

Path planning can be (note the dot):
- .d > Dijkstra
- .a > A*
- .t > Theta*
- .s > S-Theta*
Other path planners are implemented and can be implemented in the TaskPlannerConnect.java.
