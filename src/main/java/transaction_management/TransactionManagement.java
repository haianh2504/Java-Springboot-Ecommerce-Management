package transaction_management;

import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

@Component
public class TransactionManagement {
    private final Connection connection;

    public TransactionManagement(Connection connection) {
        this.connection = Objects.requireNonNull(connection, "connection is null");
    }

    public <T> T execute(TransactionWork<T> work) {
        Objects.requireNonNull(work, "transaction work is null");
        boolean previousAutoCommit = getAutoCommit();

        try {
            connection.setAutoCommit(false);
        } catch (SQLException exception) {
            throw new RuntimeException("Transaction failed", exception);
        }

        try {
            T result = work.execute();
            connection.commit();
            return result;
        } catch (RuntimeException | Error exception) {
            rollbackAfterFailure(exception);
            throw exception;
        } catch (SQLException exception) {
            rollbackAfterFailure(exception);
            throw new RuntimeException("Transaction failed", exception);
        } finally {
            restoreAutoCommit(previousAutoCommit);
        }
    }

    private boolean getAutoCommit() {
        try {
            return connection.getAutoCommit();
        } catch (SQLException exception) {
            throw new RuntimeException("Transaction failed", exception);
        }
    }

    private void rollbackAfterFailure(Throwable originalFailure) {
        try {
            connection.rollback();
        } catch (SQLException rollbackFailure) {
            originalFailure.addSuppressed(rollbackFailure);
        }
    }

    private void restoreAutoCommit(boolean autoCommit) {
        try {
            connection.setAutoCommit(autoCommit);
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to restore auto-commit", exception);
        }
    }
}
