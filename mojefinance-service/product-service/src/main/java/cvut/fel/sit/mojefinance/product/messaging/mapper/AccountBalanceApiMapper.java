package cvut.fel.sit.mojefinance.product.messaging.mapper;

import cvut.fel.sit.airbank.openapi.model.BalanceList;
import cvut.fel.sit.airbank.openapi.model.BalanceListBalancesInnerAmount;
import cvut.fel.sit.airbank.openapi.model.BalanceListBalancesInnerType;
import cvut.fel.sit.airbank.openapi.model.BalanceListBalancesInnerTypeCodeOrProprietary;
import cvut.fel.sit.cs.openapi.model.AccountBalanceType;
import cvut.fel.sit.cs.openapi.model.AccountBalanceTypeCodeOrProprietary;
import cvut.fel.sit.cs.openapi.model.AmountBalance;
import cvut.fel.sit.cs.openapi.model.MyAccountsIdBalanceGet200Response;
import cvut.fel.sit.csob.balances.openapi.model.BalanceAmount;
import cvut.fel.sit.csob.balances.openapi.model.GetAccountBalanceRes;
import cvut.fel.sit.kb.openapi.model.BalanceAmountType;
import cvut.fel.sit.kb.openapi.model.BalanceType;
import cvut.fel.sit.kb.openapi.model.CodeOrProprietary;
import cvut.fel.sit.kb.openapi.model.GetAccountBalanceResponse;
import cvut.fel.sit.mojefinance.product.domain.entity.Balance;
import cvut.fel.sit.reif.openapi.model.GetBalance200Response;
import cvut.fel.sit.reif.openapi.model.GetBalance200ResponseCurrencyFoldersInnerBalancesInner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

@Component
public class AccountBalanceApiMapper {
    private static final String CLAV_TYPE_CODE = "CLAV";
    private static final String DEBIT_INDICATOR = "DBIT";
    private static final String CZK = "CZK";

    public Balance toDomainBalance(BalanceList response) {
        if (response == null) return null;
        return extractBalanceTemplate(
                response.getBalances(),
                b -> Optional.ofNullable(b.getType()).map(BalanceListBalancesInnerType::getCodeOrProprietary).map(BalanceListBalancesInnerTypeCodeOrProprietary::getCode).map(Enum::name).filter(CLAV_TYPE_CODE::equals).isPresent(),
                b -> Optional.ofNullable(b.getAmount()).map(BalanceListBalancesInnerAmount::getValue).orElse(BigDecimal.ZERO),
                b -> Optional.ofNullable(b.getAmount()).map(BalanceListBalancesInnerAmount::getCurrency).orElse(null),
                b -> Optional.ofNullable(b.getCreditDebitIndicator()).map(Enum::name).filter(DEBIT_INDICATOR::equals).isPresent()
        );
    }

    public Balance toDomainBalance(MyAccountsIdBalanceGet200Response response) {
        if (response == null) return null;
        return extractBalanceTemplate(
                response.getBalances(),
                b -> Optional.ofNullable(b.getType()).map(AccountBalanceType::getCodeOrProprietary).map(AccountBalanceTypeCodeOrProprietary::getCode).map(Enum::name).filter(CLAV_TYPE_CODE::equals).isPresent(),
                b -> Optional.ofNullable(b.getAmount()).map(AmountBalance::getValue).orElse(BigDecimal.ZERO),
                b -> Optional.ofNullable(b.getAmount()).map(AmountBalance::getCurrency).orElse(null),
                b -> Optional.ofNullable(b.getCreditDebitIndicator()).map(Enum::name).filter(DEBIT_INDICATOR::equals).isPresent()
        );
    }

    public Balance toDomainBalance(GetAccountBalanceRes response) {
        if (response == null) return null;
        return extractBalanceTemplate(
                response.getBalances(),
                b -> Optional.ofNullable(b.getType()).map(cvut.fel.sit.csob.balances.openapi.model.BalanceType::getCodeOrProprietary).map(cvut.fel.sit.csob.balances.openapi.model.CodeOrProprietary::getCode).map(Enum::name).filter(CLAV_TYPE_CODE::equals).isPresent(),
                b -> Optional.ofNullable(b.getAmount()).map(BalanceAmount::getValue).orElse(BigDecimal.ZERO),
                b -> Optional.ofNullable(b.getAmount()).map(BalanceAmount::getCurrency).orElse(null),
                b -> Optional.ofNullable(b.getCreditDebitIndicator()).map(Enum::name).filter(DEBIT_INDICATOR::equals).isPresent()
        );
    }

    public Balance toDomainBalance(GetAccountBalanceResponse response) {
        if (response == null) return null;
        return extractBalanceTemplate(
                response.getBalances(),
                b -> Optional.ofNullable(b.getType()).map(BalanceType::getCodeOrProprietary).map(CodeOrProprietary::getCode).map(Enum::name).filter(CLAV_TYPE_CODE::equals).isPresent(),
                b -> Optional.ofNullable(b.getAmount()).map(a -> BigDecimal.valueOf(a.getValue())).orElse(BigDecimal.ZERO),
                b -> Optional.ofNullable(b.getAmount()).map(BalanceAmountType::getCurrency).orElse(null),
                b -> Optional.ofNullable(b.getCreditDebitIndicator()).map(Enum::name).filter(DEBIT_INDICATOR::equals).isPresent()
        );
    }

    public Balance toDomainBalance(GetBalance200Response response) {
        if (response == null || response.getCurrencyFolders() == null) return null;

        return response.getCurrencyFolders().stream()
                .filter(folder -> CZK.equals(folder.getCurrency()))
                .findFirst()
                .map(folder -> extractBalanceTemplate(
                        folder.getBalances(),
                        b -> CLAV_TYPE_CODE.equals(b.getBalanceType()),
                        b -> Optional.ofNullable(b.getValue()).map(BigDecimal::valueOf).orElse(BigDecimal.ZERO),
                        GetBalance200ResponseCurrencyFoldersInnerBalancesInner::getCurrency,
                        b -> false
                ))
                .orElse(null);
    }

    private <T> Balance extractBalanceTemplate(
            List<T> balances,
            Predicate<T> isClavPredicate,
            Function<T, BigDecimal> amountExtractor,
            Function<T, String> currencyExtractor,
            Predicate<T> isDebitPredicate) {

        if (balances == null || balances.isEmpty()) {
            return null;
        }
        T selectedBalance = balances.stream()
                .filter(isClavPredicate)
                .findFirst()
                .orElse(balances.get(0));

        BigDecimal amount = amountExtractor.apply(selectedBalance);
        if (amount == null) amount = BigDecimal.ZERO;
        if (isDebitPredicate.test(selectedBalance)) {
            amount = amount.negate();
        }

        return Balance.builder()
                .amount(amount)
                .currency(currencyExtractor.apply(selectedBalance))
                .build();
    }
}