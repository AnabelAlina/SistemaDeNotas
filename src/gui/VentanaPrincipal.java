package gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import app.GestorNotas;
import exportacion.Exportable;
import modelo.Estudiante;

/**
 * Ventana principal del Sistema de Notas UNSA.
 * Reemplaza a MenuPrincipal de la consola y coordina el acceso a las ventanas
 * de Gestión Académica, Registro de Notas, Análisis, Exportación CSV y Guardado.
 */
public class VentanaPrincipal extends JFrame {

    private GestorNotas gestor;
    private Exportable exportador;
    private JLabel lblEstudianteActual;

    public VentanaPrincipal(GestorNotas gestor, Exportable exportador) {
        super("Sistema de Notas UNSA");
        if (gestor == null || exportador == null) {
            throw new IllegalArgumentException("El gestor y el exportador no pueden ser nulos.");
        }
        this.gestor = gestor;
        this.exportador = exportador;

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(560, 440);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(12, 12));
        getContentPane().setBackground(Estilo.COLOR_FONDO);

        // Encabezado
        JPanel norte = new JPanel(new GridLayout(2, 1, 4, 4));
        norte.setBackground(Estilo.COLOR_FONDO);
        norte.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));

        JLabel lblTitulo = new JLabel("SISTEMA DE NOTAS UNSA", SwingConstants.CENTER);
        lblTitulo.setFont(Estilo.FUENTE_TITULO);
        lblTitulo.setForeground(Estilo.COLOR_TEXTO);

        lblEstudianteActual = new JLabel("", SwingConstants.CENTER);
        lblEstudianteActual.setFont(Estilo.FUENTE_NORMAL);
        lblEstudianteActual.setForeground(Estilo.COLOR_TEXTO);

        norte.add(lblTitulo);
        norte.add(lblEstudianteActual);
        add(norte, BorderLayout.NORTH);

        // Botonera central
        JPanel centro = new JPanel(new GridLayout(5, 1, 10, 10));
        centro.setBackground(Estilo.COLOR_FONDO);
        centro.setBorder(BorderFactory.createEmptyBorder(10, 40, 20, 40));

        Escucha escucha = new Escucha();
        centro.add(crearBoton("Gestión académica (estudiantes, ciclos, cursos)", "ACADEMICA", escucha));
        centro.add(crearBoton("Registro de notas (exámenes, continuas, sustitutorio)", "NOTAS", escucha));
        centro.add(crearBoton("Análisis de notas (promedios, estado, estadísticas)", "ANALISIS", escucha));
        centro.add(crearBoton("Exportar reporte CSV", "EXPORTAR", escucha));
        centro.add(crearBoton("Guardar datos", "GUARDAR", escucha));
        add(centro, BorderLayout.CENTER);

        // Actualiza el estudiante mostrado cada vez que esta ventana recupera el foco
        addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                actualizarEstudianteActual();
            }
        });

        // Pregunta si desea guardar antes de cerrar la aplicación
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cerrarAplicacion();
            }
        });

        actualizarEstudianteActual();
        setVisible(true);
    }

    private JButton crearBoton(String texto, String comando, ActionListener escucha) {
        JButton boton = new JButton(texto);
        boton.setFont(Estilo.FUENTE_NORMAL);
        boton.setActionCommand(comando);
        boton.addActionListener(escucha);
        return boton;
    }

    private void actualizarEstudianteActual() {
        Estudiante actual = gestor.getActual();
        if (actual == null) {
            lblEstudianteActual.setText("Sin estudiantes registrados (abre Gestión académica)");
        } else {
            lblEstudianteActual.setText("Estudiante actual: " + actual.getNombreCompleto()
                    + " (CUI " + actual.getCodigo() + ")");
        }
    }

    private void exportarReporte() {
        ArrayList<Estudiante> lista = gestor.getEstudiantes();
        if (lista.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Primero registra un estudiante.",
                    "Sin datos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Estudiante elegido;
        if (lista.size() == 1) {
            elegido = lista.get(0);
        } else {
            String[] opciones = new String[lista.size()];
            for (int i = 0; i < lista.size(); i++) {
                Estudiante e = lista.get(i);
                opciones[i] = e.getNombreCompleto() + " (" + e.getCodigo() + ")";
            }
            Object seleccion = JOptionPane.showInputDialog(this,
                    "Elige el estudiante para exportar:", "Exportar CSV",
                    JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);
            if (seleccion == null) {
                return;
            }
            int idx = 0;
            for (int i = 0; i < opciones.length; i++) {
                if (opciones[i].equals(seleccion)) {
                    idx = i;
                    break;
                }
            }
            elegido = lista.get(idx);
        }

        String rutaDefecto = "datos/reporte_" + elegido.getCodigo() + ".csv";
        String ruta = JOptionPane.showInputDialog(this,
                "Ruta del archivo CSV:", rutaDefecto);
        if (ruta == null || ruta.trim().isEmpty()) {
            return;
        }

        try {
            exportador.exportar(elegido, ruta.trim());
            JOptionPane.showMessageDialog(this,
                    "Reporte exportado correctamente en:\n" + ruta.trim(),
                    "Exportación exitosa", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo exportar el reporte: " + e.getMessage(),
                    "Error al exportar", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void guardarDatos() {
        try {
            gestor.guardar();
            JOptionPane.showMessageDialog(this,
                    "Datos guardados correctamente.",
                    "Guardado", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo guardar: " + e.getMessage(),
                    "Error al guardar", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cerrarAplicacion() {
        int opcion = JOptionPane.showConfirmDialog(this,
                "¿Deseas guardar los cambios antes de salir?",
                "Salir del Sistema de Notas UNSA",
                JOptionPane.YES_NO_CANCEL_OPTION);
        if (opcion == JOptionPane.CANCEL_OPTION || opcion == JOptionPane.CLOSED_OPTION) {
            return;
        }
        if (opcion == JOptionPane.YES_OPTION) {
            try {
                gestor.guardar();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "No se pudo guardar: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        dispose();
        System.exit(0);
    }

    private class Escucha implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String cmd = e.getActionCommand();
            if ("ACADEMICA".equals(cmd)) {
                new VentanaAcademica(gestor);
            } else if ("NOTAS".equals(cmd)) {
                new VentanaNotas(gestor);
            } else if ("ANALISIS".equals(cmd)) {
                new VentanaAnalisis(gestor);
            } else if ("EXPORTAR".equals(cmd)) {
                exportarReporte();
            } else if ("GUARDAR".equals(cmd)) {
                guardarDatos();
            }
        }
    }
}