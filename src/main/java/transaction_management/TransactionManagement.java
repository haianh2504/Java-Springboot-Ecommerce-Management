package transaction_management;

import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Objects;

@Component
public class TransactionManagement {
    private final TransactionTemplate transactionTemplate;

    public TransactionManagement(PlatformTransactionManager transactionManager) {
        // TransactionTemplate thay thế việc commit/rollback thủ công trên Connection singleton.
        this.transactionTemplate = new TransactionTemplate(
                Objects.requireNonNull(transactionManager, "transactionManager is null")
        );
    }

    public <T> T execute(TransactionWork<T> work) {
        Objects.requireNonNull(work, "transaction work is null");
        // Spring tự commit khi callback thành công và rollback khi callback ném RuntimeException/Error.
        return transactionTemplate.execute(status -> work.execute());
    }
}
