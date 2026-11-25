package modelo;

import java.io.Serializable;
import java.util.ArrayList;
import excepciones.PesoInvalidoException;
import plan.PlanEstudios;

public class Ciclo implements Calculable, Serializable {

    private static final long serialVersionUID = 1L;

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

    // Carga los 7 cursos del semestre UNSA con sus créditos y evaluaciones.
    // Salta los que ya existan en el ciclo. Devuelve cuántos agregó.
    public int agregarCursosPredeterminados(double meta) throws PesoInvalidoException {
        int agregados = 0;
        for (PlanEstudios.CursoPlan plan : PlanEstudios.getCursosPrimerAnioSegundoSemestre()) {
            if (buscarCurso(plan.getCodigo()) == null) {
                agregarCurso(PlanEstudios.crearCurso(plan, meta));
                agregados++;
            }
        }
        return agregados;
    }

    // Curso manual (repitentes u otros años): se rechaza si los % no suman 100
    public Curso agregarCursoManual(String codigo, String nombre, int creditos, double meta,
                                    double[] pesosExamen, double[] pesosContinua) throws PesoInvalidoException {
        Curso curso = PlanEstudios.crearCurso(codigo, nombre, creditos, meta, pesosExamen, pesosContinua);
        agregarCurso(curso);
        return curso;
    }

    // Quita el curso con ese código. Devuelve true si lo quitó, false si no existía
    public boolean quitarCurso(String codigo) {
        Curso curso = buscarCurso(codigo);
        if (curso == null) {
            return false;
        }
        return cursos.remove(curso);
    }

    // Devuelve el curso con ese código o null si no existe
    public Curso buscarCurso(String codigo) {
        if (codigo == null) {
            return null;
        }
        for (Curso c : cursos) {
            if (c.getCodigo().equalsIgnoreCase(codigo.trim())) {
                return c;
            }
        }
        return null;
    }

    // Promedio ponderado por créditos
    @Override
    public double calcularPromedio() {
        double suma = 0;
        double creditos = 0;
        for (Curso curso : cursos) {
            if (curso.tieneNotas()) {
                creditos += curso.getCreditos();
                suma += curso.calcularPromedio() * curso.getCreditos();
            }
        }
        return creditos == 0 ? 0 : suma / creditos;
    }
}