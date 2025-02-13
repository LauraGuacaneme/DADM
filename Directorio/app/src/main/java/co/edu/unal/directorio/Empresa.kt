package co.edu.unal.directorio

data class Empresa(
    val id: Long = 0,
    val nombre: String,
    val url: String,
    val telefono: String,
    val email: String,
    val productosServicios: String,
    val clasificacion: String
)
