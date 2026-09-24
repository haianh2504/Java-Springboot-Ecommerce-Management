package transaction_management;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionManagementTest {
    @Test
    @DisplayName("A successful transaction commits through Spring's transaction manager")
    void execute_successfulWork_commitsThroughTransactionManager()
    {
        // --GIVEN--
        PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
        TransactionStatus transactionStatus = mock(TransactionStatus.class);
        @SuppressWarnings("unchecked")
        TransactionWork<String> work = mock(TransactionWork.class);
        when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);
        when(work.execute()).thenReturn("completed");
        TransactionManagement transactionManagement = new TransactionManagement(transactionManager);

        // --WHEN--
        String result = transactionManagement.execute(work);

        // --THEN--
        assertEquals("completed", result);
        var inOrder = inOrder(transactionManager, work);
        inOrder.verify(transactionManager).getTransaction(any());
        inOrder.verify(work).execute();
        inOrder.verify(transactionManager).commit(transactionStatus);
        verify(transactionManager, never()).rollback(any());
    }

    @Test
    @DisplayName("A failed transaction rolls back through Spring's transaction manager")
    void execute_runtimeFailure_rollsBackThroughTransactionManager()
    {
        // --GIVEN--
        PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
        TransactionStatus transactionStatus = mock(TransactionStatus.class);
        @SuppressWarnings("unchecked")
        TransactionWork<String> work = mock(TransactionWork.class);
        RuntimeException failure = new RuntimeException("checkout failed");
        when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);
        when(work.execute()).thenThrow(failure);
        TransactionManagement transactionManagement = new TransactionManagement(transactionManager);

        // --WHEN--
        RuntimeException actualException = assertThrows(
                RuntimeException.class,
                () -> transactionManagement.execute(work)
        );

        // --THEN--
        assertSame(failure, actualException);
        var inOrder = inOrder(transactionManager, work);
        inOrder.verify(transactionManager).getTransaction(any());
        inOrder.verify(work).execute();
        inOrder.verify(transactionManager).rollback(transactionStatus);
        verify(transactionManager, never()).commit(any());
    }

    @Test
    @DisplayName("Executing null transaction work throws NullPointerException before opening a transaction")
    void execute_nullWork_throwsNullPointerException()
    {
        // --GIVEN--
        PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
        TransactionManagement transactionManagement = new TransactionManagement(transactionManager);

        // --WHEN--
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> transactionManagement.execute(null)
        );

        // --THEN--
        assertEquals("transaction work is null", exception.getMessage());
        verifyNoInteractions(transactionManager);
    }
}
