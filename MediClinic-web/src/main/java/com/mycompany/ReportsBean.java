package com.mycompany;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import service.ReportService;
import service.ReportService.DoctorPerformance;

/**
 * Backs admin/reports.xhtml: revenue total and doctor performance table.
 *
 * @author acefonfo
 */
@ManagedBean(name = "reportsBean")
@ViewScoped
public class ReportsBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final int MONTHS_BACK = 6;

    @EJB
    private ReportService reportService;

    private List<DoctorPerformance> doctorPerformance;
    private BigDecimal totalRevenueLast6Months = BigDecimal.ZERO;

    @PostConstruct
    public void init() {
        LinkedHashMap<String, BigDecimal> monthly = reportService.monthlyRevenue(MONTHS_BACK);
        for (BigDecimal v : monthly.values()) {
            totalRevenueLast6Months = totalRevenueLast6Months.add(v);
        }

        doctorPerformance = reportService.doctorPerformanceReport();
    }

    public List<DoctorPerformance> getDoctorPerformance() {
        return doctorPerformance;
    }

    public BigDecimal getTotalRevenueLast6Months() {
        return totalRevenueLast6Months;
    }
}