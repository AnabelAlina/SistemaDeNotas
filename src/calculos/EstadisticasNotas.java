package calculos;

import evaluaciones.Evaluacion;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

// Estudiante 2 - Ayelen Choquecota - Estadisticas de las notas: moda y frecuencias (HashMap), mediana y
// maxima/minima (array ordenado), promedio, desviacion y tendencia.
//
// NOTA para Estudiante 3 (solo si hace falta) - como usarla en el menu:
//   EstadisticasNotas est = new EstadisticasNotas();
//   est.calcularFrecuencias(curso.getEvaluaciones());   // SIEMPRE primero
//   if (est.hayDatos()) { est.getModa(); est.getMediana(); est.getMaxima(); est.getMinima();
//                         est.getDesviacion(); est.getTendencia(); est.getFrecuencias(); }
//   Si no hay notas rendidas, los metodos devuelven 0: avisa al usuario con hayDatos().
// Estadisticas sobre las notas de las evaluaciones rendidas.
// Primero se llama a calcularFrecuencias(...) y luego se piden los resultados.
// Si no hay notas rendidas, los metodos devuelven 0 (usa hayDatos() para saberlo).
public class EstadisticasNotas {
    // Diferencia minima para decir que la tendencia sube o baja
    private static final double UMBRAL_TENDENCIA = 0.5;

    private HashMap<Double, Integer> frecuencias = new HashMap<>();
    private double[] notasOrdenadas = new double[0];
    private ArrayList<Double> notasEnOrden = new ArrayList<>(); // en el orden en que estan en la lista

    // Llena el HashMap (nota -> cuantas veces aparece) y el array ordenado, con las notas de UN curso
    public void calcularFrecuencias(ArrayList<Evaluacion> evaluaciones) {
        // Notas que cuentan (ya con la regla del sustitutorio aplicada)
        calcularDesdeNotas(new ArrayList<>(NotasEfectivas.calcular(evaluaciones).values()));
    }

    // Igual que calcularFrecuencias, pero con una lista de notas ya calculadas.
    // Sirve para un ciclo o para todo el historial: las notas efectivas se sacan curso por curso
    // (ver EvaluadorCurso) y aqui solo se juntan los numeros.
    public void calcularDesdeNotas(ArrayList<Double> notas) {
        frecuencias = new HashMap<>();
        notasEnOrden = new ArrayList<>();
        if (notas != null) {
            for (double nota : notas) {
                notasEnOrden.add(nota);
                frecuencias.put(nota, frecuencias.getOrDefault(nota, 0) + 1);
            }
        }
        notasOrdenadas = new double[notasEnOrden.size()];
        for (int i = 0; i < notasEnOrden.size(); i++) {
            notasOrdenadas[i] = notasEnOrden.get(i);
        }
        Arrays.sort(notasOrdenadas);
    }

    public boolean hayDatos() {
        return notasOrdenadas.length > 0;
    }

    // Copia del mapa de frecuencias (nota -> veces), para mostrarlo en el menu
    public HashMap<Double, Integer> getFrecuencias() {
        return new HashMap<>(frecuencias);
    }

    // La nota que mas se repite. Si hay empate, devuelve la menor de las empatadas.
    public double getModa() {
        if (!hayDatos()) {
            return 0;
        }
        double moda = 0;
        int maxFrecuencia = 0;
        for (double nota : notasOrdenadas) { // recorre de menor a mayor, asi gana la menor en empate
            int f = frecuencias.get(nota);
            if (f > maxFrecuencia) {
                maxFrecuencia = f;
                moda = nota;
            }
        }
        return moda;
    }

    // Cuantas veces aparece la moda
    public int getFrecuenciaModa() {
        return hayDatos() ? frecuencias.get(getModa()) : 0;
    }

    public double getMediana() {
        int n = notasOrdenadas.length;
        if (n == 0) {
            return 0;
        }
        if (n % 2 == 1) {
            return notasOrdenadas[n / 2];
        }
        return (notasOrdenadas[n / 2 - 1] + notasOrdenadas[n / 2]) / 2.0;
    }

    public double getMaxima() {
        return hayDatos() ? notasOrdenadas[notasOrdenadas.length - 1] : 0;
    }

    public double getMinima() {
        return hayDatos() ? notasOrdenadas[0] : 0;
    }

    public double getPromedio() {
        if (!hayDatos()) {
            return 0;
        }
        double suma = 0;
        for (double n : notasOrdenadas) {
            suma += n;
        }
        return suma / notasOrdenadas.length;
    }

    // Desviacion estandar poblacional (con una sola nota da 0)
    public double getDesviacion() {
        if (!hayDatos()) {
            return 0;
        }
        double promedio = getPromedio();
        double suma = 0;
        for (double n : notasOrdenadas) {
            suma += (n - promedio) * (n - promedio);
        }
        return Math.sqrt(suma / notasOrdenadas.length);
    }

    // Compara la ultima nota con el promedio de las anteriores
    public String getTendencia() {
        int n = notasEnOrden.size();
        if (n < 2) {
            return "sin datos suficientes";
        }
        double suma = 0;
        for (int i = 0; i < n - 1; i++) {
            suma += notasEnOrden.get(i);
        }
        double promedioAnteriores = suma / (n - 1);
        double diferencia = notasEnOrden.get(n - 1) - promedioAnteriores;
        if (diferencia > UMBRAL_TENDENCIA) {
            return "sube";
        }
        if (diferencia < -UMBRAL_TENDENCIA) {
            return "baja";
        }
        return "se mantiene";
    }
}