package transaction_management;

@FunctionalInterface
public interface TransactionWork<T> {
    T execute();
}
