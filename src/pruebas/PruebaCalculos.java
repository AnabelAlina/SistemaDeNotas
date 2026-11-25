package pruebas;

import calculos.EstadisticasNotas;
import calculos.EstadoCurso;
import calculos.EvaluadorCurso;
import calculos.PromedioSimple;
import calculos.SimuladorNotas;
import evaluaciones.Continua;
import evaluaciones.Evaluacion;
import evaluaciones.Examen;
import evaluaciones.ExamenSustitutorio;
import evaluaciones.TrabajoContinua;
import excepciones.NotaInvalidaException;
import excepciones.PesoInvalidoException;
import modelo.Ciclo;
import modelo.Curso;
import modelo.Estudiante;
import plan.PlanEstudios;

/**
 * Pruebas de evaluaciones UNSA, promedios, estadísticas, estado del curso y simulador.
 */
public class PruebaCalculos {
    private static int fallos = 0;

    private static void verificar(String descripcion, double esperado, double obtenido) {
        boolean ok = Math.abs(esperado - obtenido) < 0.0001;
        if (!ok) fallos++;
        System.out.println((ok ? "OK    " : "FALLO ") + descripcion + " -> esperado " + esperado + ", obtenido " + obtenido);
    }

    private static void verificar(String descripcion, Object esperado, Object obtenido) {
        boolean ok = esperado.equals(obtenido);
        if (!ok) fallos++;
        System.out.println((ok ? "OK    " : "FALLO ") + descripcion + " -> esperado " + esperado + ", obtenido " + obtenido);
    }

    private static Curso cursoUNSA(String codigo) throws Exception {
        return PlanEstudios.crearCurso(codigo, "Curso " + codigo, 4, 14,
            new double[]{20, 20, 20}, new double[]{15, 15, 10});
    }

    private static void notaContinua(Curso curso, int numero, double nota) throws Exception {
        Continua c = (Continua) curso.getEvaluaciones().get((numero - 1) * 2);
        TrabajoContinua t = new TrabajoContinua("Trabajo unico", 100, false);
        c.agregarTrabajo(t);
        t.setNota(nota);
    }

    private static void notaExamen(Curso curso, int numero, double nota) throws Exception {
        curso.getEvaluaciones().get((numero - 1) * 2 + 1).setNota(nota);
    }

    private static void notaSustitutorio(Curso curso, double nota) throws Exception {
        curso.obtenerOCrearSustitutorio().setNota(nota);
    }
        // Curso del documento: Examen 25, 25, 20 y Continuas 10, 10, 10
    private static Curso cursoDoc(String codigo) throws Exception {
        return PlanEstudios.crearCurso(codigo, "Curso " + codigo, 4, 14,
            new double[]{25, 25, 20}, new double[]{10, 10, 10});
    }
    public static void main(String[] args) throws Exception {
        // --- Validaciones básicas de Examen ---
        Examen ex = new Examen(1, 20);
        try { ex.setNota(21); verificar("nota 21 debe lanzar excepcion", true, false); }
        catch (NotaInvalidaException e) { verificar("nota 21 lanza NotaInvalidaException", true, true); }
        try { ex.setNota(-1); verificar("nota -1 debe lanzar excepcion", true, false); }
        catch (NotaInvalidaException e) { verificar("nota -1 lanza NotaInvalidaException", true, true); }
        verificar("antes de poner nota no esta rendida", false, ex.isRendida());
        ex.setNota(14);
        verificar("con nota valida queda rendida", true, ex.isRendida());
        verificar("getNotaPonderada = 14 x 20 / 100", 2.8, ex.getNotaPonderada());

        // --- Polimorfismo: getTipo ---
        Evaluacion[] tipos = { new Examen(1, 10), new Continua(1, 10), new ExamenSustitutorio() };
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tipos.length; i++) sb.append(i > 0 ? "," : "").append(tipos[i].getTipo());
        verificar("getTipo de las 3 clases UNSA", "Examen,Continua,Sustitutorio", sb.toString());

        // --- Pesos > 100 ---
        Curso cPesos = cursoUNSA("P100");
        try {
            cPesos.agregarEvaluacion(new Examen(1, 10));
            verificar("pesos > 100 debe fallar", true, false);
        } catch (PesoInvalidoException e) {
            verificar("pesos > 100 lanza PesoInvalidoException", true, true);
        }

        // --- Promedio simple ---
        Curso cSimple = cursoUNSA("SIM1");
        notaContinua(cSimple, 1, 12);
        notaExamen(cSimple, 1, 16);
        verificar("promedio simple", 14.0, new PromedioSimple().calcular(cSimple.getEvaluaciones()));

        // --- Modelo UNSA: estado, rangos, sustitutorio, simulador, ciclo y quitarTrabajo ---
        EvaluadorCurso evaluador = new EvaluadorCurso();
        SimuladorNotas simulador = new SimuladorNotas();

        Curso sinNotas = cursoUNSA("U1");
        verificar("estado: sin notas no se calcula (null)", true, evaluador.estadoDe(sinNotas) == null);

        Curso aprobado = cursoUNSA("U2");
        notaContinua(aprobado, 1, 16);
        notaExamen(aprobado, 1, 14);
        verificar("estado: promedio 15 con pendientes -> VAS_APROBANDO", EstadoCurso.VAS_APROBANDO, evaluador.estadoDe(aprobado));

        Curso enRiesgo = cursoUNSA("U3");
        notaContinua(enRiesgo, 1, 8);
        notaExamen(enRiesgo, 1, 8);
        verificar("estado: promedio 8 pero aun posible -> EN_RIESGO", EstadoCurso.EN_RIESGO, evaluador.estadoDe(enRiesgo));

        Curso imposible = cursoUNSA("U4");
        notaContinua(imposible, 1, 0);
        notaExamen(imposible, 1, 0);
        notaContinua(imposible, 2, 0);
        notaExamen(imposible, 2, 0);
        verificar("estado: ya imposible llegar a 10.5 -> DESAPROBADO", EstadoCurso.DESAPROBADO, evaluador.estadoDe(imposible));

        Curso terminadoBajo = cursoUNSA("U5");
        for (int i = 1; i <= 3; i++) {
            notaContinua(terminadoBajo, i, 5);
            notaExamen(terminadoBajo, i, 5);
        }
        verificar("estado: curso terminado con 5 -> DESAPROBADO", EstadoCurso.DESAPROBADO, evaluador.estadoDe(terminadoBajo));

        Curso terminadoBien = cursoUNSA("U6");
        for (int i = 1; i <= 3; i++) {
            notaContinua(terminadoBien, i, 12);
            notaExamen(terminadoBien, i, 12);
        }
        verificar("estado: curso terminado con 12 -> APROBADO", EstadoCurso.APROBADO, evaluador.estadoDe(terminadoBien));
        verificar("estado: texto para mostrar", "En riesgo", EstadoCurso.EN_RIESGO.getTexto());

        // --- Distribucion por rangos ---
        Curso rangos = cursoUNSA("R1");
        notaContinua(rangos, 1, 18);
        notaExamen(rangos, 1, 8);
        notaContinua(rangos, 2, 12);
        notaExamen(rangos, 2, 15);
        notaContinua(rangos, 3, 10);
        notaExamen(rangos, 3, 17);
        java.util.LinkedHashMap<String, Integer> dist = evaluador.distribucionPorRangos(rangos);
        verificar("rangos: 0 a 10.49 tiene 2 (8 y 10)", 2.0, dist.get("0 a 10.49").intValue());
        verificar("rangos: 10.5 a 13.99 tiene 1 (12)", 1.0, dist.get("10.5 a 13.99").intValue());
        verificar("rangos: 14 a 16.99 tiene 1 (15)", 1.0, dist.get("14 a 16.99").intValue());
        verificar("rangos: 17 a 20 tiene 2 (18 y 17)", 2.0, dist.get("17 a 20").intValue());

        // --- Sustitutorio y simulador ---
        Curso sust = cursoUNSA("S1");
        notaContinua(sust, 1, 14);
        notaExamen(sust, 1, 6);
        verificar("sustitutorio: para meta 12 se necesita 10.5", 10.5, simulador.notaNecesariaSustitutorio(sust, 12));
        verificar("sustitutorio: coincide con simularSustitutorio", 12.0, simulador.simularSustitutorio(sust, 10.5));
        notaSustitutorio(sust, 10);
        verificar("sustitutorio: si ya se rindio no aplica (NaN)", true,
            Double.isNaN(simulador.notaNecesariaSustitutorio(sust, 12)));

        // --- Estadisticas de ciclo ---
        Ciclo ciclo = new Ciclo("2026-B", 2026);
        Curso a = cursoUNSA("C1");
        notaExamen(a, 1, 6);
        notaExamen(a, 2, 14);
        notaSustitutorio(a, 10);
        Curso b = cursoUNSA("C2");
        notaExamen(b, 1, 12);
        notaExamen(b, 2, 8);
        notaSustitutorio(b, 15);
        ciclo.agregarCurso(a);
        ciclo.agregarCurso(b);
        EstadisticasNotas estCiclo = evaluador.estadisticasDeCiclo(ciclo);
        verificar("ciclo: hay datos", true, estCiclo.hayDatos());
        verificar("ciclo: maxima 15", 15.0, estCiclo.getMaxima());
        verificar("ciclo: minima 10", 10.0, estCiclo.getMinima());
        verificar("ciclo: mediana 13", 13.0, estCiclo.getMediana());
        verificar("ciclo: promedio 12.75", 12.75, estCiclo.getPromedio());

        Estudiante estudiante = new Estudiante("Ana", "Perez", "71234567", "ana@unsa.edu.pe", "20261111", "Sistemas");
        estudiante.agregarCiclo(ciclo);
        verificar("acumuladas: maxima 15", 15.0, evaluador.estadisticasAcumuladas(estudiante).getMaxima());

        // --- Continua.quitarTrabajo ---
        Continua continua = new Continua(1, 15);
        TrabajoContinua t1 = new TrabajoContinua("Tarea 1", 40, false);
        TrabajoContinua t2 = new TrabajoContinua("Tarea 2", 60, true);
        continua.agregarTrabajo(t1);
        continua.agregarTrabajo(t2);
        verificar("quitarTrabajo: quita uno que esta (true)", true, continua.quitarTrabajo(t1));
        verificar("quitarTrabajo: el peso interno baja a 60", 60.0, continua.getPesoInterno());
                // --- Peso pendiente real y nota necesaria (continua incompleta) ---
        Curso incompleta = cursoDoc("D1");
        Continua contInc = (Continua) incompleta.getEvaluaciones().get(0);
        TrabajoContinua trabajo60 = new TrabajoContinua("Trabajo 1", 60, false);
        contInc.agregarTrabajo(trabajo60);
        trabajo60.setNota(18);
        verificar("peso pendiente: 100 - 6 ganado = 94", 94.0, simulador.pesoPendiente(incompleta));
        verificar("puntos ganados: 18 x 6 / 100 = 1.08", 1.08, simulador.puntosGanados(incompleta));
        verificar("nota necesaria: continua incompleta, meta 14 -> 13.7447", 13.7447, simulador.notaNecesaria(incompleta, 14));
        verificar("continua incompleta cuenta como pendiente", true, simulador.hayEvaluacionesPendientes(incompleta));
        verificar("nota necesaria: curso todo rendido -> 0", 0.0, simulador.notaNecesaria(terminadoBien, 14));
        verificar("peso pendiente: curso todo rendido -> 0", 0.0, simulador.pesoPendiente(terminadoBien));
        verificar("simularNota no cambia la nota guardada", 8.0, enRiesgo.calcularPromedio());
        double antesSim = enRiesgo.calcularPromedio();
        simulador.simularNota(enRiesgo, 15);
        verificar("simularNota deja el promedio igual", antesSim, enRiesgo.calcularPromedio());
        verificar("simularNota deja la evaluacion pendiente", false, enRiesgo.getEvaluaciones().get(2).isRendida());

        // --- Estado con cuatro etiquetas (Ex 25, 25, 20 y Continuas 10, 10, 10) ---
        Curso soloEx1 = cursoDoc("D2");
        notaExamen(soloEx1, 1, 15);
        verificar("estado: solo Examen 1 = 15 -> VAS_APROBANDO", EstadoCurso.VAS_APROBANDO, evaluador.estadoDe(soloEx1));

        Curso asegurado = cursoDoc("D3");
        notaExamen(asegurado, 1, 20);
        notaExamen(asegurado, 2, 20);
        notaContinua(asegurado, 1, 20);
        verificar("estado: 60% con 20 = 12 puntos -> APROBADO", EstadoCurso.APROBADO, evaluador.estadoDe(asegurado));

        Curso soloEx1Bajo = cursoDoc("D4");
        notaExamen(soloEx1Bajo, 1, 3);
        verificar("estado: solo Examen 1 = 3 -> EN_RIESGO", EstadoCurso.EN_RIESGO, evaluador.estadoDe(soloEx1Bajo));

        Curso dosCeros = cursoDoc("D5");
        notaExamen(dosCeros, 1, 0);
        notaExamen(dosCeros, 2, 0);
        verificar("estado: Examen 1 = 0 y Examen 2 = 0 -> DESAPROBADO", EstadoCurso.DESAPROBADO, evaluador.estadoDe(dosCeros));

        verificar("estado: curso sin notas -> null", true, evaluador.estadoDe(cursoDoc("D6")) == null);
        verificar("estado: texto de VAS_APROBANDO", "Vas aprobando", EstadoCurso.VAS_APROBANDO.getTexto());
        System.out.println();
        System.out.println(fallos == 0 ? "TODO OK" : "HAY " + fallos + " FALLO(S)");
    }
}