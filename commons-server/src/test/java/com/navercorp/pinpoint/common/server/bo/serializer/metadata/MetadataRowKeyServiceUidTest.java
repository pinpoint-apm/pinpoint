package com.navercorp.pinpoint.common.server.bo.serializer.metadata;

import com.navercorp.pinpoint.common.server.bo.ApiMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.MethodTypeEnum;
import com.navercorp.pinpoint.common.server.bo.SqlUidMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.serializer.metadata.uid.DefaultUidMetaDataRowKey;
import com.navercorp.pinpoint.common.server.bo.serializer.metadata.uid.UidMetaDataRowKey;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MetadataRowKeyServiceUidTest {
    private static final ServiceUid SERVICE = ServiceUid.of(100_001);

    @Test
    void legacyKeysUseDefaultService() {
        assertThat(new DefaultMetaDataRowKey("agent", 10L, 7).getServiceUid())
                .isSameAs(ServiceUid.DEFAULT);
        assertThat(new DefaultUidMetaDataRowKey("agent", 10L, new byte[16]).getServiceUid())
                .isSameAs(ServiceUid.DEFAULT);
    }

    @Test
    void explicitKeysRetainService() {
        assertThat(new DefaultMetaDataRowKey(SERVICE, "agent", 10L, 7).getServiceUid())
                .isEqualTo(SERVICE);
        assertThat(new DefaultUidMetaDataRowKey(SERVICE, "agent", 10L, new byte[16]).getServiceUid())
                .isEqualTo(SERVICE);
    }

    @Test
    void apiBuilderRetainsService() {
        ApiMetaDataBo api = new ApiMetaDataBo.Builder(SERVICE, "agent", 10L, 7, 0, MethodTypeEnum.DEFAULT, "api")
                .build();
        assertThat(api.getServiceUid()).isEqualTo(SERVICE);
    }

    @Test
    void sqlUidBoRequiresMurmur128Length() {
        assertThatThrownBy(() -> new SqlUidMetaDataBo(SERVICE, "agent", 10L, "app", new byte[15], "select 1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sqlUid length");
        assertThat(new SqlUidMetaDataBo(SERVICE, "agent", 10L, "app", new byte[UidMetaDataRowKey.UID_LENGTH], "select 1")
                .getServiceUid()).isEqualTo(SERVICE);
    }
}
