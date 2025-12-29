package com.paletlabels.print;

import com.paletlabels.model.Product;

import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

public class LabelPrintable implements Printable {
    private final String orderNumber;
    private final int boxes;
    private final String lot;
    private final String bestBefore;
    private final double netWeight;
    private final Product product;
    private final String gs1Data;
    private final BufferedImage barcode;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/uuuu");

    public LabelPrintable(String orderNumber, int boxes, String lot, String bestBefore, double netWeight, Product product, String gs1Data, BufferedImage barcode) {
        this.orderNumber = orderNumber;
        this.boxes = boxes;
        this.lot = lot;
        this.bestBefore = bestBefore;
        this.netWeight = netWeight;
        this.product = product;
        this.gs1Data = gs1Data;
        this.barcode = barcode;
    }

    //Wrapper de texto
    private float drawWrappedText(
        Graphics2D g2d,
        String text,
        float x,
        float y,
        double maxWidth,
        float lineHeight
) {
    FontMetrics fm = g2d.getFontMetrics();
    String[] words = text.split(" ");
    StringBuilder line = new StringBuilder();

    for (String word : words) {
        String testLine = line + word + " ";
        double testWidth = fm.stringWidth(testLine);

        if (testWidth > maxWidth && line.length() > 0) {
            g2d.drawString(line.toString(), x, y);
            y += lineHeight;
            line = new StringBuilder(word).append(" ");
        } else {
            line.append(word).append(" ");
        }
    }

    if (line.length() > 0) {
        g2d.drawString(line.toString(), x, y);
        y += lineHeight;
    }

    return y;
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex > 0) {
            return NO_SUCH_PAGE;
        }
        Graphics2D g2d = (Graphics2D) graphics;
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());               
        g2d.setPaint(Color.BLACK);
        double printableWidth = pageFormat.getImageableWidth();         //Ancho útil (restando márgenes)

        float y = 15;
        
        //DATOS PALET
        g2d.setFont(g2d.getFont().deriveFont(Font.PLAIN, 16f));
        g2d.drawString("Nº DE PEDIDO: " + (orderNumber == null ? "" : orderNumber), 0, y);
        y += 25;

        g2d.drawString("LOTE: " + lot, 0, y);
        y += 25;

        g2d.drawString("CAJAS: " + boxes, 0, y);
        y += 25;

        g2d.drawString("FECHA CONSUMO PREFERENTE: " + bestBefore, 0, y);
        y += 25;

        java.text.DecimalFormatSymbols sym = new java.text.DecimalFormatSymbols(new java.util.Locale("es", "ES"));
        sym.setDecimalSeparator(',');

        DecimalFormat weightFormat = new DecimalFormat("#0.000", sym);
        weightFormat.setGroupingUsed(false); // sin miles

        g2d.drawString("PESO NETO PALET: " + weightFormat.format(netWeight) + " KG", 0, y);
        y += 35;

        // NOMBRE DE PRODUCTO
        Font productFont = g2d.getFont().deriveFont(Font.BOLD, 18f);
        g2d.setFont(productFont);

        FontMetrics fm = g2d.getFontMetrics();
        int lineHeight = fm.getHeight();

        y = drawWrappedText(
            g2d,
            product.getName(),
            0,
            y,
            printableWidth,
            lineHeight
        );

        //y += 10; // separación extra


        /*if (barcode != null) {            //Genera a tamaño real, no entra en A5
            int x = 10;
            g2d.drawImage(barcode, x, (int) y, barcode.getWidth(), barcode.getHeight(), null);
            y += barcode.getHeight() + 13;
        }*/

        //Generación img GS1 con centrado y ajustado a ancho de página
        if (barcode != null) {
            //double printableWidth = pageFormat.getImageableWidth();         //Ancho útil (restando márgenes)

            // Queremos que ocupe como mucho el 90% del ancho imprimible
            double maxBarcodeWidth = printableWidth * 0.9;

            double scale = maxBarcodeWidth / barcode.getWidth();
            if (scale > 1.0) {
                scale = 1.0; // no escalar hacia arriba
            }

            int drawWidth = (int) (barcode.getWidth() * scale);
            int drawHeight = (int) (barcode.getHeight() * scale);

            int x = (int) ((printableWidth - drawWidth) / 2);

            g2d.drawImage(barcode, x, (int) y, drawWidth, drawHeight, null);
            y += drawHeight + 13;
        }


        g2d.setFont(g2d.getFont().deriveFont(Font.PLAIN, 12f));       //Alineado a izquierda
        g2d.drawString(gs1Data, 0, y);

        //Texto GS1 centrado
        /*FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(gs1Data);
        int x = (int) ((pageFormat.getImageableWidth() - textWidth) / 2);
        g2d.drawString(gs1Data, x, y);*/

        return PAGE_EXISTS;
    }

    public static PageFormat buildA5PageFormat() {
    Paper paper = new Paper();

    // A5 portrait: 148 x 210 mm -> 419.53 x 595.28 points
    double w = 419.53;
    double h = 595.28;

    paper.setSize(w, h);

    // No fuerces márgenes aquí: el driver de la Konica define su imageable real
    paper.setImageableArea(0, 0, w, h);

    PageFormat format = new PageFormat();
    format.setPaper(paper);
    format.setOrientation(PageFormat.LANDSCAPE);
    return format;
}
}
