package co.edu.unal.catalogodedatos

import android.annotation.SuppressLint
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

import retrofit2.http.GET
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

import com.google.gson.annotations.SerializedName

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var icfesAdapter: IcfesAdapter
    private var icfesList = mutableListOf<IcfesData>()
    private var icfesListFull = mutableListOf<IcfesData>()
    private lateinit var errorMessage: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var categorySpinner: Spinner
    private lateinit var exceptionSpinner: Spinner
    private lateinit var filterButton: Button

    private var categoryList = mutableListOf<String>()
    private var exceptionList = mutableListOf<String>()

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Configurar RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        errorMessage = findViewById(R.id.errorMessage)
        progressBar = findViewById(R.id.progressBar)
        categorySpinner = findViewById(R.id.categorySpinner)
        exceptionSpinner = findViewById(R.id.exceptionSpinner)
        filterButton = findViewById(R.id.filterButton)

        recyclerView.layoutManager = LinearLayoutManager(this)
        icfesAdapter = IcfesAdapter(icfesList)
        recyclerView.adapter = icfesAdapter

        showLoading(true)

        // Hacer la petición para obtener los datos
        val retrofit = getRetrofit()
        val apiService = retrofit.create(ApiService::class.java)
        apiService.getIcfesData().enqueue(object : Callback<List<IcfesData>> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<List<IcfesData>>, response: Response<List<IcfesData>>) {
                showLoading(false)

                if (response.isSuccessful) {
                    if (response.body().isNullOrEmpty()) {
                        showErrorMessage(true)
                    } else {
                        icfesList.clear()
                        icfesList.addAll(response.body() ?: emptyList())


                        icfesListFull.clear()
                        icfesListFull.addAll(response.body() ?: emptyList())

                        // Extraer los valores únicos para los filtros
                        extractUniqueFilters()

                        // Actualizar los Spinners
                        updateSpinners()

                        icfesAdapter.notifyDataSetChanged()
                        showErrorMessage(false)
                    }
                } else {
                    showErrorMessage(true)
                }
            }

            override fun onFailure(call: Call<List<IcfesData>>, t: Throwable) {
                showLoading(false)
                showErrorMessage(true)
            }
        })

        // Configurar el botón de filtro
        filterButton.setOnClickListener {
            Log.d("FilterButton", "Button clicked!") // Asegúrate de que esto se imprime en los logs.

            val selectedCategory = categorySpinner.selectedItem.toString()
            val selectedException = exceptionSpinner.selectedItem.toString()

            var filteredList = icfesListFull.toMutableList() // Copia la lista completa

            Log.d("LISTA INICIAL", filteredList.toString())

            // Filtrar por categoría
            if (selectedCategory != "Todos") {
                filteredList = filteredList.filter { it.nombreCategoria == selectedCategory }.toMutableList()
            }

            // Filtrar por excepción
            if (selectedException != "Todos") {
                filteredList = filteredList.filter { it.excepcion == selectedException }.toMutableList()
            }

            Log.d("ESTA FILTRANDO", filteredList.toString())

            // Aquí se actualiza la lista de datos del adaptador
            icfesAdapter.updateData(filteredList) // Llamamos a una función del adaptador que actualiza la lista

            // Notificar al adaptador que los datos han cambiado
            icfesAdapter.notifyDataSetChanged()
        }


    }

    private fun extractUniqueFilters() {
        // Extraer valores únicos para el filtro de categoría

        categoryList.clear()
        categoryList.add("Todos")
        icfesList.forEach { icfesData ->
            icfesData.nombreCategoria?.let { if (!categoryList.contains(it)) categoryList.add(it) }
        }

        // Extraer valores únicos para el filtro de excepción
        exceptionList.clear()
        exceptionList.add("Todos")
        icfesList.forEach { icfesData ->
            icfesData.excepcion?.let { if (!exceptionList.contains(it)) exceptionList.add(it) }
        }
    }

    private fun updateSpinners() {
        // Configurar el Spinner de Categoría
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryList)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = categoryAdapter

        // Configurar el Spinner de Excepción
        val exceptionAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, exceptionList)
        exceptionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        exceptionSpinner.adapter = exceptionAdapter
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            progressBar.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            errorMessage.visibility = View.GONE
        } else {
            progressBar.visibility = View.GONE
        }
    }

    private fun showErrorMessage(show: Boolean) {
        if (show) {
            errorMessage.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            errorMessage.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun getRetrofit(): Retrofit {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl("https://www.datos.gov.co/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}


interface ApiService {
    @GET("resource/jr4w-3mcm.json")
    fun getIcfesData(): Call<List<IcfesData>>
}

data class IcfesData(
    @SerializedName("nombre_o_t_tulo_de_la_categor") val nombreCategoria: String?,
    @SerializedName("nombre_o_t_tulo_de_la") val nombre: String?,
    @SerializedName("idioma") val idioma: String?,
    @SerializedName("medio_de_conservaci_n_o") val medioDeConservacion: String?,
    @SerializedName("fecha_de_generaci_n_de_la") val fechaGeneracion: String?,
    @SerializedName("nombre_del_responsable_de") val responsable: String?,
    @SerializedName("nombre_del_responsable_de_1") val responsable1: String?,
    @SerializedName("objetivo_legitimo_de_la") val objetivoLegitimo: String?,
    @SerializedName("fundamento_constitucional") val fundamentoConstitucional: String?,
    @SerializedName("fundamento_jur_dico_de_la") val fundamentoJurisdico: String?,
    @SerializedName("excepci_n_total_o_parcial") val excepcion: String?,
    @SerializedName("fecha_de_la_calificaci_n") val fechaCalificacion: String?,
    @SerializedName("plazo_de_la_calificaci_n") val plazoCalificacion: String?
)

