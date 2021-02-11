package com.example.model;

public class Dashboard {
    private MutualFundStatistics mutualFundStatistics;

    public Dashboard() {}

    public Dashboard(MutualFundStatistics mutualFundStatistics) {
        this.mutualFundStatistics = mutualFundStatistics;
    }

    public MutualFundStatistics getMutualFundStatistics() {
        return mutualFundStatistics;
    }
}
