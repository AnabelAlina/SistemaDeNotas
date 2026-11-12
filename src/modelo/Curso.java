package modelo;

import java.util.ArrayList;
import calculos.EstrategiaPromedio;
import calculos.PromedioPonderado;
import calculos.SimuladorNotas;
import evaluaciones.Evaluacion;
import excepciones.PesoInvalidoException;

public class Curso implements Calculable {
    // Constante acordada por el grupo (cámbiala si deciden otra)
    public static final double NOTA_MINIMA_APROBATORIA = 10.5;
    private static final double PESO_TOTAL = 100.0;
    private static final double TOLERANCIA = 0.0001; // por errores de decimales

    private String codigo;
    private String nombre;
    private int creditos;
    private double metaPromedio;
    private ArrayList<Evaluacion> evaluaciones; // composición
    private EstrategiaPromedio estrategia;      // patrón Strategy

    public Curso(String codigo, String nombre, int creditos, double metaPromedio) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("El código del curso no puede estar vacío.");
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del curso no puede estar vacío.");
        }
        if (creditos <= 0) {
            throw new IllegalArgumentException("Los créditos deben ser mayores a 0.");
        }
        if (metaPromedio < 0 || metaPromedio > 20) {
            throw new IllegalArgumentException("La meta debe estar entre 0 y 20.");
        }
        this.codigo = codigo.trim();
        this.nombre = nombre.trim();
        this.creditos = creditos;
        this.metaPromedio = metaPromedio;
        this.evaluaciones = new ArrayList<>();
        this.estrategia = new PromedioPonderado(); // por defecto ponderado
    }
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public int getCreditos() { return creditos; }
    public double getMetaPromedio() { return metaPromedio; }
    public ArrayList<Evaluacion> getEvaluaciones() { return evaluaciones; }
    // Permite cambiar entre promedio simple y ponderado
    public void setEstrategia(EstrategiaPromedio estrategia) {
        if (estrategia == null) {
            throw new IllegalArgumentException("La estrategia no puede ser nula.");
        }
        this.estrategia = estrategia;
    }
    // Rechaza la evaluación si el peso es inválido o si el total pasaría de 100%
    public void agregarEvaluacion(Evaluacion evaluacion) throws PesoInvalidoException {
        if (evaluacion == null) {
            throw new IllegalArgumentException("La evaluación no puede ser nula.");
        }
        if (evaluacion.getPeso() <= 0) {
            throw new PesoInvalidoException("El peso debe ser mayor que 0.");
        }
        if (getPesoTotal() + evaluacion.getPeso() > PESO_TOTAL + TOLERANCIA) {
            throw new PesoInvalidoException("Los pesos superan el 100%. Peso actual: "
                + getPesoTotal() + "%, intentas agregar: " + evaluacion.getPeso() + "%.");
        }
        evaluaciones.add(evaluacion);
    }
    // Suma de los pesos de todas las evaluaciones registradas
    public double getPesoTotal() {
        double total = 0;
        for (Evaluacion e : evaluaciones) {
            total += e.getPeso();
        }
        return total;
    }
    // Úsalo cuando el curso ya esté completo (ej. antes de guardar)
    public void verificarPesos() throws PesoInvalidoException {
        if (Math.abs(getPesoTotal() - PESO_TOTAL) > TOLERANCIA) {
            throw new PesoInvalidoException(
                "Los pesos del curso " + codigo + " suman " + getPesoTotal() + "% y deben sumar 100%.");
        }
    }
    // True si al menos una evaluación ya fue rendida
    public boolean tieneNotas() {
        for (Evaluacion e : evaluaciones) {
            if (e.isRendida()) {
                return true;
            }
        }
        return false;
    }
    // No calcula nada: delega en la estrategia
    @Override
    public double calcularPromedio() {
        return estrategia.calcular(evaluaciones);
    }
    public boolean estaAprobado() {
        return calcularPromedio() >= NOTA_MINIMA_APROBATORIA;
    }
    // Delega el cálculo en el simulador (Estudiante 2)
    public double notaNecesaria() {
        return new SimuladorNotas().notaNecesaria(this, metaPromedio);
    }
}