package cvut.fel.sit.mojefinance.product.api.controller;

import cvut.fel.sit.mojefinance.openapi.api.BudgetsApi;
import cvut.fel.sit.mojefinance.openapi.model.BudgetRequest;
import cvut.fel.sit.mojefinance.openapi.model.BudgetsResponse;
import cvut.fel.sit.mojefinance.product.api.mapper.BudgetsMapper;
import cvut.fel.sit.mojefinance.product.domain.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class BudgetController implements BudgetsApi {
    private final BudgetService budgetService;
    private final BudgetsMapper budgetsMapper;

    @Override
    public ResponseEntity<Void> createBudget(String authorization, BudgetRequest budgetRequest) {
        cvut.fel.sit.mojefinance.product.domain.dto.BudgetRequest budgetDomainRequest = budgetsMapper
                .toBudgetRequest(budgetRequest);
        budgetService.createBudget(budgetDomainRequest);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> deleteBudget(String authorization, Integer budgetId) {
        budgetService.deleteBudget(Long.valueOf(budgetId));
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<BudgetsResponse> getBudgets(String authorization) {
        cvut.fel.sit.mojefinance.product.domain.dto.BudgetsResponse domainResponse = budgetService.getBudgets();
        BudgetsResponse apiResponse = budgetsMapper.toBudgetsResponse(domainResponse);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> updateBudget(String authorization, BudgetRequest budgetRequest) {
        cvut.fel.sit.mojefinance.product.domain.dto.BudgetRequest budgetDomainRequest = budgetsMapper
                .toBudgetRequest(budgetRequest);
        budgetService.updateBudget(budgetDomainRequest);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
