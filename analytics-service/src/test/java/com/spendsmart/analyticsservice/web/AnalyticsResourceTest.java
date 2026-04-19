package com.spendsmart.analyticsservice.web;

import com.spendsmart.analyticsservice.domain.FinancialSnapshot;
import com.spendsmart.analyticsservice.service.AnalyticsService;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnalyticsResource.class)
class AnalyticsResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalyticsService analyticsService;

    @Test
    void generateMonthlySnapshotReturnsSnapshot() throws Exception {
        FinancialSnapshot snapshot = new FinancialSnapshot();
        snapshot.setSnapshotId(8L);
        snapshot.setUserId(7L);
        snapshot.setYear(2026);
        snapshot.setMonth(4);
        snapshot.setPeriod("2026-04");
        snapshot.setTotalIncome(new BigDecimal("5000"));
        snapshot.setTotalExpenses(new BigDecimal("1200"));
        snapshot.setNetSavings(new BigDecimal("3800"));
        snapshot.setSavingsRate(new BigDecimal("76.00"));
        snapshot.setTopCategory("category-1");
        snapshot.setCreatedAt(Instant.parse("2026-04-18T00:00:00Z"));

        when(analyticsService.generateMonthlySnapshot(
                anyLong(),
                anyInt(),
                anyInt(),
                any(BigDecimal.class),
                any(BigDecimal.class),
                any()
        )).thenReturn(snapshot);

        mockMvc.perform(
                        post("/analytics/monthlySnapshot")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "userId": 7,
                                          "year": 2026,
                                          "month": 4,
                                          "totalIncome": 5000,
                                          "totalExpenses": 1200,
                                          "topCategory": "category-1"
                                        }
                                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.snapshotId").value(8))
                .andExpect(jsonPath("$.period").value("2026-04"));
    }
}
