package common.exception.resource.detailed_exceptions;

import common.exception.resource.ResourceException;
import product.entities.name.ProductName;

public class ProductNotFoundException extends ResourceException
{
        public ProductNotFoundException(Long id)
        {
            super("Product with id " + id + " not found");
        }
        public ProductNotFoundException(Long id,Throwable cause)
        {
            super("Product with id " + id + " not found", cause);
        }
        public ProductNotFoundException(ProductName productName)
        {
            super(String.format(
                    "Product with name [%s] not found", productName.name()
            ));
        }
        public ProductNotFoundException(ProductName productName, Throwable cause)
        {
            super(String.format(
                    "Product with name [%s] not found", productName.name()
            ), cause);
        }
}
