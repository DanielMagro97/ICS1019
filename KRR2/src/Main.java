import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    public static class Node {
        String name;
        ArrayList<Edge> connections = new ArrayList<Edge>();
    }

    public static class Edge {
        Node start;
        Node end;
        boolean polarity;
    }

    public static class Graph {
        ArrayList<Node> nodes = new ArrayList<Node>();
    }

    public static class Path {
        ArrayList<Node> path = new ArrayList<Node>();
        boolean polarity;
    }

    // Main Method
    public static void main(String[] args) throws IOException {

        // Declaring inheritance network as a Graph - an array list of Nodes
        Graph IN = new Graph();
        // Calling method to parse inputted inheritance network
        IN = parseInput(args[0]);

        // printing the IN to make sure it was parsed correctly / user selected the correct IN
        System.out.println();   //inserting a new line
        printIN(IN);
        System.out.println();   //inserting a new line

        // User Input - Query
        System.out.println("Please enter your query:");
        Scanner scan = new Scanner(System.in);
        String query = scan.nextLine();
        // parsing the user's query:
        String[] queryParts = query.split(" ");
        String start = queryParts[0];
        String end = queryParts[2];

        // Find all paths from subConcept to superConcept

        // Find which nodes are the subConcept and the superConcept
        Node subConcept = new Node();
        for (int i = 0; i < IN.nodes.size(); i++) {
            if (IN.nodes.get(i).name.equals(start)) {
                subConcept = IN.nodes.get(i);
            }
        }
        Node superConcept = new Node();
        for (int i = 0; i < IN.nodes.size(); i++) {
            if (IN.nodes.get(i).name.equals(end)) {
                superConcept = IN.nodes.get(i);
            }
        }
        // ArrayList of paths which will store all the paths found
        ArrayList<Path> paths = new ArrayList<Path>();
        // Path object which will store all the visited nodes in an ArrayList of Nodes
        Path visited = new Path();
        // setting the first visited node to the starting node (subConcept)
        visited.path.add(subConcept);
        findPaths(IN, subConcept, superConcept, visited, paths);
        System.out.println("\nAll Paths found are:");
        printPaths(paths);

        // Shortest distance
        shortestDistance(paths);

        // Inferential distance
        inferentialDistance(IN, superConcept, paths);
    }

    // File IO method, retrieves all the lines in the text file and stores each connection
    // as an element in an array of Strings
    private static String[] readInput(String inputFile) throws IOException {
        String[] data = Files.readAllLines(Paths.get(inputFile)).toArray(new String[]{});
        return data;
    }

    private static Graph parseInput(String inputFile) throws IOException {
        String textFile[] = readInput(inputFile);

        Graph IN = new Graph();

        for (int i = 0; i < textFile.length; i++) {
            // splitting by whitespace
            String[] statementParts = textFile[i].split(" ");

            // declaring a new node since the current concept is not already in the list of nodes
            Node subConcept = new Node();
            // checking whether the sub-concept is already in the list of nodes
            if (!checkExistence(IN, statementParts[0])) {
                // setting the name of the node to the first part of the statement (the subConcept)
                subConcept.name = statementParts[0];
                // adding the node to the list of nodes
                IN.nodes.add(subConcept);
                /*
                // storing the current connection within the list of connections of that node
                IN.nodes.get( IN.nodes.size()-1 ).connections.add(edge);
                */
            } else {
                for (int j = 0; j < IN.nodes.size(); j++) {
                    if (IN.nodes.get(j).name.equals(statementParts[0])) {
                        subConcept = IN.nodes.get(j);
                        break;
                    }
                }
            }

            Node superConcept = new Node();
            // checking whether the super-concept is already in the list of nodes
            if (!checkExistence(IN, statementParts[2])) {
                superConcept.name = statementParts[2];
                IN.nodes.add(superConcept);
            } else {
                for (int j = 0; j < IN.nodes.size(); j++) {
                    if (IN.nodes.get(j).name.equals(statementParts[2])) {
                        superConcept = IN.nodes.get(j);
                        break;
                    }
                }
            }

            // declaring a new edge to store the current statement
            Edge edge = new Edge();
            edge.start = subConcept;
            edge.end = superConcept;

            if (statementParts[1].equals("IS-A")) {
                edge.polarity = true;
            } else {
                edge.polarity = false;
            }

            // storing the edge inside the list of connections of the subConcept
            subConcept.connections.add(edge);
        }
        return IN;
    }

    // method which checks if a certain concept is already in the list of nodes
    private static boolean checkExistence(Graph IN, String conceptName) {
        for (int j = 0; j < IN.nodes.size(); j++) {
            if (IN.nodes.get(j).name.equals(conceptName)) {
                return true;
            }
        }
        return false;
    }

    private static void printIN(Graph IN) {
        for (int i = 0; i < IN.nodes.size(); i++) {
            for (int j = 0; j < IN.nodes.get(i).connections.size(); j++) {
                System.out.print(IN.nodes.get(i).name);
                if (IN.nodes.get(i).connections.get(j).polarity) {
                    System.out.print(" IS-A ");
                } else {
                    System.out.print(" IS-NOT-A ");
                }
                System.out.println(IN.nodes.get(i).connections.get(j).end.name);
            }
        }
    }

    // recursive method which finds all the paths between two provided nodes in a graph
    private static void findPaths(Graph IN, Node start, Node end, Path visited, ArrayList<Path> paths) {
        // if the current start Node is equal to the end Node
        if (start.equals(end)) {
            // add the current Path to the ArrayList of Paths
            visited.polarity = true;
            paths.add(visited);
            return;
        } else {
            // find all the nodes adjacent to the current one
            ArrayList<Node> adjacentNodes = new ArrayList<Node>();
            ArrayList<Boolean> polarities = new ArrayList<Boolean>();
            for (int i = 0; i < start.connections.size(); i++) {
                adjacentNodes.add(start.connections.get(i).end);
                polarities.add(start.connections.get(i).polarity);
            }
            // for every node adjacent to the current one
            for (int i = 0; i < adjacentNodes.size(); i++) {
                // if an adjacent node has already been visited, ignore it
                if (visited.path.contains(adjacentNodes.get(i))) {
                    continue;
                }
                Path temp = new Path();
                // if the connection is 'IS-NOT-A'
                if (!polarities.get(i)) {
                    // if the node at the end of the connection is the destination, we accept it as a path
                    if (adjacentNodes.get(i) == end) {
                        temp.path.addAll(visited.path);
                        temp.path.add(adjacentNodes.get(i));
                        temp.polarity = false;
                        paths.add(temp);
                        continue;
                    }
                    // if the node after the connection (IS-NOT-A) is not the destination, we ignore that path since
                    // IS-NOT-A can only be at the end of the path
                    else {
                        continue;
                    }
                } else {
                    temp.path.addAll(visited.path);
                    temp.path.add(adjacentNodes.get(i));
                    // recursive call, this time starts search for paths from the adjacent node instead of the original starting node
                    findPaths(IN, adjacentNodes.get(i), end, temp, paths);
                }
            }
        }
    }

    // method which prints paths
    private static void printPaths(ArrayList<Path> paths) {
        for (int i = 0; i < paths.size(); i++) {
            for (int j = 0; j < paths.get(i).path.size(); j++) {
                System.out.print(paths.get(i).path.get(j).name);
                if (j < paths.get(i).path.size() - 2) {
                    System.out.print(" IS-A ");
                }
                if (j == paths.get(i).path.size() - 2) {
                    if (paths.get(i).polarity) {
                        System.out.print(" IS-A ");
                    } else {
                        System.out.print(" IS-NOT-A ");
                    }
                }
            }
            System.out.println();
        }
        System.out.println();
    }

    // Method which determines the Shortest Distance Path
    private static void shortestDistance(ArrayList<Path> paths) {
        ArrayList<Path> shortestPaths = new ArrayList<Path>();

        int minSize = paths.get(0).path.size();
        for (int i = 1; i < paths.size(); i++) {
            if (paths.get(i).path.size() < minSize) {
                minSize = paths.get(i).path.size();
            }
        }

        for (int i = 0; i < paths.size(); i++) {
            if (paths.get(i).path.size() == minSize) {
                shortestPaths.add(paths.get(i));
            }
        }

        System.out.println("Preferred Path/s (Shortest Distance):");
        printPaths(shortestPaths);
    }

    // Method which determines the Shortest Inferential Distance Path
    private static void inferentialDistance(Graph IN, Node superConcept, ArrayList<Path> paths) {
        // ArrayList of Paths which will hold all the Preffered Path/s (Inferential Distance)
        ArrayList<Path> inferentialPaths = paths;

        // Checking for Preempted Edges:

        // boolean value storing whether a certain edge has already been shown to be preempted, thus avoiding having to
        // search the rest of the edges in the path
        boolean preempted;
        // Iterating through every path
        for (int i = 0; i < inferentialPaths.size(); i++) {
            preempted = false;
            if (inferentialPaths.get(i).polarity) {
                // Iterating through every Node in the Path (starting from the second node => j=1)
                for (int j = 1; j < inferentialPaths.get(i).path.size(); j++) {
                    // ArrayList of Paths which will store every path between the previous node and the superConcept of the
                    // current node
                    ArrayList<Path> preemptedEdges = new ArrayList<Path>();
                    Path visited = new Path();
                    visited.path.add(inferentialPaths.get(i).path.get(j - 1));
                    findPaths(IN, inferentialPaths.get(i).path.get(j - 1), superConcept, visited, preemptedEdges);
                    // finding if any of the paths lead to the same superConcept but have a negative edge
                    for (int k = 0; k < preemptedEdges.size(); k++) {
                        // if the alternate path found ends with a Negative Edge
                        if (!preemptedEdges.get(k).polarity) {
                            // we can say that the edge is pre empted
                            preempted = true;
                        }
                    }

                    // if an edge is found to be pre empted
                    if (preempted) {
                        // the Path is removed from the Array List of admissible Paths
                        inferentialPaths.remove(i);
                        // the counter is adjusted to account for the left shift of the Array List
                        i--;
                        // the inner loop breaks to check the next Path
                        break;
                    }
                }
            }
        }

        // Checking for Redundant Edges:

        // boolean value storing whether a certain edge has already been shown to be redundant, thus avoiding having to
        // search the rest of the edges in the path
        boolean redundant;
        // Iterating through every path
        for (int i = 0; i < inferentialPaths.size(); i++) {
            redundant = false;
            // Iterating through every node (except the last => -1)
            for (int j = 0; j < inferentialPaths.get(i).path.size() - 1; j++) {
                // ArrayList of Paths which will store every path between 2 consecutive nodes along a path
                ArrayList<Path> redundantEdges = new ArrayList<Path>();
                Path visited = new Path();
                visited.path.add(inferentialPaths.get(i).path.get(j));
                findPaths(IN, inferentialPaths.get(i).path.get(j), inferentialPaths.get(i).path.get(j + 1), visited, redundantEdges);
                // finding the longest path between the 2 consecutive nodes
                int maxPathLength = 1;
                for (int k = 0; k < redundantEdges.size(); k++) {
                    // minus 1 since a Path of 2 nodes only has 1 Edge
                    if ((redundantEdges.get(k).path.size() - 1) > maxPathLength && redundantEdges.get(k).polarity) {
                        maxPathLength = redundantEdges.get(k).path.size() - 1;
                    }
                }
                // if the longest path is longer than 1 edge, therefore that edge is redundant and that path is inadmissible
                if (maxPathLength > 1) {
                    redundant = true;
                }

                // if an edge is found to be redundant, the loop through nodes breaks as an optimisation
                if (redundant) {
                    inferentialPaths.remove(i);
                    i--;
                    break;
                }
            }
        }

        System.out.println("Preferred Path/s (Inferential Distance):");
        printPaths(inferentialPaths);
    }
}