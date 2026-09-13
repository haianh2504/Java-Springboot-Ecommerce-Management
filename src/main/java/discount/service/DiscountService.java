package discount.service;

import java.math.BigDecimal;

public interface DiscountService {
    BigDecimal calculateDiscountAmount(BigDecimal subTotal);
}
