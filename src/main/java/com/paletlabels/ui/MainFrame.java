package com.paletlabels.ui;

import com.paletlabels.model.Product;
import com.paletlabels.print.LabelPrintable;
import com.paletlabels.service.ProductService;
import com.paletlabels.util.BarcodeUtil;
import com.paletlabels.util.Gs1Formatter;

import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;

import javax.swing.*;
import javax.imageio.ImageIO;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

public class MainFrame extends JFrame {
    private final ProductService productService;
    private JComboBox<Product> productCombo;
    private JSpinner boxesSpinner;
    private JTextField orderNumberField;
    private JTextField lotField;
    private JFormattedTextField bestBeforeField;
    private JSpinner netWeightSpinner;
    private JPanel netWeightPanel;
    private JLabel weightLabel;
    private JTextArea gs1Preview;
    private Image backgroundImage;

    public MainFrame() {
        super("Etiquetas A5 - Palet");
        this.productService = new ProductService();
        setIconImage(loadImage("/images/icon.png"));
        this.backgroundImage = loadImage("/images/background.png");
        buildUi();
        updateNetWeight();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(720, 520);
        setLocationRelativeTo(null);
    }

    private void buildUi() {
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        productCombo = new JComboBox<>(productService.getAll().toArray(new Product[0]));
        productCombo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                updateNetWeight();
            }
        });
        c.gridx = 0; c.gridy = row; form.add(new JLabel("Producto"), c);
        c.gridx = 1; c.gridy = row++; form.add(productCombo, c);

        //Num cajas
        boxesSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10_000, 1));
        boxesSpinner.addChangeListener(e -> updateNetWeight());
        c.gridx = 0; c.gridy = row; form.add(new JLabel("CAJAS"), c);
        c.gridx = 1; c.gridy = row++; form.add(boxesSpinner, c);

        //Num pedido
        orderNumberField = new JTextField();
        c.gridx = 0; c.gridy = row; form.add(new JLabel("Nº DE PEDIDO"), c);
        c.gridx = 1; c.gridy = row++; form.add(orderNumberField, c);

        //Lote
        lotField = new JTextField();
        lotField.getDocument().addDocumentListener(SimpleDocumentListener.onChange(this::updateNetWeight));
        c.gridx = 0; c.gridy = row; form.add(new JLabel("LOTE"), c);
        c.gridx = 1; c.gridy = row++; form.add(lotField, c);

        //Fecha caducidad
        bestBeforeField = new JFormattedTextField(DateTimeFormatter.ofPattern("dd/MM/yyyy").toFormat());
        bestBeforeField.setToolTipText("Formato dd/MM/yyyy");
        bestBeforeField.getDocument().addDocumentListener(SimpleDocumentListener.onChange(this::updateNetWeight));
        c.gridx = 0; c.gridy = row; form.add(new JLabel("FECHA CONSUMO PREFERENTE"), c);
        c.gridx = 1; c.gridy = row++; form.add(bestBeforeField, c);

        //Peso neto si es variable (02)
        netWeightSpinner = new JSpinner(new SpinnerNumberModel(1.000, 0.001, 9999.999, 0.001));
        netWeightSpinner.addChangeListener(e -> updateNetWeight());

        netWeightPanel = new JPanel(new BorderLayout(8, 0));
        netWeightPanel.add(new JLabel("PESO NETO (kg)"), BorderLayout.WEST);
        netWeightPanel.add(netWeightSpinner, BorderLayout.CENTER);

        // Se mostrará/ocultará según producto
        c.gridx = 0; c.gridy = row; c.gridwidth = 2; form.add(netWeightPanel, c);
        row++;
        c.gridwidth = 1;

        //Peso neto (sólo muestra, no interactuable)
        weightLabel = new JLabel("PESO NETO PALET: 0000.000 kg");
        c.gridx = 0; c.gridy = row; c.gridwidth = 2; form.add(weightLabel, c);
        row++;

        //Botones
        JButton printButton = new JButton("Imprimir etiqueta");
        printButton.addActionListener(e -> printLabel());

        JButton configButton = new JButton("Configurar productos");
        configButton.addActionListener(e -> openProductConfig());

        JPanel buttons = new JPanel();
        buttons.add(printButton);
        buttons.add(configButton);

        //Panel texto gs1
        gs1Preview = new JTextArea(3, 40);
        gs1Preview.setEditable(false);
        gs1Preview.setLineWrap(true);
        gs1Preview.setWrapStyleWord(true);
        gs1Preview.setBorder(BorderFactory.createTitledBorder("Preview GS1"));

        JPanel container = new BackgroundPanel(new BorderLayout(), backgroundImage);
        container.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        container.add(form, BorderLayout.NORTH);
        container.add(gs1Preview, BorderLayout.CENTER);
        container.add(buttons, BorderLayout.SOUTH);

        setContentPane(container);
    }

    private void updateNetWeight() {
    Product product = (Product) productCombo.getSelectedItem();
    if (product == null) {
        weightLabel.setText("PESO NETO PALET: -");
        gs1Preview.setText("");
        if (netWeightPanel != null) netWeightPanel.setVisible(false);
        return;
    }

    int boxes = ((Number) boxesSpinner.getValue()).intValue();

    boolean variable = product.isVariableWeight();
    if (netWeightPanel != null) netWeightPanel.setVisible(variable);

    double netWeightKg;
    if (variable) {
        netWeightKg = ((Number) netWeightSpinner.getValue()).doubleValue();
    } else {
        netWeightKg = product.calculateNetWeightKg(boxes);
    }

    DecimalFormat df = new DecimalFormat("0000.000");
    weightLabel.setText("PESO NETO PALET: " + df.format(netWeightKg) + " kg");

    gs1Preview.setText(Gs1Formatter.buildGs1Data(
            product,
            netWeightKg,
            lotField.getText(),
            bestBeforeField.getText()
    ));
}

    private void openProductConfig() {
        ProductConfigDialog dialog = new ProductConfigDialog(this, productService);
        dialog.setVisible(true);
        productCombo.setModel(new DefaultComboBoxModel<>(productService.getAll().toArray(new Product[0])));
        updateNetWeight();
    }

    private void printLabel() {
        Product product = (Product) productCombo.getSelectedItem();
        if (product == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto", "Producto requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int boxes = ((Number) boxesSpinner.getValue()).intValue();

        // Si el producto es de peso variable (02), el usuario introduce el peso neto manualmente.
        // Si es fijo (01), se calcula automáticamente a partir de cajas * unidades/caja * peso unitario.
        double netWeight = product.isVariableWeight()
                ? ((Number) netWeightSpinner.getValue()).doubleValue()
                : product.calculateNetWeightKg(boxes);

        String gs1Data = Gs1Formatter.buildGs1Data(product, netWeight, lotField.getText(), bestBeforeField.getText());
        BufferedImage barcode = BarcodeUtil.buildCode128(gs1Data, 320, 80);

        LabelPrintable printable = new LabelPrintable(orderNumberField.getText(), boxes, lotField.getText(), bestBeforeField.getText(), netWeight, product, gs1Data, barcode);
        PrinterJob job = PrinterJob.getPrinterJob();
        
        PageFormat format = job.validatePage(LabelPrintable.buildA5PageFormat());
        format = job.pageDialog(format); // importante en Konica para que el driver “acepte” A5/Landscape
        
        job.setPrintable(printable, format);
        
        PrintRequestAttributeSet attrs = new HashPrintRequestAttributeSet();
        attrs.add(MediaSizeName.ISO_A5);
        attrs.add(OrientationRequested.LANDSCAPE);
        
        try {
            if (job.printDialog(attrs)) {
                job.print(attrs);
            }
        } catch (PrinterException e) {
            JOptionPane.showMessageDialog(this, "No se pudo imprimir: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Image loadImage(String resourcePath) {
        try (InputStream stream = getClass().getResourceAsStream(resourcePath)) {
            if (stream == null) {
                return null;
            }
            return ImageIO.read(stream);
        } catch (IOException e) {
            System.err.println("No se pudo cargar la imagen " + resourcePath + ": " + e.getMessage());
            return null;
        }
    }

    private static class BackgroundPanel extends JPanel {
        private final Image background;

        BackgroundPanel(LayoutManager layout, Image background) {
            super(layout);
            this.background = background;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (background != null) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.12f));
                g2d.drawImage(background, 0, 0, getWidth(), getHeight(), this);
                g2d.dispose();
            }
        }
    }
}
