#!/bin/bash

# Compilation des fichiers dans Automate
javac -d . Automate/*.java

# Compilation des fichiers dans KMPAlgorithm
javac -d . KMPAlgorithm/*.java

# Compilation du fichier Benchmark dans Tests
javac -cp . Tests/Benchmark.java

# Exécution de la classe Benchmark en incluant le classpath actuel
java -cp . Tests.Benchmark
