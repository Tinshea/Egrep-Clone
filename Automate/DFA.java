package Automate;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.io.File;

public class DFA {
    private final List<Map<Integer, Integer>> transitions;
    private final Set<Integer> finalStates;

    public DFA(NFA nfa) {
        transitions = new ArrayList<>();
        finalStates = new HashSet<>();
        NFA_to_DFA(nfa);
    }

    public void addTransition(int parent, int symbol, int fils) {
        while (transitions.size() <= parent) {
            transitions.add(new HashMap<>());
        }
        transitions.get(parent).put(symbol, fils);
    }

    public void addFinalState(int state) {
        finalStates.add(state);
    }

    public void printTransitions() {
        for (int i = 0; i < transitions.size(); i++) {
            System.out.println("État " + i + ": " + transitions.get(i));
        }
    }

    public void NFA_to_DFA(NFA nfa) {
        Map<Set<Integer>, Integer> etats = new HashMap<>();
        Queue<Set<Integer>> file = new LinkedList<>();

        Set<Integer> etat_initial = epsilon(nfa, Set.of(0));
        file.add(etat_initial);
        etats.put(etat_initial, 0);

        while (!file.isEmpty()) {
            Set<Integer> current = file.poll();
            int dfaState = etats.get(current);

            Map<Integer, Set<Integer>> symbolfilsStates = new HashMap<>();

            for (int state : current) {
                for (List<Integer> transition : nfa.getTransitions().get(state)) {
                    int symbol = transition.get(0);
                    int targetState = transition.get(1);
                    if (symbol != -1) {
                        symbolfilsStates.putIfAbsent(symbol, new HashSet<>());
                        symbolfilsStates.get(symbol).add(targetState);
                    }
                }
            }

            for (Map.Entry<Integer, Set<Integer>> entry : symbolfilsStates.entrySet()) {
                int symbol = entry.getKey();
                Set<Integer> nextStates = epsilon(nfa, entry.getValue());

                if (!etats.containsKey(nextStates)) {
                    int newState = etats.size();
                    etats.put(nextStates, newState);
                    file.add(nextStates);
                }

                this.addTransition(dfaState, symbol, etats.get(nextStates));
            }
        }

        for (Set<Integer> nfaStates : etats.keySet()) {
            for (int state : nfaStates) {
                if (nfa.getTransitions().get(state).isEmpty()) {
                    this.addFinalState(etats.get(nfaStates));
                    break;
                }
            }
        }
    }

    private static Set<Integer> epsilon(NFA nfa, Set<Integer> states) {
        Set<Integer> epsilon_set = new HashSet<>(states);
        Stack<Integer> pile = new Stack<>();
        pile.addAll(states);

        while (!pile.isEmpty()) {
            int state = pile.pop();
            for (List<Integer> transition : nfa.getTransitions().get(state)) {
                if (transition.get(0) == -1 && !epsilon_set.contains(transition.get(1))) {
                    epsilon_set.add(transition.get(1));
                    pile.add(transition.get(1));
                }
            }
        }

        return epsilon_set;
    }

    public void minimize(String filename) throws IOException {
        // Étape 1 : Séparer les états finaux et non finaux
        List<Set<Integer>> groups = new ArrayList<>();
        Set<Integer> finalGroup = new HashSet<>(finalStates);
        Set<Integer> nonFinalGroup = new HashSet<>();
        
        // Ajoutez l'état 0 à un groupe non final si ce n'est pas un état final
        if (!finalGroup.contains(0)) {
            nonFinalGroup.add(0);
        } else {
            finalGroup.add(0);
        }
        
        for (int i = 0; i < transitions.size(); i++) {
            if (i != 0 && !finalGroup.contains(i)) {
                nonFinalGroup.add(i);
            }
        }
    
        // Vérifiez que nonFinalGroup n'est pas vide avant d'ajouter
        if (!nonFinalGroup.isEmpty()) {
            groups.add(nonFinalGroup);
        }
        groups.add(finalGroup);
        
        // Étape 2 : Répéter la division jusqu'à ce qu'il n'y ait plus de nouvelles divisions
boolean changed;
do {
    changed = false;
    List<Set<Integer>> newGroups = new ArrayList<>();
    for (Set<Integer> group : groups) {
        Map<Map<Integer, Integer>, Set<Integer>> partitionMap = new HashMap<>();
        for (int state : group) {
            Map<Integer, Integer> transitionsToGroups = new HashMap<>();
            if (transitions.size() > state && transitions.get(state) != null) {
                for (Map.Entry<Integer, Integer> entry : transitions.get(state).entrySet()) {
                    int symbol = entry.getKey();
                    int targetState = entry.getValue();
                    // Trouver dans quel groupe appartient l'état cible
                    int targetGroup = findGroup(groups, targetState);
                    transitionsToGroups.put(symbol, targetGroup);
                }
            }

            partitionMap.computeIfAbsent(transitionsToGroups, k -> new HashSet<>()).add(state);
        }
        newGroups.addAll(partitionMap.values());
        if (partitionMap.size() > 1) {
            changed = true;
        }
    }
    groups = newGroups;
    
    // Trier les groupes en fonction de l'état le plus petit de chaque groupe
    groups.sort(Comparator.comparingInt(group -> group.iterator().next()));

} while (changed);

// Étape 3 : Réduire les états du DFA en fonction des groupes trouvés
Map<Integer, Integer> stateToGroupMap = new HashMap<>();
Map<Integer, Integer> groupToStateMap = new HashMap<>();
for (int i = 0; i < groups.size(); i++) {
    Set<Integer> group = groups.get(i);
    for (int state : group) {
        stateToGroupMap.put(state, i);
    }
    // Garder une trace du premier état de chaque groupe pour les transitions
    groupToStateMap.put(i, group.iterator().next());
}

// Nouvelle liste de transitions et nouveaux états finaux
List<Map<Integer, Integer>> newTransitions = new ArrayList<>();
Set<Integer> newFinalStates = new HashSet<>();
for (int i = 0; i < groups.size(); i++) {
    Map<Integer, Integer> newStateTransitions = new HashMap<>();
    // Utiliser le premier état du groupe comme représentant
    int representative = groupToStateMap.get(i);
    if (transitions.size() > representative && transitions.get(representative) != null) {
        for (Map.Entry<Integer, Integer> entry : transitions.get(representative).entrySet()) {
            int symbol = entry.getKey();
            int targetState = entry.getValue();

            // Rediriger la transition vers le groupe cible
            newStateTransitions.put(symbol, stateToGroupMap.get(targetState));
        }
    }

    newTransitions.add(newStateTransitions);

    // Si le représentant est un état final, ce nouveau groupe est un état final
    if (finalStates.contains(representative)) {
        newFinalStates.add(i);
    }
}


        // Remplacer les anciennes transitions et états finaux par les nouveaux
        transitions.clear();
        transitions.addAll(newTransitions);
        finalStates.clear();
        finalStates.addAll(newFinalStates);
    
        // Étape 4 : Écrire le DFA minimisé dans un fichier DOT
        toDotFile(filename);
    }

    // Trouver dans quel groupe appartient un état
    private int findGroup(List<Set<Integer>> groups, int state) {
        for (int i = 0; i < groups.size(); i++) {
            if (groups.get(i).contains(state)) {
                return i;
            }
        }
        return -1; // Ne devrait pas arriver
    }

    public boolean accept(String input) {
        int currentState = 0; 
        boolean transitiononce = false;
        for (char symbol : input.toCharArray()) {
            int symbolInt = (int) symbol; 
            if (currentState < transitions.size() && transitions.get(currentState).containsKey(symbolInt)) {
                transitiononce = true;
                currentState = transitions.get(currentState).get(symbolInt);
            } else {
                return finalStates.contains(currentState) && transitiononce;
            }
        }
    
        return finalStates.contains(currentState) && transitiononce;
    }
    

    public void toDotFile(String filename) throws IOException {
        File schemaDirectory = new File("dotfile");
        if (!schemaDirectory.exists()) {
            schemaDirectory.mkdirs();
        }
        String newfile = "dotfile/" + filename;
        FileWriter writer = new FileWriter(newfile);
        writer.write("digraph DFA {\n");

        // Indiquer les états finaux
        for (int state : finalStates) {
            writer.write("    " + state + " [shape=doublecircle];\n");
        }

        // Ajouter les transitions
        for (int i = 0; i < transitions.size(); i++) {
            Map<Integer, Integer> stateTransitions = transitions.get(i);
            for (Map.Entry<Integer, Integer> entry : stateTransitions.entrySet()) {
                int symbol = entry.getKey();
                int targetState = entry.getValue();
                String label = Character.toString((char) symbol);
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

        String pngFilename = dotFilename.replace("dotfile", "schema").replace(".dot", ".png");

        ProcessBuilder processBuilder = new ProcessBuilder("dot", "-Tpng", dotFilename, "-o", pngFilename);
        try {
            Process process = processBuilder.start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            System.err.println("Error while converting .dot to .png: " + e.getMessage());
        }
    }

}
