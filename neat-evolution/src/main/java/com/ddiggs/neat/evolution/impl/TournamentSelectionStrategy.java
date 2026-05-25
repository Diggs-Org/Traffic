package com.ddiggs.neat.evolution.impl;

import com.ddiggs.neat.core.Genome;
import com.ddiggs.neat.evolution.SelectionStrategy;
import com.ddiggs.neat.evolution.Species;

import java.util.List;
import java.util.Random;

/**
 * Tournament selection strategy for choosing parent genomes within a species.
 *
 * <p>For each of the {@code count} requested parents, a random sample of
 * {@code min(tournamentSize, |members|)} genomes is drawn from the species member
 * list. The genome with the highest fitness among those sampled is selected.
 * Because fitness is not stored on {@link Genome} directly, this implementation
 * selects by list position (the genome with the smallest index wins ties) within
 * the random sample — callers should pre-sort the member list in descending fitness
 * order before invoking this strategy.
 *
 * <p>The returned list may contain duplicates if {@code count} exceeds the number
 * of distinct members, as guaranteed by the {@link SelectionStrategy} contract.
 */
public class TournamentSelectionStrategy implements SelectionStrategy {

    /**
     * Constructs a {@code TournamentSelectionStrategy}.
     *
     * @param tournamentSize number of candidates per tournament; must be ≥ 1
     * @param random         source of randomness; must not be {@code null}
     * @throws IllegalArgumentException if {@code tournamentSize} is less than 1
     * @throws NullPointerException     if {@code random} is {@code null}
     */
    public TournamentSelectionStrategy(int tournamentSize, Random random) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * {@inheritDoc}
     *
     * <p>Runs {@code count} independent tournaments of size
     * {@code min(tournamentSize, |species.getMembers()|)}.
     *
     * @throws NullPointerException     if {@code species} is {@code null}
     * @throws IllegalArgumentException if {@code count} is not positive
     */
    @Override
    public List<Genome> select(Species species, int count) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
