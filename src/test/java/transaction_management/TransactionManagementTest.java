package transaction_management;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionManagementTest {
    @Test
    @DisplayName("A successful transaction commits and restores the connection auto-commit setting")
    void execute_successfulWork_commitsAndRestoresAutoCommit() throws SQLException
    {
        // --GIVEN--
        Connection connection = mock(Connection.class);
        @SuppressWarnings("unchecked")
        TransactionWork<String> work = mock(TransactionWork.class);
        when(connection.getAutoCommit()).thenReturn(true);
        when(work.execute()).thenReturn("completed");
        TransactionManagement transactionManagement = new TransactionManagement(connection);

        // --WHEN--
        String result = transactionManagement.execute(work);

        // --THEN--
        assertEquals("completed", result);
        var inOrder = inOrder(connection, work);
        inOrder.verify(connection).getAutoCommit();
        inOrder.verify(connection).setAutoCommit(false);
        inOrder.verify(work).execute();
        inOrder.verify(connection).commit();
        inOrder.verify(connection).setAutoCommit(true);
        verify(connection, never()).rollback();
    }

    @Test
    @DisplayName("A failed transaction rolls back and restores the connection auto-commit setting")
    void execute_runtimeFailure_rollsBackAndRestoresAutoCommit() throws SQLException
    {
        // --GIVEN--
        Connection connection = mock(Connection.class);
        @SuppressWarnings("unchecked")
        TransactionWork<String> work = mock(TransactionWork.class);
        RuntimeException failure = new RuntimeException("checkout failed");
        when(connection.getAutoCommit()).thenReturn(true);
        when(work.execute()).thenThrow(failure);
        TransactionManagement transactionManagement = new TransactionManagement(connection);

        // --WHEN--
        RuntimeException actualException = assertThrows(
                RuntimeException.class,
                () -> transactionManagement.execute(work)
        );

        // --THEN--
        assertSame(failure, actualException);
        var inOrder = inOrder(connection, work);
        inOrder.verify(connection).setAutoCommit(false);
        inOrder.verify(work).execute();
        inOrder.verify(connection).rollback();
        inOrder.verify(connection).setAutoCommit(true);
        verify(connection, never()).commit();
    }

    @Test
    @DisplayName("Executing null transaction work throws NullPointerException before using the connection")
    void execute_nullWork_throwsNullPointerException()
    {
        // --GIVEN--
        Connection connection = mock(Connection.class);
        TransactionManagement transactionManagement = new TransactionManagement(connection);

        // --WHEN--
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> transactionManagement.execute(null)
        );

        // --THEN--
        assertEquals("transaction work is null", exception.getMessage());
        verifyNoInteractions(connection);
    }
}
