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
import dao.ConsultationDao;
import dao.PatientDao;
import entity.Consultation;
import entity.Patient;
import entity.Prescription;
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
import service.PrescriptionService;

/**
 *
 * @author acefonfo
 */

@WebServlet("/medicalrecord/*")
public class MedicalRecordServlet extends HttpServlet {

    @EJB
    private ConsultationDao consultationDao;
    @EJB
    private PrescriptionService prescriptionService;
    @EJB
    private BillingService billingService;
    @EJB
    private PatientDao patientDao;

    /**
     * Handles HTTP GET requests.
     * 
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException 
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long consultationId = parseId(req.getPathInfo());
        if (consultationId == null) {
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

        Consultation consultation = consultationDao.findById(consultationId);
        if (consultation == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if (!canAccess(actor, consultation)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        List<Prescription> prescriptions = prescriptionService.listForConsultation(consultationId);

        resp.setContentType("application/pdf");
        resp.setHeader("Content-Disposition", "attachment; filename=medical-record-" + consultationId + ".pdf");

        try (OutputStream out = resp.getOutputStream()) {
            buildPdf(consultation, prescriptions, out);
        } catch (ServletException | IOException e) {
            throw e;
        } catch (Exception e) {
            throw new ServletException("Failed to generate medical record PDF", e);
        }
    }

    /**
     * Checks weather a user has permission to access a consultation.
     * 
     * Different roll have different permission
     * ADMIN        -> Can access all consultations.
     * RECEPTIONIST  -> Can access all consultations.
     * DOCTOR       -> Can access consultations belonging to that doctor.
     * PATIENT      -> Can access their own consultation only if it is unlocked.
     * 
     * @param actor
     * @param consultation
     * @return 
     */
    private boolean canAccess(User actor, Consultation consultation) {
        String role = (actor.getRole() != null) ? actor.getRole().getName() : "";

        if ("ADMIN".equals(role) || "RECEPTIONIST".equals(role)) {
            return true;
        }
        
        if ("DOCTOR".equals(role)) {
            return consultation.getAppointment().getDoctor().getUser() != null
                    && consultation.getAppointment().getDoctor().getUser().getId().equals(actor.getId());
        }
        
        if ("PATIENT".equals(role)) {
            Patient patient = patientDao.findByUserId(actor.getId());
            boolean owns = patient != null
                    && consultation.getAppointment().getPatient().getId().equals(patient.getId());
            return owns && billingService.isConsultationUnlockedForPatient(consultation.getId());
        }
        
        return false;
    }

    /**
     * Creates the medicinal record PDF.
     * 
     * The PDF contains:
     *      - Consultation information
     *      - Patient information
     *      - Doctor information
     *      - Symptoms
     *      - Diagnosis
     *      - Notes
     *      - Prescription information
     * 
     * @param c
     * @param prescriptions
     * @param out
     * @throws Exception 
     */
    private void buildPdf(Consultation c, List<Prescription> prescriptions, OutputStream out) throws Exception {
        Document doc = new Document();
        PdfWriter.getInstance(doc, out);
        doc.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        doc.add(new Paragraph("MediClinic - Medical Record", titleFont));
        doc.add(new Paragraph(" "));

        doc.add(new Paragraph("Consultation Details", headFont));
        doc.add(new Paragraph("Patient: " + nullSafe(c.getAppointment().getPatient().getName()), normalFont));
        doc.add(new Paragraph("Doctor: " + nullSafe(c.getAppointment().getDoctor().getName()), normalFont));
        doc.add(new Paragraph("Date: " + c.getConsultationDate(), normalFont));
        doc.add(new Paragraph("Symptoms: " + nullSafe(c.getSymptoms()), normalFont));
        doc.add(new Paragraph("Diagnosis: " + nullSafe(c.getDiagnosis()), normalFont));
        doc.add(new Paragraph("Notes: " + nullSafe(c.getNotes()), normalFont));
        doc.add(new Paragraph(" "));

        doc.add(new Paragraph("Prescriptions", headFont));
        if (prescriptions == null || prescriptions.isEmpty()) {
            doc.add(new Paragraph("No prescriptions recorded.", normalFont));
        } else {
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            addHeaderCell(table, "Medicine", labelFont);
            addHeaderCell(table, "Dosage", labelFont);
            addHeaderCell(table, "Frequency", labelFont);
            addHeaderCell(table, "Duration", labelFont);
            addHeaderCell(table, "Instructions", labelFont);

            for (Prescription rx : prescriptions) {
                table.addCell(new Paragraph(nullSafe(rx.getMedicineName()), normalFont));
                table.addCell(new Paragraph(nullSafe(rx.getDosage()), normalFont));
                table.addCell(new Paragraph(nullSafe(rx.getFrequency()), normalFont));
                table.addCell(new Paragraph(nullSafe(rx.getDuration()), normalFont));
                table.addCell(new Paragraph(nullSafe(rx.getInstructions()), normalFont));
            }
            doc.add(table);
        }

        doc.close();
    }

    /**
     * Adds a formatted header cell to the prescription table.
     * 
     * @param table
     * @param text
     * @param font 
     */
    private void addHeaderCell(PdfPTable table, String text, Font font) {
        table.addCell(new PdfPCell(new Paragraph(text, font)));
    }

    /**
     * Prevents null or empty string from being displayed in pdf.
     * 
     * Example:
     * null      -> "-"
     * ""        -> "-"
     * "   "     -> "-"
     * "Headache" -> "Headache"
     * 
     * @param s
     * @return 
     */
    private String nullSafe(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s;
    }

    /**
     * Extracts the consultation ID from the URL path
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