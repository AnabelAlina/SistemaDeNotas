package app;

import java.io.IOException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import exportacion.Exportable;
import exportacion.ExportadorCSV;
import gui.VentanaPrincipal;
import persistencia.Repositorio;
import persistencia.RepositorioArchivo;

/**
 * Punto de entrada de la aplicación gráfica (Swing).
 * Carga los datos persistidos y abre VentanaPrincipal.
 */
public class Main {
    public static void main(String[] args) {
        Repositorio repositorio = new RepositorioArchivo("datos/notas.txt");
        Exportable exportador = new ExportadorCSV();

        GestorNotas gestor = new GestorNotas(repositorio);
        try {
            gestor.cargar();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "No se pudieron cargar los datos previos: " + e.getMessage(),
                    "Aviso", JOptionPane.WARNING_MESSAGE);
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new VentanaPrincipal(gestor, exportador);
            }
        });
    }
}