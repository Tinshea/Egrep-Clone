package Tests;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import Automate.*;
import KMPAlgorithm.*;

import java.io.InputStreamReader;

public class Benchmark {
    public Benchmark() {}

    public static void main(String[] args) {
        try {
            BufferedReader patternReader = new BufferedReader(new FileReader("Tests/most_used_words.txt"));
            List<String> patternArray = new ArrayList<>(); 
            String line = patternReader.readLine();
            while (line != null) {
                patternArray.add(line);
                line = patternReader.readLine();
            }
            patternReader.close();

            // Exécuter les tests KMP et DFA
            // TestAlgorithmsExecutionTime(patternArray);

            // Comparer les résultats de grep et DFA    
            compareGrepDFAAndKMP(patternArray);
        } catch (IOException e) {
            e.printStackTrace();
        }    
    }

    public static void TestAlgorithmsExecutionTime(List<String> regexPatterns) {
        List<String> book = new ArrayList<>();
        
        // Lire le contenu du livre
        try (BufferedReader file = new BufferedReader(new FileReader("Tests/56667-0.txt"))) {
            String bookLine;
            while ((bookLine = file.readLine()) != null) {
                book.add(bookLine);
            }
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
            return;
        }
    
        System.out.println("Test KMP, DFA, and Egrep with varying data sizes:");
    
        int[] dataSizes = {100, 500, 1000, 5000, 10000, 12000 , 15000}; // Taille des sous-ensembles de données (lignes du livre)
        int repetitions = 10;
    
        // Tableaux pour cumuler les temps d'exécution moyens pour chaque taille de données
        long[] kmpCumulativeTimes = new long[dataSizes.length];
        long[] dfaCumulativeTimes = new long[dataSizes.length];
        long[] egrepCumulativeTimes = new long[dataSizes.length];
    
        // Test pour chaque taille de données
        for (int j = 0; j < dataSizes.length; j++) {
            int dataSize = dataSizes[j];
            List<String> subList = book.subList(0, Math.min(dataSize, book.size()));
    
            long totalKMPExecutionTime = 0;
            long totalDFAExecutionTime = 0;
            long totalEgrepExecutionTime = 0;
    
            for (String regex : regexPatterns) {
                // Test egrep pour chaque motif
                for (int i = 0; i < repetitions; i++) {
                    totalEgrepExecutionTime += runGrepOnSubset(regex, subList); 
                }

                // Test KMP pour chaque motif
                for (int i = 0; i < repetitions; i++) {
                    long startTime = System.nanoTime();
                    KMP kmp = new KMP();
    
                    for (String test : subList) {
                        kmp.accept(test, regex);
                    }
    
                    long endTime = System.nanoTime();
                    totalKMPExecutionTime += (endTime - startTime);
                }
    
                // Test DFA pour chaque motif (avec la construction de l'automate et minimisation)
                for (int i = 0; i < repetitions; i++) {
                    try {
                        long startTime = System.nanoTime();
    
                        // Construction de l'automate
                        RegEx ret = new RegEx();
                        ret.setRegEx(regex);
                        RegExTree regExTree = ret.parse();
                        NFA nfa = NFA.build(regExTree);
    
                        DFA dfa = new DFA(nfa);
                        dfa.minimize("dfamini.dot");
    
                        for (String test : subList) {
                            dfa.accept(test);
                        }
    
                        long endTime = System.nanoTime();
    
                        totalDFAExecutionTime += (endTime - startTime);
                    } catch (Exception e) {
                        System.err.println("Error processing regex: " + regex);
                        e.printStackTrace();
                    }
                }
    
            }
    
            kmpCumulativeTimes[j] += totalKMPExecutionTime / repetitions;
            dfaCumulativeTimes[j] += totalDFAExecutionTime / repetitions;
            egrepCumulativeTimes[j] += totalEgrepExecutionTime / repetitions;
        }
    
        StringBuilder res = new StringBuilder();
        res.append("DataSize,KMP_Time(mics),DFA_Time(mics),Egrep_Time(mics)\n");
        for (int i = 0; i < dataSizes.length; i++) {
            double averageKMPTime = kmpCumulativeTimes[i] / 1e3; 
            double averageDFATime = dfaCumulativeTimes[i] / 1e3; 
            double averageEgrepTime = egrepCumulativeTimes[i] / 1e3; 
            res.append(dataSizes[i]).append(",").append(averageKMPTime).append(",").append(averageDFATime).append(",").append(averageEgrepTime).append("\n");
        }
    
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("Algorithms-Time-Execution.csv"))) {
            writer.write(res.toString());
            System.out.println("Algorithms Time Execution File Generated !");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
            


    // Calculer la moyenne d'un tableau de temps
    public static long getAverageTime(long[] times) {
        long total = 0;
        for (long time : times) {
            total += time;
        }
        return total / times.length;
    }




    public static void compareGrepDFAAndKMP(List<String> regexPatterns) throws FileNotFoundException, IOException {
        List<String> book = new ArrayList<>();
    
        try (BufferedReader file = new BufferedReader(new FileReader("Tests/56667-0.txt"))) {
            String bookLine;
            while ((bookLine = file.readLine()) != null) {
                book.add(bookLine);
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            return; 
        }
    
        StringBuilder csvResults = new StringBuilder();
        
        int totalPatterns = 0;
        int matchingResults = 0;
    
        // Itérer sur chaque motif
        for (String regex : regexPatterns) {
            totalPatterns++;
    
            int grepOccurrences = runGrep(regex);
            int dfaOccurrences = runDFA(regex, book);
            int kmpOccurrences = runKMP(regex, book);
    
            boolean match = (grepOccurrences == dfaOccurrences) && (dfaOccurrences == kmpOccurrences);
            if (match) {
                matchingResults++;
            }
    
            csvResults.append(regex).append(",")
                      .append(grepOccurrences).append(",")
                      .append(dfaOccurrences).append(",")
                      .append(kmpOccurrences).append(",")
                      .append(match ? "Oui" : "Non").append("\n");
        }
    
        StringBuilder header = new StringBuilder();
        header.append("Nombre total de motifs testés,").append(totalPatterns).append("\n");
        header.append("Nombre de correspondances trouvées,").append(matchingResults).append("\n");
        header.append("Motif,Nb_Occurrences_Grep,Nb_Occurrences_DFA,Nb_Occurrences_KMP,Correspondance\n");
    
        // Écrire les résultats dans un fichier CSV
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("Grep_vs_DFA_vs_KMP_Comparison.csv"))) {
            writer.write(header.toString() + csvResults.toString());
            System.out.println("Comparison results written to Grep_vs_DFA_vs_KMP_Comparison.csv");
        } catch (IOException e) {
            e.printStackTrace();
        }
    
        System.out.println("Nombre total de motifs testés : " + totalPatterns);
        System.out.println("Nombre de correspondances trouvées : " + matchingResults);
    }
    
    

    // Méthode pour exécuter grep en ligne de commande et retourner le nombre d'occurrences
    public static int runGrep(String pattern) {
        int occurrences = 0;
        try {
            ProcessBuilder pb = new ProcessBuilder("egrep", "-o", pattern, "Tests/56667-0.txt");
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            while (reader.readLine() != null) {
                occurrences++;
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return occurrences;
    }


    // Méthode pour exécuter KMP et retourner le nombre d'occurrences
    public static int runKMP(String regex, List<String> book) throws FileNotFoundException, IOException {
            int occurrences = 0;
            try {
                KMP k = new KMP();
                for (String line : book) {
                    int lineLength = line.length();
                    int i = 0;
                    while (i < lineLength) {
                        boolean matched = false;
                        for (int j = i; j <= lineLength; j++) {
                            String substring = line.substring(i, j);
                            if (k.accept(regex,substring)) {
                                occurrences++;
                                i = j;
                                matched = true;
                                break;
                            }
                        }
                        if (!matched) {
                            i++; 
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error processing regex: " + regex);
                e.printStackTrace();
            }
        
            return occurrences;
        }



    // Méthode pour exécuter DFA et retourner le nombre d'occurrences
    public static int runDFA(String regex, List<String> book) {
        int occurrences = 0;
        try {
            RegEx ret = new RegEx();
            ret.setRegEx(regex);
            RegExTree regExTree = ret.parse();
            NFA nfa = NFA.build(regExTree);
            DFA dfa = new DFA(nfa);
            dfa.minimize("dfamini.dot");
    
            for (String line : book) {
                int lineLength = line.length();
                int i = 0;
                while (i < lineLength) {
                    boolean matched = false;
                    for (int j = i; j <= lineLength; j++) {
                        String substring = line.substring(i, j);
                        if (dfa.accept(substring)) {
                            occurrences++;
                            i = j;
                            matched = true;
                            break;
                        }
                    }
                    if (!matched) {
                        i++;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing regex: " + regex);
            e.printStackTrace();
        }
    
        return occurrences;
    }
    


    public static long runGrepOnSubset(String pattern, List<String> subList) {
        long executionTime = 0;
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("temp_subset.txt"));
            for (String line : subList) {
                writer.write(line + "\n");
            }
            writer.close();
            
            long startTime = System.nanoTime();
            ProcessBuilder pb = new ProcessBuilder("egrep"," -o ",pattern, "temp_subset.txt");
            Process process = pb.start();
            long endTime = System.nanoTime();
            executionTime = (endTime - startTime);
    
        } catch (IOException e) {
            e.printStackTrace();
        }
    
        return executionTime; 
    }
    





    
}
