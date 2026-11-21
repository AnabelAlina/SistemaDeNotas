package gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import app.GestorNotas;
import evaluaciones.Evaluacion;
import evaluaciones.Examen;
import evaluaciones.ExamenSustitutorio;
import excepciones.NotaInvalidaException;
import modelo.Curso;

/**
 * Ventana de registro de notas: exámenes de teoría y examen sustitutorio
 * (en el siguiente PR se añade la zona de trabajos de evaluación continua).
 * Reemplaza a MenuNotas de la consola.
 */
public class VentanaNotas extends JFrame {

    private GestorNotas gestor;
    private SelectorCurso selector;

    // Controles para Examen 1, 2 y 3
    private JLabel[] lblExamenInfo;
    private JTextField[] txtExamenNota;
    private JButton[] btnExamenGuardar;
    private Examen[] examenesActuales;

    // Controles para Examen Sustitutorio
    private JLabel lblSustiInfo;
    private JTextField txtSustiNota;
    private JButton btnSustiGuardar;

    // Resumen inferior
    private JLabel lblPromedioCurso;

    public VentanaNotas(GestorNotas gestor) {
        super("Registro de notas");
        if (gestor == null) {
            throw new IllegalArgumentException("El gestor no puede ser nulo.");
        }
        this.gestor = gestor;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(820, 520);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Estilo.COLOR_FONDO);

        // NORTH: selector encadenado
        selector = new SelectorCurso(gestor);
        selector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actualizarVista();
            }
        });
        add(selector, BorderLayout.NORTH);

        // CENTER: exámenes y sustitutorio
        JPanel centro = new JPanel(new GridLayout(2, 1, 10, 10));
        centro.setBackground(Estilo.COLOR_FONDO);
        centro.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        centro.add(crearPanelExamenes());
        centro.add(crearPanelSustitutorio());
        add(centro, BorderLayout.CENTER);

        // SOUTH: promedio actual del curso
        lblPromedioCurso = new JLabel("Promedio actual del curso: --", SwingConstants.CENTER);
        lblPromedioCurso.setFont(Estilo.FUENTE_TITULO);
        lblPromedioCurso.setForeground(Estilo.COLOR_TEXTO);
        lblPromedioCurso.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        add(lblPromedioCurso, BorderLayout.SOUTH);

        actualizarVista();
        setVisible(true);
    }

    // ---------- Construcción de paneles ----------

    private JPanel crearPanelExamenes() {
        JPanel panel = new JPanel(new GridLayout(3, 3, 8, 8));
        panel.setBackground(Estilo.COLOR_FONDO);
        panel.setBorder(BorderFactory.createTitledBorder("Exámenes de teoría (nota de 0 a 20)"));

        lblExamenInfo = new JLabel[3];
        txtExamenNota = new JTextField[3];
        btnExamenGuardar = new JButton[3];
        examenesActuales = new Examen[3];

        for (int i = 0; i < 3; i++) {
            final int indice = i;
            lblExamenInfo[i] = new JLabel("Examen " + (i + 1) + ": --");
            lblExamenInfo[i].setFont(Estilo.FUENTE_NORMAL);
            lblExamenInfo[i].setForeground(Estilo.COLOR_TEXTO);

            txtExamenNota[i] = new JTextField(6);
            txtExamenNota[i].setFont(Estilo.FUENTE_NORMAL);

            btnExamenGuardar[i] = new JButton("Guardar Examen " + (i + 1));
            btnExamenGuardar[i].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    guardarExamen(indice);
                }
            });

            panel.add(lblExamenInfo[i]);
            panel.add(txtExamenNota[i]);
            panel.add(btnExamenGuardar[i]);
        }
        return panel;
    }

    private JPanel crearPanelSustitutorio() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Estilo.COLOR_FONDO);
        panel.setBorder(BorderFactory.createTitledBorder("Examen Sustitutorio (opcional)"));

        JLabel ayuda = new JLabel(
                "Reemplaza la nota más baja entre Examen 1 y Examen 2 (nunca Examen 3), solo si la mejora.");
        ayuda.setFont(Estilo.FUENTE_NORMAL);
        ayuda.setForeground(Estilo.COLOR_TEXTO);
        panel.add(ayuda, BorderLayout.NORTH);

        JPanel fila = new JPanel(new GridLayout(1, 3, 8, 8));
        fila.setBackground(Estilo.COLOR_FONDO);

        lblSustiInfo = new JLabel("Sustitutorio: sin rendir");
        lblSustiInfo.setFont(Estilo.FUENTE_NORMAL);
        lblSustiInfo.setForeground(Estilo.COLOR_TEXTO);

        txtSustiNota = new JTextField(6);
        txtSustiNota.setFont(Estilo.FUENTE_NORMAL);

        btnSustiGuardar = new JButton("Guardar Sustitutorio");
        btnSustiGuardar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                guardarSustitutorio();
            }
        });

        fila.add(lblSustiInfo);
        fila.add(txtSustiNota);
        fila.add(btnSustiGuardar);
        panel.add(fila, BorderLayout.CENTER);

        return panel;
    }

    // ---------- Actualización de datos ----------

    private void actualizarVista() {
        if (selector.getEstudiante() != null) {
            gestor.setActual(selector.getEstudiante());
        }

        Curso curso = selector.getCurso();
        if (curso == null) {
            for (int i = 0; i < 3; i++) {
                examenesActuales[i] = null;
                lblExamenInfo[i].setText("Examen " + (i + 1) + ": (sin curso)");
                txtExamenNota[i].setText("");
                txtExamenNota[i].setEnabled(false);
                btnExamenGuardar[i].setEnabled(false);
            }
            lblSustiInfo.setText("Sustitutorio: (sin curso)");
            txtSustiNota.setText("");
            txtSustiNota.setEnabled(false);
            btnSustiGuardar.setEnabled(false);
            lblPromedioCurso.setText("Promedio actual del curso: --");
            return;
        }

        ArrayList<Examen> listaExamenes = new ArrayList<>();
        for (Evaluacion ev : curso.getEvaluaciones()) {
            if (ev instanceof Examen) {
                listaExamenes.add((Examen) ev);
            }
        }

        for (int i = 0; i < 3; i++) {
            if (i < listaExamenes.size()) {
                Examen ex = listaExamenes.get(i);
                examenesActuales[i] = ex;
                String estado = ex.isRendida() ? String.format("%.2f", ex.getNota()) : "pendiente";
                lblExamenInfo[i].setText(ex.getNombre() + " (" + ex.getPeso() + "%): " + estado);
                txtExamenNota[i].setText(ex.isRendida() ? String.valueOf(ex.getNota()) : "");
                txtExamenNota[i].setEnabled(true);
                btnExamenGuardar[i].setEnabled(true);
            } else {
                examenesActuales[i] = null;
                lblExamenInfo[i].setText("Examen " + (i + 1) + ": no registrado");
                txtExamenNota[i].setText("");
                txtExamenNota[i].setEnabled(false);
                btnExamenGuardar[i].setEnabled(false);
            }
        }

        ExamenSustitutorio susti = curso.getSustitutorio();
        if (susti != null && susti.isRendida()) {
            lblSustiInfo.setText("Sustitutorio: " + String.format("%.2f", susti.getNota()));
            txtSustiNota.setText(String.valueOf(susti.getNota()));
        } else {
            lblSustiInfo.setText("Sustitutorio: sin rendir");
            txtSustiNota.setText("");
        }
        txtSustiNota.setEnabled(true);
        btnSustiGuardar.setEnabled(true);

        actualizarPromedio(curso);
    }

    private void actualizarPromedio(Curso curso) {
        if (!curso.tieneNotas()) {
            lblPromedioCurso.setText("Promedio actual del curso: sin notas");
        } else {
            lblPromedioCurso.setText(String.format("Promedio actual de %s: %.2f",
                    curso.getNombre(), curso.calcularPromedio()));
        }
    }

    // ---------- Guardado de notas ----------

    private void guardarExamen(int indice) {
        Curso curso = selector.getCurso();
        Examen examen = examenesActuales[indice];
        if (curso == null || examen == null) {
            return;
        }
        double nota;
        try {
            nota = Double.parseDouble(txtExamenNota[indice].getText().trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Escribe un número válido (ej. 14.5).",
                    "Dato inválido", JOptionPane.ERROR_MESSAGE);
            txtExamenNota[indice].requestFocus();
            return;
        }
        try {
            gestor.asignarNota(curso, examen, nota);
            actualizarVista();
            JOptionPane.showMessageDialog(this,
                    examen.getNombre() + " guardado con nota " + nota + ".",
                    "Nota registrada", JOptionPane.INFORMATION_MESSAGE);
        } catch (NotaInvalidaException | IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Nota inválida", JOptionPane.ERROR_MESSAGE);
            txtExamenNota[indice].requestFocus();
        }
    }

    private void guardarSustitutorio() {
        Curso curso = selector.getCurso();
        if (curso == null) {
            return;
        }
        double nota;
        try {
            nota = Double.parseDouble(txtSustiNota.getText().trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Escribe un número válido (ej. 15.0).",
                    "Dato inválido", JOptionPane.ERROR_MESSAGE);
            txtSustiNota.requestFocus();
            return;
        }
        try {
            ExamenSustitutorio susti = curso.obtenerOCrearSustitutorio();
            gestor.asignarNota(curso, susti, nota);
            actualizarVista();
            JOptionPane.showMessageDialog(this,
                    "Examen sustitutorio guardado con nota " + nota + ".",
                    "Sustitutorio registrado", JOptionPane.INFORMATION_MESSAGE);
        } catch (NotaInvalidaException | IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Nota inválida", JOptionPane.ERROR_MESSAGE);
            txtSustiNota.requestFocus();
        }
    }
}