package com.HATW.service;

import com.HATW.dto.ConnectorWalkDetailDTO;

import java.util.List;

public interface TransitService {
    List<ConnectorWalkDetailDTO> computeWalkPath(String jsonData) throws Exception;

    String connectingTrafficWalkPaths(String transitJson) throws Exception;

    String connectingWalkPaths(String transitJson) throws Exception;

    String getRouteWithElevator(String transitJson) throws Exception;
}
