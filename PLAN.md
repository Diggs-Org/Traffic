# Plan: CLOD-17: Plan out the architecture

## What & Why

The project needs a multi-module Java architecture for a NEAT (NeuroEvolution of Augmenting
Topologies) system. NEAT is an evolutionary algorithm that simultaneously evolves both the
topology and weights of artificial neural networks. The architecture is divided into three
focused modules to enforce separation of concerns: **neat-core** provides the fundamental
NEAT data structures and neural-network execution engine; **neat-evolution** provides the
population management and evolutionary machinery; **neat-training** provides the high-level
training harness that ties everything together. A fourth module (a specific reward/environment
implementation) is deferred to a future ticket.

This ticket delivers **only interfaces, enums, and immutable value classes** — no concrete
implementations. The goal is to establish the contracts between modules so that future
implementation tickets can work independently within each module.

## Approach

1. **Initialize Maven multi-module project** at repo root
   - `pom.xml` — parent POM; Java 21, three child modules declared
   - Each module gets its own `pom.xml` with the parent reference

2. **Module 1 — `neat-core`**: NEAT core data structures and neural-network execution
   - Package: `com.ddiggs.neat.core`
   - Define the gene-level building blocks of a NEAT genome and the phenotype interface

3. **Module 2 — `neat-evolution`**: Population evolution system
   - Package: `com.ddiggs.neat.evolution`
   - Depends on `neat-core`
   - Define strategies and engines for speciation, selection, mutation, crossover, and the
     overall evolutionary cycle

4. **Module 3 — `neat-training`**: Training harness
   - Package: `com.ddiggs.neat.training`
   - Depends on `neat-core` and `neat-evolution`
   - Define the environment abstraction, trainer orchestrator, callbacks, configuration, and
     training result

5. **Write `README.md`** with a project overview and a Mermaid diagram showing module
   dependencies and key interface relationships

6. **Verify** no concrete implementations exist anywhere — only interfaces, enums, and
   final value classes

## Files to Change

### Build files
- `pom.xml` — parent Maven POM; Java 21, UTF-8, three child modules
- `neat-core/pom.xml` — Module 1 descriptor (no inter-module deps)
- `neat-evolution/pom.xml` — Module 2 descriptor (depends on neat-core)
- `neat-training/pom.xml` — Module 3 descriptor (depends on neat-core + neat-evolution)

### Module 1 — `neat-core`
- `neat-core/src/main/java/com/ddiggs/neat/core/NodeType.java` — enum: INPUT, HIDDEN, OUTPUT, BIAS
- `neat-core/src/main/java/com/ddiggs/neat/core/Gene.java` — base marker interface for all genes
- `neat-core/src/main/java/com/ddiggs/neat/core/NodeGene.java` — neuron gene; exposes id, NodeType, bias
- `neat-core/src/main/java/com/ddiggs/neat/core/ConnectionGene.java` — synapse gene; exposes innovation number, in/out node ids, weight, enabled flag
- `neat-core/src/main/java/com/ddiggs/neat/core/Genome.java` — genotype; collections of NodeGene and ConnectionGene; exposes compatibility-distance method signature
- `neat-core/src/main/java/com/ddiggs/neat/core/ActivationFunction.java` — functional interface: `double activate(double x)`
- `neat-core/src/main/java/com/ddiggs/neat/core/NeuralNetwork.java` — phenotype; exposes `double[] activate(double[] inputs)` and `Genome getGenome()`
- `neat-core/src/main/java/com/ddiggs/neat/core/InnovationTracker.java` — global innovation counter; `int getInnovationNumber(int fromNode, int toNode)`

### Module 2 — `neat-evolution`
- `neat-evolution/src/main/java/com/ddiggs/neat/evolution/FitnessEvaluator.java` — `double evaluate(Genome genome)`; implemented in Module 4
- `neat-evolution/src/main/java/com/ddiggs/neat/evolution/Species.java` — group of similar Genomes; exposes representative, members, shared-fitness sum, generation-stagnation counter
- `neat-evolution/src/main/java/com/ddiggs/neat/evolution/Population.java` — manages all Genomes and Species; `List<Species> getSpecies()`, `List<Genome> getGenomes()`, `Genome getChampion()`
- `neat-evolution/src/main/java/com/ddiggs/neat/evolution/SpeciationStrategy.java` — `void speciate(Population population)` — assigns genomes to species
- `neat-evolution/src/main/java/com/ddiggs/neat/evolution/SelectionStrategy.java` — `List<Genome> select(Species species, int count)` — picks parents within a species
- `neat-evolution/src/main/java/com/ddiggs/neat/evolution/CrossoverStrategy.java` — `Genome crossover(Genome parent1, Genome parent2)` — produces offspring
- `neat-evolution/src/main/java/com/ddiggs/neat/evolution/MutationStrategy.java` — `Genome mutate(Genome genome, InnovationTracker tracker)` — applies structural/weight mutations
- `neat-evolution/src/main/java/com/ddiggs/neat/evolution/EvolutionEngine.java` — `Population nextGeneration(Population current, FitnessEvaluator evaluator)` — one full cycle

### Module 3 — `neat-training`
- `neat-training/src/main/java/com/ddiggs/neat/training/TrainingConfig.java` — immutable value class: population size, max generations, fitness threshold, species compatibility threshold, etc.
- `neat-training/src/main/java/com/ddiggs/neat/training/TrainingEnvironment.java` — `void reset()`, `double[] observe()`, `void step(double[] actions)`, `boolean isDone()`, `double reward()`
- `neat-training/src/main/java/com/ddiggs/neat/training/TrainingCallback.java` — `onGenerationComplete(int gen, Population pop, double bestFitness)`, `onTrainingComplete(TrainingResult result)`
- `neat-training/src/main/java/com/ddiggs/neat/training/TrainingResult.java` — immutable value class: champion Genome, generations elapsed, best fitness achieved
- `neat-training/src/main/java/com/ddiggs/neat/training/Trainer.java` — `TrainingResult train(TrainingConfig config, TrainingEnvironment env, TrainingCallback callback)`

### Documentation
- `README.md` — project overview + Mermaid dependency and interface relationship diagrams

## Acceptance Criteria Checklist

- [ ] Structure and interfaces written for Module 1 (neat-core): `Gene`, `NodeGene`,
  `ConnectionGene`, `Genome`, `NeuralNetwork`, `InnovationTracker`, `ActivationFunction`,
  `NodeType`
- [ ] Structure and interfaces written for Module 2 (neat-evolution): `FitnessEvaluator`,
  `Species`, `Population`, `SpeciationStrategy`, `SelectionStrategy`, `CrossoverStrategy`,
  `MutationStrategy`, `EvolutionEngine`
- [ ] Structure and interfaces written for Module 3 (neat-training): `TrainingEnvironment`,
  `TrainingCallback`, `TrainingResult`, `Trainer`, `TrainingConfig`
- [ ] `README.md` written with a Mermaid chart showing how the different parts of the system
  connect
- [ ] No concrete implementations exist — only interfaces, enums, and immutable value classes

## Out of Scope

- Module 4 (specific reward/environment implementation) — explicitly deferred to a future ticket
- Any concrete class that implements the interfaces defined here
- Unit tests
- NEAT-specific hyperparameter tuning or algorithm configuration
- Continuous integration / build tooling beyond the Maven POMs
