package com.example.service;

import com.example.model.*;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@ApplicationScoped
public class MutualFundService {

    @Inject
    LocalSearchService searchService;

    @Inject @RestClient
    MutualFundApiWebClient webClient;

    @Inject
    StatisticsCalculator statisticsCalculator;

    private static final String DATE_PATTERN = "dd-MM-yyyy";
    private static final Map<Long, MutualFund> cache = new HashMap<>();

    public List<Dashboard> getDashBoardFrom(List<Long> schemeCodes) {
        return schemeCodes.stream()
                .map(this::getStatistics)
                .map(Dashboard::new)
                .sorted((d1, d2) -> d2.getMutualFundStatistics().getMutualFundMeta().getSchemeCode().compareTo(d1.getMutualFundStatistics().getMutualFundMeta().getSchemeCode()))
                .collect(Collectors.toList());
    }

    public List<SearchableMutualFund> searchMutualFunds(String schemeName) {
        return searchForMutualFund(schemeName, 1, 25);
    }

    public List<Dashboard> exploreMutualFunds(String schemeName, int sampleSize) {
        return searchForMutualFund(schemeName, 1, sampleSize)
                .stream()
                .map(searchableMutualFund -> getStatistics(searchableMutualFund.getSchemeCode()))
                .map(Dashboard::new)
                .sorted((d1, d2) -> d2.getMutualFundStatistics().getPercentageIncrease().compareTo(d1.getMutualFundStatistics().getPercentageIncrease()))
                .limit(100)
                .sorted(Comparator.comparing(d -> d.getMutualFundStatistics().getPercentageIncrease()))
                .collect(Collectors.toList());
    }

    public MutualFund retrieveMutualFundNav(long schemeCode) {
        synchronized (String.valueOf(schemeCode).intern()) {
            if (cache.containsKey(schemeCode)) {
                return cache.get(schemeCode);
            } else {
                return downloadMutualFundNav(schemeCode);
            }
        }
    }

    public MutualFundStatistics getStatistics(long schemeCode) {
        return statisticsCalculator.getStatistics(retrieveMutualFundNav(schemeCode));
    }

    private MutualFund downloadMutualFundNav(Long schemeCode) {
        MfApiResponse downloaded = webClient.getMfData(schemeCode);

        if(nonNull(downloaded) && nonNull(downloaded.getMfMetaData()) && nonNull(downloaded.getMfMetaData().getSchemeCode())) {
            List<NavPerDate> mutualFundNavPerDateList = downloaded.getNavDataList().stream().map(navData -> new NavPerDate(schemeCode,
                    LocalDate.parse(navData.getDate(), DateTimeFormatter.ofPattern(DATE_PATTERN)), BigDecimal.valueOf(Double.parseDouble(navData.getNav()))))
                    .collect(Collectors.toList());

            MutualFund mutualFund = new MutualFund()
                    .setMeta(new MutualFundMeta().setSchemeCode(schemeCode)
                            .setSchemeCategory(downloaded.getMfMetaData().getSchemeCategory())
                            .setSchemeType(downloaded.getMfMetaData().getSchemeType())
                            .setSchemeName(downloaded.getMfMetaData().getSchemeName())
                            .setFundHouse(downloaded.getMfMetaData().getFundHouse()))
                    .setNavPerDates(mutualFundNavPerDateList);

            cache.put(schemeCode, mutualFund);
            return mutualFund;
        }

        return null;
    }

    @SuppressWarnings("SameParameterValue")
    private List<SearchableMutualFund> searchForMutualFund(String schemeName, int page, int size) {
        synchronized (this) {
            if(searchService.indexSize() <= 0) {
                searchService.saveAll(webClient.getAllMfMetaData());
            }
        }

        return searchService.searchByTag(schemeName, page, size);
    }
}
