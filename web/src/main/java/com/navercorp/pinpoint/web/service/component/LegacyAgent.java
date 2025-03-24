package com.navercorp.pinpoint.web.service.component;

import org.apache.commons.lang3.ArrayUtils;
import org.semver4j.Semver;

import java.util.List;

public class LegacyAgent {
    private final int[] serviceTypes;
    private final Semver legacySemver;

    public LegacyAgent(List<Integer> serviceType, String legacySemver) {
        this.serviceTypes = ArrayUtils.toPrimitive(serviceType.toArray(new Integer[0]));
        this.legacySemver = parse(legacySemver);
    }

    private Semver parse(String legacySemver) {
        if (legacySemver == null) {
            return null;
        }
        return Semver.parse(legacySemver);
    }

    public boolean isLegacyType(int serviceTypeCode) {
        return ArrayUtils.contains(serviceTypes, serviceTypeCode);
    }

    public boolean isLegacyVersion(String version) {
        if (version == null) {
            return true;
        }
        if (legacySemver == null) {
            return false;
        }
        Semver semver = Semver.parse(version);
        if (semver == null) {
            return false;
        }
        return semver.isLowerThan(legacySemver);
    }
}
