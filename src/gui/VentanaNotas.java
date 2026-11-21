package gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import app.GestorNotas;
import evaluaciones.Continua;
import evaluaciones.Evaluacion;
import evaluaciones.Examen;
import evaluaciones.ExamenSustitutorio;
import evaluaciones.TrabajoContinua;
import excepciones.NotaInvalidaException;
import excepciones.PesoInvalidoException;
import modelo.Curso;

/**
 * Ventana completa de registro de notas: exámenes de teoría,
 * trabajos de evaluación continua (Continua 1, 2 y 3) y examen sustitutorio.
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

    // Controles para Evaluación Continua y sus trabajos
    private JComboBox<String> comboContinua;
    private JLabel lblContinuaEstado;
    private JTextField txtTrabajoNombre;
    private JTextField txtTrabajoPeso;
    private JCheckBox chkTrabajoGrupal;
    private JButton btnAgregarTrabajo;
    private JComboBox<String> comboTrabajos;
    private JTextField txtTrabajoNota;
    private JButton btnGuardarNotaTrabajo;
    private JButton btnQuitarTrabajo;
    private ArrayList<Continua> continuasActuales;
    private ArrayList<TrabajoContinua> trabajosActuales;
    private boolean cargandoCombos;

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
        this.continuasActuales = new ArrayList<>();
        this.trabajosActuales = new ArrayList<>();
        this.cargandoCombos = false;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(920, 680);
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

        // CENTER: exámenes arriba, continuas al medio, sustitutorio abajo
        JPanel centro = new JPanel(new BorderLayout(8, 8));
        centro.setBackground(Estilo.COLOR_FONDO);
        centro.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        centro.add(crearPanelExamenes(), BorderLayout.NORTH);
        centro.add(crearPanelContinua(), BorderLayout.CENTER);
        centro.add(crearPanelSustitutorio(), BorderLayout.SOUTH);
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
        JPanel panel = new JPanel(new GridLayout(3, 3, 8, 6));
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

    private JPanel crearPanelContinua() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 6, 6));
        panel.setBackground(Estilo.COLOR_FONDO);
        panel.setBorder(BorderFactory.createTitledBorder("Evaluación Continua (trabajos internos suman 100%)"));

        // Fila 1: selector de Continua 1, 2 o 3 + estado actual
        JPanel fila1 = new JPanel(new GridLayout(1, 3, 8, 5));
        fila1.setBackground(Estilo.COLOR_FONDO);
        JLabel lblSel = new JLabel("Elegir continua:");
        lblSel.setFont(Estilo.FUENTE_NORMAL);

        comboContinua = new JComboBox<>();
        comboContinua.setFont(Estilo.FUENTE_NORMAL);
        comboContinua.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!cargandoCombos) {
                    actualizarDetalleContinua();
                }
            }
        });

        lblContinuaEstado = new JLabel("Nota: -- | Peso usado: 0%");
        lblContinuaEstado.setFont(Estilo.FUENTE_NORMAL);
        lblContinuaEstado.setForeground(Estilo.COLOR_TEXTO);

        fila1.add(lblSel);
        fila1.add(comboContinua);
        fila1.add(lblContinuaEstado);

        // Fila 2: agregar nuevo trabajo a la continua
        JPanel fila2 = new JPanel(new GridLayout(1, 6, 6, 5));
        fila2.setBackground(Estilo.COLOR_FONDO);
        JLabel lblNom = new JLabel("Nuevo trabajo:");
        lblNom.setFont(Estilo.FUENTE_NORMAL);
        txtTrabajoNombre = new JTextField(10);
        txtTrabajoNombre.setFont(Estilo.FUENTE_NORMAL);

        JLabel lblPeso = new JLabel("Peso interno (%):");
        lblPeso.setFont(Estilo.FUENTE_NORMAL);
        txtTrabajoPeso = new JTextField(4);
        txtTrabajoPeso.setFont(Estilo.FUENTE_NORMAL);

        chkTrabajoGrupal = new JCheckBox("Es grupal");
        chkTrabajoGrupal.setBackground(Estilo.COLOR_FONDO);
        chkTrabajoGrupal.setFont(Estilo.FUENTE_NORMAL);

        btnAgregarTrabajo = new JButton("Agregar trabajo");
        btnAgregarTrabajo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                agregarTrabajoContinua();
            }
        });

        fila2.add(lblNom);
        fila2.add(txtTrabajoNombre);
        fila2.add(lblPeso);
        fila2.add(txtTrabajoPeso);
        fila2.add(chkTrabajoGrupal);
        fila2.add(btnAgregarTrabajo);

        // Fila 3: elegir trabajo registrado para calificar o quitar
        JPanel fila3 = new JPanel(new GridLayout(1, 2, 8, 5));
        fila3.setBackground(Estilo.COLOR_FONDO);
        JLabel lblTrab = new JLabel("Trabajo registrado:");
        lblTrab.setFont(Estilo.FUENTE_NORMAL);

        comboTrabajos = new JComboBox<>();
        comboTrabajos.setFont(Estilo.FUENTE_NORMAL);
        comboTrabajos.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!cargandoCombos) {
                    actualizarCampoNotaTrabajo();
                }
            }
        });

        fila3.add(lblTrab);
        fila3.add(comboTrabajos);

        // Fila 4: nota del trabajo + botones guardar nota / quitar trabajo
        JPanel fila4 = new JPanel(new GridLayout(1, 4, 8, 5));
        fila4.setBackground(Estilo.COLOR_FONDO);
        JLabel lblNotaT = new JLabel("Nota del trabajo (0-20):");
        lblNotaT.setFont(Estilo.FUENTE_NORMAL);

        txtTrabajoNota = new JTextField(6);
        txtTrabajoNota.setFont(Estilo.FUENTE_NORMAL);

        btnGuardarNotaTrabajo = new JButton("Guardar nota trabajo");
        btnGuardarNotaTrabajo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                guardarNotaTrabajo();
            }
        });

        btnQuitarTrabajo = new JButton("Quitar trabajo");
        btnQuitarTrabajo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                quitarTrabajoContinua();
            }
        });

        fila4.add(lblNotaT);
        fila4.add(txtTrabajoNota);
        fila4.add(btnGuardarNotaTrabajo);
        fila4.add(btnQuitarTrabajo);

        panel.add(fila1);
        panel.add(fila2);
        panel.add(fila3);
        panel.add(fila4);
        return panel;
    }

    private JPanel crearPanelSustitutorio() {
        JPanel panel = new JPanel(new BorderLayout(8, 6));
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
            deshabilitarContinua();
            lblSustiInfo.setText("Sustitutorio: (sin curso)");
            txtSustiNota.setText("");
            txtSustiNota.setEnabled(false);
            btnSustiGuardar.setEnabled(false);
            lblPromedioCurso.setText("Promedio actual del curso: --");
            return;
        }

        // 1. Exámenes
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

        // 2. Continuas
        int idxPrevio = comboContinua.getSelectedIndex();
        cargandoCombos = true;
        continuasActuales.clear();
        comboContinua.removeAllItems();
        for (Evaluacion ev : curso.getEvaluaciones()) {
            if (ev instanceof Continua) {
                Continua c = (Continua) ev;
                continuasActuales.add(c);
                comboContinua.addItem(c.getNombre() + " (" + c.getPeso() + "% del curso)");
            }
        }
        if (!continuasActuales.isEmpty()) {
            int elegir = (idxPrevio >= 0 && idxPrevio < continuasActuales.size()) ? idxPrevio : 0;
            comboContinua.setSelectedIndex(elegir);
        }
        cargandoCombos = false;
        actualizarDetalleContinua();

        // 3. Sustitutorio
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

    private void deshabilitarContinua() {
        cargandoCombos = true;
        continuasActuales.clear();
        trabajosActuales.clear();
        comboContinua.removeAllItems();
        comboTrabajos.removeAllItems();
        cargandoCombos = false;

        comboContinua.setEnabled(false);
        lblContinuaEstado.setText("Nota: -- | Peso usado: 0%");
        txtTrabajoNombre.setText("");
        txtTrabajoPeso.setText("");
        txtTrabajoNombre.setEnabled(false);
        txtTrabajoPeso.setEnabled(false);
        chkTrabajoGrupal.setEnabled(false);
        btnAgregarTrabajo.setEnabled(false);
        comboTrabajos.setEnabled(false);
        txtTrabajoNota.setText("");
        txtTrabajoNota.setEnabled(false);
        btnGuardarNotaTrabajo.setEnabled(false);
        btnQuitarTrabajo.setEnabled(false);
    }

    private Continua getContinuaSeleccionada() {
        int i = comboContinua.getSelectedIndex();
        return (i >= 0 && i < continuasActuales.size()) ? continuasActuales.get(i) : null;
    }

    private TrabajoContinua getTrabajoSeleccionado() {
        int i = comboTrabajos.getSelectedIndex();
        return (i >= 0 && i < trabajosActuales.size()) ? trabajosActuales.get(i) : null;
    }

    private void actualizarDetalleContinua() {
        Continua c = getContinuaSeleccionada();
        if (c == null) {
            deshabilitarContinua();
            return;
        }
        comboContinua.setEnabled(true);
        txtTrabajoNombre.setEnabled(true);
        txtTrabajoPeso.setEnabled(true);
        chkTrabajoGrupal.setEnabled(true);
        btnAgregarTrabajo.setEnabled(true);

        String notaTxt = c.isRendida() ? String.format("%.2f", c.getNota()) : "sin notas";
        String compTxt = c.isCompleta() ? "completa" : "incompleta";
        lblContinuaEstado.setText(String.format("Nota: %s (%s) | Peso usado: %.1f%%",
                notaTxt, compTxt, c.getPesoInterno()));

        int idxTrabPrevio = comboTrabajos.getSelectedIndex();
        cargandoCombos = true;
        trabajosActuales = c.getTrabajos();
        comboTrabajos.removeAllItems();
        for (TrabajoContinua t : trabajosActuales) {
            comboTrabajos.addItem(t.toString());
        }
        if (!trabajosActuales.isEmpty()) {
            int elegir = (idxTrabPrevio >= 0 && idxTrabPrevio < trabajosActuales.size()) ? idxTrabPrevio : 0;
            comboTrabajos.setSelectedIndex(elegir);
        }
        cargandoCombos = false;
        actualizarCampoNotaTrabajo();
    }

    private void actualizarCampoNotaTrabajo() {
        TrabajoContinua t = getTrabajoSeleccionado();
        boolean hay = (t != null);
        comboTrabajos.setEnabled(hay);
        txtTrabajoNota.setEnabled(hay);
        btnGuardarNotaTrabajo.setEnabled(hay);
        btnQuitarTrabajo.setEnabled(hay);
        if (hay) {
            txtTrabajoNota.setText(t.isRendido() ? String.valueOf(t.getNota()) : "");
        } else {
            txtTrabajoNota.setText("");
        }
    }

    private void actualizarPromedio(Curso curso) {
        if (!curso.tieneNotas()) {
            lblPromedioCurso.setText("Promedio actual del curso: sin notas");
        } else {
            lblPromedioCurso.setText(String.format("Promedio actual de %s: %.2f",
                    curso.getNombre(), curso.calcularPromedio()));
        }
    }

    // ---------- Acciones de Exámenes y Sustitutorio ----------

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

    // ---------- Acciones de Trabajos de Continua ----------

    private void agregarTrabajoContinua() {
        Curso curso = selector.getCurso();
        Continua continua = getContinuaSeleccionada();
        if (curso == null || continua == null) {
            return;
        }
        String nombre = txtTrabajoNombre.getText().trim();
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Escribe el nombre del trabajo.",
                    "Dato inválido", JOptionPane.ERROR_MESSAGE);
            txtTrabajoNombre.requestFocus();
            return;
        }
        double peso;
        try {
            peso = Double.parseDouble(txtTrabajoPeso.getText().trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Escribe un porcentaje numérico válido.",
                    "Dato inválido", JOptionPane.ERROR_MESSAGE);
            txtTrabajoPeso.requestFocus();
            return;
        }
        try {
            TrabajoContinua trabajo = new TrabajoContinua(nombre, peso, chkTrabajoGrupal.isSelected());
            continua.agregarTrabajo(trabajo);
            txtTrabajoNombre.setText("");
            txtTrabajoPeso.setText("");
            chkTrabajoGrupal.setSelected(false);
            actualizarDetalleContinua();
            actualizarPromedio(curso);
            JOptionPane.showMessageDialog(this,
                    "Trabajo agregado a " + continua.getNombre() + ".",
                    "Trabajo registrado", JOptionPane.INFORMATION_MESSAGE);
        } catch (PesoInvalidoException | IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Error al agregar trabajo", JOptionPane.ERROR_MESSAGE);
            txtTrabajoPeso.requestFocus();
        }
    }

    private void guardarNotaTrabajo() {
        Curso curso = selector.getCurso();
        TrabajoContinua trabajo = getTrabajoSeleccionado();
        if (curso == null || trabajo == null) {
            return;
        }
        double nota;
        try {
            nota = Double.parseDouble(txtTrabajoNota.getText().trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Escribe una nota numérica válida (0 a 20).",
                    "Dato inválido", JOptionPane.ERROR_MESSAGE);
            txtTrabajoNota.requestFocus();
            return;
        }
        try {
            gestor.asignarNotaTrabajo(trabajo, nota);
            actualizarDetalleContinua();
            actualizarPromedio(curso);
            JOptionPane.showMessageDialog(this,
                    "Nota guardada para " + trabajo.getNombre() + ": " + nota + ".",
                    "Nota registrada", JOptionPane.INFORMATION_MESSAGE);
        } catch (NotaInvalidaException | IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Nota inválida", JOptionPane.ERROR_MESSAGE);
            txtTrabajoNota.requestFocus();
        }
    }

    private void quitarTrabajoContinua() {
        Curso curso = selector.getCurso();
        Continua continua = getContinuaSeleccionada();
        TrabajoContinua trabajo = getTrabajoSeleccionado();
        if (curso == null || continua == null || trabajo == null) {
            return;
        }
        int r = JOptionPane.showConfirmDialog(this,
                "¿Quitar el trabajo \"" + trabajo.getNombre() + "\"?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            continua.quitarTrabajo(trabajo);
            actualizarDetalleContinua();
            actualizarPromedio(curso);
        }
    }
}