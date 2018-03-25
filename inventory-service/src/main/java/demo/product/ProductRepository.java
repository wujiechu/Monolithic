package demo.product;

import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

public interface ProductRepository extends GraphRepository<Product> {
    Product getProductByProductId(@Param("productId") String productId);
}
