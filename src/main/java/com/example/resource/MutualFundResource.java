package com.example.resource;

import com.example.model.Dashboard;
import com.example.model.SearchableMutualFund;
import com.example.service.MutualFundService;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Path("mf/api/v1")
@ApplicationScoped
public class MutualFundResource {
    @Inject
    MutualFundService service;

    @GET
    @Path("dashboard/create")
    public List<Dashboard> createDashboard(@QueryParam("schemeCodes") String schemeCodes) {
        List<Long> schemeCodeLongList = Arrays.stream(schemeCodes.split(",")).map(Long::parseLong).collect(Collectors.toList());

        return service.getDashBoardFrom(schemeCodeLongList);
    }

    @GET
    @Path("mutualfund/search")
    public List<SearchableMutualFund> searchMutualFunds(@QueryParam("schemeName") String schemeName) {
        return service.searchMutualFunds(schemeName);
    }

    @GET
    @Path("mutualfund/explore")
    public List<Dashboard> exploreMutualFunds(@QueryParam("schemeName") String schemeName, @QueryParam("sampleSize") int sampleSize) {
        if(sampleSize <= 0) {
            sampleSize = 300;
        }
        return service.exploreMutualFunds(schemeName, sampleSize);
    }
}
