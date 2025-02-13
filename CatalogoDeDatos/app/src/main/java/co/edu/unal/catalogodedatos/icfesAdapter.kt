package co.edu.unal.catalogodedatos

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class IcfesAdapter(private var icfeList: List<IcfesData>) : RecyclerView.Adapter<IcfesAdapter.IcfesViewHolder>() {

    // Inicializar la lista completa con la lista original recibida
    private var icfeListFullOriginal: MutableList<IcfesData> = icfeList.toMutableList()
    private var icfeListFull: MutableList<IcfesData> = icfeList.toMutableList()

    fun updateData(newList: List<IcfesData>) {
        icfeList = newList // Actualizar la lista de datos
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IcfesViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.icfes_item, parent, false)
        return IcfesViewHolder(view)
    }

    override fun onBindViewHolder(holder: IcfesViewHolder, position: Int) {
        val currentItem = icfeList[position]
        holder.icfesName.text = currentItem.nombre ?: "Sin Nombre"
        holder.icfesCategory.text = currentItem.nombreCategoria ?: "Sin Categoría"
        holder.icfesDate.text = currentItem.fechaGeneracion ?: "Sin Fecha"
        holder.icfesResponsable.text = currentItem.responsable ?: "Sin Responsable"
        holder.icfesExcepcion.text = currentItem.excepcion ?: "Sin Excepción"
    }

    override fun getItemCount() = icfeList.size

    // Método para filtrar por categoría
    @SuppressLint("NotifyDataSetChanged")
    fun filterByCategory(category: String) {
        Log.d("AVERRRR LOS FILTROOOOS", category)
        if (category == "Todos") {

            icfeListFull.addAll(icfeList)
        } else {

            Log.d("LISTA ORIGINAL",icfeList.toString())
            icfeListFull.clear()
            icfeListFull.addAll(icfeList.filter { it.nombreCategoria == category })

            Log.d("LISTA DESPUES",icfeListFull.toString())
        }
        notifyDataSetChanged()
    }

    // Método para filtrar por excepción
    @SuppressLint("NotifyDataSetChanged")
    fun filterByExcepcion(excepcion: String?) {
        if (excepcion == "Todos") {
            icfeListFull.addAll(icfeList)
        } else {
            icfeListFull.clear()
            icfeListFull.addAll(icfeList.filter { it.excepcion == excepcion })
        }
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filterList(category: String?, excepcion: String?){
        var filteredList = icfeListFullOriginal
        if (category != null) {
            Log.d("FILTROS",category)
        }
        Log.d("LISTA INICIAL", filteredList.toString())
        if (category != "Todos"){
            filteredList = filteredList.filter { it.nombreCategoria == category }.toMutableList()
        }
        if (excepcion != "Todos"){
            filteredList = filteredList.filter { it.excepcion == excepcion }.toMutableList()
        }
        Log.d("ESTA FILTRANDO", filteredList.toString())
        icfeListFull.clear()
        icfeListFull.addAll(filteredList)
        notifyDataSetChanged()
    }

    class IcfesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icfesName: TextView = itemView.findViewById(R.id.icfesName)
        val icfesCategory: TextView = itemView.findViewById(R.id.icfesCategory)
        val icfesDate: TextView = itemView.findViewById(R.id.icfesDate)
        val icfesResponsable: TextView = itemView.findViewById(R.id.icfesResponsable)
        val icfesExcepcion: TextView = itemView.findViewById(R.id.icfesExcepcion)
    }
}
