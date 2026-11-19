package app;

import java.io.IOException;
import exportacion.Exportable;
import modelo.Estudiante;

/**
 * Menú principal de consola. Coordina el acceso a los tres submenús
 * (Académico, Notas y Análisis) y las opciones de exportación y guardado.
 */
public class MenuPrincipal {

    private GestorNotas gestor;
    private Exportable exportador;
    private Entrada entrada;
    private MenuAcademico menuAcademico;
    private MenuNotas menuNotas;
    private MenuAnalisis menuAnalisis;

    public MenuPrincipal(GestorNotas gestor, Exportable exportador) {
        this.gestor = gestor;
        this.exportador = exportador;
        this.entrada = new Entrada();
        this.menuAcademico = new MenuAcademico(gestor, entrada);
        this.menuNotas = new MenuNotas(gestor, entrada);
        this.menuAnalisis = new MenuAnalisis(gestor, entrada);
    }

    public void iniciar() {
        boolean salir = false;
        while (!salir) {
            mostrarMenu();
            int opcion = entrada.leerEntero("Elige una opción: ", 1, 5);
            try {
                salir = ejecutar(opcion);
            } catch (IllegalArgumentException e) {
                System.out.println("Dato inválido: " + e.getMessage());
            }
        }
        System.out.println("¡Hasta pronto!");
    }

    public void mostrarMenu() {
        System.out.println("\n===== SISTEMA DE NOTAS UNSA =====");
        Estudiante actual = gestor.getActual();
        if (actual != null) {
            System.out.println("Estudiante actual: " + actual.getNombreCompleto()
                    + " (" + actual.getCodigo() + ")");
        }
        System.out.println(" 1. Gestión académica (estudiantes, ciclos, cursos)");
        System.out.println(" 2. Registro de notas (exámenes, continuas, sustitutorio)");
        System.out.println(" 3. Análisis y simulador (promedios, estadísticas, metas)");
        System.out.println(" 4. Exportar reporte CSV");
        System.out.println(" 5. Guardar y salir");
    }

    private boolean ejecutar(int opcion) {
        switch (opcion) {
            case 1:
                menuAcademico.mostrar();
                break;
            case 2:
                menuNotas.mostrar();
                break;
            case 3:
                menuAnalisis.mostrar();
                break;
            case 4:
                exportarReporte();
                break;
            case 5:
                if (gestor.guardarTodo()) {
                    System.out.println("Datos guardados.");
                }
                return true;
        }
        return false;
    }

    private void exportarReporte() {
        Estudiante est = entrada.seleccionarEstudiante(gestor);
        if (est == null) return;
        String porDefecto = "datos/reporte_" + est.getCodigo() + ".csv";
        String ruta = entrada.leerLinea("Ruta del archivo (Enter para " + porDefecto + "): ");
        if (ruta.isEmpty()) {
            ruta = porDefecto;
        }
        try {
            exportador.exportar(est, ruta);
            System.out.println("Reporte exportado en: " + ruta);
        } catch (IOException e) {
            System.out.println("No se pudo exportar: " + e.getMessage());
        }
    }
}