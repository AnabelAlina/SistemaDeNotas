package app;

/**
 * Submenú de análisis: promedios, estadísticas, simulador y nota necesaria.
 * Responsable: Estudiante 2 (Ayelen Choquecota).
 */
public class MenuAnalisis {

    private GestorNotas gestor;
    private Entrada entrada;

    public MenuAnalisis(GestorNotas gestor, Entrada entrada) {
        this.gestor = gestor;
        this.entrada = entrada;
    }

    public void mostrar() {
        System.out.println("\n--- ANÁLISIS Y SIMULADOR ---");
        System.out.println("(Módulo en construcción por Estudiante 2)");
    }
}