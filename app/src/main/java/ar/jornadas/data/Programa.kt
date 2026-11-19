package ar.jornadas.data

import androidx.room.*
import retrofit2.http.GET
import kotlinx.coroutines.flow.Flow

data class FichaRemota(val id: String, val title: String, val track: String, val day: Int,
    val time: String, val room: String, val speaker: String, val summary: String)

interface ProgramaApi {
    @GET("programa") suspend fun consultar(): List<FichaRemota>
}

@Entity(tableName = "charlas")
data class Charla(@PrimaryKey val id: String, val titulo: String, val tema: String, val dia: Int,
    val hora: String, val sala: String, val ponente: String, val resumen: String,
    val favorita: Boolean = false)

@Dao
interface CarteleraDao {
    @Query("SELECT * FROM charlas ORDER BY dia, hora, sala")
    fun observar(): Flow<List<Charla>>

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

    @Query("UPDATE charlas SET favorita = NOT favorita WHERE id = :id")
    suspend fun alternar(id: String)
}

@Database(entities = [Charla::class], version = 1, exportSchema = false)
abstract class Archivo : RoomDatabase() {
    abstract fun cartelera(): CarteleraDao
}
