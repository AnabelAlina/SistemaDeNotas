package app;

import exportacion.Exportable;
import exportacion.ExportadorCSV;
import persistencia.Repositorio;
import persistencia.RepositorioArchivo;

/**
 * Punto de entrada alternativo en modo consola.
 * Conserva el flujo original por terminal mientras Main abre la interfaz gráfica.
 */
public class MainConsola {
    public static void main(String[] args) {
        Repositorio repositorio = new RepositorioArchivo("datos/notas.txt");
        Exportable exportador = new ExportadorCSV();

        GestorNotas gestor = new GestorNotas(repositorio);
        gestor.cargarTodo();

        new MenuPrincipal(gestor, exportador).iniciar();
    }
}