package com.example.service;

import com.example.model.MfApiResponse;
import com.example.model.SearchableMutualFund;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
//import org.jboss.resteasy.annotations.jaxrs.PathParam;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.List;

@RegisterRestClient(configKey="mf-api")
public interface MutualFundApiWebClient {

    @GET
    @Path("{schemeCode}")
    MfApiResponse getMfData(@PathParam("schemeCode") long schemeCode);

    @GET
    List<SearchableMutualFund> getAllMfMetaData();

}
