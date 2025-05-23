package br.com.siecola.productsservice.products.repository;

import br.com.siecola.productsservice.products.models.Product;
import com.amazonaws.xray.spring.aop.XRayEnabled;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.PagePublisher;
import software.amazon.awssdk.enhanced.dynamodb.model.UpdateItemEnhancedRequest;

import java.util.concurrent.CompletableFuture;

@Repository
@XRayEnabled
public class ProductsRepository {

    private static final Logger LOG = LogManager.getLogger(ProductsRepository.class);

    private final DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;
    private final DynamoDbAsyncTable<Product> productsTable;

    @Autowired
    public ProductsRepository(
            DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient, @Value("${aws.productsddb.name}") String productsDdbName) {
        this.dynamoDbEnhancedAsyncClient = dynamoDbEnhancedAsyncClient;
        this.productsTable = dynamoDbEnhancedAsyncClient
                .table(productsDdbName, TableSchema.fromBean(Product.class));
    }

    public PagePublisher<Product> getAll() {
        // DO not use this method in production
        return productsTable.scan();
    }

    public CompletableFuture<Product> getById(String productId) {
        LOG.info("ProductID: {}", productId);
        return productsTable.getItem(Key.builder()
                .partitionValue(productId)
                .build());
    }

    public CompletableFuture<Void> create(Product product) {
        return productsTable.putItem(product);
    }

    public CompletableFuture<Product> deleteById(String productId) {
        return productsTable.deleteItem(Key.builder()
                .partitionValue(productId)
                .build());
    }

    public CompletableFuture<Product> update(Product product, String productId) {
        product.setId(productId);
        return productsTable.updateItem(
                UpdateItemEnhancedRequest.builder(Product.class)
                        .item(product)
                        .conditionExpression(Expression.builder()
                                .expression("attribute_exists(id)")
                                .build())
                        .build());
    }
}
