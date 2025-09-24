package cabinet.views;

import javax.swing.*;
import java.awt.*;
import java.awt.print.*;

public class InvoicePrinter implements Printable {
    private String invoiceContent;

    public InvoicePrinter(String content) {
        this.invoiceContent = content;
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex > 0) {
            return NO_SUCH_PAGE;
        }

        Graphics2D g2d = (Graphics2D) graphics;
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

        // Définir la police
        Font font = new Font("Monospaced", Font.PLAIN, 10);
        g2d.setFont(font);

        // Diviser le contenu en lignes
        String[] lines = invoiceContent.split("\n");
        int y = 15;

        for (String line : lines) {
            g2d.drawString(line, 10, y);
            y += 15;
        }

        return PAGE_EXISTS;
    }

    public static void printInvoice(String content) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Facture Médicale");

        job.setPrintable(new InvoicePrinter(content));

        if (job.printDialog()) {
            try {
                job.print();
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(null, "Erreur d'impression: " + e.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
