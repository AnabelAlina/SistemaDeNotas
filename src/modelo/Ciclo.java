package modelo;
import java.util.ArrayList;
public class Ciclo implements Calculable {
    private String nombre; // ejemplo: "2026-A"
    private int anio;
    private ArrayList<Curso> cursos;
    public Ciclo(String nombre, int anio) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del ciclo no puede estar vacío.");
        }
        if (anio <= 0) {
            throw new IllegalArgumentException("El año debe ser mayor que 0.");
        }
        this.nombre = nombre.trim();
        this.anio = anio;
        this.cursos = new ArrayList<>();
    }
    public String getNombre() { return nombre; }
    public int getAnio() { return anio; }
    public ArrayList<Curso> getCursos() { return cursos; }
    // Valida que no haya dos cursos con el mismo código dentro del ciclo
    public void agregarCurso(Curso curso) {
        if (curso == null) {
            throw new IllegalArgumentException("El curso no puede ser nulo.");
        }
        for (Curso c : cursos) {
            if (c.getCodigo().equalsIgnoreCase(curso.getCodigo())) {
                throw new IllegalArgumentException(
                    "Ya existe un curso con el código " + curso.getCodigo() + " en este ciclo.");
            }
        }
        cursos.add(curso);
    }
    // Promedio ponderado por créditos
    @Override
    public double calcularPromedio() {
        double suma = 0;
        double creditos = 0;
        for (Curso curso : cursos) {
            if (curso.tieneNotas()) {
                suma += curso.calcularPromedio() * curso.getCreditos();
                creditos += curso.getCreditos();
            }
        }
        return creditos == 0 ? 0 : suma / creditos;
    }
}