package org.crud;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;

import org.crud.empleados.dao.EmpleadoDAO;
import org.crud.empleados.domain.Empleado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainForm extends JFrame {

    /**
     * 
     */
    private static final long serialVersionUID = 4720079838247703732L;

    private static final Logger logger = LoggerFactory.getLogger(MainForm.class);

    private JButton addEmpleadoBtn = new JButton("Agregar empleado");
    private JButton deleteEmpleadoBtn = new JButton("Borrar empleado");
    private JButton limpiarCamposBtn = new JButton("Limpiar Campos");
    private JLabel idLabel = new JLabel("Id");
    private JTextField idTextField = new JTextField(20);
    private JLabel nombreLabel = new JLabel("Nombre");
    private JTextField nombreTextField = new JTextField(20);
    private JLabel correoLabel = new JLabel("Correo");
    private JTextField correoTextField = new JTextField(20);
    private JTable empleadosTable = new JTable();
    private DefaultTableModel empleadosTableModel = new DefaultTableModel();
    private JScrollPane empleadosTableScroll = new JScrollPane(empleadosTable);

    public MainForm() {
        setTitle("Java Crud");
        setSize(500, 600);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        init();
    }

    private void init() {
        Container c = this.getContentPane();
        c.setLayout(null);
        idLabel.setBounds(20, 20, 60, 25);
        nombreLabel.setBounds(20, 50, 60, 25);
        correoLabel.setBounds(20, 80, 60, 25);
        c.add(idLabel);
        c.add(nombreLabel);
        c.add(correoLabel);
        idTextField.setBounds(80, 20, 60, 25);
        idTextField.setEditable(false);
        nombreTextField.setBounds(80, 50, 380, 25);
        correoTextField.setBounds(80, 80, 380, 25);
        c.add(idTextField);
        c.add(nombreTextField);
        c.add(correoTextField);
        addEmpleadoBtn.setBounds(20, 130, 140, 25);
        deleteEmpleadoBtn.setBounds(170, 130, 140, 25);
        limpiarCamposBtn.setBounds(320, 130, 140, 25);
        c.add(addEmpleadoBtn);
        c.add(deleteEmpleadoBtn);
        c.add(limpiarCamposBtn);
        empleadosTable.setModel(empleadosTableModel);
        empleadosTableModel.addColumn("id");
        empleadosTableModel.addColumn("nombre");
        empleadosTableModel.addColumn("correo");
        empleadosTableScroll.setBounds(20, 180, 440, 340);
        c.add(empleadosTableScroll);
        refresh();
        addEmpleadoBtn.addActionListener(this::addEmpleadoClick);
        empleadosTable.getSelectionModel().addListSelectionListener(this::empleadosTableChange);
        deleteEmpleadoBtn.addActionListener(this::deleteEmpleadoClick);
        limpiarCamposBtn.addActionListener(event -> limpiarCampos());
    }

    private void refresh() {
        try {
            EmpleadoDAO empleadoDAO = new EmpleadoDAO();
            List<Empleado> empleados = empleadoDAO.getEmpleados();
            empleadosTableModel.setRowCount(0);
            empleados.stream().forEach(empleado -> {
                empleadosTableModel
                        .addRow(new Object[] { empleado.getId(), empleado.getNombre(), empleado.getCorreo() });
            });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void addEmpleadoClick(ActionEvent event) {
        Empleado empleado = new Empleado();
        String idText = idTextField.getText();
        if (!idText.isEmpty()) {
            empleado.setId(Long.parseLong(idText));
        }
        empleado.setNombre(nombreTextField.getText());
        empleado.setCorreo(correoTextField.getText());
        if (empleadoIsValido(empleado)) {
            if (!isCorreoValido(empleado.getCorreo())) {
                JOptionPane.showMessageDialog(this, "El correo no es valido", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                EmpleadoDAO empleadoDAO = new EmpleadoDAO();
                try {
                    empleadoDAO.saveEmpleado(empleado);
                    JOptionPane.showMessageDialog(this, "Exito al agregar empleado", "Informacion",
                            JOptionPane.INFORMATION_MESSAGE);
                    this.limpiarCampos();
                    this.refresh();
                } catch (ClassNotFoundException | SQLException e) {
                    logger.error(e.getMessage(), e);
                    JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Todos los campos son requeridos", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void empleadosTableChange(ListSelectionEvent event) {
        int row = empleadosTable.getSelectedRow();
        if (row != -1) {
            Long id = (Long) empleadosTableModel.getValueAt(row, 0);
            EmpleadoDAO empleadoDAO = new EmpleadoDAO();
            try {
                Empleado empleado = empleadoDAO.getEmpleado(id);
                idTextField.setText(String.valueOf(empleado.getId()));
                nombreTextField.setText(empleado.getNombre());
                correoTextField.setText(empleado.getCorreo());
            } catch (ClassNotFoundException | SQLException e) {
                logger.error(e.getMessage(), e);
                JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void limpiarCampos() {
        idTextField.setText("");
        nombreTextField.setText("");
        correoTextField.setText("");
    }

    private void deleteEmpleadoClick(ActionEvent event) {
        String idField = idTextField.getText();
        if (!idField.isEmpty()) {
            if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this,
                    "¿Esta seguro de que desea borrar el empleado?", "Pregunta", JOptionPane.YES_NO_OPTION)) {
                long id = Long.parseLong(idField);
                EmpleadoDAO empleadoDAO = new EmpleadoDAO();
                try {
                    empleadoDAO.deleteEmpleado(id);
                } catch (ClassNotFoundException | SQLException e) {
                    logger.error(e.getMessage(), e);
                    JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                this.limpiarCampos();
                this.refresh();
                JOptionPane.showMessageDialog(this, "Exito al borrar empleado", "Informacion",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "No se ha seleccionado un empleado para borrar", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean empleadoIsValido(Empleado empleado) {
        return !empleado.getCorreo().isEmpty() && !empleado.getNombre().isEmpty();
    }

    public boolean isCorreoValido(String email) {
        String regex = "^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

}
