package ar.jornadas.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import ar.jornadas.Servicios
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.IOException

class ProgramaModel(app: Application, private val savedState: SavedStateHandle) : AndroidViewModel(app) {
    private val servicios = Servicios(app)
    val mesa = servicios.mesa
    private val repo = servicios.agenda
    val charlas = repo.programa.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val dia = savedState.getStateFlow("dia", 0)
    
    fun setDia(d: Int) {
        savedState["dia"] = d
    }
    
    val aviso = MutableStateFlow("")
    val cargando = MutableStateFlow(false)
    
    var descargado: Boolean
        get() = getApplication<Application>().getSharedPreferences("app", Context.MODE_PRIVATE).getBoolean("descargado", false)
        set(value) { getApplication<Application>().getSharedPreferences("app", Context.MODE_PRIVATE).edit().putBoolean("descargado", value).apply() }
        
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
            reintentosPendientes = 1
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
                    recargar()
                }
            }
        }
    }

    fun marcar(id: String) { viewModelScope.launch { repo.cambiarFavorita(id) } }
}
