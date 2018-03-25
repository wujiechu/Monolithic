package demo.v1;


import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import demo.catalog.Catalog;
import demo.catalog.CatalogRepository;
import demo.cataloginfo.CatalogInfo;
import demo.cataloginfo.CatalogInfoRepository;
import demo.inventory.Inventory;
import demo.inventory.InventoryRepository;
import demo.product.Product;
import demo.product.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CatalogServiceV1 {
    private CatalogInfoRepository catalogInfoRepository;
    private InventoryRepository inventoryRepository;
    private CatalogRepository catalogRepository;
    private ProductRepository productRepository;
    @Autowired
    public CatalogServiceV1(ProductRepository productRepository,CatalogRepository catalogRepository,CatalogInfoRepository catalogInfoRepository, InventoryRepository inventoryRepository) {
        this.catalogRepository = catalogRepository;
        this.catalogInfoRepository = catalogInfoRepository;
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
    }

    public Catalog getCatalog() {
        Catalog catalog;

        CatalogInfo activeCatalog = catalogInfoRepository.findCatalogByActive(true);

        catalog = catalogRepository.findCatalogByCatalogNumber(activeCatalog.getCatalogId());
        /*catalog = restTemplate.getForObject(String.format("http://inventory-service/api/catalogs/search/findCatalogByCatalogNumber?catalogNumber=%s",
                activeCatalog.getCatalogId()), Catalog.class);
        ProductsResource products = restTemplate.getForObject(String.format("http://inventory-service/api/catalogs/%s/products",
                catalog.getId()), ProductsResource.class);
        catalog.setProducts(products.getContent().stream().collect(Collectors.toSet()));*/
        List<Product> products = catalog.getProducts();
        for(Product product:products){
            if(inventoryRepository.getAvailableInventoryForProduct(product.getProductId())!=null){
                product.setInStock(true);
            }
        }
        System.out.println(catalog.getProducts());
        return catalog;
    }

    public Product getProduct(String productId) {
        return productRepository.getProductByProductId(productId);
        /*return restTemplate.getForObject(String.format("http://inventory-service/v1/products/%s",
                productId), Product.class);*/
    }
}
