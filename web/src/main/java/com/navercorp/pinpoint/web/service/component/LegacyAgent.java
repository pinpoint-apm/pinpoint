package com.navercorp.pinpoint.web.service.component;

import org.apache.commons.lang3.ArrayUtils;
import org.semver4j.Semver;

import java.util.List;

public class LegacyAgent {
    private final short[] serviceTypes;
    private final Semver legacySemver;

    public LegacyAgent(List<Short> serviceType, String legacySemver) {
        this.serviceTypes = ArrayUtils.toPrimitive(serviceType.toArray(new Short[0]));
        this.legacySemver = parse(legacySemver);
    }

    private Semver parse(String legacySemver) {
        if (legacySemver == null) {
            return null;
        }
        return Semver.parse(legacySemver);
    }

    public boolean isLegacyType(short serviceTypeCode) {
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
