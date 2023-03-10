package com.navercorp.pinpoint.pinot.datasource;

import org.apache.pinot.client.PinotConnection;
import org.apache.pinot.client.PinotDriver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Woonduk Kang(emeroad)
 */
public class PinotDataSourceTest {

    @Test
    public void close() throws Exception {
        PinotDriver driver = mock(PinotDriver.class);
        PinotConnection pinotConnection = mock(PinotConnection.class);
        when(driver.connect(any(), any())).thenReturn(pinotConnection);


        final PinotDataSource dataSource = spy(new PinotDataSource(driver));
        try (dataSource) {
            Connection con1 = dataSource.getConnection();
            Assertions.assertFalse(con1.isClosed());
            con1.close();
            Assertions.assertTrue(con1.isClosed());

            Connection con2 = dataSource.getConnection();
            Assertions.assertFalse(con2.isClosed());
            con2.close();
            Assertions.assertTrue(con2.isClosed());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        verify(pinotConnection).close();
        verify(dataSource).close();
    }

    @Test
    public void unwrap() throws Exception {
        PinotDriver driver = mock(PinotDriver.class);
        PinotConnection pinotConnection = mock(PinotConnection.class);
        when(driver.connect(any(), any())).thenReturn(pinotConnection);


        final PinotDataSource dataSource = new PinotDataSource(driver);
        try (dataSource) {
            Connection con1 = dataSource.getConnection();
            PinotConnection unwrap = con1.unwrap(PinotConnection.class);
            con1.close();

            Assertions.assertSame(pinotConnection, unwrap);
            Assertions.assertFalse(pinotConnection.isClosed());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


//    @Test
//    public void executeAsync() throws Exception {
//        PinotDriver driver = mock(PinotDriver.class);
//        PinotConnection pinotConnection = mock(PinotConnection.class);
//        when(driver.connect(any(), any())).thenReturn(pinotConnection);
//
//
//        final PinotDataSource dataSource = new PinotDataSource(driver);
//        try (dataSource) {
//            Connection con1 = dataSource.getConnection();
//            PinotConnection unwrap = con1.unwrap(PinotConnection.class);
//            con1.close();
//
//            org.apache.pinot.client.Connection session = unwrap.getSession();
//            Future<ResultSetGroup> future = session.executeAsync(new Request("sql", "~~~"));
//
//            Future<ResultSet> mappingFuture = new Future<>() {
//
//                @Override
//                public boolean cancel(boolean mayInterruptIfRunning) {
//                    return future.cancel(mayInterruptIfRunning);
//                }
//
//                @Override
//                public boolean isCancelled() {
//                    return future.isCancelled();
//                }
//
//                @Override
//                public boolean isDone() {
//                    return future.isDone();
//                }
//
//                @Override
//                public ResultSet get() throws InterruptedException, ExecutionException {
//                    ResultSetGroup resultSetGroup = future.get();
//                    ResultSet resultSet = toResultSet(resultSetGroup);
//                    return resultSet;
//                }
//
//                private ResultSet toResultSet(ResultSetGroup resultSetGroup) {
//                    return null;
//                }
//
//                @Override
//                public ResultSet get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
//                    return null;
//                }
//            };
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
}