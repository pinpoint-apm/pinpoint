package com.navercorp.pinpoint.profiler.context.provider;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.profiler.instrument.ASMGuardedInterceptorFactory;
import com.navercorp.pinpoint.profiler.instrument.DisableGuardedInterceptorFactory;
import com.navercorp.pinpoint.profiler.instrument.classloading.BootstrapCore;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GuardedInterceptorFactoryProviderTest {

    private final BootstrapCore bootstrapCore = mock(BootstrapCore.class);

    @Test
    void codegenEnabledByDefault() {
        ProfilerConfig config = mock(ProfilerConfig.class);
        when(config.readBoolean(GuardedInterceptorFactoryProvider.GUARD_CODEGEN_KEY, true)).thenReturn(true);

        assertThat(new GuardedInterceptorFactoryProvider(config, bootstrapCore).get())
                .isInstanceOf(ASMGuardedInterceptorFactory.class);
    }

    @Test
    void codegenDisabled() {
        ProfilerConfig config = mock(ProfilerConfig.class);
        when(config.readBoolean(GuardedInterceptorFactoryProvider.GUARD_CODEGEN_KEY, true)).thenReturn(false);

        assertThat(new GuardedInterceptorFactoryProvider(config, bootstrapCore).get())
                .isInstanceOf(DisableGuardedInterceptorFactory.class);
    }
}
