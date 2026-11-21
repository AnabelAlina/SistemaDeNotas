package pruebas;

import calculos.EstadisticasNotas;
import calculos.EstadoCurso;
import calculos.EvaluadorCurso;
import calculos.PromedioSimple;
import calculos.SimuladorNotas;
import evaluaciones.*;
import excepciones.NotaInvalidaException;
import excepciones.PesoInvalidoException;
import java.time.LocalDate;
import modelo.Ciclo;
import modelo.Curso;
import modelo.Estudiante;
import plan.PlanEstudios;

// Estudiante 2 - Ayelen Choquecota - Pruebas manuales de evaluaciones, promedios, estadisticas y simulador.
// Se puede borrar o dejar al final del proyecto.
// Pruebas manuales del Estudiante 2. Ejecutar y revisar que todo diga OK.
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

    public static void main(String[] args) throws Exception {
        LocalDate hoy = LocalDate.of(2026, 11, 12);

        // --- Evaluacion: validacion de notas ---
        Practica p1 = new Practica("Practica 1", 20, hoy, 1);
        try { p1.setNota(21); verificar("nota 21 debe lanzar excepcion", true, false); }
        catch (NotaInvalidaException e) { verificar("nota 21 lanza NotaInvalidaException", true, true); }
        try { p1.setNota(-1); verificar("nota -1 debe lanzar excepcion", true, false); }
        catch (NotaInvalidaException e) { verificar("nota -1 lanza NotaInvalidaException", true, true); }
        verificar("antes de poner nota no esta rendida", false, p1.isRendida());
        p1.setNota(14);
        verificar("con nota valida queda rendida", true, p1.isRendida());
        verificar("getNotaPonderada = 14 x 20 / 100", 2.8, p1.getNotaPonderada());

        // --- Polimorfismo: getTipo ---
        Evaluacion[] tipos = {
            new Practica("P", 10, hoy, 1), new Parcial("Par", 10, hoy, true),
            new ExamenFinal("Fin", 10, hoy, false), new Trabajo("Tra", 10, hoy, true) };
        String esperados = "Practica,Parcial,Examen Final,Trabajo";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tipos.length; i++) sb.append(i > 0 ? "," : "").append(tipos[i].getTipo());
        verificar("getTipo de las 4 clases", esperados, sb.toString());

        // --- Curso: practicas 30%, parcial 30%, final 40% ---
        Curso curso = new Curso("CS101", "Programacion", 4, 15);
        Practica pr = new Practica("Practica", 30, hoy, 1);
        Parcial pa = new Parcial("Parcial", 30, hoy, false);
        ExamenFinal fi = new ExamenFinal("Final", 40, hoy, false);
        curso.agregarEvaluacion(pr);
        curso.agregarEvaluacion(pa);
        curso.agregarEvaluacion(fi);

        verificar("sin notas: promedio 0", 0.0, curso.calcularPromedio());
        pr.setNota(12);
        pa.setNota(16);
        // ponderado sobre lo rendido: (12x30 + 16x30) / 60 = 14
        verificar("promedio ponderado de lo rendido", 14.0, curso.calcularPromedio());
        verificar("promedio simple", 14.0, new PromedioSimple().calcular(curso.getEvaluaciones()));
        pr.setNota(10);
        // (10x30 + 16x30) / 60 = 13 ; simple = 13 tambien, probamos pesos distintos abajo
        verificar("promedio tras cambiar nota", 13.0, curso.calcularPromedio());

        // --- Nota necesaria (meta 15): (15x100 - (10x30+16x30)) / 40 = (1500-780)/40 = 18 ---
        SimuladorNotas sim = new SimuladorNotas();
        verificar("nota necesaria para meta 15", 18.0, sim.notaNecesaria(curso, 15));
        verificar("curso.notaNecesaria() usa metaPromedio", 18.0, curso.notaNecesaria());
        // meta 10.5: (1050-780)/40 = 6.75
        verificar("nota necesaria para aprobar (10.5)", 6.75, sim.notaNecesaria(curso, 10.5));
        verificar("es posible aprobar", true, sim.esPosibleAprobar(curso));
        // meta 19: (1900-780)/40 = 28 -> imposible
        verificar("meta 19 pide mas de 20", 28.0, sim.notaNecesaria(curso, 19));
        // meta baja: ya la tienes -> 0
        verificar("meta ya alcanzada devuelve 0", 0.0, sim.notaNecesaria(curso, 5));

        // --- Simulador ---
        // Si el final sale 20: (10x30+16x30+20x40)/100 = (300+480+800)/100 = 15.8
        verificar("simular final = 20", 15.8, sim.simularNota(curso, 20));
        verificar("la simulacion no modifica el curso", false, fi.isRendida());
        try { sim.simularNota(curso, 25); verificar("simular 25 debe fallar", true, false); }
        catch (NotaInvalidaException e) { verificar("simular 25 lanza excepcion", true, true); }

        // --- Todo rendido: no queda peso restante (evitar dividir entre 0) ---
        fi.setNota(8);
        verificar("sin pendientes: nota necesaria 0", 0.0, sim.notaNecesaria(curso, 15));
        verificar("sin pendientes: hayEvaluacionesPendientes", false, sim.hayEvaluacionesPendientes(curso));
        // (10x30 + 16x30 + 8x40)/100 = (300+480+320)/100 = 11 -> aprobado
        verificar("promedio final", 11.0, curso.calcularPromedio());
        verificar("sin pendientes: esPosibleAprobar = estaAprobado", true, sim.esPosibleAprobar(curso));

        // --- Imposible aprobar ---
        Curso malo = new Curso("CS102", "Calculo", 3, 11);
        Parcial m1 = new Parcial("P1", 50, hoy, false);
        Parcial m2 = new Parcial("P2", 50, hoy, false);
        malo.agregarEvaluacion(m1);
        malo.agregarEvaluacion(m2);
        m1.setNota(0);
        // (10.5x100 - 0) / 50 = 21 -> imposible
        verificar("necesita 21 en lo que falta", 21.0, sim.notaNecesaria(malo, 10.5));
        verificar("no es posible aprobar", false, sim.esPosibleAprobar(malo));

        // --- Estadisticas ---
        {
            java.util.ArrayList<Evaluacion> lista = new java.util.ArrayList<>();
            double[] notas = {12, 14, 14, 16, 10};
            for (int i = 0; i < notas.length; i++) {
                Evaluacion e = new Practica("P" + (i + 1), 10, hoy, i + 1);
                e.setNota(notas[i]);
                lista.add(e);
            }
            lista.add(new Trabajo("Pendiente", 10, hoy, false)); // no rendida: no cuenta
            EstadisticasNotas est = new EstadisticasNotas();
            est.calcularFrecuencias(lista);
            verificar("moda", 14.0, est.getModa());
            verificar("frecuencia de la moda", 2.0, est.getFrecuenciaModa());
            verificar("mediana (impar)", 14.0, est.getMediana());
            verificar("maxima", 16.0, est.getMaxima());
            verificar("minima", 10.0, est.getMinima());
            verificar("promedio", 13.2, est.getPromedio());
            // varianza = ((1.2^2)+(0.8^2)+(0.8^2)+(2.8^2)+(3.2^2))/5 = (1.44+0.64+0.64+7.84+10.24)/5 = 4.16
            verificar("desviacion poblacional", Math.sqrt(4.16), est.getDesviacion());
            // ultima = 10, anteriores = (12+14+14+16)/4 = 14 -> baja
            verificar("tendencia", "baja", est.getTendencia());

            // Mediana par
            lista.remove(lista.size() - 1);
            lista.remove(0); // quedan 14,14,16,10
            est.calcularFrecuencias(lista);
            verificar("mediana (par): ordenadas 10,14,14,16", 14.0, est.getMediana());

            // Empate de moda: gana la menor
            java.util.ArrayList<Evaluacion> empate = new java.util.ArrayList<>();
            double[] ne = {18, 11, 18, 11};
            for (int i = 0; i < ne.length; i++) {
                Evaluacion e = new Practica("E" + i, 10, hoy, i + 1);
                e.setNota(ne[i]);
                empate.add(e);
            }
            est.calcularFrecuencias(empate);
            verificar("moda con empate devuelve la menor", 11.0, est.getModa());

            // Sin datos y una sola nota
            est.calcularFrecuencias(new java.util.ArrayList<>());
            verificar("sin datos: hayDatos", false, est.hayDatos());
            verificar("sin datos: moda 0", 0.0, est.getModa());
            verificar("sin datos: tendencia", "sin datos suficientes", est.getTendencia());
            java.util.ArrayList<Evaluacion> una = new java.util.ArrayList<>();
            Evaluacion u = new Practica("U", 10, hoy, 1);
            u.setNota(15);
            una.add(u);
            est.calcularFrecuencias(una);
            verificar("una nota: desviacion 0", 0.0, est.getDesviacion());
            verificar("una nota: tendencia", "sin datos suficientes", est.getTendencia());
        }

        // --- Pesos: lo valida Curso (Estudiante 1) ---
        try {
            curso.agregarEvaluacion(new Trabajo("Extra", 10, hoy, false));
            verificar("pesos > 100 debe fallar", true, false);
        } catch (PesoInvalidoException e) {
            verificar("pesos > 100 lanza PesoInvalidoException", true, true);
        }

        // --- Modelo UNSA: estado, rangos, sustitutorio, simulador, ciclo y quitarTrabajo (Ani) ---
        pruebasModeloUNSA();

        System.out.println();
        System.out.println(fallos == 0 ? "TODO OK" : "HAY " + fallos + " FALLO(S)");
    }

    // ===================== Pruebas del modelo UNSA (Ani, Estudiante 2) =====================

    // Curso con el orden Continua 1, Examen 1, Continua 2, Examen 2, Continua 3, Examen 3.
    // Continuas 15 / 15 / 10 y examenes 20 / 20 / 20 (suman 100)
    private static Curso cursoUNSA(String codigo) throws Exception {
        return PlanEstudios.crearCurso(codigo, "Curso " + codigo, 4, 14,
            new double[]{20, 20, 20}, new double[]{15, 15, 10});
    }

    // Pone nota a una continua con un solo trabajo de 100% (queda completa)
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

    private static void pruebasModeloUNSA() throws Exception {
        EvaluadorCurso evaluador = new EvaluadorCurso();
        SimuladorNotas simulador = new SimuladorNotas();

        // --- Estado del curso ---
        Curso sinNotas = cursoUNSA("U1");
        verificar("estado: sin notas no se calcula (null)", true, evaluador.estadoDe(sinNotas) == null);

        Curso aprobado = cursoUNSA("U2");
        notaContinua(aprobado, 1, 16);
        notaExamen(aprobado, 1, 14);
        verificar("estado: promedio 15 con pendientes -> APROBADO", EstadoCurso.APROBADO, evaluador.estadoDe(aprobado));

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
        verificar("rangos: el orden es de menor a mayor", "0 a 10.49", dist.keySet().iterator().next());

        Curso bordes = cursoUNSA("R2");
        notaContinua(bordes, 1, 10.5);
        notaExamen(bordes, 1, 14);
        notaContinua(bordes, 2, 17);
        java.util.LinkedHashMap<String, Integer> distBordes = evaluador.distribucionPorRangos(bordes);
        verificar("rangos: 10.5 cae en el segundo", 1.0, distBordes.get("10.5 a 13.99").intValue());
        verificar("rangos: 14 cae en el tercero", 1.0, distBordes.get("14 a 16.99").intValue());
        verificar("rangos: 17 cae en el cuarto", 1.0, distBordes.get("17 a 20").intValue());
        verificar("rangos: sin notas todo en 0", 0.0, evaluador.distribucionPorRangos(sinNotas).get("17 a 20").intValue());

        // --- Nota necesaria en el sustitutorio ---
        // Continua 1 = 14 (15%) y Examen 1 = 6 (20%): promedio actual 9.4286, el mas bajo es 6 con peso 20
        Curso sust = cursoUNSA("S1");
        notaContinua(sust, 1, 14);
        notaExamen(sust, 1, 6);
        verificar("sustitutorio: para meta 12 se necesita 10.5", 10.5, simulador.notaNecesariaSustitutorio(sust, 12));
        verificar("sustitutorio: coincide con simularSustitutorio (da la meta)", 12.0, simulador.simularSustitutorio(sust, 10.5));
        verificar("sustitutorio: meta 20 es imposible (mayor que 20)", true, simulador.notaNecesariaSustitutorio(sust, 20) > 20);
        verificar("sustitutorio: meta baja, no hace falta (0)", 0.0, simulador.notaNecesariaSustitutorio(sust, 5));
        verificar("sustitutorio: simular no crea el sustitutorio", true, sust.getSustitutorio() == null);
        Curso soloContinua = cursoUNSA("S2");
        notaContinua(soloContinua, 1, 14);
        verificar("sustitutorio: sin Examen 1 ni 2 rendido no aplica (NaN)", true,
            Double.isNaN(simulador.notaNecesariaSustitutorio(soloContinua, 12)));
        notaSustitutorio(sust, 10);
        verificar("sustitutorio: si ya se rindio no aplica (NaN)", true,
            Double.isNaN(simulador.notaNecesariaSustitutorio(sust, 12)));

        // --- simularNota para una evaluacion concreta ---
        Curso sim = cursoUNSA("M1");
        notaContinua(sim, 1, 14);
        notaExamen(sim, 1, 6);
        Evaluacion examen3 = sim.getEvaluaciones().get(5);
        double antes = sim.calcularPromedio();
        verificar("simular Examen 3 = 20: (14x15 + 6x20 + 20x20) / 55", 730.0 / 55, simulador.simularNota(sim, examen3, 20));
        verificar("simular no cambia las notas guardadas", true, !examen3.isRendida());
        verificar("simular no cambia el promedio actual", antes, sim.calcularPromedio());
        verificar("simularNota(curso, nota) sigue simulando la proxima pendiente (Continua 2)", true,
            Math.abs(simulador.simularNota(sim, 20) - (14 * 15 + 6 * 20 + 20 * 15) / 50.0) < 0.0001);
        try {
            simulador.simularNota(sim, sim.getEvaluaciones().get(1), 10);
            verificar("simular una evaluacion ya rendida debe fallar", true, false);
        } catch (IllegalArgumentException e) {
            verificar("simular una evaluacion ya rendida lanza IllegalArgumentException", true, true);
        }
        try {
            simulador.simularNota(sim, examen3, 25);
            verificar("simular con nota 25 debe fallar", true, false);
        } catch (NotaInvalidaException e) {
            verificar("simular con nota 25 lanza NotaInvalidaException", true, true);
        }

        // --- Estadisticas de un ciclo con dos cursos (cada sustitutorio solo afecta a su curso) ---
        Ciclo ciclo = new Ciclo("2026-B", 2026);
        Curso a = cursoUNSA("C1");
        notaExamen(a, 1, 6);
        notaExamen(a, 2, 14);
        notaSustitutorio(a, 10);   // reemplaza el 6 por 10
        Curso b = cursoUNSA("C2");
        notaExamen(b, 1, 12);
        notaExamen(b, 2, 8);
        notaSustitutorio(b, 15);   // reemplaza el 8 por 15
        ciclo.agregarCurso(a);
        ciclo.agregarCurso(b);
        verificar("ciclo: notas que cuentan son 10, 14, 12 y 15", "[10.0, 14.0, 12.0, 15.0]",
            evaluador.notasEfectivasDeCiclo(ciclo).toString());
        EstadisticasNotas estCiclo = evaluador.estadisticasDeCiclo(ciclo);
        verificar("ciclo: hay datos", true, estCiclo.hayDatos());
        verificar("ciclo: maxima 15", 15.0, estCiclo.getMaxima());
        verificar("ciclo: minima 10", 10.0, estCiclo.getMinima());
        verificar("ciclo: mediana 13", 13.0, estCiclo.getMediana());
        verificar("ciclo: promedio 12.75", 12.75, estCiclo.getPromedio());
        Estudiante estudiante = new Estudiante("Ana", "Perez", "71234567", "ana@unsa.edu.pe", "20261111", "Sistemas");
        estudiante.agregarCiclo(ciclo);
        verificar("acumuladas: mismas 4 notas", 4.0, evaluador.notasEfectivasAcumuladas(estudiante).size());
        verificar("acumuladas: maxima 15", 15.0, evaluador.estadisticasAcumuladas(estudiante).getMaxima());
        verificar("ciclo sin cursos: no hay datos", false, evaluador.estadisticasDeCiclo(new Ciclo("2027-A", 2027)).hayDatos());

        // --- Continua.quitarTrabajo ---
        Continua continua = new Continua(1, 15);
        TrabajoContinua t1 = new TrabajoContinua("Tarea 1", 40, false);
        TrabajoContinua t2 = new TrabajoContinua("Tarea 2", 60, true);
        continua.agregarTrabajo(t1);
        continua.agregarTrabajo(t2);
        verificar("quitarTrabajo: quita uno que esta (true)", true, continua.quitarTrabajo(t1));
        verificar("quitarTrabajo: el peso interno baja a 60", 60.0, continua.getPesoInterno());
        verificar("quitarTrabajo: quitar uno que ya no esta (false)", false, continua.quitarTrabajo(t1));
        verificar("quitarTrabajo: queda 1 trabajo", 1.0, continua.getTrabajos().size());
    }
}