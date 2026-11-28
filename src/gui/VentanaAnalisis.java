package gui;

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
import exportacion.ExportadorHTML;
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
    private Exportable exportador;      // CSV
    private Exportable exportadorHtml;  // HTML

    private JLabel lblPromedioCurso;
    private JLabel lblPromedioCiclo;
    private JLabel lblPromedioAcumulado;
    private JLabel lblNotaNecesaria;
    private JLabel lblEstado;
    private JLabel lblLeyenda;

    // Controles del simulador
    private JComboBox<String> comboEvalSimular;
    private JTextField txtNotaSimular;
    private JLabel lblResultadoSimulacion;
    private ArrayList<Evaluacion> pendientesSimulables;

    private JTextArea areaEstadisticas;
    private JComboBox<String> comboFormato;

    public VentanaAnalisis(GestorNotas gestor) {
        this(gestor, new ExportadorCSV());
    }

    public VentanaAnalisis(GestorNotas gestor, Exportable exportador) {
        this(gestor, exportador, new ExportadorHTML());
    }

    public VentanaAnalisis(GestorNotas gestor, Exportable exportador, Exportable exportadorHtml) {
        super("Análisis de notas");
        if (gestor == null) {
            throw new IllegalArgumentException("El gestor no puede ser nulo.");
        }
        if (exportador == null) {
            throw new IllegalArgumentException("El exportador no puede ser nulo.");
        }
        this.gestor = gestor;
        this.evaluador = new EvaluadorCurso();
        this.simulador = new SimuladorNotas();
        if (exportadorHtml == null) {
            throw new IllegalArgumentException("El exportador HTML no puede ser nulo.");
        }
        this.exportador = exportador;
        this.exportadorHtml = exportadorHtml;
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

        // Leyenda pequeña con lo que significa cada estado
        lblLeyenda = crearEtiqueta();
        lblLeyenda.setFont(Estilo.FUENTE_NORMAL.deriveFont(11f));
        lblLeyenda.setText("<html><b>Aprobado:</b> ya aseguraste 10.5 · "
                + "<b>Vas aprobando:</b> tu promedio actual es 10.5 o más, pero falta rendir · "
                + "<b>En riesgo:</b> tu promedio actual es menor que 10.5, pero aún puedes aprobar · "
                + "<b>Desaprobado:</b> ya no alcanza</html>");

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
        panel.add(lblLeyenda);
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
        JButton btnExportar = new JButton("Exportar");
        comboFormato = new JComboBox<>(new String[]{"CSV", "HTML"});
        comboFormato.setFont(Estilo.FUENTE_NORMAL);
        btnActualizar.setActionCommand("Actualizar");
        btnExportar.setActionCommand("Exportar");
        btnActualizar.addActionListener(escucha);
        btnExportar.addActionListener(escucha);

        panel.add(btnActualizar);
        panel.add(new JLabel("Formato:"));
        panel.add(comboFormato);
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
                String texto = "Necesitas en pendientes para meta ("
                        + formato(curso.getMetaPromedio()) + "): " + formato(nec);
                if (hayContinuaIncompleta(curso)) {
                    // <html> hace que el texto largo baje de línea en vez de cortarse
                    texto = "<html>" + texto + " (aprox.: faltan trabajos por calificar)</html>";
                }
                lblNotaNecesaria.setText(texto);
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
        if (estado == EstadoCurso.VAS_APROBANDO) {
            return Estilo.COLOR_VAS_APROBANDO;
        }
        if (estado == EstadoCurso.EN_RIESGO) {
            return Estilo.COLOR_EN_RIESGO;
        }
        return Estilo.COLOR_DESAPROBADO;
    }

    // True si alguna continua tiene trabajos por agregar o por calificar
    private boolean hayContinuaIncompleta(Curso curso) {
        for (Evaluacion ev : curso.getEvaluaciones()) {
            if (ev instanceof Continua && !((Continua) ev).isCompleta()) {
                return true;
            }
        }
        return false;
    }

    // El promedio es parcial si falta rendir algo (sin contar el sustitutorio)
    // o si hay una continua con trabajos por agregar o por calificar.
    private boolean esParcial(Curso curso) {
        for (Evaluacion ev : curso.getEvaluaciones()) {
            if (NotasEfectivas.esOpcional(ev)) {
                continue;
            }
            if (!ev.isRendida()) {
                return true;
            }
            if (ev instanceof Continua && !((Continua) ev).isCompleta()) {
                return true;
            }
        }
        return false;
    }

    // ---------- Texto de estadísticas ----------

    private String textoEstadisticas(Curso curso) {
        StringBuilder sb = new StringBuilder();
        EstadisticasNotas est = new EstadisticasNotas();
        est.calcularFrecuencias(curso.getEvaluaciones()); // SIEMPRE antes de pedir resultados

        if (!est.hayDatos()) {
            sb.append("Todavía no hay notas.\n");
        } else {
            sb.append("Nota máxima: ").append(formato(est.getMaxima())).append("\n");
            sb.append("Nota mínima: ").append(formato(est.getMinima())).append("\n");
            sb.append("Mediana: ").append(formato(est.getMediana())).append("\n");
            sb.append("Desviación: ").append(formato(est.getDesviacion())).append("\n");
            sb.append("Tendencia: ").append(est.getTendencia()).append("\n");
            if (est.getFrecuenciaModa() <= 1) {
                sb.append("Moda: ninguna nota se repite\n");
            } else {
                sb.append("Moda: ").append(formato(est.getModa())).append(" (aparece ")
                        .append(est.getFrecuenciaModa()).append(" veces)\n");
            }
        }

        sb.append("\nDistribución por rangos:\n");
        for (Map.Entry<String, Integer> entrada : evaluador.distribucionPorRangos(curso).entrySet()) {
            sb.append("  ").append(entrada.getKey()).append(": ").append(entrada.getValue()).append("\n");
        }

        // Avisos de continuas incompletas
        ArrayList<String> avisos = new ArrayList<>();
        for (Evaluacion ev : curso.getEvaluaciones()) {
            if (ev instanceof Continua) {
                Continua c = (Continua) ev;
                if (c.isRendida() && !c.isCompleta()) {
                    avisos.add("Continua " + c.getNumero() + " está incompleta: el promedio y la nota "
                            + "necesaria son aproximados.");
                }
            }
        }
        if (!avisos.isEmpty()) {
            sb.append("\n");
            for (String aviso : avisos) {
                sb.append(aviso).append("\n");
            }
        }
        return sb.toString();
    }

    private String formato(double valor) {
        return String.format("%.2f", valor);
    }

    // ---------- Acciones de simulación y exportación ----------

    private void ejecutarSimulacion() {
        Curso curso = selector.getCurso();
        int idx = comboEvalSimular.getSelectedIndex();
        if (curso == null || idx < 0) {
            return;
        }

        double nota;
        try {
            nota = Double.parseDouble(txtNotaSimular.getText().trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingresa una nota válida (0 a 20).",
                    "Dato inválido", JOptionPane.ERROR_MESSAGE);
            txtNotaSimular.requestFocus();
            return;
        }

        try {
            String elegido = (String) comboEvalSimular.getSelectedItem();
            double promSimulado;
            if ("Examen sustitutorio".equals(elegido)) {
                promSimulado = simulador.simularSustitutorio(curso, nota);
            } else {
                Evaluacion ev = pendientesSimulables.get(idx);
                promSimulado = simulador.simularNota(curso, ev, nota);
            }
            lblResultadoSimulacion.setText("Con " + formato(nota) + " en " + elegido
                    + " -> Promedio: " + formato(promSimulado));
        } catch (NotaInvalidaException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Error en simulación", JOptionPane.ERROR_MESSAGE);
            txtNotaSimular.requestFocus();
        }
    }

    private void exportar() {
        Estudiante est = selector.getEstudiante();
        if (est == null) {
            JOptionPane.showMessageDialog(this, "No hay estudiante seleccionado.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        boolean esHtml = "HTML".equals(comboFormato.getSelectedItem());
        String extension = esHtml ? ".html" : ".csv";
        String rutaDefecto = "datos/reporte_" + est.getCodigo() + extension;
        String ruta = JOptionPane.showInputDialog(this, "Ruta del archivo " + (esHtml ? "HTML" : "CSV") + ":", rutaDefecto);
        if (ruta == null || ruta.trim().isEmpty()) {
            return;
        }

        try {
            Exportable elegido = esHtml ? exportadorHtml : exportador;
            elegido.exportar(est, ruta.trim());
            JOptionPane.showMessageDialog(this, "Reporte exportado en:\n" + ruta.trim(),
                    "Exportación exitosa", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error al exportar: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    // ---------- Listener de los botones ----------

    private class Escucha implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String comando = e.getActionCommand();
            try {
                if (comando.equals("Actualizar")) {
                    selector.recargar();
                } else if (comando.equals("Simular")) {
                    ejecutarSimulacion();
                } else if (comando.equals("Exportar")) {
                    exportar();
                }
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(VentanaAnalisis.this, ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}