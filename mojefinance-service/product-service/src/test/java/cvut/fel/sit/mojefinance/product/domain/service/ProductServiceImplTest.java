package cvut.fel.sit.mojefinance.product.domain.service;

import cvut.fel.sit.mojefinance.bank.domain.dto.ConnectedBanksResponse;
import cvut.fel.sit.mojefinance.bank.domain.entity.BankConnection;
import cvut.fel.sit.mojefinance.bank.domain.service.BankConnectionService;
import cvut.fel.sit.mojefinance.product.domain.dto.AssetsAndLiabilitiesResponse;
import cvut.fel.sit.mojefinance.product.domain.dto.ProductsResponse;
import cvut.fel.sit.mojefinance.product.domain.entity.AssetLiability;
import cvut.fel.sit.mojefinance.product.domain.entity.BankDetails;
import cvut.fel.sit.mojefinance.product.domain.entity.Product;
import cvut.fel.sit.mojefinance.product.domain.helper.ProductHelper;
import cvut.fel.sit.shared.entity.ProductType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private BankConnectionService bankConnectionService;

    @Mock
    private ProductHelper productHelper;

    @InjectMocks
    private ProductServiceImpl productService;

    private final String PRINCIPAL_NAME = "testUser";

    @BeforeEach
    void setUp() {
        // Mock the Spring Security Context
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn(PRINCIPAL_NAME);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getProducts_WithRealBanks_ShouldFetchAndEnrichProducts() {
        // 1. Arrange
        ConnectedBanksResponse connectedBanksResponse = ConnectedBanksResponse.builder().build();
        BankConnection realBankConnection = new BankConnection();
        realBankConnection.setClientRegistrationId("test-bank-id");

        List<BankConnection> activeConnections = List.of(realBankConnection);
        List<BankConnection> realConnections = List.of(realBankConnection);

        BankDetails bankDetails = new BankDetails();
        String authToken = "Bearer test-token";

        Product product1 = new Product();
        Product product2 = new Product();
        List<Product> mockProducts = List.of(product1, product2);

        // Setup Mocks
        when(bankConnectionService.getConnectedBanks()).thenReturn(connectedBanksResponse);
        when(productHelper.filterBanksWithActiveConnection(connectedBanksResponse)).thenReturn(activeConnections);
        when(productHelper.filterRealBanks(activeConnections)).thenReturn(realConnections);

        when(productHelper.constructAuthorizationHeader("test-bank-id")).thenReturn(authToken);
        when(productHelper.mapBankDetails(realBankConnection)).thenReturn(bankDetails);
        when(productHelper.getProductsFromExternalApi(bankDetails, authToken, PRINCIPAL_NAME)).thenReturn(mockProducts);

        // Enrich is a void method, doNothing() is default for mocks, but good to be explicit
        doNothing().when(productHelper).enrichProducts(mockProducts, bankDetails, authToken, PRINCIPAL_NAME);

        // 2. Act
        ProductsResponse response = productService.getProducts();

        // 3. Assert
        assertNotNull(response);
        assertEquals(2, response.getProducts().size());

        // Verify workflow methods were called
        verify(productHelper, times(1)).getProductsFromExternalApi(bankDetails, authToken, PRINCIPAL_NAME);
        verify(productHelper, times(1)).enrichProducts(mockProducts, bankDetails, authToken, PRINCIPAL_NAME);
    }

    @Test
    void getProducts_WithNoRealBanks_ShouldReturnEmptyList() {
        // 1. Arrange
        ConnectedBanksResponse connectedBanksResponse = ConnectedBanksResponse.builder().build();

        when(bankConnectionService.getConnectedBanks()).thenReturn(connectedBanksResponse);
        when(productHelper.filterBanksWithActiveConnection(connectedBanksResponse)).thenReturn(Collections.emptyList());
        when(productHelper.filterRealBanks(Collections.emptyList())).thenReturn(Collections.emptyList());

        // 2. Act
        ProductsResponse response = productService.getProducts();

        // 3. Assert
        assertNotNull(response);
        assertTrue(response.getProducts().isEmpty());

        // Verify external API was never called since there are no banks
        verify(productHelper, never()).getProductsFromExternalApi(any(), any(), any());
    }

    @Test
    void getAssetsAndLiabilities_ShouldCalculateAndReturnBoth() {
        // 1. Arrange
        // Mock getProducts() internal flow to return an empty list just to get past the initial call
        ConnectedBanksResponse connectedBanksResponse = ConnectedBanksResponse.builder().build();
        when(bankConnectionService.getConnectedBanks()).thenReturn(connectedBanksResponse);
        when(productHelper.filterBanksWithActiveConnection(connectedBanksResponse)).thenReturn(Collections.emptyList());
        when(productHelper.filterRealBanks(Collections.emptyList())).thenReturn(Collections.emptyList());

        // Mock the Assets/Liabilities calculation
        AssetLiability mockAssets = new AssetLiability();
        AssetLiability mockLiabilities = new AssetLiability();

        when(productHelper.buildAssetLiability(anyList(), eq(ProductType.ASSET))).thenReturn(mockAssets);
        when(productHelper.buildAssetLiability(anyList(), eq(ProductType.LIABILITY))).thenReturn(mockLiabilities);

        // 2. Act
        AssetsAndLiabilitiesResponse response = productService.getAssetsAndLiabilities();

        // 3. Assert
        assertNotNull(response);
        assertEquals(mockAssets, response.getAssets());
        assertEquals(mockLiabilities, response.getLiabilities());

        // Verify both calculations were triggered
        verify(productHelper, times(1)).buildAssetLiability(anyList(), eq(ProductType.ASSET));
        verify(productHelper, times(1)).buildAssetLiability(anyList(), eq(ProductType.LIABILITY));
    }
}