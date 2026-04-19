package cvut.fel.sit.mojefinance.product.api.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Arrays;
import java.math.BigDecimal;
import cvut.fel.sit.mojefinance.openapi.model.*;

class ProductsMapperTest {
    private ProductsMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(ProductsMapper.class);
    }

    @Test
    void toProductsResponse_mapsFieldsCorrectly() {
        cvut.fel.sit.mojefinance.product.domain.entity.ProductIdentification identification = cvut.fel.sit.mojefinance.product.domain.entity.ProductIdentification.builder().iban("iban123").productNumber("prod123").build();
        cvut.fel.sit.mojefinance.product.domain.entity.Amount amount = cvut.fel.sit.mojefinance.product.domain.entity.Amount.builder().currency("CZK").value(new BigDecimal("100.00")).build();
        cvut.fel.sit.mojefinance.product.domain.entity.BankDetails bankDetails = cvut.fel.sit.mojefinance.product.domain.entity.BankDetails.builder().bankName("BankX").clientRegistrationId("reg123").build();
        cvut.fel.sit.mojefinance.product.domain.entity.Product product = cvut.fel.sit.mojefinance.product.domain.entity.Product.builder()
                .productId("p1")
                .accountName("acc1")
                .productIdentification(identification)
                .currency("CZK")
                .ownersNames(List.of("Alice"))
                .productCategory(cvut.fel.sit.shared.entity.ProductCategory.LOAN)
                .productName("Loan Product")
                .balance(amount)
                .manuallyCreated(true)
                .bankCode("123")
                .bankDetails(bankDetails)
                .build();
        cvut.fel.sit.mojefinance.product.domain.dto.ProductsResponse productsResponse = cvut.fel.sit.mojefinance.product.domain.dto.ProductsResponse.builder().products(List.of(product)).build();
        ProductsResponse result = mapper.toProductsResponse(productsResponse);
        assertNotNull(result);
        assertEquals(1, result.getProducts().size());
        Product mapped = result.getProducts().get(0);
        assertEquals("p1", mapped.getProductId());
        assertEquals("acc1", mapped.getAccountName());
        assertEquals("CZK", mapped.getCurrency());
        assertEquals("Loan", mapped.getProductCategory());
        assertEquals(true, mapped.getManuallyCreated());
        assertEquals("123", mapped.getBankCode());
        assertEquals("BankX", mapped.getBankDetails().getBankName());
        assertEquals("reg123", mapped.getBankDetails().getClientRegistrationId());
        assertEquals("iban123", mapped.getProductIdentification().getIban());
        assertEquals("prod123", mapped.getProductIdentification().getProductNumber());
        assertEquals("Alice", mapped.getOwnersNames().get(0));
        assertEquals("CZK", mapped.getBalance().getCurrency());
        assertEquals(100.00, mapped.getBalance().getValue());
    }

    @Test
    void toAssetsAndLiabilitiesResponse_mapsFieldsCorrectly() {
        cvut.fel.sit.mojefinance.product.domain.entity.Amount amount = cvut.fel.sit.mojefinance.product.domain.entity.Amount.builder().currency("CZK").value(new BigDecimal("500.00")).build();
        cvut.fel.sit.mojefinance.product.domain.entity.Product product = cvut.fel.sit.mojefinance.product.domain.entity.Product.builder().productId("p2").productName("Asset Product").build();
        cvut.fel.sit.mojefinance.product.domain.entity.GroupedProducts grouped = cvut.fel.sit.mojefinance.product.domain.entity.GroupedProducts.builder().groupName("GroupA").products(List.of(product)).totalAmount(amount).build();
        cvut.fel.sit.mojefinance.product.domain.entity.AssetLiability assets = cvut.fel.sit.mojefinance.product.domain.entity.AssetLiability.builder().totalAmount(amount).groupedProducts(List.of(grouped)).build();
        cvut.fel.sit.mojefinance.product.domain.entity.AssetLiability liabilities = cvut.fel.sit.mojefinance.product.domain.entity.AssetLiability.builder().totalAmount(amount).groupedProducts(List.of(grouped)).build();
        cvut.fel.sit.mojefinance.product.domain.dto.AssetsAndLiabilitiesResponse dto = cvut.fel.sit.mojefinance.product.domain.dto.AssetsAndLiabilitiesResponse.builder().assets(assets).liabilities(liabilities).build();
        AssetsAndLiabilitiesResponse result = mapper.toAssetsAndLiabilitiesResponse(dto);
        assertNotNull(result);
        assertNotNull(result.getAssets());
        assertNotNull(result.getLiabilities());
        assertEquals("GroupA", result.getAssets().getGroupedProducts().get(0).getGroupName());
        assertEquals("p2", result.getAssets().getGroupedProducts().get(0).getProducts().get(0).getProductId());
        assertEquals(500.00, result.getAssets().getTotalAmount().getValue());
    }

    @Test
    void toProduct_mapsFieldsCorrectly() {
        cvut.fel.sit.mojefinance.product.domain.entity.ProductIdentification identification = cvut.fel.sit.mojefinance.product.domain.entity.ProductIdentification.builder().iban("iban456").productNumber("prod456").build();
        cvut.fel.sit.mojefinance.product.domain.entity.Amount amount = cvut.fel.sit.mojefinance.product.domain.entity.Amount.builder().currency("USD").value(new BigDecimal("250.00")).build();
        cvut.fel.sit.mojefinance.product.domain.entity.BankDetails bankDetails = cvut.fel.sit.mojefinance.product.domain.entity.BankDetails.builder().bankName("BankY").clientRegistrationId("reg456").build();
        cvut.fel.sit.mojefinance.product.domain.entity.Product product = cvut.fel.sit.mojefinance.product.domain.entity.Product.builder()
                .productId("p3")
                .accountName("acc3")
                .productIdentification(identification)
                .currency("USD")
                .ownersNames(Arrays.asList("Bob", "Carol"))
                .productCategory(cvut.fel.sit.shared.entity.ProductCategory.CREDIT_CARD)
                .productName("Credit Card")
                .balance(amount)
                .manuallyCreated(false)
                .bankCode("456")
                .bankDetails(bankDetails)
                .build();
        Product result = mapper.toProduct(product);
        assertNotNull(result);
        assertEquals("p3", result.getProductId());
        assertEquals("acc3", result.getAccountName());
        assertEquals("USD", result.getCurrency());
        assertEquals("Credit Card", result.getProductCategory());
        assertEquals(false, result.getManuallyCreated());
        assertEquals("456", result.getBankCode());
        assertEquals("BankY", result.getBankDetails().getBankName());
        assertEquals("reg456", result.getBankDetails().getClientRegistrationId());
        assertEquals("iban456", result.getProductIdentification().getIban());
        assertEquals("prod456", result.getProductIdentification().getProductNumber());
        assertEquals("Bob", result.getOwnersNames().get(0));
        assertEquals("USD", result.getBalance().getCurrency());
        assertEquals(250.00, result.getBalance().getValue());
    }
}
