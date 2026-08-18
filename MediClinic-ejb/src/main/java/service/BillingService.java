/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dao.ActivityLogDao;
import dao.InvoiceDao;
import dao.PatientDao;
import dao.PaymentDao;
import entity.ActivityLog;
import entity.Consultation;
import entity.Invoice;
import entity.Invoice.Status;
import entity.Patient;
import entity.Payment;
import entity.Payment.Method;
import entity.User;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import javax.annotation.security.PermitAll;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import util.EsewaConfig;
import util.KhaltiConfig;

/**
 *
 * @author acefonfo
 */
@Stateless
@PermitAll
public class BillingService {

    public static final BigDecimal TAX_RATE = new BigDecimal("0.15");

    @EJB
    private InvoiceDao invoiceDao;
    @EJB
    private PaymentDao paymentDao;
    @EJB
    private PatientDao patientDao;
    @EJB
    private ActivityLogDao activityLogDao;

    /**
     * Auto generates an invoice newly completed consultation.
     *
     * @param consultation
     * @return
     */
    public Invoice generateInvoiceForConsultation(Consultation consultation) {

        Invoice existing = invoiceDao.findByConsultationId(consultation.getId());
        if (existing != null) {
            return existing;
        }

        BigDecimal fee = consultation.getAppointment().getDoctor().getFee();
        if (fee == null) {
            fee = BigDecimal.ZERO;
        }

        Invoice invoice = new Invoice();
        invoice.setConsultation(consultation);
        invoice.setInvoiceNumber(generateInvoiceNumber());
        applyFee(invoice, fee);
        invoice.setStatus(Invoice.Status.UNPAID);
        invoice.setIssuedDate(new Date());

        invoiceDao.create(invoice);
        log(null, "GENERATE_INVOICE", "Invoice", invoice.getId(), "Invoice " + invoice.getInvoiceNumber() + " generated for consultation #" + consultation.getId());
        return invoice;

    }

    /**
     * Recalculates tax and total fee from the consultation fee.
     *
     * @param invoice
     * @param fee
     */
    private void applyFee(Invoice invoice, BigDecimal fee) {
        BigDecimal tax = fee.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        invoice.setConsultationFee(fee);
        invoice.setTax(tax);
        invoice.setTotalAmount(fee.add(tax));
    }

    /**
     * Builds a sequential invoice invoice number
     *
     * @return
     */
    private String generateInvoiceNumber() {
        String prefix = "INV-" + new SimpleDateFormat("yyyyMM").format(new Date()) + "-";
        long count = invoiceDao.countAll() + 1;
        return prefix + String.format("%04d", count);
    }

    public Invoice findById(Long id) {
        return invoiceDao.findById(id);
    }

    public List<Invoice> findAllInvoices() {
        return invoiceDao.findAll();
    }

    public List<Invoice> findInvoicesForPatient(Long patientId) {
        return invoiceDao.findByPatientId(patientId);
    }

    public List<Payment> findPaymentsForInvoice(Long invoiceId) {
        return paymentDao.findByInvoiceId(invoiceId);
    }

    /**
     * Edits the consultation fee on an invoice. Admin / receptionist only.
     * Blocked once the invoice is PAID or VOID.
     *
     * @param actor
     * @param invoiceId
     * @param newFee
     * @return
     * @throws AuthException
     */
    public Invoice updateInvoice(User actor, Long invoiceId, BigDecimal newFee) throws AuthException {
        requireStaff(actor);

        Invoice invoice = invoiceDao.findById(invoiceId);
        if (invoice == null) {
            throw new AuthException("Invoice not found");
        }

        if (invoice.getStatus() == Status.PAID || invoice.getStatus() == Status.VOID) {
            throw new AuthException("Apaid or void invoice cannot be edited");
        }

        if (newFee == null || newFee.compareTo(BigDecimal.ZERO) < 0) {
            throw new AuthException("Fee must be positive amount.");
        }

        applyFee(invoice, newFee);
        invoice.setUpdatedAt(new Date());
        invoiceDao.update(invoice);

        log(actor, "UPDATE_INVOICE", "Invoice", invoice.getId(), "Invoice " + invoice.getInvoiceNumber() + " fee updated to " + newFee);
        return invoice;
    }

    /**
     * Voids an invoice. Blocked once any payment exists.
     *
     * @param actor
     * @param invoiceId
     * @return
     * @throws AuthException
     */
    public Invoice voidInvoice(User actor, Long invoiceId) throws AuthException {
        requireStaff(actor);

        Invoice invoice = invoiceDao.findById(invoiceId);

        if (invoice == null) {
            throw new AuthException("Invoice not fouund.");
        }

        if (invoice.getStatus() == Status.VOID) {
            throw new AuthException("Invoice is already VOID");
        }

        if (!paymentDao.findByInvoiceId(invoiceId).isEmpty()) {
            throw new AuthException("An invoice with a recorded payment canot be voided");
        }

        invoice.setStatus(Status.VOID);
        invoice.setUpdatedAt(new Date());
        invoiceDao.update(invoice);

        log(actor, "VOID_INVOICE", "Invoice", invoice.getId(), "Invoice " + invoice.getInvoiceNumber() + " voided");
        return invoice;

    }

    /**
     * Records a cash payment. Receptionist only. Pays the invoice in full.
     *
     * @param actor
     * @param invoiceId
     * @param transactionRef
     * @return
     * @throws AuthException
     */
    public Payment payCash(User actor, Long invoiceId, String transactionRef) throws AuthException {
        if (actor == null || actor.getRole() == null || !"RECEPTIONIST".equals(actor.getRole().getName())) {
            throw new AuthException("Only a receptionist can record a cash payment.");
        }
        return recordPayment(actor, invoiceId, Method.CASH, transactionRef);
    }

    /**
     * Records online payment made by the payment for their own invoice.
     *
     * @param actor
     * @param invoiceId
     * @return
     * @throws AuthException
     */
    public Payment payOnline(User actor, Long invoiceId) throws AuthException {
        if (actor == null || actor.getRole() == null || !"PATIENT".equals(actor.getRole().getName())) {
            throw new AuthException("Only a patient can make an online payment.");
        }

        Invoice invoice = invoiceDao.findById(invoiceId);
        if (invoice == null) {
            throw new AuthException("Invoice not found.");
        }

        Patient patient = patientDao.findByUserId(actor.getId());
        Long owningPatientId = invoice.getConsultation().getAppointment().getPatient().getId();
        if (patient == null || !owningPatientId.equals(patient.getId())) {
            throw new AuthException("You can only pay your own invoices.");
        }

        return recordPayment(actor, invoiceId, Method.ONLINE, "ONLINE-" + System.currentTimeMillis());
    }

    /**
     * Shared payment recording logic used by both payCash and payOnline
     *
     * @param actor
     * @param invoiceId
     * @param method
     * @param transactionRef
     * @return
     * @throws AuthException
     */
    private Payment recordPayment(User actor, Long invoiceId, Method method, String transactionRef) throws AuthException {

        Invoice invoice = invoiceDao.findById(invoiceId);

        if (invoice == null) {
            throw new AuthException("Invoice not found.");
        }
        if (invoice.getStatus() == Status.VOID) {
            throw new AuthException("Cannot pay a voided invoice.");
        }
        if (invoice.getStatus() == Status.PAID) {
            throw new AuthException("Invoice is already paid.");
        }

        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setAmount(invoice.getTotalAmount());
        payment.setPaymentMethod(method);
        payment.setPaymentDate(new Date());
        payment.setTransactionRef(transactionRef);
        paymentDao.create(payment);

        invoice.setStatus(Status.PAID);
        invoice.setUpdatedAt(new Date());
        invoiceDao.update(invoice);

        log(actor, "RECORD_PAYMENT", "Payment", payment.getId(), method + " payment of " + invoice.getTotalAmount() + " recorded for invoice " + invoice.getInvoiceNumber());

        return payment;

    }

    /**
     * True once the invoice is tied to consultation has been fully paid. This
     * signal is used to unlock patient's consultation and prescription view for
     * patients.
     *
     * @param consultationId
     * @return
     */
    public boolean isConsultationUnlockedForPatient(Long consultationId) {
        Invoice invoice = invoiceDao.findByConsultationId(consultationId);
        return invoice != null && invoice.getStatus() == Status.PAID;
    }

    private void requireStaff(User actor) throws AuthException {
        String role = (actor != null && actor.getRole() != null) ? actor.getRole().getName() : "";
        if (!"ADMIN".equals(role) && !"RECEPTIONIST".equals(role)) {
            throw new AuthException("Only Admin or Receptionist can perform this action.");
        }
    }

    /**
     * Builds the signed form fields needed to redirect the patient to esewa's
     * hosted payment page.
     *
     * The request includes: - Amount information. - Transaction UUID. - eSewa
     * product code. - Success/failure URLs. - HMAC signature.
     *
     * @param actor
     * @param invoiceId
     * @param baseUrl
     * @return
     * @throws AuthException
     */
    public Map<String, String> buildEsewaFormFields(User actor, Long invoiceId, String baseUrl) throws AuthException {

        if (actor == null || actor.getRole() == null || !"PATIENT".equals(actor.getRole().getName())) {
            throw new AuthException("Only a patient can make an online payment");
        }

        Invoice invoice = invoiceDao.findById(invoiceId);
        if (invoice == null) {
            throw new AuthException("Invoice not found");
        }
        if (invoice.getStatus() == Status.PAID) {
            throw new AuthException("Invoice already paid");
        }
        if (invoice.getStatus() == Status.VOID) {
            throw new AuthException("Cannot pay a voided invoice.");
        }

        Patient patient = patientDao.findByUserId(actor.getId());
        Long owningPatientId = invoice.getConsultation().getAppointment().getPatient().getId();

        if (patient == null || !owningPatientId.equals(patient.getId())) {
            throw new AuthException("You can only pay your own invoices.");
        }

        String transactionUuid = invoice.getId() + "-" + System.currentTimeMillis();
        String amount = invoice.getConsultationFee().setScale(2, RoundingMode.HALF_UP).toPlainString();
        String tax = invoice.getTax() == null ? "0.00" : invoice.getTax().setScale(2, RoundingMode.HALF_UP).toPlainString();
        String total = invoice.getTotalAmount().setScale(2, RoundingMode.HALF_UP).toPlainString();

        String signedFieldNames = "total_amount,transaction_uuid,product_code";
        String message = "total_amount=" + total + ",transaction_uuid=" + transactionUuid + ",product_code=" + EsewaConfig.PRODUCT_CODE;
        String signature = signEsewaMessage(message);

        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("amount", amount);
        fields.put("tax_amount", tax);
        fields.put("total_amount", total);
        fields.put("transaction_uuid", transactionUuid);
        fields.put("product_code", EsewaConfig.PRODUCT_CODE);
        fields.put("product_service_charge", "0");
        fields.put("product_delivery_charge", "0");
        fields.put("success_url", baseUrl + "/payment/esewa/callback");
        fields.put("failure_url", baseUrl + "/patient/billing.xhtml?esewaStatus=failed");
        fields.put("signed_field_names", signedFieldNames);
        fields.put("signature", signature);

        log(actor, "INITIATE_ESEWA_PAYMENT", "Invoice", invoice.getId(), "eSewa payment initiated for invoice " + invoice.getInvoiceNumber() + " (txn " + transactionUuid + ")");

        return fields;

    }

    /**
     * Completes an eSewa payment after receiving the response from eSewa.
     *
     * The method: 1. Decodes the Base64 response. 2. Reads the JSON response.
     * 3. Verifies the response signature. 4. Checks that the payment status is
     * COMPLETE. 5. Finds the invoice from the transaction UUID. 6. Verifies the
     * transaction with eSewa's status API. 7. Records the payment in the local
     * database.
     *
     * @param actor
     * @param encodedData
     * @return
     * @throws AuthException
     */
    public Payment completeEsewaPayment(User actor, String encodedData) throws AuthException {

        if (encodedData == null || encodedData.isEmpty()) {
            throw new AuthException("Missing eSewa response data.");
        }

        JsonNode node;
        try {
            String decoded = new String(Base64.getDecoder().decode(encodedData), "UTF-8");
            node = new ObjectMapper().readTree(decoded);
        } catch (Exception e) {
            throw new AuthException("Could not read eSewa response.");
        }

        String status = node.path("status").asText("");
        String transactionUuid = node.path("transaction_uuid").asText("");
        String transactionCode = node.path("transaction_code").asText("");
        String totalAmount = node.path("total_amount").asText("");
        String productCode = node.path("product_code").asText("");
        String signedFieldNames = node.path("signed_field_names").asText("");
        String signature = node.path("signature").asText("");

        String expectedMessage = buildMessageFromSignedFields(node, signedFieldNames);
        String expectedSignature = signEsewaMessage(expectedMessage);
        if (!expectedSignature.equals(signature)) {
            throw new AuthException("eSewa response signature could not be verified.");
        }

        if (!"COMPLETE".equalsIgnoreCase(status)) {
            throw new AuthException("eSewa payment was not completed (status: " + status + ").");
        }

        Long invoiceId = parseInvoiceIdFromTransactionUuid(transactionUuid);
        if (invoiceId == null) {
            throw new AuthException("Could not resolve invoice from eSewa transaction reference.");
        }

        if (!verifyEsewaStatus(productCode, totalAmount, transactionUuid)) {
            throw new AuthException("eSewa transaction status could not be verified.");
        }

        return recordPayment(actor, invoiceId, Method.ESEWA, "ESEWA-" + transactionCode);

    }

 
    /**
     * 
     * @param actor
     * @param invoiceId
     * @param baseUrl
     * @return
     * @throws AuthException 
     */
    public String initiateKhaltiPayment(User actor, Long invoiceId, String baseUrl) throws AuthException {
        if (actor == null || actor.getRole() == null || !"PATIENT".equals(actor.getRole().getName())) {
            throw new AuthException("Only a patient can make an online payment.");
        }

        Invoice invoice = invoiceDao.findById(invoiceId);
        if (invoice == null) {
            throw new AuthException("Invoice not found.");
        }
        if (invoice.getStatus() == Status.PAID) {
            throw new AuthException("Invoice is already paid.");
        }
        if (invoice.getStatus() == Status.VOID) {
            throw new AuthException("Cannot pay a voided invoice.");
        }

        Patient patient = patientDao.findByUserId(actor.getId());
        Long owningPatientId = invoice.getConsultation().getAppointment().getPatient().getId();
        if (patient == null || !owningPatientId.equals(patient.getId())) {
            throw new AuthException("You can only pay your own invoices.");
        }

        long amountPaisa = invoice.getTotalAmount().multiply(new java.math.BigDecimal(100)).longValueExact();

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode payload = mapper.createObjectNode();
        payload.put("return_url", baseUrl + "/payment/khalti/callback");
        payload.put("website_url", baseUrl);
        payload.put("amount", String.valueOf(amountPaisa));
        payload.put("purchase_order_id", String.valueOf(invoice.getId()));
        payload.put("purchase_order_name", "Invoice " + invoice.getInvoiceNumber());

        ObjectNode customerInfo = mapper.createObjectNode();
        customerInfo.put("name", nullSafe(patient.getName()));
        customerInfo.put("email", nullSafe(actor.getEmail()));
        customerInfo.put("phone", nullSafe(patient.getPhone()));
        payload.set("customer_info", customerInfo);

        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(KhaltiConfig.INITIATE_URL).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "key " + KhaltiConfig.SECRET_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.toString().getBytes("UTF-8"));
            }

            int code = conn.getResponseCode();
            java.io.InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();

            StringBuilder body = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    body.append(line);
                }
            }

            if (code < 200 || code >= 300) {
                throw new AuthException("Khalti rejected the payment request: " + body.toString());
            }

            JsonNode responseNode = mapper.readTree(body.toString());
            String paymentUrl = responseNode.path("payment_url").asText(null);
            if (paymentUrl == null) {
                throw new AuthException("Khalti did not return a payment URL.");
            }

            log(actor, "INITIATE_KHALTI_PAYMENT", "Invoice", invoice.getId(),
                    "Khalti payment initiated for invoice " + invoice.getInvoiceNumber());

            return paymentUrl;
        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthException("Could not reach Khalti: " + e.getMessage());
        }
    }

    /**
     * 
     * @param actor
     * @param pidx
     * @param purchaseOrderId
     * @return
     * @throws AuthException 
     */
    public Payment completeKhaltiPayment(User actor, String pidx, String purchaseOrderId) throws AuthException {
        if (pidx == null || pidx.isEmpty()) {
            throw new AuthException("Missing Khalti reference.");
        }

        Long invoiceId;
        try {
            invoiceId = Long.parseLong(purchaseOrderId);
        } catch (Exception e) {
            throw new AuthException("Could not resolve invoice from Khalti order reference.");
        }

        Invoice invoice = invoiceDao.findById(invoiceId);
        if (invoice == null) {
            throw new AuthException("Invoice not found.");
        }

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode payload = mapper.createObjectNode();
        payload.put("pidx", pidx);

        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(KhaltiConfig.LOOKUP_URL).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "key " + KhaltiConfig.SECRET_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.toString().getBytes("UTF-8"));
            }

            int code = conn.getResponseCode();
            java.io.InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();

            StringBuilder body = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    body.append(line);
                }
            }

            if (code < 200 || code >= 300) {
                throw new AuthException("Khalti lookup failed: " + body.toString());
            }

            JsonNode result = mapper.readTree(body.toString());
            String status = result.path("status").asText("");
            String transactionId = result.path("transaction_id").asText("");
            long verifiedAmountPaisa = result.path("total_amount").asLong(-1);

            if (!"Completed".equalsIgnoreCase(status)) {
                throw new AuthException("Khalti payment was not completed (status: " + status + ").");
            }

            long expectedAmountPaisa = invoice.getTotalAmount().multiply(new java.math.BigDecimal(100)).longValueExact();
            if (verifiedAmountPaisa != expectedAmountPaisa) {
                throw new AuthException("Khalti payment amount does not match invoice total.");
            }

            return recordPayment(actor, invoiceId, Method.KHALTI, "KHALTI-" + transactionId);

        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthException("Could not verify Khalti payment: " + e.getMessage());
        }
    }

    private void log(User actor, String action, String entityName, Long entityId, String details) {
        ActivityLog entry = new ActivityLog();
        entry.setUser(actor);
        entry.setAction(action);
        entry.setEntityName(entityName);
        entry.setEntityId(entityId);
        entry.setDetails(details);
        activityLogDao.create(entry);
    }

    /**
     * Creates an HMAC-SHA256 signature for an eSewa message.
     *
     * @param message
     * @return
     */
    private String signEsewaMessage(String message) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(EsewaConfig.SECRET_KEY.getBytes("UTF-8"), "HmacSHA256"));
            byte[] hash = mac.doFinal(message.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign eSewa payment request", e);
        }
    }

    /**
     * Reconstructs the message that esewa used for signing.
     *
     * Example:
     *
     * signedFieldNames = total_amount,transaction_uuid,product_code
     *
     * Result: total_amount=1000,transaction_uuid=1-123456,product_code=EPAYTEST
     *
     * @param node
     * @param signedFieldNames
     * @return
     */
    private String buildMessageFromSignedFields(JsonNode node, String signedFieldNames) {
        String[] names = signedFieldNames.split(",");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < names.length; i++) {
            String key = names[i].trim();
            if (i > 0) {
                sb.append(",");
            }
            sb.append(key).append("=").append(node.path(key).asText(""));
        }
        return sb.toString();
    }

    /**
     * Extracts the invoice id from the esewa transaction UUID.
     *
     * The transaction UUID is generated in the following format:
     * invoiceId-currentTimestamp
     *
     * Example: 25-1723456789000
     *
     * Therefore, the first part is the invoice ID.
     *
     * @param transactionUuid
     * @return
     */
    private Long parseInvoiceIdFromTransactionUuid(String transactionUuid) {
        try {
            return Long.parseLong(transactionUuid.split("-")[0]);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Verifies the esewa transaction by calling esewa server side status
     * verification Api.
     *
     * This prevents the application from trusting only browser redirect when
     * making an invoice as paid.
     *
     * @param productCode
     * @param totalAmount
     * @param transactionUuid
     * @return
     */
    private boolean verifyEsewaStatus(String productCode, String totalAmount, String transactionUuid) {
        try {
            String url = EsewaConfig.STATUS_CHECK_URL
                    + "?product_code=" + URLEncoder.encode(productCode, "UTF-8")
                    + "&total_amount=" + URLEncoder.encode(totalAmount, "UTF-8")
                    + "&transaction_uuid=" + URLEncoder.encode(transactionUuid, "UTF-8");

            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);

            StringBuilder body = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    body.append(line);
                }
            }

            JsonNode statusNode = new ObjectMapper().readTree(body.toString());
            return "COMPLETE".equalsIgnoreCase(statusNode.path("status").asText(""));
        } catch (Exception e) {
            return false;
        }
    }

    private String nullSafe(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s;
    }
}
