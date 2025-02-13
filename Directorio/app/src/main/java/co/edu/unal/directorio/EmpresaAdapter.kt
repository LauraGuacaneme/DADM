package co.edu.unal.directorio

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EmpresaAdapter(
    private val empresas: List<Empresa>,
    private val onEliminar: (Empresa) -> Unit,
    private val onEditar: (Empresa) -> Unit
) : RecyclerView.Adapter<EmpresaAdapter.EmpresaViewHolder>() {

    inner class EmpresaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombre)
        val tvClasificacion: TextView = view.findViewById(R.id.tvClasificacion)
        val tvTelefono: TextView = view.findViewById(R.id.tvTelefono)
        val btnEditar: TextView = view.findViewById(R.id.btnEditar)
        val btnEliminar: TextView = view.findViewById(R.id.btnEliminar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmpresaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_empresa, parent, false)
        return EmpresaViewHolder(view)
    }

    override fun onBindViewHolder(holder: EmpresaViewHolder, position: Int) {
        val empresa = empresas[position]
        holder.tvNombre.text = empresa.nombre
        holder.tvClasificacion.text = empresa.clasificacion
        holder.tvTelefono.text = empresa.telefono

        holder.btnEditar.setOnClickListener { onEditar(empresa) }
        holder.btnEliminar.setOnClickListener { onEliminar(empresa) }
    }

    override fun getItemCount(): Int = empresas.size
}
