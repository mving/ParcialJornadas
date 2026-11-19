package ar.jornadas.data;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\t\n\u0002\u0010 \n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\bg\u0018\u00002\u00020\u0001JN\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00052\u0006\u0010\u0007\u001a\u00020\u00052\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u00052\u0006\u0010\u000b\u001a\u00020\u00052\u0006\u0010\f\u001a\u00020\u00052\u0006\u0010\r\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u000eJ\u0016\u0010\u000f\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0010J\u001c\u0010\u0011\u001a\u00020\u00032\f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00050\u0013H\u00a7@\u00a2\u0006\u0002\u0010\u0014J\u000e\u0010\u0015\u001a\u00020\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0016J\u001c\u0010\u0017\u001a\u00020\u00032\f\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00190\u0013H\u0097@\u00a2\u0006\u0002\u0010\u0014J\u0016\u0010\u001a\u001a\u00020\u001b2\u0006\u0010\u001c\u001a\u00020\u0019H\u00a7@\u00a2\u0006\u0002\u0010\u001dJ\u0014\u0010\u001e\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00190\u00130\u001fH\'\u00a8\u0006 "}, d2 = {"Lar/jornadas/data/CarteleraDao;", "", "actualizarDatos", "", "id", "", "titulo", "tema", "dia", "", "hora", "sala", "ponente", "resumen", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "alternar", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "borrarSobrantes", "ids", "", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "borrarTodas", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "guardar", "fichas", "Lar/jornadas/data/Charla;", "insertarIgnorando", "", "ficha", "(Lar/jornadas/data/Charla;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "observar", "Lkotlinx/coroutines/flow/Flow;", "app_debug"})
@androidx.room.Dao()
public abstract interface CarteleraDao {
    
    @androidx.room.Query(value = "SELECT * FROM charlas ORDER BY dia, hora, sala")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<ar.jornadas.data.Charla>> observar();
    
    @androidx.room.Query(value = "DELETE FROM charlas WHERE id NOT IN (:ids)")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object borrarSobrantes(@org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> ids, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM charlas")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object borrarTodas(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Insert(onConflict = 5)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertarIgnorando(@org.jetbrains.annotations.NotNull()
    ar.jornadas.data.Charla ficha, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    @androidx.room.Query(value = "UPDATE charlas SET titulo=:titulo, tema=:tema, dia=:dia, hora=:hora, sala=:sala, ponente=:ponente, resumen=:resumen WHERE id=:id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object actualizarDatos(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    java.lang.String titulo, @org.jetbrains.annotations.NotNull()
    java.lang.String tema, int dia, @org.jetbrains.annotations.NotNull()
    java.lang.String hora, @org.jetbrains.annotations.NotNull()
    java.lang.String sala, @org.jetbrains.annotations.NotNull()
    java.lang.String ponente, @org.jetbrains.annotations.NotNull()
    java.lang.String resumen, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Transaction()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object guardar(@org.jetbrains.annotations.NotNull()
    java.util.List<ar.jornadas.data.Charla> fichas, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "UPDATE charlas SET favorita = NOT favorita WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object alternar(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 3, xi = 48)
    public static final class DefaultImpls {
        
        @androidx.room.Transaction()
        @org.jetbrains.annotations.Nullable()
        public static java.lang.Object guardar(@org.jetbrains.annotations.NotNull()
        ar.jornadas.data.CarteleraDao $this, @org.jetbrains.annotations.NotNull()
        java.util.List<ar.jornadas.data.Charla> fichas, @org.jetbrains.annotations.NotNull()
        kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
            return null;
        }
    }
}