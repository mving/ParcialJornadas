# Registro de Cambios Implementados

Este documento detalla las modificaciones realizadas en el código para satisfacer los requerimientos 41 al 44, explicando el estado anterior, la solución actual, los fragmentos de código modificados y el razonamiento técnico detrás de cada cambio.

---

## 1. Persistencia de Filtros ante la Muerte del Proceso
**Requerimiento 43: Volver al punto de lectura**

### Problema (Cómo estaba antes)
La aplicación perdía los filtros seleccionados (día y tema) cuando el sistema operativo mataba su proceso para liberar memoria.

### Solución en Código (Cómo está ahora)
**En `ProgramaModel.kt`:**
Se inyectó `SavedStateHandle` y se reemplazó el `MutableStateFlow` estándar.
```diff
- class ProgramaModel(app: Application) : AndroidViewModel(app) {
+ class ProgramaModel(app: Application, private val savedState: SavedStateHandle) : AndroidViewModel(app) {

-    val dia = MutableStateFlow(0)
+    val dia = savedState.getStateFlow("dia", 0)
+    
+    fun setDia(d: Int) {
+        savedState["dia"] = d
+    }
```

**En `Catalogo.kt`:**
Se reemplazó el uso de `remember` por `rememberSaveable`.
```diff
-    var tema by remember { mutableStateOf("Todos") }
-    Selector(listOf("Ambos días", "Día 1", "Día 2"), dia) { model.dia.value = it }
+    var tema by rememberSaveable { mutableStateOf("Todos") }
+    Selector(listOf("Ambos días", "Día 1", "Día 2"), dia) { model.setDia(it) }
```

### ¿Por qué?
`SavedStateHandle` y `rememberSaveable` están diseñados para integrarse con el mecanismo de retención de estado de Android (`onSaveInstanceState`). Al utilizarlos, los datos sobreviven al proceso de destrucción y recreación provocado por el sistema (simulable con `adb shell am kill`).

---

## 2. Actualización de Datos Preservando Preferencias de Usuario
**Requerimiento 42: Agenda personal**

### Problema (Cómo estaba antes)
El repositorio de datos (`CarteleraDao`) utilizaba `REPLACE` al descargar el programa, destruyendo la información local del usuario (el campo `favorita`).
```kotlin
// ANTES
@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun guardar(fichas: List<Charla>)
```

### Solución en Código (Cómo está ahora)
**En `Programa.kt` (interfaz `CarteleraDao`):**
Se reescribió la lógica usando una `@Transaction`.
```kotlin
// AHORA
@Query("DELETE FROM charlas WHERE id NOT IN (:ids)")
suspend fun borrarSobrantes(ids: List<String>)

@Query("DELETE FROM charlas")
suspend fun borrarTodas()

@Insert(onConflict = OnConflictStrategy.IGNORE)
suspend fun insertarIgnorando(ficha: Charla): Long

@Query("UPDATE charlas SET titulo=:titulo, tema=:tema, dia=:dia, hora=:hora, sala=:sala, ponente=:ponente, resumen=:resumen WHERE id=:id")
suspend fun actualizarDatos(id: String, titulo: String, tema: String, dia: Int, hora: String, sala: String, ponente: String, resumen: String)

@Transaction
suspend fun guardar(fichas: List<Charla>) {
    val ids = fichas.map { it.id }
    if (ids.isEmpty()) borrarTodas() else borrarSobrantes(ids)
    
    for (f in fichas) {
        val insertado = insertarIgnorando(f)
        if (insertado == -1L) {
            // Si ya existía, actualizamos todo menos la columna 'favorita'
            actualizarDatos(f.id, f.titulo, f.tema, f.dia, f.hora, f.sala, f.ponente, f.resumen)
        }
    }
}
```

### ¿Por qué?
Al separar el borrado de la inserción/actualización (Upsert manual) logramos el control necesario para sobreescribir únicamente la información administrativa que viene del servidor, dejando intacto el estado del registro local (marca de favorita).

---

## 3. Control de Concurrencia en Descargas
**Requerimiento 44: Control de descargas**

### Problema (Cómo estaba antes)
La aplicación disparaba descargas tras cada rotación de pantalla (`onStart` de la actividad) o al recibir múltiples toques.

### Solución en Código (Cómo está ahora)
**En `MainActivity.kt`:**
```diff
-    override fun onStart() {
-        super.onStart()
-        model.recargar()
-    }
```

**En `Catalogo.kt`:**
```diff
-    LaunchedEffect(Unit) { model.recargar() }
+    LaunchedEffect(Unit) { model.inicializar() }
```

**En `ProgramaModel.kt`:**
Se agregó control de estados y reintentos.
```kotlin
// AHORA
private var inicializado = false
private var procesando = false
private var reintentosPendientes = 0

fun inicializar() {
    if (!inicializado) {
        inicializado = true
        recargar()
    }
}

fun recargar() {
    if (procesando) {
        reintentosPendientes = 1 // Encola solo 1 intento
        return
    }
    
    viewModelScope.launch {
        procesando = true
        cargando.value = true
        aviso.value = ""
        try {
            repo.sincronizar()
            descargado = true
        } catch (e: IOException) {
            aviso.value = if (descargado) "Usando datos guardados" else "No se pudo consultar el programa"
        } finally {
            cargando.value = false
            procesando = false
            if (reintentosPendientes > 0) {
                reintentosPendientes = 0
                recargar() // Procesa el intento encolado
            }
        }
    }
}
```

### ¿Por qué?
Centralizar la carga inicial en el `ViewModel` (`inicializar()`) elimina peticiones por rotación. El estado `procesando` evita sobrecargar la red, y `reintentosPendientes` asegura que toques adicionales agreguen exactamente un reintento a ejecutarse tras finalizar el proceso actual.

---

## 4. Estado Histórico y Tolerancia a Fallos sin Red
**Requerimiento 41: Programa sin señal**

### Problema (Cómo estaba antes)
No se diferenciaba un error de red puro (sin datos locales previos) de un error de red con datos ya validados, ni se podía distinguir una base de datos vacía por error de una válida pero sin charlas.

### Solución en Código (Cómo está ahora)
**En `ProgramaModel.kt`:**
Se añadió persistencia en las preferencias locales.
```kotlin
// AHORA
var descargado: Boolean
    get() = getApplication<Application>().getSharedPreferences("app", Context.MODE_PRIVATE).getBoolean("descargado", false)
    set(value) { getApplication<Application>().getSharedPreferences("app", Context.MODE_PRIVATE).edit().putBoolean("descargado", value).apply() }
```
*(Nota: `descargado = true` se ejecuta tras un `repo.sincronizar()` exitoso, como se ve en el código de la sección anterior).*

**En `Catalogo.kt`:**
Se actualizó el renderizado condicional.
```diff
-    if (aviso.isNotEmpty()) {
-        Text(aviso)
-        OutlinedButton(onClick = model::recargar) { Text("Reintentar") }
-    } else {
-        Grilla(fichas.filter { (dia == 0 || it.dia == dia) && (tema == "Todos" || it.tema == tema) }, abrir)
-    }

+    if (!model.descargado && aviso.isNotEmpty()) {
+        Text(aviso)
+        OutlinedButton(onClick = model::recargar) { Text("Reintentar") }
+    } else {
+        if (aviso.isNotEmpty()) {
+            Text(aviso)
+        }
+        val mostradas = fichas.filter { (dia == 0 || it.dia == dia) && (tema == "Todos" || it.tema == tema) }
+        
+        if (model.descargado && fichas.isEmpty()) {
+            Text("Sin charlas programadas")
+        } else {
+            Grilla(mostradas, abrir)
+        }
+    }
```

### ¿Por qué?
Persistir el flag `descargado` nos permite evaluar si debemos bloquear al usuario pidiendo red (botón "Reintentar") o mostrar la UI con los datos en caché (advirtiendo "Usando datos guardados"). Si se tiene la certeza de que la conexión fue exitosa (`descargado = true`) y aun así la lista viene vacía, podemos mostrar confiadamente el mensaje oficial "Sin charlas programadas".