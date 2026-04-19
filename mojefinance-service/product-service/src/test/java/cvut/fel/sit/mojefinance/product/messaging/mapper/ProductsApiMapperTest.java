package cvut.fel.sit.mojefinance.product.messaging.mapper;

import cvut.fel.sit.airbank.openapi.model.AccountList;
import cvut.fel.sit.airbank.openapi.model.AccountListAccountsInner;
import cvut.fel.sit.airbank.openapi.model.AccountListAccountsInnerIdentification;
import cvut.fel.sit.airbank.openapi.model.AccountListAccountsInnerServicer;
import cvut.fel.sit.cs.openapi.model.AccountDetail;
import cvut.fel.sit.cs.openapi.model.AccountDetailIdentification;
import cvut.fel.sit.cs.openapi.model.AccountDetailServicer;
import cvut.fel.sit.cs.openapi.model.MyAccountsGet200Response;
import cvut.fel.sit.csob.accounts.openapi.model.AccountInfo;
import cvut.fel.sit.csob.accounts.openapi.model.AccountServicer;
import cvut.fel.sit.csob.accounts.openapi.model.GetAccountsRes;
import cvut.fel.sit.kb.openapi.model.Account;
import cvut.fel.sit.kb.openapi.model.AccountIdentification;
import cvut.fel.sit.kb.openapi.model.GetAccountListResponse;
import cvut.fel.sit.kb.openapi.model.ServicingBank;
import cvut.fel.sit.mojefinance.product.domain.dto.ProductsResponse;
import cvut.fel.sit.mojefinance.product.domain.entity.BankDetails;
import cvut.fel.sit.mojefinance.product.domain.entity.Product;
import cvut.fel.sit.reif.openapi.model.GetAccounts200Response;
import cvut.fel.sit.reif.openapi.model.GetAccounts200ResponseAccountsInner;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductsApiMapperTest {

    private ProductsApiMapper mapper;
    private BankDetails mockBankDetails;

    @BeforeEach
    void setUp() {
        mapper = new ProductsApiMapperImpl();

        mockBankDetails = new BankDetails();
        mockBankDetails.setClientRegistrationId("test-bank-id");
    }

    @Test
    void toProductsResponse_NullInputs_ShouldReturnNull() {
        assertNull(mapper.toProductsResponse((AccountList) null, mockBankDetails));
        assertNull(mapper.toProductsResponse((MyAccountsGet200Response) null, mockBankDetails));
        assertNull(mapper.toProductsResponse((GetAccountsRes) null, mockBankDetails));
        assertNull(mapper.toProductsResponse((GetAccountListResponse) null, mockBankDetails));
        assertNull(mapper.toProductsResponse((GetAccounts200Response) null, mockBankDetails));
    }

    @Test
    void toProductsResponse_AirBank_ShouldMapCorrectly() {
        // Arrange nested objects
        AccountList response = getAccountList();

        // Act
        ProductsResponse productsResponse = mapper.toProductsResponse(response, mockBankDetails);

        // Assert
        assertNotNull(productsResponse);
        assertEquals(1, productsResponse.getProducts().size());
        Product product = productsResponse.getProducts().get(0);

        assertEquals("air-1", product.getProductId());
        assertEquals("CZ12345", product.getProductIdentification().getIban());
        assertEquals("98765", product.getProductIdentification().getProductNumber());
        assertEquals("Air Current", product.getAccountName());
        assertEquals("Everyday Account", product.getProductName());
        assertEquals("3030", product.getBankCode());
        assertEquals("CZK", product.getCurrency());
        assertFalse(product.getManuallyCreated());
        assertEquals(mockBankDetails, product.getBankDetails());
    }

    private static @NonNull AccountList getAccountList() {
        AccountListAccountsInnerIdentification identification = new AccountListAccountsInnerIdentification();
        identification.setIban("CZ12345");
        identification.setOther("98765");

        AccountListAccountsInnerServicer servicer = new AccountListAccountsInnerServicer();
        servicer.setBankCode("3030");

        AccountListAccountsInner apiAccount = new AccountListAccountsInner();
        apiAccount.setId("air-1");
        apiAccount.setIdentification(identification);
        apiAccount.setNameI18N("Air Current");
        apiAccount.setProductI18N("Everyday Account");
        apiAccount.setCurrency("CZK");
        apiAccount.setServicer(servicer);

        AccountList response = new AccountList();
        response.setAccounts(List.of(apiAccount));
        return response;
    }

    @Test
    void toProductsResponse_CeskaSporitelna_ShouldMapCorrectly() {
        // Arrange
        MyAccountsGet200Response response = getMyAccountsGet200Response();

        // Act
        ProductsResponse productsResponse = mapper.toProductsResponse(response, mockBankDetails);

        // Assert
        Product product = productsResponse.getProducts().get(0);
        assertEquals("cs-1", product.getProductId());
        assertEquals("CZ0800", product.getProductIdentification().getIban());
        assertEquals("CS-123", product.getProductIdentification().getProductNumber());
        assertEquals("CS Savings", product.getAccountName());
        assertEquals("0800", product.getBankCode());
    }

    private static @NonNull MyAccountsGet200Response getMyAccountsGet200Response() {
        AccountDetailIdentification identification = new AccountDetailIdentification();
        identification.setIban("CZ0800");
        identification.setOther("CS-123");

        AccountDetailServicer servicer = new AccountDetailServicer();
        servicer.setBankCode("0800");

        AccountDetail apiAccount = new AccountDetail();
        apiAccount.setId("cs-1");
        apiAccount.setIdentification(identification);
        apiAccount.setNameI18N("CS Savings");
        apiAccount.setProductI18N("Savings Plus");
        apiAccount.setCurrency("EUR");
        apiAccount.setServicer(servicer);

        MyAccountsGet200Response response = new MyAccountsGet200Response();
        response.setAccounts(List.of(apiAccount));
        return response;
    }

    @Test
    void toProductsResponse_CSOB_ShouldMapCorrectly() {
        // Arrange - Note CSOB AccountIdentification does not have 'other' in the mapper
        GetAccountsRes response = getGetAccountsRes();

        // Act
        ProductsResponse productsResponse = mapper.toProductsResponse(response, mockBankDetails);

        // Assert
        Product product = productsResponse.getProducts().get(0);
        assertEquals("csob-1", product.getProductId());
        assertEquals("CZ0300", product.getProductIdentification().getIban());
        assertNull(product.getProductIdentification().getProductNumber()); // CSOB specific
        assertEquals("0300", product.getBankCode());
    }

    private static @NonNull GetAccountsRes getGetAccountsRes() {
        cvut.fel.sit.csob.accounts.openapi.model.AccountIdentification identification =
                new cvut.fel.sit.csob.accounts.openapi.model.AccountIdentification();
        identification.setIban("CZ0300");

        AccountServicer servicer = new AccountServicer();
        servicer.setBankCode("0300");

        AccountInfo apiAccount = new AccountInfo();
        apiAccount.setId("csob-1");
        apiAccount.setIdentification(identification);
        apiAccount.setNameI18N("CSOB Main");
        apiAccount.setCurrency("CZK");
        apiAccount.setServicer(servicer);

        GetAccountsRes response = new GetAccountsRes();
        response.setAccounts(List.of(apiAccount));
        return response;
    }

    @Test
    void toProductsResponse_KomercniBanka_ShouldMapCorrectly() {
        // Arrange
        GetAccountListResponse response = getGetAccountListResponse();

        // Act
        ProductsResponse productsResponse = mapper.toProductsResponse(response, mockBankDetails);

        // Assert
        Product product = productsResponse.getProducts().get(0);
        assertEquals("kb-1", product.getProductId());
        assertEquals("CZ0100", product.getProductIdentification().getIban());
        assertEquals("0100", product.getBankCode());
    }

    private static @NonNull GetAccountListResponse getGetAccountListResponse() {
        AccountIdentification identification = new AccountIdentification();
        identification.setIban("CZ0100");
        identification.setOther("KB-456");

        ServicingBank servicer = new ServicingBank();
        servicer.setBankCode("0100");

        Account apiAccount = new Account();
        apiAccount.setId("kb-1");
        apiAccount.setIdentification(identification);
        apiAccount.setNameI18N("KB Business");
        apiAccount.setServicer(servicer);

        GetAccountListResponse response = new GetAccountListResponse();
        response.setAccounts(List.of(apiAccount));
        return response;
    }

    @Test
    void toProductsResponse_Reiffeisen_ShouldMapCorrectly() {
        // Arrange - Reiffeisen uses a completely flat structure compared to the others
        GetAccounts200Response response = getGetAccounts200Response();

        // Act
        ProductsResponse productsResponse = mapper.toProductsResponse(response, mockBankDetails);

        // Assert
        Product product = productsResponse.getProducts().get(0);
        assertEquals("999", product.getProductId()); // Verifying BigDecimal to String conversion
        assertEquals("CZ5500", product.getProductIdentification().getIban());
        assertEquals("RB-789", product.getProductIdentification().getProductNumber());
        assertEquals("Official Name", product.getAccountName());
        assertEquals("RB Checking", product.getProductName());
        assertEquals("USD", product.getCurrency());
        assertEquals("5500", product.getBankCode());
        assertFalse(product.getManuallyCreated());
        assertEquals(mockBankDetails, product.getBankDetails());
    }

    private static @NonNull GetAccounts200Response getGetAccounts200Response() {
        GetAccounts200ResponseAccountsInner apiAccount = new GetAccounts200ResponseAccountsInner();
        apiAccount.setAccountId(999); // Converted to String in mapper
        apiAccount.setIban("CZ5500");
        apiAccount.setAccountNumber("RB-789");
        apiAccount.setFriendlyName("RB Checking");
        apiAccount.setMainCurrency("USD");
        apiAccount.setAccountName("Official Name");
        apiAccount.setBankCode("5500");

        GetAccounts200Response response = new GetAccounts200Response();
        response.setAccounts(List.of(apiAccount));
        return response;
    }
}