package discount.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;

@Service
public class DiscountServiceImpl implements DiscountService {
    @Override
    public BigDecimal calculateDiscountAmount(BigDecimal subTotal) {
        Objects.requireNonNull(subTotal, "subTotal must not be null");
        if (subTotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("subTotal must not be negative");
        }
        BigDecimal discountAmount = BigDecimal.ZERO;
        if(subTotal.compareTo(BigDecimal.valueOf(5_000_000)) >= 0)
        {
            discountAmount = subTotal.multiply(BigDecimal.valueOf(15)).divide(BigDecimal.valueOf(100));
        }
        else if(subTotal.compareTo(BigDecimal.valueOf(1_000_000)) >= 0)
        {
            discountAmount = subTotal.multiply(BigDecimal.valueOf(8)).divide(BigDecimal.valueOf(100));
        }
        return discountAmount;
    }
}
