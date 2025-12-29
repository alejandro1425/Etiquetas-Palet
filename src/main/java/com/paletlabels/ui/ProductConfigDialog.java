package com.paletlabels.ui;

import com.paletlabels.model.Product;
import com.paletlabels.service.ProductService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ProductConfigDialog extends JDialog {
    private final ProductService productService;
    private final DefaultTableModel tableModel;
    private JTable table;
    private static final Object[] COLS = new Object[]{
        "Nombre", "Unidades/Caja", "Peso unitario (kg)", "EAN14", "Peso variable"
    };


    public ProductConfigDialog(Window owner, ProductService productService) {
        super(owner, "Configurador de productos", ModalityType.APPLICATION_MODAL);
        this.productService = productService;
        this.tableModel = new DefaultTableModel(COLS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 4 ? Boolean.class : super.getColumnClass(columnIndex);
            }
        };
        buildUi();
        loadData();
        setSize(720, 360);
        setLocationRelativeTo(owner);
    }

    private void buildUi() {
        setLayout(new BorderLayout());
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JButton addButton = new JButton("Añadir");
        JButton editButton = new JButton("Editar");
        JButton deleteButton = new JButton("Eliminar");

        addButton.addActionListener(e -> openForm(null, -1));
        editButton.addActionListener(e -> {
            int selected = table.getSelectedRow();
            if (selected >= 0) {
                Product existing = productService.getAll().get(selected);
                openForm(existing, selected);
            }
        });
        deleteButton.addActionListener(e -> {
            int selected = table.getSelectedRow();
            if (selected >= 0) {
                int confirm = JOptionPane.showConfirmDialog(this, "¿Eliminar el producto seleccionado?", "Confirmar", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    productService.delete(selected);
                    SwingUtilities.invokeLater(this::loadData);
                }
            }
        });

        JPanel buttons = new JPanel();
        buttons.add(addButton);
        buttons.add(editButton);
        buttons.add(deleteButton);
        add(buttons, BorderLayout.SOUTH);
    }

    private void openForm(Product product, int index) {
        JTextField nameField = new JTextField(product != null ? product.getName() : "");
        JSpinner unitsField = new JSpinner(new SpinnerNumberModel(product != null ? product.getUnitsPerBox() : 1, 1, 10_000, 1));
        JSpinner weightField = new JSpinner(new SpinnerNumberModel(product != null ? product.getWeightPerUnitKg() : 0.1, 0.001, 9_999.999, 0.001));
        JTextField eanField = new JTextField(product != null ? product.getEan13() : "");
        JCheckBox variableWeightCheck = new JCheckBox("", product != null && product.isVariableWeight());

        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        panel.add(new JLabel("Nombre"));
        panel.add(nameField);
        panel.add(new JLabel("Unidades por caja"));
        panel.add(unitsField);
        panel.add(new JLabel("Peso unitario (kg)"));
        panel.add(weightField);
        panel.add(new JLabel("EAN14"));
        panel.add(eanField);
        panel.add(new JLabel("Peso variable"));
        panel.add(variableWeightCheck);

        int result = JOptionPane.showConfirmDialog(this, panel, product == null ? "Nuevo producto" : "Editar producto", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            Product updated = new Product(nameField.getText().trim(), ((Number) unitsField.getValue()).intValue(), ((Number) weightField.getValue()).doubleValue(), eanField.getText().trim());
            updated.setVariableWeight(variableWeightCheck.isSelected());
            if (index >= 0) {
                productService.update(index, updated);
            } else {
                productService.add(updated);
            }
            SwingUtilities.invokeLater(this::loadData);
        }
    }

    private void loadData() {
        List<Product> products = productService.getAll();

        tableModel.setRowCount(0);
        for (Product p : products) {
            tableModel.addRow(new Object[]{
                p.getName(),
                p.getUnitsPerBox(),
                p.getWeightPerUnitKg(),
                p.getEan13(),
                p.isVariableWeight()
            });
        }

        // Fuerza a JTable a "reenganchar" el modelo y reconstruir vista
        if (table != null) {
            table.setModel(tableModel);
            table.revalidate();
            table.repaint();
        }
    }

}
