package Automate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;

public class NFA {
    private final List<List<List<Integer>>> transitions;
    private final Set<Integer> finalStates; 
    public NFA(int num) {
        transitions = new ArrayList<>();
        finalStates = new HashSet<>();
        for (int i = 0; i < num; i++) {
            transitions.add(new ArrayList<>());
        }
    }

    public List<List<List<Integer>>> getTransitions() {
        return this.transitions;
    }

    public void addTransitionToAutomate(int parent, int fils, int symbol) {
        List<Integer> transition = new ArrayList<>();
        transition.add(symbol);
        transition.add(fils);
        transitions.get(parent).add(transition);
    }

    public void addFinalState(int state) {
        finalStates.add(state);  
    }

    public Set<Integer> getFinalStates() {
        return finalStates;
    }

    private static void Fusion(NFA result, NFA automate, int pas) {
        for (int i = 0; i < automate.transitions.size(); i++) {
            for (List<Integer> t : automate.transitions.get(i)) {
                result.addTransitionToAutomate(i + pas, t.get(1) + pas, t.get(0));
            }
        }
    }

    public static NFA build(RegExTree tree) throws Exception {
        if (tree.subTrees.isEmpty()) {
            return buildAutomateLeaf(tree.root);
        }

        if (tree.root == RegEx.CONCAT) {
            return ConcatAutomate(tree);
        }
        if (tree.root == RegEx.ALTERN) {
            return AlternAutomate(tree);
        }
        if (tree.root == RegEx.ETOILE) {
            return EtoileAutomate(tree);
        }

        throw new Exception("fail");
    }

    private static NFA buildAutomateLeaf(int symbol) {
        NFA automate = new NFA(2);
        automate.addTransitionToAutomate(0, 1, symbol);
        automate.addFinalState(1);
        return automate;
    }

    private static NFA ConcatAutomate(RegExTree tree) throws Exception {
        NFA gauche = build(tree.subTrees.get(0));
        NFA droite = build(tree.subTrees.get(1));
        NFA res = new NFA(gauche.transitions.size() + droite.transitions.size());
        Fusion(res, gauche, 0);
        res.addTransitionToAutomate(gauche.transitions.size() - 1, gauche.transitions.size(), -1);
        Fusion(res, droite, gauche.transitions.size());
        res.addFinalState(gauche.transitions.size() + droite.transitions.size() - 1);  
        return res;
    }

    public static NFA AlternAutomate(RegExTree tree) throws Exception {
        NFA gauche = build(tree.subTrees.get(0));
        NFA droite = build(tree.subTrees.get(1));
        NFA res = new NFA(gauche.transitions.size() + droite.transitions.size() + 2);

        res.addTransitionToAutomate(0, 1, -1);
        res.addTransitionToAutomate(0, gauche.transitions.size() + 1, -1);
        Fusion(res, gauche, 1);
        Fusion(res, droite, gauche.transitions.size() + 1);

        res.addTransitionToAutomate(gauche.transitions.size(), res.transitions.size() - 1, -1);
        res.addTransitionToAutomate(res.transitions.size() - 2, res.transitions.size() - 1, -1);
        res.addFinalState(res.transitions.size() - 1);  

        return res;
    }

    public static NFA EtoileAutomate(RegExTree tree) throws Exception {
        NFA etoile = build(tree.subTrees.get(0));
        NFA automate = new NFA(etoile.transitions.size() + 2);
        automate.addTransitionToAutomate(0, 1, -1);
        automate.addTransitionToAutomate(0, etoile.transitions.size() + 1, -1);
        Fusion(automate, etoile, 1);
        automate.addTransitionToAutomate(etoile.transitions.size(), 1, -1);
        automate.addTransitionToAutomate(etoile.transitions.size(), etoile.transitions.size() + 1, -1);
        automate.addFinalState(etoile.transitions.size() + 1);

        return automate;
    }

    public void printTransitions() {
        for (int i = 0; i < transitions.size(); i++) {
            System.out.println("État " + i + ": " + transitions.get(i));
        }
    }

    // Mise à jour de la méthode toDotFile pour dessiner les états finaux avec un double cercle
    public void toDotFile(String filename) throws IOException {

        File schemaDirectory = new File("dotfile");
        if (!schemaDirectory.exists()) {
            schemaDirectory.mkdirs();
        }
        String newfile = "dotfile/" + filename;
        FileWriter writer = new FileWriter(newfile);
        writer.write("digraph NFA {\n");

        // Dessiner les états finaux avec un double cercle
        for (Integer finalState : finalStates) {
            writer.write("    " + finalState + " [shape=doublecircle];\n");
        }

        // Dessiner les transitions
        for (int i = 0; i < transitions.size(); i++) {
            for (List<Integer> transition : transitions.get(i)) {
                int symbol = transition.get(0);
                int targetState = transition.get(1);
                String label = (symbol == -1) ? "ε" : Character.toString((char) symbol);
                writer.write("    " + i + " -> " + targetState + " [label=\"" + label + "\"];\n");
            }
        }

        writer.write("}\n");
        writer.close();

        convertDotToPng(newfile);

    }

    private void convertDotToPng(String dotFilename) {
        
        File schemaDirectory = new File("schema");
        if (!schemaDirectory.exists()) {
            schemaDirectory.mkdir();
        }
    
        String pngFilename = dotFilename.replace("dotfile/", "schema/").replace(".dot", ".png");
        ProcessBuilder processBuilder = new ProcessBuilder("dot", "-Tpng", dotFilename, "-o", pngFilename);
        try {
            Process process = processBuilder.start();
            process.waitFor(); 
        } catch (IOException | InterruptedException e) {
            System.err.println("Error while converting .dot to .png: " + e.getMessage());
        }
    }
    
}
