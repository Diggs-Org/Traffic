# NEAT — NeuroEvolution of Augmenting Topologies

A multi-module Java 25 scaffold for the NEAT algorithm by Kenneth O. Stanley and Risto
Miikkulainen. NEAT simultaneously evolves the **topology** (structure) and **weights** of
artificial neural networks, starting from minimal networks and complexifying over generations.

> **Status:** Interface-only scaffold. No concrete implementations exist yet — this is the
> architectural skeleton that future implementation tickets will fill in.

---

## Module Overview

| Module | Package | Role |
|--------|---------|------|
| [`neat-core`](neat-core/) | `com.ddiggs.neat.core` | Gene types, Genome genotype, NeuralNetwork phenotype, InnovationTracker, ActivationFunction |
| [`neat-evolution`](neat-evolution/) | `com.ddiggs.neat.evolution` | Population management, Species, all pluggable strategy interfaces, EvolutionEngine |
| [`neat-training`](neat-training/) | `com.ddiggs.neat.training` | Training harness, TrainingEnvironment, Trainer, TrainingConfig, TrainingResult, TrainingCallback |
| *(neat-reward)* | *(future)* | Concrete task environment and FitnessEvaluator implementation |

---

## Module Dependency Graph

```mermaid
graph TD
    core["<b>neat-core</b><br/>Gene · NodeGene · ConnectionGene<br/>Genome · NeuralNetwork<br/>InnovationTracker · ActivationFunction"]
    evolution["<b>neat-evolution</b><br/>Population · Species<br/>EvolutionEngine<br/>Speciation · Selection<br/>Crossover · Mutation"]
    training["<b>neat-training</b><br/>Trainer · TrainingEnvironment<br/>TrainingConfig · TrainingResult<br/>TrainingCallback"]
    reward["<b>neat-reward</b><br/><i>(future)</i><br/>Concrete FitnessEvaluator<br/>Concrete TrainingEnvironment"]

    evolution -->|depends on| core
    training -->|depends on| core
    training -->|depends on| evolution
    reward -->|depends on| training
    reward -->|depends on| evolution
    reward -->|depends on| core
```

---

## Interface Relationship Diagram

```mermaid
classDiagram
    direction TB

    class Gene {
        <<interface>>
        +getId() int
    }
    class NodeGene {
        <<interface>>
        +getNodeType() NodeType
        +getBias() double
    }
    class ConnectionGene {
        <<interface>>
        +getInnovationNumber() int
        +getInNodeId() int
        +getOutNodeId() int
        +getWeight() double
        +isEnabled() boolean
    }
    class NodeType {
        <<enumeration>>
        INPUT
        HIDDEN
        OUTPUT
        BIAS
    }
    class ActivationFunction {
        <<interface>>
        +activate(double) double
    }
    class Genome {
        <<interface>>
        +getNodeGenes() List~NodeGene~
        +getConnectionGenes() List~ConnectionGene~
        +compatibilityDistance(Genome,double,double,double) double
    }
    class NeuralNetwork {
        <<interface>>
        +activate(double[]) double[]
        +getGenome() Genome
    }
    class InnovationTracker {
        <<interface>>
        +getInnovationNumber(int,int) int
        +getCurrentInnovationNumber() int
        +reset() void
    }

    class FitnessEvaluator {
        <<interface>>
        +evaluate(Genome) double
    }
    class Species {
        <<interface>>
        +getId() int
        +getRepresentative() Genome
        +getMembers() List~Genome~
        +getSharedFitnessSum() double
        +getBestFitness() double
        +getGenerationsSinceImprovement() int
    }
    class Population {
        <<interface>>
        +getGenomes() List~Genome~
        +getSpecies() List~Species~
        +getChampion() Genome
        +getSize() int
        +getGeneration() int
    }
    class SpeciationStrategy {
        <<interface>>
        +speciate(Population) void
    }
    class SelectionStrategy {
        <<interface>>
        +select(Species,int) List~Genome~
    }
    class CrossoverStrategy {
        <<interface>>
        +crossover(Genome,Genome) Genome
    }
    class MutationStrategy {
        <<interface>>
        +mutate(Genome,InnovationTracker) Genome
    }
    class EvolutionEngine {
        <<interface>>
        +nextGeneration(Population,FitnessEvaluator) Population
    }

    class TrainingEnvironment {
        <<interface>>
        +reset() void
        +observe() double[]
        +step(double[]) void
        +isDone() boolean
        +reward() double
        +getObservationSize() int
        +getActionSize() int
    }
    class TrainingConfig {
        <<record>>
        +populationSize int
        +maxGenerations int
        +fitnessThreshold double
        +compatibilityThreshold double
        +compatibilityModifier double
        +targetSpeciesCount int
    }
    class TrainingResult {
        <<record>>
        +champion Genome
        +generationsElapsed int
        +bestFitness double
    }
    class TrainingCallback {
        <<interface>>
        +onGenerationComplete(int,Population,double) void
        +onTrainingComplete(TrainingResult) void
    }
    class Trainer {
        <<interface>>
        +train(TrainingConfig,TrainingEnvironment,TrainingCallback) TrainingResult
    }

    Gene <|-- NodeGene
    Gene <|-- ConnectionGene
    NodeGene --> NodeType
    Genome --> NodeGene
    Genome --> ConnectionGene
    NeuralNetwork --> Genome
    NeuralNetwork --> ActivationFunction

    Species --> Genome
    Population --> Genome
    Population --> Species
    EvolutionEngine --> Population
    EvolutionEngine --> FitnessEvaluator
    SpeciationStrategy --> Population
    SelectionStrategy --> Species
    CrossoverStrategy --> Genome
    MutationStrategy --> Genome
    MutationStrategy --> InnovationTracker

    TrainingResult --> Genome
    TrainingCallback --> Population
    TrainingCallback --> TrainingResult
    Trainer --> TrainingConfig
    Trainer --> TrainingEnvironment
    Trainer --> TrainingCallback
    Trainer --> TrainingResult
```

---

## NEAT Algorithm — Generational Loop

```mermaid
sequenceDiagram
    participant T as Trainer
    participant E as EvolutionEngine
    participant F as FitnessEvaluator
    participant Sp as SpeciationStrategy
    participant Sel as SelectionStrategy
    participant C as CrossoverStrategy
    participant M as MutationStrategy

    T->>E: nextGeneration(population, evaluator)
    loop for each genome
        E->>F: evaluate(genome)
        F-->>E: fitness score
    end
    E->>Sp: speciate(population)
    loop for each species (by offspring allocation)
        E->>Sel: select(species, count)
        Sel-->>E: parents
        E->>C: crossover(parent1, parent2)
        C-->>E: child genome
        E->>M: mutate(child, innovationTracker)
        M-->>E: mutated genome
    end
    E-->>T: next Population
    T->>T: onGenerationComplete callback
```

---

## Getting Started

**Prerequisites:** Java 25+, Maven 3.9+

```bash
# Build all modules
mvn compile

# (After implementations are added)
mvn test
mvn package
```

---

## Architecture Notes

- **Interfaces only.** No concrete classes implement these interfaces yet. Future tickets
  will add implementations in their respective modules and in `neat-reward`.
- **`FitnessEvaluator`** is declared in `neat-evolution` (not `neat-training`) because it is
  a direct input to `EvolutionEngine`. Concrete implementations belong in `neat-reward`.
- **`TrainingConfig` and `TrainingResult`** are Java **records** — immutable, structurally
  transparent, and automatically equipped with `equals`, `hashCode`, and `toString`.
- **Strategy pattern** throughout `neat-evolution` means every algorithm variant
  (e.g. tournament vs roulette selection) is a swappable implementation, with no
  changes to the engine itself.
- **`InnovationTracker`** is owned by `neat-core` because innovation numbers are a property
  of the genome representation, not of any particular evolution strategy.
