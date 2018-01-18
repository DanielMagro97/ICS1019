import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;
import static java.lang.System.exit;

public class Main {

    // Literal class - contains value String and polarity Boolean, indicating whether a literal is +ve or -ve
    public static class Literal {
        String value;
        boolean polarity;
    }

    // HornClause class - contains an Array List of Literals
    public static class HornClause {
        ArrayList<Literal> clause = new ArrayList<Literal>();
    }

    // Main Method
    public static void main(String[] args) throws IOException {
        // Declaring knowledge base as an Array List of Horn Clauses
        // And calling the method to parse the text file
        //ArrayList<HornClause> knowledgeBase = parseInput("input.txt");
        ArrayList<HornClause> knowledgeBase = parseInput(args[0]);
        System.out.println("The inputted knowledge base is the following:");
        printKB(knowledgeBase);
        System.out.println("Please enter the negated query you would like to resolve with the Knowledge Base in CNF");
        Scanner scan = new Scanner(System.in);
        String queryInput = scan.nextLine();
        // The negated query inputted by the user is apssed to the parseQuery method to be parsed into
        // a Horn Clause (Array List of Literals) and stored in 'query'
        ArrayList<Literal> query = parseQuery(queryInput);
        System.out.print("Resolving with the Knowledge Base: "); printHC(query);
        resolve(knowledgeBase, query);
    }

    // File IO method, retrieves all the lines in the text file and stores each Clause
    // as an element in an array of Strings
    private static String[] readInput(String inputFile) throws IOException {
        String[] data = Files.readAllLines(Paths.get(inputFile)).toArray(new String[]{});
        return data;
    }

    private static ArrayList<HornClause> parseInput(String inputFile) throws IOException {
        // Calling method to retrieve input from the text file of clauses
        String textFile[] = readInput(inputFile);
        // Declaring kb
        ArrayList<HornClause> kb = new ArrayList<HornClause>();

        // Parser
        // for loop goes through each element in the array of unparsed clauses taken from the text file
        for (int i = 0; i < textFile.length; i++) {
            // Declaring horn clause
            HornClause hc = new HornClause();

            hc.clause = parseQuery(textFile[i]);
            kb.add(hc);
        }
        return kb;
    }

    private static ArrayList<Literal> parseQuery(String queryText) {
        // Declaring horn clause
        HornClause hc = new HornClause();

        // removing [ and ]
        String cnf = queryText.replaceAll("\\[|\\]", "");
        // splitting by ,
        String[] literalsInClause = cnf.split(",");
        // looping over every literal in the cnf
        for (String s : literalsInClause) {
            // Declare literal
            Literal lit = new Literal();
            // remove white space
            lit.value = s.trim();
            // if the literal starts with -, set the polarity to false and remove the first character
            if (lit.value.charAt(0) == '-') {
                lit.polarity = false;
                lit.value = lit.value.substring(1);
            } else {
                lit.polarity = true;
            }
            hc.clause.add(lit);
        }

        return hc.clause;
    }

    private static void printKB(ArrayList<HornClause> knowledgeBase) {
        for (int i = 0; i < knowledgeBase.size(); i++) {
            System.out.print("[");
            for (int j = 0; j < knowledgeBase.get(i).clause.size(); j++) {
                if (knowledgeBase.get(i).clause.get(j).polarity)
                    System.out.print(knowledgeBase.get(i).clause.get(j).value);
                else System.out.print("-" + knowledgeBase.get(i).clause.get(j).value);
                if(j+1 != knowledgeBase.get(i).clause.size()){
                    System.out.print(", ");
                }
            }
            System.out.println("]");
        }
    }

    private static void printHC(ArrayList<Literal> hornClause) {
        System.out.print("[");
        for (int j = 0; j < hornClause.size(); j++) {
            if (hornClause.get(j).polarity)
                System.out.print(hornClause.get(j).value);
            else System.out.print("-" + hornClause.get(j).value);
            if(j+1 != hornClause.size()){
                System.out.print(", ");
            }
        }
        System.out.println("]");
    }

    // method to resolve a query and the knowledge base to the empty clause
    private static void resolve(ArrayList<HornClause> knowledgeBase, ArrayList<Literal> query) {
        // if a query is empty, this means that it has been SOLVED
        if (query.size() == 0) {
            System.out.println("SOLVED");
            exit(0);
        }

        // traversing kb for a matching literal with opposite polarity
        for (int i = 0; i < query.size(); i++) {
            for (int j = 0; j < knowledgeBase.size(); j++) {
                for (int k = 0; k < knowledgeBase.get(j).clause.size(); k++) {
                    if (knowledgeBase.get(j).clause.get(k).value.equals(query.get(i).value)
                            && knowledgeBase.get(j).clause.get(k).polarity != query.get(i).polarity) {
                        System.out.print("Resolving with: ");
                        printHC(knowledgeBase.get(j).clause);
                        // remove the matched literal from the query
                        query.remove(i);
                        // add the literals inside the clause the query was resolved with
                        // (except the resolvent of the original query) to the query being resolved
                        for (int l = 0; l < knowledgeBase.get(j).clause.size(); l++) {
                            if (l != k) {
                                query.add(knowledgeBase.get(j).clause.get(l));
                            }
                        }
                        System.out.print("Query: ");
                        printHC(query);
                        resolve(knowledgeBase, query);
                    }
                }
            }
        }
        System.out.println("NOT SOLVED");
        exit(0);
    }
}