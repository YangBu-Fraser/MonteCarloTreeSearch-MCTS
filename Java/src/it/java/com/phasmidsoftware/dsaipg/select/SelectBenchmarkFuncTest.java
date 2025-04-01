package com.phasmidsoftware.dsaipg.select;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;

public class SelectBenchmarkFuncTest {

    /**
     * Test to check the output of the runBenchmarks method for a medium dataset size.
     * Verifies that the method executes without exceptions and returns a non-null result.
     */
    @Test
    public void testRunBenchmarks_MediumDataset() throws IOException {
        // Arrange
        SelectBenchmark selectBenchmark = new SelectBenchmark(50, 1000);

        // Act
        String result = selectBenchmark.runBenchmarks();

        // Assert
        assertNotNull("Result should not be null for a medium dataset", result);
    }

    /**
     * Test to ensure that the runBenchmarks method can handle a high number of runs.
     * Verifies that the method executes and produces a non-null output.
     */
    @Test
    public void testRunBenchmarks_HighRuns() throws IOException {
        // Arrange
        SelectBenchmark selectBenchmark = new SelectBenchmark(1000, 500);

        // Act
        String result = selectBenchmark.runBenchmarks();

        // Assert
        assertNotNull("Result should not be null for a high number of runs", result);
    }
}