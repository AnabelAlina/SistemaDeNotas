package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import app.GestorNotas;
import calculos.EstadisticasNotas;
import calculos.EstadoCurso;
import calculos.EvaluadorCurso;
import calculos.NotasEfectivas;
import evaluaciones.Continua;
import evaluaciones.Evaluacion;
import modelo.Ciclo;
import modelo.Curso;
import modelo.Estudiante;

/**
 * Ventana de análisis: promedios, estado del curso (con color) y estadísticas.
 * Reemplaza a MenuAnalisis de la consola. No tiene lógica de cálculo propia:
 * todo sale de EvaluadorCurso, EstadisticasNotas y el modelo.
 */
public class VentanaAnalisis extends JFrame {

    private GestorNotas gestor;
    private SelectorCurso selector;
    private EvaluadorCurso evaluador;

    private JLabel lblPromedioCurso;
    private JLabel lblPromedioCiclo;
    private JLabel lblPromedioAcumulado;
    private JLabel lblEstado;
    private JTextArea areaEstadisticas;

    public VentanaAnalisis(GestorNotas gestor) {
        super("Análisis de notas");
        if (gestor == null) {
            throw new IllegalArgumentException("El gestor no puede ser nulo.");
        }
        this.gestor = gestor;
        this.evaluador = new EvaluadorCurso();

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Estilo.COLOR_FONDO);

        // NORTH: selector de estudiante, ciclo y curso
        selector = new SelectorCurso(gestor);
        selector.addActionListener(new ActionListener() { // clase interior anónima
            @Override
            public void actionPerformed(ActionEvent e) {
                mostrar();
            }
        });
        add(selector, BorderLayout.NORTH);

        // CENTER: resultados a la izquierda, estadísticas a la derecha
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
        panel.setBorder(BorderFactory.createTitledBorder("Resultados"));

        lblPromedioCurso = crearEtiqueta();
        lblPromedioCiclo = crearEtiqueta();
        lblPromedioAcumulado = crearEtiqueta();

        lblEstado = crearEtiqueta();
        lblEstado.setOpaque(true);
        lblEstado.setHorizontalAlignment(SwingConstants.CENTER);
        lblEstado.setFont(Estilo.FUENTE_TITULO);

        panel.add(lblPromedioCurso);
        panel.add(lblPromedioCiclo);
        panel.add(lblPromedioAcumulado);
        panel.add(lblEstado);
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
            lblEstado.setText("Sin curso");
            lblEstado.setBackground(Estilo.COLOR_FONDO);
            areaEstadisticas.setText("Elige un curso.");
            return;
        }

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
        lblEstado.setText(mensaje);
        lblEstado.setBackground(Estilo.COLOR_FONDO);
        lblEstado.setForeground(Estilo.COLOR_TEXTO);
        areaEstadisticas.setText(mensaje);
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

    // ---------- Listener de los botones ----------

    private class Escucha implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String comando = e.getActionCommand();
            try {
                if (comando.equals("Actualizar")) {
                    selector.recargar();
                } else if (comando.equals("Exportar CSV")) {
                    // Tarea 4: aquí se conecta el Exportable.
                    JOptionPane.showMessageDialog(VentanaAnalisis.this,
                            "La exportación a CSV se conecta en la tarea 4.",
                            "Exportar CSV", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(VentanaAnalisis.this, ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}