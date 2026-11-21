package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import app.GestorNotas;
import calculos.EstadisticasNotas;
import calculos.EstadoCurso;
import calculos.EvaluadorCurso;
import calculos.NotasEfectivas;
import calculos.SimuladorNotas;
import evaluaciones.Continua;
import evaluaciones.Evaluacion;
import excepciones.NotaInvalidaException;
import exportacion.Exportable;
import exportacion.ExportadorCSV;
import modelo.Ciclo;
import modelo.Curso;
import modelo.Estudiante;

/**
 * Ventana de análisis: promedios, estado del curso (con color), simulador de notas,
 * estadísticas y exportación a CSV.
 * Reemplaza a MenuAnalisis de la consola. No tiene lógica de cálculo propia:
 * todo sale de EvaluadorCurso, SimuladorNotas, EstadisticasNotas y el modelo.
 */
public class VentanaAnalisis extends JFrame {

    private GestorNotas gestor;
    private SelectorCurso selector;
    private EvaluadorCurso evaluador;
    private SimuladorNotas simulador;
    private Exportable exportador;

    private JLabel lblPromedioCurso;
    private JLabel lblPromedioCiclo;
    private JLabel lblPromedioAcumulado;
    private JLabel lblNotaNecesaria;
    private JLabel lblEstado;

    // Controles del simulador
    private JComboBox<String> comboEvalSimular;
    private JTextField txtNotaSimular;
    private JLabel lblResultadoSimulacion;
    private ArrayList<Evaluacion> pendientesSimulables;

    private JTextArea areaEstadisticas;

    public VentanaAnalisis(GestorNotas gestor) {
        super("Análisis de notas");
        if (gestor == null) {
            throw new IllegalArgumentException("El gestor no puede ser nulo.");
        }
        this.gestor = gestor;
        this.evaluador = new EvaluadorCurso();
        this.simulador = new SimuladorNotas();
        this.exportador = new ExportadorCSV();
        this.pendientesSimulables = new ArrayList<>();

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(920, 620);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Estilo.COLOR_FONDO);

        // NORTH: selector de estudiante, ciclo y curso
        selector = new SelectorCurso(gestor);
        selector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mostrar();
            }
        });
        add(selector, BorderLayout.NORTH);

        // CENTER: resultados/simulador a la izquierda, estadísticas a la derecha
        JPanel centro = new JPanel(new GridLayout(1, 2, 10, 10));
        centro.setBackground(Estilo.COLOR_FONDO);
        centro.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        centro.add(crearPanelResultados());
        centro.add(crearPanelEstadisticas());
        add(centro, BorderLayout.CENTER);

        // SOUTH: botones
        add(crearPanelBotones(), BorderLayout.SOUTH);

        mostrar();
        setVisible(true);
    }

    // ---------- Construcción de paneles ----------

    private JPanel crearPanelResultados() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.setBackground(Estilo.COLOR_FONDO);
        panel.setBorder(BorderFactory.createTitledBorder("Resultados y Simulador"));

        lblPromedioCurso = crearEtiqueta();
        lblPromedioCiclo = crearEtiqueta();
        lblPromedioAcumulado = crearEtiqueta();
        lblNotaNecesaria = crearEtiqueta();

        lblEstado = crearEtiqueta();
        lblEstado.setOpaque(true);
        lblEstado.setHorizontalAlignment(SwingConstants.CENTER);
        lblEstado.setFont(Estilo.FUENTE_TITULO);

        // Fila de simulación
        JPanel panelSim = new JPanel(new GridLayout(1, 3, 5, 5));
        panelSim.setBackground(Estilo.COLOR_FONDO);

        comboEvalSimular = new JComboBox<>();
        comboEvalSimular.setFont(Estilo.FUENTE_NORMAL);

        txtNotaSimular = new JTextField(5);
        txtNotaSimular.setFont(Estilo.FUENTE_NORMAL);

        JButton btnSimular = new JButton("Simular");
        btnSimular.setActionCommand("Simular");
        btnSimular.addActionListener(new Escucha());

        panelSim.add(comboEvalSimular);
        panelSim.add(txtNotaSimular);
        panelSim.add(btnSimular);

        lblResultadoSimulacion = crearEtiqueta();

        panel.add(lblPromedioCurso);
        panel.add(lblPromedioCiclo);
        panel.add(lblPromedioAcumulado);
        panel.add(lblNotaNecesaria);
        panel.add(lblEstado);
        panel.add(panelSim);
        panel.add(lblResultadoSimulacion);
        return panel;
    }

    private JLabel crearEtiqueta() {
        JLabel etiqueta = new JLabel(" ");
        etiqueta.setFont(Estilo.FUENTE_NORMAL);
        etiqueta.setForeground(Estilo.COLOR_TEXTO);
        return etiqueta;
    }

    private JScrollPane crearPanelEstadisticas() {
        areaEstadisticas = new JTextArea();
        areaEstadisticas.setEditable(false);
        areaEstadisticas.setFont(Estilo.FUENTE_NORMAL);
        areaEstadisticas.setLineWrap(true);
        areaEstadisticas.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(areaEstadisticas);
        scroll.setBorder(BorderFactory.createTitledBorder("Estadísticas"));
        return scroll;
    }

    private JPanel crearPanelBotones() {
        JPanel panel = new JPanel();
        panel.setBackground(Estilo.COLOR_FONDO);

        Escucha escucha = new Escucha();
        JButton btnActualizar = new JButton("Actualizar");
        JButton btnExportar = new JButton("Exportar CSV");
        btnActualizar.setActionCommand("Actualizar");
        btnExportar.setActionCommand("Exportar CSV");
        btnActualizar.addActionListener(escucha);
        btnExportar.addActionListener(escucha);

        panel.add(btnActualizar);
        panel.add(btnExportar);
        return panel;
    }

    // ---------- Actualizar pantalla ----------

    private void mostrar() {
        Estudiante estudiante = selector.getEstudiante();
        Ciclo ciclo = selector.getCiclo();
        Curso curso = selector.getCurso();

        if (estudiante == null) {
            limpiar("No hay estudiantes registrados.");
            return;
        }

        // Promedios de ciclo y acumulado
        if (ciclo != null) {
            lblPromedioCiclo.setText("Promedio del ciclo " + ciclo.getNombre() + ": "
                    + formato(ciclo.calcularPromedio()));
        } else {
            lblPromedioCiclo.setText("Promedio del ciclo: (sin ciclo)");
        }
        lblPromedioAcumulado.setText("Promedio acumulado: " + formato(estudiante.calcularPromedio()));

        if (curso == null) {
            lblPromedioCurso.setText("Promedio del curso: (sin curso)");
            lblNotaNecesaria.setText(" ");
            lblEstado.setText("Sin curso");
            lblEstado.setBackground(Estilo.COLOR_FONDO);
            lblEstado.setForeground(Estilo.COLOR_TEXTO);
            limpiarSimulador();
            areaEstadisticas.setText("Elige un curso.");
            return;
        }

        // Simulador y nota necesaria (disponible incluso si aún no hay notas rendidas)
        actualizarSimulador(curso);

        // Promedio y estado del curso
        EstadoCurso estado = evaluador.estadoDe(curso);
        if (estado == null) {
            lblPromedioCurso.setText("Promedio del curso: sin notas");
            lblEstado.setText("Todavía no hay notas");
            lblEstado.setBackground(Estilo.COLOR_FONDO);
            lblEstado.setForeground(Estilo.COLOR_TEXTO);
            areaEstadisticas.setText("Todavía no hay notas.");
            return;
        }

        String parcial = esParcial(curso) ? ", parcial" : "";
        lblPromedioCurso.setText("Promedio del curso: " + formato(curso.calcularPromedio()) + parcial);
        lblEstado.setText(estado.getTexto());
        lblEstado.setBackground(colorDe(estado));
        lblEstado.setForeground(Color.WHITE);

        areaEstadisticas.setText(textoEstadisticas(curso));
        areaEstadisticas.setCaretPosition(0);
    }

    private void limpiar(String mensaje) {
        lblPromedioCurso.setText(" ");
        lblPromedioCiclo.setText(" ");
        lblPromedioAcumulado.setText(" ");
        lblNotaNecesaria.setText(" ");
        lblEstado.setText(mensaje);
        lblEstado.setBackground(Estilo.COLOR_FONDO);
        lblEstado.setForeground(Estilo.COLOR_TEXTO);
        limpiarSimulador();
        areaEstadisticas.setText(mensaje);
    }

    private void limpiarSimulador() {
        pendientesSimulables.clear();
        comboEvalSimular.removeAllItems();
        comboEvalSimular.setEnabled(false);
        txtNotaSimular.setText("");
        txtNotaSimular.setEnabled(false);
        lblResultadoSimulacion.setText(" ");
    }

    private void actualizarSimulador(Curso curso) {
        pendientesSimulables.clear();
        comboEvalSimular.removeAllItems();
        lblResultadoSimulacion.setText(" ");

        if (!simulador.hayEvaluacionesPendientes(curso)) {
            lblNotaNecesaria.setText("Meta (" + formato(curso.getMetaPromedio()) + "): curso finalizado");
        } else {
            double nec = simulador.notaNecesaria(curso, curso.getMetaPromedio());
            if (nec > 20) {
                lblNotaNecesaria.setText("Meta (" + formato(curso.getMetaPromedio()) + "): inalcanzable (> 20)");
            } else {
                lblNotaNecesaria.setText("Necesitas en pendientes para meta ("
                        + formato(curso.getMetaPromedio()) + "): " + formato(nec));
            }
            for (Evaluacion ev : curso.getEvaluaciones()) {
                if (!ev.isRendida() && !NotasEfectivas.esOpcional(ev)) {
                    pendientesSimulables.add(ev);
                    comboEvalSimular.addItem(ev.getNombre());
                }
            }
        }

        // Opción de simular sustitutorio si aún no se rindió
        if (curso.getSustitutorio() == null || !curso.getSustitutorio().isRendida()) {
            comboEvalSimular.addItem("Examen sustitutorio");
        }

        boolean hayOpciones = comboEvalSimular.getItemCount() > 0;
        comboEvalSimular.setEnabled(hayOpciones);
        txtNotaSimular.setEnabled(hayOpciones);
    }

    private Color colorDe(EstadoCurso estado) {
        if (estado == EstadoCurso.APROBADO) {
            return Estilo.COLOR_APROBADO;
        }
        if (estado == EstadoCurso.EN_RIESGO) {
            return Estilo.COLOR_EN_RIESGO;
        }
        return Estilo.COLOR_DESAPROBADO;
    }

    // El promedio es parcial si falta rendir algo (sin contar el sustitutorio)
    // o si