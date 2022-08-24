package com.navercorp.pinpoint.web.service.map;

import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;
import com.navercorp.pinpoint.web.vo.ResponseHistograms;

import java.util.Objects;

public class FilteredMapTimeData {

    private final LinkDataDuplexMap linkDataDuplexMap;
    private final ResponseHistograms responseHistograms;

    FilteredMapTimeData(LinkDataDuplexMap linkDataDuplexMap, ResponseHistograms responseHistograms) {
        this.linkDataDuplexMap = Objects.requireNonNull(linkDataDuplexMap, "linkDataDuplexMap");
        this.responseHistograms = responseHistograms;
    }

    public LinkDataDuplexMap getLinkDataDuplexMap() {
        return linkDataDuplexMap;
    }

    public ResponseHistograms getResponseHistograms() {
        return responseHistograms;
    }
}
