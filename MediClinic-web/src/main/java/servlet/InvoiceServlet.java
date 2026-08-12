/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlet;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.mycompany.LoggedInUser;
import dao.InvoiceDao;
import dao.PatientDao;
import entity.Invoice;
import entity.Patient;
import entity.Payment;
import entity.User;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import service.BillingService;

/**
 *
 * @author acefonfo
 */

@WebServlet("/invoice/*")
public class InvoiceServlet extends HttpServlet {

    @EJB
    private InvoiceDao invoiceDao;
    @EJB
    private BillingService billingService;
    @EJB
    private PatientDao patientDao;

    /**
     * Handles HTTP requests 
     * 
     * The GET requests is used to generate and download the invoice as PDF file.
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException 
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long invoiceId = parseId(req.getPathInfo());
        if (invoiceId == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        HttpSession session = req.getSession(false);
        LoggedInUser loggedInUser = (session != null) ? (LoggedInUser) session.getAttribute("loggedInUser") : null;
        User actor = (loggedInUser != null) ? loggedInUser.getUser() : null;
        if (actor == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Invoice invoice = invoiceDao.findById(invoiceId);
        if (invoice == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if (!canAccess(actor, invoice)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        List<Payment> payments = billingService.findPaymentsForInvoice(invoiceId);

        resp.setContentType("application/pdf");
        resp.setHeader("Content-Disposition", "attachment; filename=" + invoice.getInvoiceNumber() + ".pdf");

        try (OutputStream out = resp.getOutputStream()) {
            buildPdf(invoice, payments, out);
        } catch (ServletException | IOException e) {
            throw e;
        } catch (Exception e) {
            throw new ServletException("Failed to generate invoice PDF", e);
        }
    }

    /**
     * Checks weather a particular user has permission to access the specified invoice.
     * 
     * Access rule:
     *      - Admin: Can access all invoices
     *      - Receptionist Can access all invoices.
     *      - Patient Can only access invoice belonging to themselves only.
     * 
     * @param actor
     * @param invoice
     * @return 
     */
    private boolean canAccess(User actor, Invoice invoice) {
        String role = (actor.getRole() != null) ? actor.getRole().getName() : "";

        if ("ADMIN".equals(role) || "RECEPTIONIST".equals(role)) {
            return true;
        }
        if ("PATIENT".equals(role)) {
            Patient patient = patientDao.findByUserId(actor.getId());
            Long owningPatientId = invoice.getConsultation().getAppointment().getPatient().getId();
            return patient != null && owningPatientId.equals(patient.getId());
        }
        return false;
    }

    /**
     * Creates the invoice PDF and writes it to the supplied OutputStream.
     * 
     * The PDF contains:
     * - Invoice number
     * - Invoice status
     * - Issue date
     * - Patient name
     * - Doctor name
     * - Consultation fee
     * - Tax
     * - Total amount
     * - Payment history
     * 
     * @param invoice
     * @param payments
     * @param out
     * @throws Exception 
     */
    private void buildPdf(Invoice invoice, List<Payment> payments, OutputStream out) throws Exception {
        Document doc = new Document();
        PdfWriter.getInstance(doc, out);
        doc.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        doc.add(new Paragraph("MediClinic - Invoice", titleFont));
        doc.add(new Paragraph(" "));

        doc.add(new Paragraph("Invoice #: " + invoice.getInvoiceNumber(), normalFont));
        doc.add(new Paragraph("Status: " + invoice.getStatus(), normalFont));
        doc.add(new Paragraph("Issued: " + invoice.getIssuedDate(), normalFont));
        doc.add(new Paragraph("Patient: " + nullSafe(invoice.getConsultation().getAppointment().getPatient().getName()), normalFont));
        doc.add(new Paragraph("Doctor: " + nullSafe(invoice.getConsultation().getAppointment().getDoctor().getName()), normalFont));
        doc.add(new Paragraph(" "));

        doc.add(new Paragraph("Charges", headFont));
        doc.add(new Paragraph("Consultation Fee: " + invoice.getConsultationFee(), normalFont));
        doc.add(new Paragraph("Tax: " + invoice.getTax(), normalFont));
        doc.add(new Paragraph("Total Amount: " + invoice.getTotalAmount(), normalFont));
        doc.add(new Paragraph(" "));

        doc.add(new Paragraph("Payment History", headFont));
        if (payments == null || payments.isEmpty()) {
            doc.add(new Paragraph("No payments recorded.", normalFont));
        } else {
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            addHeaderCell(table, "Method", labelFont);
            addHeaderCell(table, "Amount", labelFont);
            addHeaderCell(table, "Date", labelFont);
            addHeaderCell(table, "Reference", labelFont);

            for (Payment p : payments) {
                table.addCell(new Paragraph(String.valueOf(p.getPaymentMethod()), normalFont));
                table.addCell(new Paragraph(String.valueOf(p.getAmount()), normalFont));
                table.addCell(new Paragraph(String.valueOf(p.getPaymentDate()), normalFont));
                table.addCell(new Paragraph(nullSafe(p.getTransactionRef()), normalFont));
            }
            doc.add(table);
        }

        doc.close();
    }

    /**
     * Adds a formatted header cell to the payment table.
     * 
     * @param table
     * @param text
     * @param font 
     */
    private void addHeaderCell(PdfPTable table, String text, Font font) {
        table.addCell(new PdfPCell(new Paragraph(text, font)));
    }

    /**
     * Safely handles null or empty strings.
     * @param s
     * @return 
     */
    private String nullSafe(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s;
    }

    /**
     * Extracts the invoice Id from the URL patch.
     * @param pathInfo
     * @return 
     */
    private Long parseId(String pathInfo) {
        if (pathInfo == null || pathInfo.length() < 2) {
            return null;
        }
        try {
            return Long.parseLong(pathInfo.substring(1));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}