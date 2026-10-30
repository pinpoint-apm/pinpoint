
package com.navercorp.pinpoint.plugin.db2;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcConfig;


public class DB2Config extends JdbcConfig {
    private final boolean profileSetAutoCommit;
    private final boolean profileCommit;
    private final boolean profileRollback;

    public DB2Config(ProfilerConfig config) {
        super(config.readBoolean("profiler.jdbc.DB2", false),
                config.readBoolean("profiler.jdbc.DB2.tracesqlbindvalue", config.isTraceSqlBindValue()),
                config.getMaxSqlBindValueSize());
        this.profileSetAutoCommit = config.readBoolean("profiler.jdbc.DB2.setautocommit", false);
        this.profileCommit = config.readBoolean("profiler.jdbc.DB2.commit", false);
        this.profileRollback = config.readBoolean("profiler.jdbc.DB2.rollback", false);
    }

    public boolean isProfileSetAutoCommit() {
        return profileSetAutoCommit;
    }

    public boolean isProfileCommit() {
        return profileCommit;
    }

    public boolean isProfileRollback() {
        return profileRollback;
    }


    @Override
    public String toString() {
        return "DB2Config [" + super.toString() + ", profileSetAutoCommit=" + profileSetAutoCommit + ", profileCommit=" + profileCommit + ", profileRollback=" + profileRollback + "]";
    }

}