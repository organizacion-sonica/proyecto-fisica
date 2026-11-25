<!-- evaluacion:inicio -->
# Evaluación — organizacion-sonica/proyecto-fisica

> Documento **generado automáticamente** por la cátedra de Programación II (RepoSense + análisis de calidad POO/SOLID). **No editar a mano**: se regenera en cada evaluación.
> Rama evaluada: `main`. Análisis **heurístico** (regex + métricas): es una criba para orientar la revisión, **no una nota**.
> Generado: 2026-11-25.

## 1. Veredicto

| Aspecto | Resultado |
|---|---|
| Índice de calidad POO/SOLID | **94/100** |
| Commit evaluado | `58ed704c6d33` (2026-11-20) |
| Código (LOC) | 3,342 |
| Archivos de código | 71 |
| Clases (bien usadas) | 89 (51 OK) |
| Código dentro de clases (%POO) | 96% |
| Funciones fuera de clases | 0 |
| Duplicación lógica | 0.0% |
| Señales S/O/L/I/D | 5/0/0/0/4 |

> Este informe **describe** lo que el análisis midió con los criterios ya establecidos de la materia. No es una nota.

### Semáforo de aprobación

Criterios de `CRITERIOS-CALIDAD.md`. **Aprobar** se juega con el perfil `estandar`; **destacar**, con el `exigente` (meta de excelencia).

| Semáforo | Resultado |
|---|---|
| Aprobar (`estandar`) | **cumple** — 4/4 |
| Destacar (`exigente`) | **no cumple** — 4/5 |

**Criterios para aprobar**

| Criterio | Requisito | Tu resultado | Cumple |
|---|---|---|---|
| Código dentro de clases (%POO) | ≥ 85% | 96% | sí |
| Duplicación lógica | ≤ 10% | 0.0% | sí |
| Funciones fuera de clases | ≤ 1 | 0 | sí |
| Señales POO/SOLID críticas | ninguna | ninguna | sí |

**Criterios para destacar**

| Criterio | Requisito | Tu resultado | Cumple |
|---|---|---|---|
| Código dentro de clases (%POO) | ≥ 95% | 96% | sí |
| Duplicación lógica | ≤ 5% | 0.0% | sí |
| Funciones fuera de clases | ≤ 1 | 0 | sí |
| Señales POO/SOLID críticas | ninguna | Baja cohesión (LCOM), Acoplamiento alto (CBO) | no |
| Índice de calidad POO/SOLID | ≥ 60 | 94/100 | sí |

### Cómo se calcula el %POO (y por qué no sube solo)

`%POO = líneas de lógica dentro de clases ÷ líneas de lógica totales`.

Se cuentan las líneas de **lógica y campos**; no cuentan `package`/`import`/`using`/anotaciones ni comentarios, Javadoc o docstrings. Es una **proporción**: si agregás código nuevo dentro de una clase, el numerador y el denominador crecen juntos y el porcentaje casi no cambia.

Sube cuando el código que hoy está **fuera** de clases pasa a ser método de una clase (o desaparece): funciones sueltas, `main`, constantes de módulo, scripts y lógica de archivos sueltos. También importa que ese trabajo esté **integrado en `main`**: la evaluación siempre mide `main`, así que lo que sigue en otra rama no se refleja todavía.

## 2. Autoría (RepoSense)

| Autor | Commits | Código (LOC) | Tests (LOC) | Docs (LOC) | Días activos | % del código |
|---|---:|---:|---:|---:|---:|---:|
| simonK2010 | 44 | 3,879 | 76 | 14 | 21 | 100% |

> _El LOC de autoría lo calcula RepoSense sobre el historial y puede diferir del LOC de código del análisis de calidad (que mira solo el estado de `main`)._

## 3. Fallas detectadas

Reúne lo que el análisis marcó y lo que todavía falta para cada semáforo. Cuando la señal se puede asociar a archivos, se indican para orientar la revisión. No es una nota.

| Falla | Tu resultado | Requisito | Archivos |
|---|---|---|---|
| Señales POO/SOLID críticas | Baja cohesión (LCOM), Acoplamiento alto (CBO) | Destacar: ninguna | `app/src/main/kotlin/org/example/BasketDefaults.kt`, `app/src/main/kotlin/org/example/BirdPile.kt`, `app/src/main/kotlin/org/example/Box2DWorld.kt`, `app/src/main/kotlin/org/example/MenuController.kt`, `app/src/main/kotlin/org/example/SlingshotGame.kt`, `app/src/main/kotlin/org/example/SlingshotInput.kt` (+2 más) |

## 4. Aspectos evaluados

Cada aspecto indica **qué mide**, **por qué importa** y **tu resultado**. Cuando el análisis reportó una señal, se agregan preguntas para **investigar la causa** (no hay recetas: entender el porqué es parte de la evaluación).

### 1. Índice de calidad POO/SOLID (indice)

- **Qué mide:** Un resumen numérico (0-100) que combina la riqueza de clases bien usadas con la penalización por señales POO/SOLID y duplicación, y luego aplica topes duros.
- **Por qué importa:** Ordena y prioriza la revisión: un índice bajo indica varias señales que conviene mirar juntas.
- **Tu resultado:** índice **94/100**

### 2. Duplicación lógica (DRY) (duplicacion) — sin señales

- **Qué mide:** Porcentaje de bloques de LÓGICA repetidos. Quedan fuera imports/includes, anotaciones, declaraciones de campos y comentarios/Javadoc/docstrings: esos bloques son boilerplate, no lógica.
- **Por qué importa:** Cada copia de lógica se mantiene por separado: un arreglo hay que repetirlo y los defectos se multiplican.
- **Tu resultado:** **0.0%** de duplicación lógica

### 3. Adopción de POO (% de código en clases) (poo) — sin señales

- **Qué mide:** Proporción de las **líneas de lógica** que viven dentro de una clase. Quedan fuera `package`/`import`/`using`/anotaciones y comentarios/Javadoc/docstrings: no son código que pueda estar dentro de una clase y, si contaran, el porcentaje quedaría topado por el boilerplate de cada archivo.
- **Por qué importa:** El diseño orientado a objetos es el eje de la materia: el comportamiento debería vivir como método de una clase.
- **Tu resultado:** **95.7%** del código dentro de clases
- **Cómo leerlo:** Es una **proporción**, no un acumulado: agregar código dentro de una clase no la sube por sí solo (numerador y denominador crecen juntos). Sube cuando el código que está **fuera** de clases (funciones sueltas, `main`, constantes de módulo, scripts) pasa a ser método de una clase o deja de existir. La medición se hace sobre `main`: el trabajo que todavía vive en otra rama no se refleja hasta que se integra.

### 4. Funciones fuera de clases (sueltas) — sin señales

- **Qué mide:** Funciones de nivel de módulo que no pertenecen a ninguna clase (en esta materia el comportamiento debe ser método de una clase).
- **Por qué importa:** Son la forma más directa de código procedural en un proyecto que debería ser orientado a objetos.
- **Tu resultado:** **0** función(es) fuera de clases

### 5. Clase demasiado grande (SRP) (god) — sin señales

- **Qué mide:** Clases con demasiadas líneas o métodos de LÓGICA. No cuentan los getters/setters triviales ni la cantidad de campos por sí sola.
- **Por qué importa:** Una clase que hace de todo cambia por muchos motivos y cuesta entenderla y probarla.
- **Tu resultado:** **0** clase(s) demasiado grande(s)

### 6. Baja cohesión (LCOM) (cohesion) — con señales

- **Qué mide:** Métodos y campos de una clase que casi no se relacionan entre sí.
- **Por qué importa:** Una unidad que cambia por motivos distintos y comparte poca información suele esconder más de una responsabilidad.
- **Tu resultado:** **5** clase(s) con baja cohesión
- **Para investigar la causa:**
    - ¿Qué métodos de la clase no comparten estado con el resto?
    - ¿Cambian por motivos distintos según la tarea?
    - ¿Se pensó su responsabilidad antes de escribirla, o se fue agregando?

### 7. Complejidad alta (complejidad) — sin señales

- **Qué mide:** Complejidad ciclomática: cantidad de caminos posibles dentro de un método o función (condicionales, bucles, casos).
- **Por qué importa:** Muchos caminos = difícil de seguir y de probar; suelen esconderse casos sin cubrir.
- **Tu resultado:** **0** método(s)/función(es) con complejidad alta

### 8. Métodos/funciones largos o con muchos parámetros (tamano) — sin señales

- **Qué mide:** Tamaño de los métodos/funciones y cantidad de parámetros.
- **Por qué importa:** Una función que hace varias cosas cuesta leerla, nombrarla y reutilizarla.
- **Tu resultado:** **0** método(s)/función(es) largos o con muchos parámetros

### 9. Acoplamiento alto (CBO) (acoplamiento) — con señales

- **Qué mide:** Cuánto depende una clase de otros tipos del proyecto. No cuentan los tipos de la plataforma (biblioteca estándar, DOM, motor Godot) ni los métodos/propiedades propias.
- **Por qué importa:** Piezas muy enlazadas hacen que un cambio se propague y que sea difícil reemplazar partes.
- **Tu resultado:** **4** clase(s) muy acoplada(s)
- **Para investigar la causa:**
    - ¿La clase conoce detalles internos de muchas otras?
    - ¿Se puede cambiar una parte sin tocar las demás?
    - ¿Los colaboradores llegan como parámetros o se crean por dentro?

### 10. Herencia profunda (DIT) (herencia) — sin señales

- **Qué mide:** Profundidad de la cadena de herencia de una clase.
- **Por qué importa:** Jerarquías muy profundas vuelven difícil saber de dónde viene cada comportamiento.
- **Tu resultado:** **0** clase(s) con herencia profunda

### 11. Switch sobre tipos (OCP) (ocp) — sin señales

- **Qué mide:** Cadenas de dispatch por tipo (`else if ... is` / `else if ... instanceof`, `switch` sobre tipo o `typeof`). Un `is Tipo variable` aislado (pattern matching / cast seguro) NO cuenta como switch.
- **Por qué importa:** Cada tipo nuevo obliga a tocar varios lugares, en vez de extender con una implementación nueva.
- **Tu resultado:** **0** clase(s) con switch sobre tipos

### 12. Interfaz con demasiados métodos (ISP) (isp) — sin señales

- **Qué mide:** Interfaces o clases abstractas con muchos métodos declarados.
- **Por qué importa:** Obliga a implementar operaciones que no se usan y acopla a los clientes a métodos que no necesitan.
- **Tu resultado:** **0** interfaz(es) con demasiados métodos

### 13. Override vacío o 'no implementado' (LSP) (lsp) — sin señales

- **Qué mide:** Métodos sobrescritos que quedan vacíos o lanzan 'no implementado'.
- **Por qué importa:** Suele indicar que la jerarquía no representa bien el problema y rompe la sustitución entre clases.
- **Tu resultado:** **0** override(s) vacío(s) o 'no implementado'

### 14. Clase sin métodos (solo datos) (data) — sin señales

- **Qué mide:** Clases que solo acumulan campos y no tienen comportamiento.
- **Por qué importa:** Separar datos de comportamiento reparte por el proyecto la lógica que debería estar cerca de esos datos.
- **Tu resultado:** **0** clase(s) sin métodos (solo datos)

### 15. Campos mutables públicos (encapsulación) (encapsulacion) — sin señales

- **Qué mide:** Clases que exponen estado **modificable** desde afuera. No cuentan constantes (`static final`, `const`), campos `final`/`readonly`, propiedades `val` de Kotlin ni propiedades con `private set`.
- **Por qué importa:** Expone el estado interno y hace que cualquier cambio dependa de quién lo modifica desde afuera.
- **Tu resultado:** **0** clase(s) con campos mutables públicos

### 16. Clase solo con métodos estáticos (utilitaria) (static) — sin señales

- **Qué mide:** Clases donde todos los métodos son estáticos.
- **Por qué importa:** Es comportamiento procedural disfrazado de clase: no se instancia ni participa del modelo de objetos.
- **Tu resultado:** **0** clase(s) solo con métodos estáticos

### Resumen de señales por tipo

| Señal | Cantidad | Principio | Archivos |
|---|---:|---|---:|
| baja cohesión | 5 | S | 5 |
| acoplamiento alto | 4 | D | 4 |

### Resumen por archivo

> Archivos ordenados por cantidad de señales. Se excluyen **0** señal(es) de nivel repositorio sin archivo propio.

| Archivo | Señales | Aspectos | Líneas |
|---|---:|---|---|
| `app/src/main/kotlin/org/example/SlingshotGame.kt` | 2 | acoplamiento, cohesión | 10 |
| `app/src/main/kotlin/org/example/BasketDefaults.kt` | 1 | acoplamiento | 5 |
| `app/src/main/kotlin/org/example/BirdPile.kt` | 1 | cohesión | 11 |
| `app/src/main/kotlin/org/example/Box2DWorld.kt` | 1 | acoplamiento | 16 |
| `app/src/main/kotlin/org/example/MenuController.kt` | 1 | cohesión | 17 |
| `app/src/main/kotlin/org/example/SlingshotInput.kt` | 1 | acoplamiento | 13 |
| `app/src/main/kotlin/org/example/SlingshotView.kt` | 1 | cohesión | 18 |
| `app/src/main/kotlin/org/example/Target.kt` | 1 | cohesión | 13 |

### Archivos donde se originan las señales

> Se listan **9** hallazgo(s) localizados en **8** archivo(s). La columna `Línea` es la línea del archivo donde el análisis detectó la señal; `—` indica una señal de nivel repositorio (sin archivo propio).

| Aspecto | Archivo | Línea | Hallazgo |
|---|---|---:|---|
| Baja cohesión (LCOM) | `app/src/main/kotlin/org/example/BirdPile.kt` | 11 | clase 'BirdPile' con baja cohesión (33%) |
| Baja cohesión (LCOM) | `app/src/main/kotlin/org/example/MenuController.kt` | 17 | clase 'MenuController' con baja cohesión (39%) |
| Baja cohesión (LCOM) | `app/src/main/kotlin/org/example/SlingshotGame.kt` | 10 | clase 'SlingshotGame' con baja cohesión (46%) |
| Baja cohesión (LCOM) | `app/src/main/kotlin/org/example/SlingshotView.kt` | 18 | clase 'SlingshotView' con baja cohesión (47%) |
| Baja cohesión (LCOM) | `app/src/main/kotlin/org/example/Target.kt` | 13 | clase 'Target' con baja cohesión (47%) |
| Acoplamiento alto (CBO) | `app/src/main/kotlin/org/example/BasketDefaults.kt` | 5 | clase 'BasketDefaults' muy acoplada (CBO=8) |
| Acoplamiento alto (CBO) | `app/src/main/kotlin/org/example/Box2DWorld.kt` | 16 | clase 'Box2DWorld' muy acoplada (CBO=8) |
| Acoplamiento alto (CBO) | `app/src/main/kotlin/org/example/SlingshotGame.kt` | 10 | clase 'SlingshotGame' muy acoplada (CBO=10) |
| Acoplamiento alto (CBO) | `app/src/main/kotlin/org/example/SlingshotInput.kt` | 13 | clase 'SlingshotInput' muy acoplada (CBO=8) |

### Clases señaladas — métricas

> Solo clases con al menos una señal. `LOC lóg.` y `Mét. lóg.` descuentan getters/setters; `LCOM` es la cantidad de pares de métodos sin campos en común.

| Clase | Archivo | Línea | LOC lóg. | Mét. lóg. | Campos | CBO | LCOM | Cohesión | WMC | RFC | Señales |
|---|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---|
| `SlingshotGame` | `app/src/main/kotlin/org/example/SlingshotGame.kt` | 10 | 63 | 10 | 12 | 10 | 15 | 46% | 12 | 44 | acoplamiento alto×1, baja cohesión×1 |
| `SlingshotInput` | `app/src/main/kotlin/org/example/SlingshotInput.kt` | 13 | 93 | 9 | 9 | 8 | 10 | 72% | 24 | 46 | acoplamiento alto×1 |
| `Box2DWorld` | `app/src/main/kotlin/org/example/Box2DWorld.kt` | 16 | 88 | 16 | 6 | 8 | 34 | 72% | 26 | 52 | acoplamiento alto×1 |
| `SlingshotView` | `app/src/main/kotlin/org/example/SlingshotView.kt` | 18 | 82 | 9 | 20 | 6 | 19 | 47% | 24 | 42 | baja cohesión×1 |
| `BasketDefaults` | `app/src/main/kotlin/org/example/BasketDefaults.kt` | 5 | 42 | 2 | 0 | 8 | 0 | 100% | 2 | 25 | acoplamiento alto×1 |
| `BirdPile` | `app/src/main/kotlin/org/example/BirdPile.kt` | 11 | 34 | 8 | 7 | 4 | 14 | 33% | 11 | 26 | baja cohesión×1 |
| `Target` | `app/src/main/kotlin/org/example/Target.kt` | 13 | 27 | 6 | 17 | 3 | 8 | 47% | 9 | 20 | baja cohesión×1 |
| `MenuController` | `app/src/main/kotlin/org/example/MenuController.kt` | 17 | 26 | 9 | 9 | 5 | 17 | 39% | 9 | 30 | baja cohesión×1 |

### Umbrales aplicados (perfil exigente)

| Señal | Regla |
|---|---|
| Clase grande (SRP) | ≥ 200 LOC lógico, o ≥ 15 métodos con lógica y ≥ 100 LOC lógico; no cuentan getters/setters. |
| Baja cohesión (LCOM) | < 50 % de pares de métodos comparten algún campo, con ≥ 4 métodos con lógica y ≥ 3 campos. |
| Complejidad alta | algún método con complejidad ciclomática ≥ 10. |
| Método largo | ≥ 30 líneas. |
| Muchos parámetros | > 4 parámetros (5 o más). |
| Acoplamiento alto (CBO) | CBO ≥ 8. |
| Herencia profunda | DIT ≥ 3. |
| Type-check / switch de tipos | ≥ 1 cadena de type-check. |
| Interfaz demasiado grande (ISP) | > 6 métodos (7 o más). |
| Override vacío (LSP) | método vacío o marcado como 'no implementado'. |
| Clase solo de datos | 0 métodos y ≥ 3 campos. |
| Campos mutables públicos | ≥ 4 campos mutables públicos y ≥ 4 campos. |
| Clase estática | ≥ 2 métodos y todos estáticos. |
| Duplicación lógica | ventana de 8 líneas; solo lógica; se informa desde 1 %. |

### Desglose de la columna S (SRP)

> La columna **S** no es un puntaje: cuenta **señales** de SRP. Aquí suma **5** entrada(s) sobre **5** clase(s) distinta(s).

| Señal | Entradas | Regla (perfil exigente) |
|---|---:|---|
| Cohesión baja | 5 | < 50 % de los pares de métodos comparten algún campo (con ≥ 4 métodos con lógica y ≥ 3 campos). |

**Clases con baja cohesión (5)**

- `BirdPile`: cohesión baja (33% de pares comparten campos)
- `MenuController`: cohesión baja (39% de pares comparten campos)
- `SlingshotGame`: cohesión baja (46% de pares comparten campos)
- `Target`: cohesión baja (47% de pares comparten campos)
- `SlingshotView`: cohesión baja (47% de pares comparten campos)

> El análisis no excluye tests: una clase de test que supere los umbrales también suma.

## 5. Evolución por commit

Métricas calculadas sobre el estado de `main` en **cada commit** (41 commits con trabajo), del primero al último. La calidad usa el perfil `exigente`; el aporte de código lo mide RepoSense. El índice puede **bajar** al crecer el proyecto: se acumulan señales.

```mermaid
xychart-beta
    title "Índice POO/SOLID a lo largo de los commits"
    x-axis ["dd8527c", "6011506", "4cafe61", "82041cc", "b371234", "d5a0e6c", "577df69", "ee78559", "b932959", "656642a", "9c6db33", "d7f64af", "9fc92df", "031ae05", "144808c", "17760fb", "627bcff", "1deb8d2", "631b308", "b386b9c", "8a205a3", "0aca0b4", "34db6b7", "0bae7a2", "e47921a", "70a9b91", "04874b1", "fe4c80f", "323fe09", "1ed7a03", "0b855c1", "63e9e29", "31f3106", "0a1a3ed", "cd3a2b4", "e03ed00", "2cf1497", "f97a90e", "ea89042", "dc325fa", "58ed704"]
    y-axis "Índice" 0 --> 100
    line [0, 0, 0, 0, 65, 70, 48, 49, 49, 83, 79, 80, 83, 84, 88, 89, 89, 89, 89, 89, 90, 90, 88, 88, 88, 88, 88, 87, 87, 87, 92, 92, 92, 92, 92, 94, 94, 94, 94, 94, 94]
```

| # | Fecha | Commit | Autor | LOC | Índice | %POO | Duplic. | Sueltas |
|---|---|---|---|---:|---:|---:|---:|---:|
| 1 | 2026-08-14 | `dd8527c` | simonK2010 | 0 | 0 | 0% | 0.0% | 0 |
| 2 | 2026-08-24 | `6011506` | dqmdz-etec | 0 | 0 | 0% | 0.0% | 0 |
| 3 | 2026-09-28 | `4cafe61` | simonK2010 | 0 | 0 | 0% | 0.0% | 0 |
| 4 | 2026-09-28 | `82041cc` | simonK2010 | 0 | 0 | 0% | 0.0% | 0 |
| 5 | 2026-09-28 | `b371234` | simonK2010 | 1,539 | 65 | 89% | 4.4% | 0 |
| 6 | 2026-10-09 | `d5a0e6c` | simonK2010 | 2,087 | 70 | 95% | 4.3% | 0 |
| 7 | 2026-10-23 | `577df69` | simonK2010 | 2,512 | 48 | 96% | 16.4% | 0 |
| 8 | 2026-10-23 | `ee78559` | simonK2010 | 2,540 | 49 | 97% | 16.2% | 0 |
| 9 | 2026-10-26 | `b932959` | simonK2010 | 2,540 | 49 | 97% | 16.2% | 0 |
| 10 | 2026-10-26 | `656642a` | simonK2010 | 2,572 | 83 | 97% | 2.0% | 0 |
| 11 | 2026-10-30 | `9c6db33` | simonK2010 | 2,905 | 79 | 97% | 2.2% | 0 |
| 12 | 2026-10-30 | `d7f64af` | simonK2010 | 2,923 | 80 | 97% | 2.2% | 0 |
| 13 | 2026-11-02 | `9fc92df` | simonK2010 | 3,051 | 83 | 97% | 1.8% | 0 |
| 14 | 2026-11-06 | `031ae05` | simonK2010 | 3,115 | 84 | 96% | 1.8% | 0 |
| 15 | 2026-11-06 | `144808c` | simonK2010 | 3,198 | 88 | 96% | 0.8% | 0 |
| 16 | 2026-11-06 | `17760fb` | simonK2010 | 3,206 | 89 | 96% | 0.8% | 0 |
| 17 | 2026-11-07 | `627bcff` | dqmdz-etec | 3,206 | 89 | 96% | 0.8% | 0 |
| 18 | 2026-11-07 | `1deb8d2` | dqmdz-etec | 3,206 | 89 | 96% | 0.8% | 0 |
| 19 | 2026-11-07 | `631b308` | dqmdz-etec | 3,206 | 89 | 96% | 0.8% | 0 |
| 20 | 2026-11-07 | `b386b9c` | dqmdz-etec | 3,206 | 89 | 96% | 0.8% | 0 |
| 21 | 2026-11-08 | `8a205a3` | simonK2010 | 3,260 | 90 | 96% | 0.8% | 0 |
| 22 | 2026-11-08 | `0aca0b4` | simonK2010 | 3,260 | 90 | 96% | 0.8% | 0 |
| 23 | 2026-11-09 | `34db6b7` | simonK2010 | 3,275 | 88 | 96% | 0.8% | 0 |
| 24 | 2026-11-10 | `0bae7a2` | simonK2010 | 3,275 | 88 | 96% | 0.8% | 0 |
| 25 | 2026-11-11 | `e47921a` | simonK2010 | 3,383 | 88 | 96% | 0.7% | 0 |
| 26 | 2026-11-13 | `70a9b91` | simonK2010 | 3,442 | 88 | 96% | 0.7% | 0 |
| 27 | 2026-11-13 | `04874b1` | dqmdz-etec | 3,442 | 88 | 96% | 0.7% | 0 |
| 28 | 2026-11-13 | `fe4c80f` | simonK2010 | 3,576 | 87 | 96% | 1.8% | 0 |
| 29 | 2026-11-14 | `323fe09` | dqmdz-etec | 3,576 | 87 | 96% | 1.8% | 0 |
| 30 | 2026-11-14 | `1ed7a03` | simonK2010 | 3,576 | 87 | 96% | 1.8% | 0 |
| 31 | 2026-11-14 | `0b855c1` | simonK2010 | 3,471 | 92 | 96% | 0.0% | 0 |
| 32 | 2026-11-16 | `63e9e29` | dqmdz-etec | 3,471 | 92 | 96% | 0.0% | 0 |
| 33 | 2026-11-16 | `31f3106` | simonK2010 | 3,448 | 92 | 96% | 0.0% | 0 |
| 34 | 2026-11-16 | `0a1a3ed` | simonK2010 | 3,448 | 92 | 96% | 0.0% | 0 |
| 35 | 2026-11-17 | `cd3a2b4` | dqmdz-etec | 3,448 | 92 | 96% | 0.0% | 0 |
| 36 | 2026-11-19 | `e03ed00` | dqmdz-etec | 3,348 | 94 | 96% | 0.0% | 0 |
| 37 | 2026-11-20 | `2cf1497` | dqmdz-etec | 3,342 | 94 | 96% | 0.0% | 0 |
| 38 | 2026-11-20 | `f97a90e` | dqmdz-etec | 3,342 | 94 | 96% | 0.0% | 0 |
| 39 | 2026-11-20 | `ea89042` | dqmdz-etec | 3,342 | 94 | 96% | 0.0% | 0 |
| 40 | 2026-11-20 | `dc325fa` | dqmdz-etec | 3,342 | 94 | 96% | 0.0% | 0 |
| 41 | 2026-11-20 | `58ed704` | dqmdz-etec | 3,342 | 94 | 96% | 0.0% | 0 |

<!-- actividad-clases:inicio -->
## Actividad en días de clase

> Período evaluado **2026-08-14 → 2026-11-25** · **23 clases** (lunes, viernes) · días de clase del calendario, extendidos hasta la fecha de corte.
>
> `Commits` = commits de trabajo (no-merge); `Merges` = merges de PR (también son push). Los días sin actividad dicen **sin actividad**. El cuadro principal usa sólo las fechas de clase del calendario oficial.
>
> **Aportes fuera del calendario:** cada alumno tiene un cuadro aparte con las fechas no lectivas con actividad, desde **2026-08-14** hasta **2026-11-25** (ventana de aportes). Esas fechas no forman parte del cuadro principal, pero se muestran para no perder trabajo hecho en fines de semana, feriados o después del período.
>
> Se recorre `git log --all` sobre un mirror (todas las ramas y `refs/pull/*`), con deduplicación por commit. Fecha = fecha de autor en UTC-3. Los commits locales no pusheados no se ven. Si el trabajo fue en pareja y el commit es de otra persona, figura a nombre del autor del commit; para reflejarlo, usar `Co-authored-by` o una nota manual.
>
> Sección informativa: no modifica el semáforo de aprobación.

### simonK2010

**13/23 clases con actividad** · 33 commits · 18 merges

| Fecha | Marca | Repositorios | Commits / merges |
|---|:--:|---|--:|
| 2026-08-14 | **Sí** | proyecto-fisica | 1 / 0 |
| 2026-08-21 | No | sin actividad | 0 / 0 |
| 2026-08-24 | No | sin actividad | 0 / 0 |
| 2026-08-28 | No | sin actividad | 0 / 0 |
| 2026-08-31 | No | sin actividad | 0 / 0 |
| 2026-09-04 | No | sin actividad | 0 / 0 |
| 2026-09-07 | No | sin actividad | 0 / 0 |
| 2026-09-14 | **Sí** | proyecto-fisica | 1 / 0 |
| 2026-09-25 | No | sin actividad | 0 / 0 |
| 2026-09-28 | **Sí** | proyecto-fisica | 2 / 3 |
| 2026-10-02 | No | sin actividad | 0 / 0 |
| 2026-10-05 | **Sí** | proyecto-fisica | 1 / 0 |
| 2026-10-09 | **Sí** | proyecto-fisica | 0 / 1 |
| 2026-10-16 | No | sin actividad | 0 / 0 |
| 2026-10-19 | No | sin actividad | 0 / 0 |
| 2026-10-23 | **Sí** | proyecto-fisica | 3 / 2 |
| 2026-10-26 | **Sí** | proyecto-fisica | 8 / 2 |
| 2026-10-30 | **Sí** | proyecto-fisica | 4 / 2 |
| 2026-11-02 | **Sí** | proyecto-fisica | 1 / 1 |
| 2026-11-06 | **Sí** | proyecto-fisica | 3 / 3 |
| 2026-11-13 | **Sí** | proyecto-fisica | 3 / 2 |
| 2026-11-16 | **Sí** | proyecto-fisica | 5 / 2 |
| 2026-11-20 | **Sí** | proyecto-fisica | 1 / 0 |

#### Aportes fuera del calendario de clases — simonK2010

**simonK2010 — 9 fechas fuera del calendario** · 15 commits · 8 merges

| Alumno | Fecha | Repositorios | Commits / merges |
|---|---|---|---|
| simonK2010 (`simonK2010`) | 2026-11-07 | proyecto-fisica | 1 / 0 |
| simonK2010 (`simonK2010`) | 2026-11-08 | proyecto-fisica | 1 / 2 |
| simonK2010 (`simonK2010`) | 2026-11-09 | proyecto-fisica | 1 / 1 |
| simonK2010 (`simonK2010`) | 2026-11-10 | proyecto-fisica | 1 / 1 |
| simonK2010 (`simonK2010`) | 2026-11-11 | proyecto-fisica | 2 / 1 |
| simonK2010 (`simonK2010`) | 2026-11-14 | proyecto-fisica | 5 / 2 |
| simonK2010 (`simonK2010`) | 2026-11-17 | proyecto-fisica | 2 / 1 |
| simonK2010 (`simonK2010`) | 2026-11-18 | proyecto-fisica | 1 / 0 |
| simonK2010 (`simonK2010`) | 2026-11-19 | proyecto-fisica | 1 / 0 |

<!-- actividad-clases:fin -->

---
_Informe generado por la cátedra. Los criterios y umbrales de medición corresponden al perfil `exigente` de `calidad.py`; el semáforo de aprobación, a `CRITERIOS-CALIDAD.md` (ver `README.md`). Ante dudas, revisá el archivo señalado._
<!-- evaluacion:fin -->
